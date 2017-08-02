package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.ManyUserBackcashPlanId;
import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.business.repository.UserBackcashPlanRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

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
    private SysParamRepository sysParamRepository;

    /**
     * @Description : 查询用户计划回款金额
     */

    @PostMapping("/query")
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
        //登录的密码设定的时间限制
        QSysParam qSysParam = QSysParam.sysParam;
        SysParam sysParams = sysParamRepository.findOne(qSysParam.code.eq(Constants.BACK_CASH_PLAN_EXCEL_URL_CODE).and(qSysParam.type.eq(Constants.BACK_CASH_PLAN_EXCEL_URL_TYPE)).and(qSysParam.companyCode.eq(companyCode)));
        if (Objects.isNull(sysParams)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Failure to obtain parameters", "未能获取参数")).body(null);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(sysParams.getValue());
    }


}
