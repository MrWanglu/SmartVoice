package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.BackPlanImportParams;
import cn.fintecher.pangolin.business.model.ManyUserBackcashPlanId;
import cn.fintecher.pangolin.business.model.UploadUserBackcashPlanExcelInfo;
import cn.fintecher.pangolin.business.model.UploadUserBackcashPlanExcelModel;
import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.business.repository.UserBackcashPlanRepository;
import cn.fintecher.pangolin.business.repository.UserRepository;
import cn.fintecher.pangolin.business.service.UserBackcashPlanExcelImportService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.entity.util.CellError;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.*;
import org.apache.commons.collections4.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-02-10:26
 */
@RestController
@RequestMapping("/api/userBackcashPlanController")
@Api(value = "用户计划回款金额", description = "用户计划回款金额")
public class UserBackcashPlanController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(UserBackcashPlanController.class);
    private static final String ENTITY_NAME = "UserBackcashPlan";
    @Autowired
    private UserBackcashPlanRepository userBackcashPlanRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SysParamRepository sysParamRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private UserBackcashPlanExcelImportService userBackcashPlanExcelImportService;

    /**
     * @Description : 查询用户计划回款金额
     */

    @GetMapping("/query")
    @ApiOperation(value = "查询用户计划回款金额", notes = "查询用户计划回款金额")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<UserBackcashPlan>> query(@RequestParam(required = false) String userName,
                                                        @RequestParam(required = false) String realName,
                                                        @RequestParam(required = false) Integer year,
                                                        @RequestParam(required = false) Integer month,
                                                        @RequestParam String companyCode,
                                                        @ApiIgnore Pageable pageable,
                                                        @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        QUserBackcashPlan qUserBackcashPlan = QUserBackcashPlan.userBackcashPlan;
        BooleanBuilder builder = new BooleanBuilder();
        if (Objects.nonNull(userName)) {
            builder.and(qUserBackcashPlan.userName.eq(userName));
        }
        if (Objects.nonNull(realName)) {
            builder.and(qUserBackcashPlan.realName.like(realName.concat("%")));
        }
        if (Objects.nonNull(year)) {
            builder.and(qUserBackcashPlan.year.eq(year));
        }
        if (Objects.nonNull(month)) {
            builder.and(qUserBackcashPlan.month.eq(month));
        }
        if (Objects.nonNull(companyCode)) {
            builder.and(qUserBackcashPlan.companyCode.eq(companyCode));
        }
        Page<UserBackcashPlan> page = userBackcashPlanRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(page);
    }

    /**
     * @Description : 新增用户计划回款
     */
    @PostMapping("/createUserBackcashPlan")
    @ApiOperation(value = "新增用户计划回款", notes = "新增用户计划回款")
    public ResponseEntity<UserBackcashPlan> createUserBackcashPlan(@Validated @ApiParam("公司对象") @RequestBody UserBackcashPlan userBackcashPlan,
                                                                   @RequestHeader(value = "X-UserToken") String token) {
        logger.debug("REST request to save company : {}", userBackcashPlan);
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        if (userBackcashPlan.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "idexists", "新增不应该含有ID")).body(null);
        }
        //判断是否有这个年份  这个月份插入的计划回款金额
        QUserBackcashPlan qUserBackcashPlan = QUserBackcashPlan.userBackcashPlan;
        boolean exist = userBackcashPlanRepository.exists(qUserBackcashPlan.userName.eq(userBackcashPlan.getUserName()).and(qUserBackcashPlan.realName.eq(userBackcashPlan.getRealName())).and(qUserBackcashPlan.year.eq(userBackcashPlan.getYear())).and(qUserBackcashPlan.month.eq(userBackcashPlan.getMonth())).and(qUserBackcashPlan.companyCode.eq(userBackcashPlan.getCompanyCode())));
        if (exist) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "idexists", "用户" + userBackcashPlan.getUserName() + ",姓名" + userBackcashPlan.getRealName() + "," + userBackcashPlan.getYear() + "年" + userBackcashPlan.getMonth() + "的计划回款已经存在")).body(null);
        }
        UserBackcashPlan userBackcashPlan1 = userBackcashPlanRepository.save(userBackcashPlan);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(userBackcashPlan1);
    }

    /**
     * @Description : 更新用户计划回款
     */
    @PostMapping("/updateUserBackcashPlan")
    @ApiOperation(value = "更新用户计划回款", notes = "更新用户计划回款")
    public ResponseEntity<UserBackcashPlan> updateUserBackcashPlan(@Validated @ApiParam("公司对象") @RequestBody UserBackcashPlan userBackcashPlan,
                                                                   @RequestHeader(value = "X-UserToken") String token) {
        logger.debug("REST request to save company : {}", userBackcashPlan);
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        if (Objects.isNull(userBackcashPlan.getId())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "idexists", "修改应该含有ID")).body(null);
        }
        //判断是否有这个年份  这个月份插入的计划回款金额
        QUserBackcashPlan qUserBackcashPlan = QUserBackcashPlan.userBackcashPlan;
        boolean exist = userBackcashPlanRepository.exists(qUserBackcashPlan.userName.eq(userBackcashPlan.getUserName()).and(qUserBackcashPlan.realName.eq(userBackcashPlan.getRealName())).and(qUserBackcashPlan.year.eq(userBackcashPlan.getYear())).and(qUserBackcashPlan.month.eq(userBackcashPlan.getMonth())).and(qUserBackcashPlan.companyCode.eq(userBackcashPlan.getCompanyCode())).and(qUserBackcashPlan.id.ne(userBackcashPlan.getId())));
        if (exist) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "idexists", "用户" + userBackcashPlan.getUserName() + ",姓名" + userBackcashPlan.getRealName() + "," + userBackcashPlan.getYear() + "年" + userBackcashPlan.getMonth() + "的计划回款已经存在")).body(null);
        }
        UserBackcashPlan userBackcashPlan1 = userBackcashPlanRepository.save(userBackcashPlan);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(userBackcashPlan1);
    }

    /**
     * @Description : 删除用户计划回款
     */
    @DeleteMapping("/deleteUserBackcashPlan")
    @ApiOperation(value = "删除用户计划回款", notes = "删除用户计划回款")
    public ResponseEntity<Void> deleteUserBackcashPlan(@RequestParam String id) {
        logger.debug("REST request to delete caseInfo : {}", id);
        if (Objects.isNull(id)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "Select delete object", "请选择删除对象")).body(null);
        }
        userBackcashPlanRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(null);
    }

    /**
     * @Description : 批量删除用户计划回款
     */
    @PostMapping("/deleteManyUserBackcashPlan")
    @ApiOperation(value = "批量删除用户计划回款", notes = "批量删除用户计划回款")
    public ResponseEntity<Void> deleteManyUserBackcashPlan(@RequestBody ManyUserBackcashPlanId request) {
        logger.debug("REST request to delete caseInfo : {}", request.getIds());
        if (Objects.isNull(request.getIds())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "Select delete object", "请选择删除对象")).body(null);
        }
        for (String id : request.getIds()) {
            userBackcashPlanRepository.delete(id);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(null);
    }

    /**
     * @Description : 下载月度回款目标Excel模板
     */
    @GetMapping("/downloadUserBackcashPlanExcelTemplate")
    @ApiOperation(value = "下载月度回款目标Excel模板", notes = "下载月度回款目标Excel模板")
    public ResponseEntity<String> downloadPaybackExcelTemplate(@RequestParam String companyCode,
                                                               @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        //登录的密码设定的时间限制
        QSysParam qSysParam = QSysParam.sysParam;
        SysParam sysParams = sysParamRepository.findOne(qSysParam.code.eq(Constants.BACK_CASH_PLAN_EXCEL_URL_CODE).and(qSysParam.type.eq(Constants.BACK_CASH_PLAN_EXCEL_URL_TYPE)).and(qSysParam.companyCode.eq(companyCode)));
        if (Objects.isNull(sysParams)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Failure to obtain parameters", "未能获取参数")).body(null);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(sysParams.getValue());
    }

    /**
     * @Description : 导入月度回款Excel数据
     */
    @PostMapping(value = "/importUserBackcashPlanExcelTemplate")
    @ApiOperation(value = "导入月度回款Excel数据", notes = "导入月度回款Excel数据")
    public ResponseEntity<String> backAmtGoalExcel(@RequestBody UploadUserBackcashPlanExcelInfo request,
                                                   @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        logger.debug("REST request to import UploadUserBackcashPlanExcelInfo");
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        //excel解析
        int[] startRow = {0};
        int[] startCol = {0};
        Class<?>[] dataClass = {UploadUserBackcashPlanExcelModel.class};
        UploadFile uploadFile = null;
        try {
            // 根据ID查找到上传服务器的文件
            uploadFile = restTemplate.getForEntity("http://file-service/api/uploadFile?id=" + request.getFileId(), UploadFile.class).getBody();
            if (Objects.isNull(uploadFile)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Failed to obtain upload file", "获取上传文件失败")).body(null);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Failed to obtain upload file", "获取上传文件失败")).body(null);
        }
        // 获取系统中所有用户
        List<User> allUser = null;
        List<String> usernameList = new ArrayList<>();
        try {
            QUser qUser = QUser.user;
            Iterator<User> userIterator = userRepository.findAll(qUser.companyCode.eq(request.getCompanyCode())).iterator();
            allUser = IteratorUtils.toList(userIterator);
            if (Objects.isNull(allUser)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Failed to get the imported user", "获取导入的用户失败")).body(null);
            }
            for (User user1 : allUser) {
                usernameList.add(user1.getUserName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Failed to get the  user", "获取用户失败")).body(null);
        }


        // 获取到已经导入的用户
        List<UserBackcashPlan> userPlan = new ArrayList<>();
        try {
            List<UserBackcashPlan> all = userBackcashPlanRepository.findAll();
            for (UserBackcashPlan accPaybackPlan : all) {
                userPlan.add(accPaybackPlan);
            }
        } catch (
                Exception e)

        {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Failed to get the imported user", "获取导入的用户失败")).body(null);
        }

        // 将要传递的参数进行封装
        BackPlanImportParams backPlanImportParams = new BackPlanImportParams();
        backPlanImportParams.setLocalUrl(uploadFile.getLocalUrl());
        backPlanImportParams.setToken(user.getUserName());
        backPlanImportParams.setUsernameList(usernameList);
        backPlanImportParams.setUserPlan(userPlan);
        backPlanImportParams.setStartRow(startRow);
        backPlanImportParams.setStartCol(startCol);
        backPlanImportParams.setDataClass(dataClass);
        //解析Excel并保存到数据库中
        List<CellError> cellErrorList = new ArrayList<>();
        try {
            cellErrorList = userBackcashPlanExcelImportService.importExcelDataInfo(backPlanImportParams);
            if (Objects.nonNull(cellErrorList)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "dmp full", "导入失败")).body(cellErrorList.get(0).getErrorMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "dmp full", "导入失败")).body(null);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(null);
    }
}
