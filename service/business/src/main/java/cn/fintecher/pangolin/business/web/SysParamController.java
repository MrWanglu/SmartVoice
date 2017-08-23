package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.job.OverNightJob;
import cn.fintecher.pangolin.business.job.ReminderTimingJob;
import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.business.service.JobTaskService;
import cn.fintecher.pangolin.entity.QSysParam;
import cn.fintecher.pangolin.entity.SysParam;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
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
 * Date: 2017-07-04-9:33
 */
@RestController
@RequestMapping("/api/sysParamController")
@Api(value = "系统参数", description = "系统参数")
public class SysParamController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(SysParamController.class);
    private static final String ENTITY_NAME = "SysParam";

    @Autowired
    private SysParamRepository sysParamRepository;

    @Autowired
    private JobTaskService jobTaskService;

    /**
     * @Description : 系统参数带条件的分页查询
     */
    @GetMapping("/query")
    @ApiOperation(value = "系统参数带条件的分页查询", notes = "系统参数带条件的分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<SysParam>> getDepartAllUser(@RequestParam(required = false) String name,
                                                           @RequestParam(required = false) String code,
                                                           @RequestParam(required = false) Integer status,
                                                           @RequestParam(required = false) String type,
                                                           @RequestParam(required = false) String value,
                                                           @RequestParam(required = false) Integer sign,
                                                           @RequestParam(required = false) String companyCode,
                                                           @ApiIgnore Pageable pageable,
                                                           @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        QSysParam qSysParam = QSysParam.sysParam;
        BooleanBuilder builder = new BooleanBuilder();
        if (Objects.nonNull(name)) {
            builder.and(qSysParam.name.like(name.concat("%")));
        }
        if (Objects.nonNull(code)) {
            builder.and(qSysParam.code.like(code.concat("%")));
        }
        if (Objects.nonNull(status)) {
            builder.and(qSysParam.status.eq(status));
        }
        if (Objects.nonNull(type)) {
            builder.and(qSysParam.type.like(type.concat("%")));
        }
        if (Objects.nonNull(value)) {
            builder.and(qSysParam.value.eq(value));
        }
        if (Objects.nonNull(sign)) {
            builder.and(qSysParam.sign.eq(sign));
        }
        if (Objects.nonNull(companyCode)) {
            builder.and(qSysParam.companyCode.eq(companyCode));
        }
        Page<SysParam> page = sysParamRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(page);
    }

    /**
     * @Description : 新增系统参数
     */
    @PostMapping("/createSysParam")
    @ApiOperation(value = "新增/修改系统参数", notes = "新增系统参数")
    public ResponseEntity<SysParam> createSysParam(@Validated @RequestBody SysParam sysParam,
                                                   @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        if (Objects.isNull(sysParam.getCompanyCode())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "companyCode code does not exist", "公司码为空")).body(null);
        }
        QSysParam qSysParam=QSysParam.sysParam;

        //修改系统参数
        if(Constants.SYSPARAM_OVERNIGHT.equals(sysParam.getCode())){
            //修改系统批量参数
            //验证批量是否正在执行
            SysParam one = sysParamRepository.findOne(qSysParam.companyCode.eq(sysParam.getCompanyCode())
                    .and(qSysParam.code.eq(Constants.SYSPARAM_OVERNIGHT_STATUS)));
           if(one.getValue().equals(Constants.BatchStatus.RUNING.getValue())){
               return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                       "syspram value is illegitmacy", "晚间批量正在执行不允许修改调度时间")).body(null);
           }
            //验证输入的参数是否合规
            String  value=sysParam.getValue();
           if(StringUtils.isBlank(value)){
               return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                       "syspram value is illegitmacy", "参数值为空")).body(null);
           }
           if (StringUtils.length(value)!=6){
               return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                       "syspram value is illegitmacy", "参数长度不满6位")).body(null);
           }
            if(!value.matches("^[0-2]{1}[0-9]{1}[0-5]{1}[0-9]{1}[0-5]{1}[0-9]{1}$")){
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                        "syspram value is illegitmacy", "参数输入不合法")).body(null);
            }
            //触发系统批量
            String hours = value.substring(0, 2);
            if(Integer.parseInt(hours)>23){
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                        "syspram value is illegitmacy", "参数输入不合法")).body(null);
            }
            String mis = value.substring(2, 4);
            String second = value.substring(4, 6);
           String  cronStr = second.concat(" ").concat(mis).concat(" ").concat(hours).concat(" * * ?");
           try{
               jobTaskService.updateJobTask(cronStr,sysParam.getCompanyCode(),Constants.SYSPARAM_OVERNIGHT_STATUS,Constants.OVERNIGHT_TRIGGER_NAME.concat("_").concat(sysParam.getCompanyCode())
                       ,Constants.OVERNIGHT_TRIGGER_GROUP,Constants.OVERNIGHT_TRIGGER_DESC.concat("_").concat(sysParam.getCompanyCode())
                       ,Constants.OVERNIGHT_JOB_NAME.concat("_").concat(sysParam.getCompanyCode()), Constants.OVERNIGHT_JOB_GROUP
                       ,Constants.OVERNIGHT_JOB_DESC.concat("_").concat(sysParam.getCompanyCode()), OverNightJob.class,
                       "overNightJobBean".concat("_").concat(sysParam.getCompanyCode()));
           }catch (Exception e){
               logger.error("更新晚间批量调度失败",e);
           }
        }
        //修改系统参数
        if(Constants.SYSPARAM_REMINDER.equals(sysParam.getCode())){
            //修改系统批量参数
            //验证批量是否正在执行
            SysParam one = sysParamRepository.findOne(qSysParam.companyCode.eq(sysParam.getCompanyCode())
                    .and(qSysParam.code.eq(Constants.SYSPARAM_REMINDER_STATUS)));
            if(one.getValue().equals(Constants.BatchStatus.RUNING.getValue())){
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                        "syspram value is illegitmacy", "晚间批量正在执行不允许修改调度时间")).body(null);
            }
            //验证输入的参数是否合规
            String  value=sysParam.getValue();
            if(StringUtils.isBlank(value)){
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                        "syspram value is illegitmacy", "参数值为空")).body(null);
            }
            if (StringUtils.length(value)!=6){
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                        "syspram value is illegitmacy", "参数长度不满6位")).body(null);
            }
            if(!value.matches("^[0-2]{1}[0-9]{1}[0-5]{1}[0-9]{1}[0-5]{1}[0-9]{1}$")){
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                        "syspram value is illegitmacy", "参数输入不合法")).body(null);
            }
            //触发系统批量
            String hours = value.substring(0, 2);
            if(Integer.parseInt(hours)>23){
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                        "syspram value is illegitmacy", "参数输入不合法")).body(null);
            }
            String mis = value.substring(2, 4);
            String second = value.substring(4, 6);
            String  cronStr = second.concat(" ").concat(mis).concat(" ").concat(hours).concat(" * * ?");
            try{
                jobTaskService.updateJobTask(cronStr,sysParam.getCompanyCode(),Constants.SYSPARAM_REMINDER,Constants.REMINDER_TRIGGER_NAME.concat("_").concat(sysParam.getCompanyCode())
                        ,Constants.REMINDER_TRIGGER_GROUP,Constants.REMINDER_TRIGGER_DESC.concat("_").concat(sysParam.getCompanyCode())
                        ,Constants.REMINDER_JOB_NAME.concat("_").concat(sysParam.getCompanyCode()), Constants.REMINDER_JOB_GROUP
                        ,Constants.REMINDER_JOB_DESC.concat("_").concat(sysParam.getCompanyCode()), ReminderTimingJob.class,
                        "reminderTimingJobBean".concat("_").concat(sysParam.getCompanyCode()));
            }catch (Exception e){
                logger.error("更新消息提醒批量调度失败",e);
            }
        }
        SysParam sysParam1 = sysParamRepository.save(sysParam);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(sysParam1);
    }
}
