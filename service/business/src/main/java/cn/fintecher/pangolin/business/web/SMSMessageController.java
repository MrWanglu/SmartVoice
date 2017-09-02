package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.CapaMessageParams;
import cn.fintecher.pangolin.business.model.CapaPersons;
import cn.fintecher.pangolin.business.model.PersonalParams;
import cn.fintecher.pangolin.business.model.SMSMessageParams;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.business.service.MessageService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.message.SMSMessage;
import cn.fintecher.pangolin.entity.message.SendSMSMessage;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 11:24 2017/9/1
 */
@RestController
@RequestMapping("/api/sMSMessageController")
@Api(value = "角色资源管理", description = "角色资源管理")
public class SMSMessageController extends BaseController {
    private static final String ENTITY_NAME = "SMSMessage";
    private final Logger logger = LoggerFactory.getLogger(SMSMessageController.class);

    @Autowired
    TemplateRepository templateRepository;
    @Autowired
    PersonalRepository personalRepository;
    @Autowired
    PersonalContactRepository personalContactRepository;
    @Autowired
    SysParamRepository sysParamRepository;
    @Autowired
    CaseInfoRepository caseInfoRepository;
    @Autowired
    SendMessageRecordRepository sendMessageRecordRepository;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    MessageService messageService;


    /**
     * 电催 短信发送 在联系人上面点击发送短息
     */
    @PostMapping("/SendMessageSingle")
    @ApiOperation(value = "添加短信记录", notes = "添加短信记录")
    public ResponseEntity<String> addAccSMSMessageByHand(@RequestBody @ApiParam("短息信息") SMSMessageParams smsMessageParams,
                                                         @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        Template template = templateRepository.findOne(smsMessageParams.getTesmId());
        if (Objects.isNull(template)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "没有找到模板")).body(null);
        }
        Personal personal = personalRepository.findOne(smsMessageParams.getPersonalId());
        if (Objects.isNull(personal)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "没有客户信息")).body(null);
        }
        BooleanBuilder exp = new BooleanBuilder();
        exp.and(QSysParam.sysParam.code.eq(Constants.SMS_PUSH_CODE));
        exp.and(QSysParam.sysParam.companyCode.eq(user.getCompanyCode()));
        exp.and(QSysParam.sysParam.status.eq(SysParam.StatusEnum.Start.getValue()));
        String type = sysParamRepository.findOne(exp).getValue();
        List<PersonalParams> personalParams = smsMessageParams.getPersonalParamsList();
        Map<String, String> params = new HashMap<>();
        if (Objects.isNull(personalParams) || personalParams.isEmpty()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "没有客户信息")).body(null);
        }
        CaseInfo caseInfo = caseInfoRepository.findOne(QCaseInfo.caseInfo.caseNumber.eq(smsMessageParams.getCaseNumber()));
