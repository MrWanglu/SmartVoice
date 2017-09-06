package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.BatchManageContent;
import cn.fintecher.pangolin.business.model.BatchManageList;
import cn.fintecher.pangolin.business.model.SysNotice;
import cn.fintecher.pangolin.business.repository.BatchManageRepository;
import cn.fintecher.pangolin.business.repository.CompanyRepository;
import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.business.repository.UserRepository;
import cn.fintecher.pangolin.business.service.JobTaskService;
import cn.fintecher.pangolin.business.service.OverNightBatchService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author : sunyanping
 * @Description : 批量管理
 * @Date : 2017/8/18.
 */
@RestController
@RequestMapping("/api/batchManageController")
@Api(value = "批量管理", description = "批量管理")
public class BatchManageController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(BatchManageController.class);
    private static final String ENTITY_NAME = "BatchManageController";

    @Inject
    private SysParamRepository sysParamRepository;

    @Autowired
    private BatchManageRepository batchManageRepository;

    @Autowired
    private JobTaskService jobTaskService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private OverNightBatchService overNightBatchService;


    @GetMapping("/getBatchSysNotice")
    @ApiOperation(notes = "首页批量系统公告", value = "首页批量系统公告")
    public ResponseEntity<SysNotice> getBatchSysNotice(@RequestHeader(value = "X-UserToken") String token) {
        logger.debug("Rest request to getBatchSysNotice");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage())).body(null);
        }
        SysNotice sysNotice = new SysNotice();
        if (Objects.isNull(user.getCompanyCode())) {
            Iterable<SysParam> all = sysParamRepository.findAll(QSysParam.sysParam.code.eq(Constants.SYSPARAM_OVERNIGHT_STEP));
            List<SysParam> sysParams = IterableUtils.toList(all);
            StringBuilder sb = new StringBuilder();
            for (SysParam sysParam : sysParams) {
                if (!Objects.equals(sysParam.getValue(), Constants.BATCH_STEP_SUCCESS)) {
                    Company one = companyRepository.findOne(QCompany.company.code.eq(sysParam.getCompanyCode()));
                    sb.append(one.getChinaName().concat("、"));
                }
            }
            if (sb.length() == 0) {
                sysNotice.setTitle("批量完成");
                sysNotice.setContent("各公司的批量完成");
                return ResponseEntity.ok().body(sysNotice);
            }
            String sbn = sb.substring(0, sb.length() - 1);
            sysNotice.setTitle("批量失败");
            sysNotice.setContent("公司" + sbn + "的批量失败");
            return ResponseEntity.ok().body(sysNotice);
        }

        SysParam one = sysParamRepository.findOne(QSysParam.sysParam.companyCode.eq(user.getCompanyCode())
                .and(QSysParam.sysParam.code.eq(Constants.SYSPARAM_OVERNIGHT_STEP)));
        if (Objects.equals(one.getValue(), Constants.BATCH_STEP_SUCCESS)) { //步数为6-批量成功
            sysNotice.setTitle("批量完成");
            sysNotice.setContent("您于[" + ZWDateUtil.fomratterDate(one.getOperateTime(), null) + "]批量完成");
            return ResponseEntity.ok().body(sysNotice);
        } else {
            sysNotice.setTitle("批量失败");
            sysNotice.setContent("您于[" + ZWDateUtil.fomratterDate(one.getOperateTime(), null) + "]批量失败");
            return ResponseEntity.ok().body(sysNotice);
        }
    }

    @GetMapping("/getBatchManage")
    @ApiOperation(value = "多条件查询批量处理记录", notes = "多条件查询批量处理记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<BatchManage>> getBatchManage(@QuerydslPredicate(root = BatchManage.class) Predicate predicate,
                                                            @ApiIgnore Pageable pageable,
                                                            @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "user not login", "用户未登录")).body(null);
        }
        BooleanBuilder booleanBuilder = new BooleanBuilder(predicate);
        Page<BatchManage> page = batchManageRepository.findAll(booleanBuilder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "batchManageController")).body(page);
    }

    @PostMapping("/manualBatchManage")
    @ApiOperation(value = "批量处理", notes = "批量处理")
    public ResponseEntity manualBatchManage(@RequestHeader(value = "X-UserToken") String token,
                                            @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode) throws JobExecutionException {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "user not login", "用户未登录")).body(null);
        }
        JobDataMap jobDataMap = new JobDataMap();
        String code;
        if (Objects.isNull(user.getCompanyCode())) {
            if (Objects.isNull(companyCode)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "batchManageController", "请先选择公司")).body(null);
            }
            jobDataMap.put("companyCode", companyCode);
            code = companyCode;
        } else {
            jobDataMap.put("companyCode", user.getCompanyCode());
            code = user.getCompanyCode();
        }
        QSysParam qSysParam = QSysParam.sysParam;
        SysParam sysParam = sysParamRepository.findOne(qSysParam.companyCode.eq(code)
                .and(qSysParam.code.eq(Constants.OVERNIGHTBATCH_STATUS_CODE))
                .and(qSysParam.type.eq(Constants.OVERNIGHTBATCH_STATUS_TYPE)));
        if (Objects.equals(StringUtils.trim(sysParam.getValue()), "1")) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "batchManageController", "晚间批量任务已停用")).body(null);
        }
        jobDataMap.put("sysParamCode", Constants.SYSPARAM_OVERNIGHT_STATUS);
        overNightBatchService.doOverNightTask(jobDataMap);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "batchManageController")).body(null);
    }

    /**
     * @Description 批量查询
     */
    @GetMapping("/queryBatchManage")
    @ApiOperation(value = "查询批量处理", notes = "查询批量处理")
    public ResponseEntity<BatchManageContent> queryBatchManage(@RequestParam String companyCode) {
        Object[] objects = batchManageRepository.batchManageFind(companyCode);
        List<BatchManageList> batchManageLists = new ArrayList<>();
        if (objects.length == 1 && Objects.isNull(((Object[]) objects[0])[0])) {
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(null);
        }
        for (int i = 0; i < objects.length; i++) {
            Object[] object = (Object[]) objects[i];
            BatchManageList batchManageList = new BatchManageList();
            if (Objects.nonNull(object[0].toString())) {
                batchManageList.setTaskName(object[0].toString());
            }
            if (Objects.nonNull(object[1].toString())) {
                batchManageList.setTaskGroup(object[1].toString());
            }
            if (Objects.nonNull(object[2].toString())) {
                String description = object[2].toString();
                String b = description.substring(0, description.length() - 5);
                batchManageList.setTaskDescription(b);
            }
            if (Objects.nonNull(object[3].toString())) {
                batchManageList.setTaskClassName(object[3].toString());
            }
            if (Objects.nonNull(object[4].toString())) {
                batchManageList.setTriggerName(object[4].toString());
            }
            if (Objects.nonNull(object[5].toString())) {
                batchManageList.setTriggerGroup(object[5].toString());
            }
            if (Objects.nonNull(object[6].toString())) {
                batchManageList.setNextExecutionTime(object[6].toString());
            }
            if (Objects.nonNull(object[7].toString())) {
                batchManageList.setExpression(object[7].toString());
            }
            if (Objects.nonNull(object[8].toString())) {
                batchManageList.setTimeZone(object[8].toString());
            }
            batchManageLists.add(batchManageList);
        }
        BatchManageContent batchManageContent = new BatchManageContent();
        batchManageContent.setContent(batchManageLists);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "batchManageController")).body(batchManageContent);
    }
}