//        if (template.getMessageContent().contains("cust_name")) {
//            template.setMessageContent(template.getMessageContent().replace("cust_name", personal.getName()));
//            params.put("cust_name", personal.getName());
//        }
//        if (template.getMessageContent().contains("date")) {
//            template.setMessageContent(template.getMessageContent().replace("date", ZWDateUtil.fomratterDate(caseInfo.getPromiseTime(), "yyyy-MM-dd")));
//            params.put("date", ZWDateUtil.fomratterDate(caseInfo.getPromiseTime(), "yyyy-MM-dd"));
//        }
//        if (template.getMessageContent().contains("day")) {
//            template.setMessageContent(template.getMessageContent().replace("day", caseInfo.getOverdueDays().toString()));
//            params.put("day", caseInfo.getOverdueDays().toString());
//        }
//        if (template.getMessageContent().contains("money")) {
//            template.setMessageContent(template.getMessageContent().replace("money", caseInfo.getPromiseAmt().toString()));
//            params.put("money", caseInfo.getPromiseAmt().toString());
//        }
//        template.setMessageContent(template.getMessageContent().replace("${", "").replace("}", ""));

        if (Objects.equals(type, "0")) {
            for (PersonalParams personalParams1 : personalParams) {
                SendSMSMessage sendSMSMessage = new SendSMSMessage();
                sendSMSMessage.setPhoneNumber(personalParams1.getPersonalPhone());
                sendSMSMessage.setTemplate(template.getId());
                sendSMSMessage.setParams(smsMessageParams.getParams());
                SendMessageRecord result = messageService.saveMessage(caseInfo,personal,template,personalParams1.getContId(),user,smsMessageParams.getSendType());
                restTemplate.postForEntity("http://common-service/api/SearchMessageController/sendSmsMessage", sendSMSMessage, Void.class);
            }
        } else if(Objects.equals(type,"1")){
            for (PersonalParams personalParams1 : personalParams) {
                SMSMessage message = new SMSMessage();
                message.setPhoneNumber(personalParams1.getPersonalPhone());
                message.setTemplate(template.getTemplateCode());
                params.put("userName",personal.getName());
                params.put("business",caseInfo.getPrincipalId().getName());
                params.put("money",caseInfo.getOverdueAmount().toString());
                message.setCompanyCode(user.getCompanyCode());
                message.setUserId(user.getId());
                message.setParams(params);
                SendMessageRecord result = messageService.saveMessage(caseInfo,personal,template,personalParams1.getContId(),user,smsMessageParams.getSendType());
                restTemplate.postForObject("http://common-service/api/SearchMessageController/sendJGSmsMessage", message, String.class);
            }
        }
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("发送成功", "")).body(null);
    }

    @PostMapping("/SendCapaMessageSingle")
    @ApiOperation(value = "智能短信记录", notes = "智能短信记录")
    public ResponseEntity<String> sendCapaMessageSingle(@RequestBody @ApiParam("短息信息") CapaMessageParams capaMessageParams,
                                                         @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        Template template = templateRepository.findOne(capaMessageParams.getTesmId());
        if (Objects.isNull(template)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "没有找到模板")).body(null);
        }
        BooleanBuilder exp = new BooleanBuilder();
        exp.and(QSysParam.sysParam.code.eq(Constants.SMS_PUSH_CODE));
        exp.and(QSysParam.sysParam.companyCode.eq(user.getCompanyCode()));
        exp.and(QSysParam.sysParam.status.eq(SysParam.StatusEnum.Start.getValue()));
        String type = sysParamRepository.findOne(exp).getValue();
        Map<String, String> params = new HashMap<>();
        for(int i =0; i<capaMessageParams.getCaseNumbers().size();i++){
            CaseInfo caseInfo = caseInfoRepository.findOne(QCaseInfo.caseInfo.caseNumber.eq(capaMessageParams.getCaseNumbers().get(i)));
            Personal personal = personalRepository.findOne(capaMessageParams.getPersonalIds().get(i));
            if (Objects.isNull(personal)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "没有客户信息")).body(null);
            }
            if(Objects.equals(type, "0")){
                for(int j=0; i < capaMessageParams.getPersonalParamsList().size();j++){
                    SendSMSMessage sendSMSMessage = new SendSMSMessage();
                    sendSMSMessage.setPhoneNumber(capaMessageParams.getPersonalParamsList().get(i).getPersonalPhone().get(j));
                    sendSMSMessage.setTemplate(template.getTemplateCode());
                    sendSMSMessage.setParams(params);
                    SendMessageRecord result = messageService.saveMessage(caseInfo,personal,template,capaMessageParams.getPersonalParamsList().get(i).getContId().get(j),user,Integer.valueOf(capaMessageParams.getSendType()));
                    restTemplate.postForEntity("http://common-service/api/SearchMessageController/sendSmsMessage", sendSMSMessage, Void.class);
                }
            }else{
                for(int j=0; i < capaMessageParams.getPersonalParamsList().size();j++){
                    SMSMessage message = new SMSMessage();
                    message.setPhoneNumber(capaMessageParams.getPersonalParamsList().get(i).getPersonalPhone().get(j));
                    message.setTemplate(template.getTemplateCode());
                    params.put("userName",capaMessageParams.getPersonalParamsList().get(i).getPersonalName().get(j));
                    params.put("business",caseInfo.getPrincipalId().getName());
                    params.put("money",caseInfo.getOverdueAmount().toString());
                    message.setCompanyCode(user.getCompanyCode());
                    message.setUserId(user.getId());
                    message.setParams(params);
                    SendMessageRecord result = messageService.saveMessage(caseInfo,personal,template,capaMessageParams.getPersonalParamsList().get(i).getContId().get(j),user,Integer.valueOf(capaMessageParams.getSendType()));
                    restTemplate.postForObject("http://common-service/api/SearchMessageController/sendJGSmsMessage", message, String.class);
                }
            }
        }
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("发送成功", "")).body(null);
    }


}
