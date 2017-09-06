package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.CapaMessageParams;
import cn.fintecher.pangolin.business.model.CapaPersonals;
import cn.fintecher.pangolin.business.model.PersonalParams;
import cn.fintecher.pangolin.business.model.SMSMessageParams;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.business.service.MessageService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.message.PaaSMessage;
import cn.fintecher.pangolin.entity.message.SMSMessage;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWStringUtils;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

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
    public ResponseEntity<List> addAccSMSMessageByHand(@RequestBody @ApiParam("短息信息") SMSMessageParams smsMessageParams,
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
        Template temp = new Template();
        BeanUtils.copyProperties(template,temp);
        BooleanBuilder exp = new BooleanBuilder();
        exp.and(QSysParam.sysParam.code.eq(Constants.SMS_PUSH_CODE));
        exp.and(QSysParam.sysParam.companyCode.eq(user.getCompanyCode()));
        exp.and(QSysParam.sysParam.status.eq(SysParam.StatusEnum.Start.getValue()));
        String type = sysParamRepository.findOne(exp).getValue();
        List<PersonalParams> personalParams = smsMessageParams.getPersonalParamsList();
        List<String> sendFails = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        if (Objects.isNull(personalParams) || personalParams.isEmpty()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "没有客户信息")).body(null);
        }
        CaseInfo caseInfo = caseInfoRepository.findOne(QCaseInfo.caseInfo.caseNumber.eq(smsMessageParams.getCaseNumber()));
        if (Objects.equals(type, "0")) {
            for (PersonalParams personalParams1 : personalParams) {
                SMSMessage sendSMSMessage = new SMSMessage();
                sendSMSMessage.setPhoneNumber(personalParams1.getPersonalPhone());
                sendSMSMessage.setTemplate(template.getId());
                sendSMSMessage.setParams(smsMessageParams.getParams());
                SendMessageRecord result = messageService.saveMessage(caseInfo, personal, template, personalParams1.getContId(), user, smsMessageParams.getSendType(),SendMessageRecord.Flag.AUTOMATIC.getValue());
                restTemplate.postForEntity("http://common-service/api/SearchMessageController/sendSmsMessage", sendSMSMessage, Void.class);
            }
        } else if (Objects.equals(type, "1")) {
            StringBuilder error = new StringBuilder();
            SMSMessage message = new SMSMessage();
            template.setMessageContent(template.getMessageContent().replace("userName", personal.getName())
                    .replace("business", caseInfo.getPrincipalId().getName()).replace("money", caseInfo.getOverdueAmount().setScale(2).toString()));
            template.setMessageContent(template.getMessageContent().replace("{{", "").replace("}}", ""));
            message.setTemplate(template.getTemplateCode());
            params.put("userName", personal.getName());
            params.put("business", caseInfo.getPrincipalId().getName());
            params.put("money", caseInfo.getOverdueAmount().toString());
            message.setCompanyCode(user.getCompanyCode());
            message.setUserId(user.getId());
            message.setParams(params);
            for (PersonalParams personalParams1 : personalParams) {
                message.setPhoneNumber(personalParams1.getPersonalPhone());
                String entity = restTemplate.postForObject("http://common-service/api/SearchMessageController/sendJGSmsMessage", message, String.class);
                if (ZWStringUtils.isNotEmpty(entity)) {
                    sendFails.add(entity);
                    SendMessageRecord result = messageService.saveMessage(caseInfo, personal, template, personalParams1.getContId(), user, smsMessageParams.getSendType(),SendMessageRecord.Flag.MANUAL.getValue());
                    templateRepository.saveAndFlush(temp);
                } else {
                    SendMessageRecord result = messageService.saveMessage(caseInfo, personal, template, personalParams1.getContId(), user, smsMessageParams.getSendType(),SendMessageRecord.Flag.AUTOMATIC.getValue());
                    templateRepository.saveAndFlush(temp);
                }
            }
            if(!sendFails.isEmpty()){
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "")).body(sendFails);
            }
        } else if (Objects.equals(type, "2")) {
            StringBuilder error = new StringBuilder();
            PaaSMessage message = new PaaSMessage();
            template.setMessageContent(template.getMessageContent().replace("userName", personal.getName())
                    .replace("business", caseInfo.getPrincipalId().getName()).replace("money", caseInfo.getOverdueAmount().setScale(2).toString()));
            template.setMessageContent(template.getMessageContent().replace("{{", "").replace("}}", ""));
            message.setTemplate(template.getTemplateCode());
            message.setCompanyCode(user.getCompanyCode());
            message.setContent(template.getMessageContent());
            message.setUserId(user.getId());
            for (PersonalParams personalParams1 : personalParams) {
                message.setPhoneNumber(personalParams1.getPersonalPhone());
                String entity = restTemplate.postForObject("http://common-service/api/SearchMessageController/sendPaaSMessage", message, String.class);
                if (ZWStringUtils.isNotEmpty(entity)) {
                    sendFails.add(entity);
                    SendMessageRecord result = messageService.saveMessage(caseInfo, personal, template, personalParams1.getContId(), user, smsMessageParams.getSendType(),SendMessageRecord.Flag.MANUAL.getValue());
                    templateRepository.saveAndFlush(temp);
                } else {
                    SendMessageRecord result = messageService.saveMessage(caseInfo, personal, template, personalParams1.getContId(), user, smsMessageParams.getSendType(),SendMessageRecord.Flag.AUTOMATIC.getValue());
                    templateRepository.saveAndFlush(temp);
                }
            }
            if(!sendFails.isEmpty()){
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "")).body(sendFails);
            }
        }
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("发送成功", "")).body(null);
    }

    @PostMapping("/SendCapaMessageSingle")
    @ApiOperation(value = "智能短信记录", notes = "智能短信记录")
    public ResponseEntity<List> sendCapaMessageSingle(@RequestBody @ApiParam("短息信息") CapaMessageParams capaMessageParams,
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
        Template temp = new Template();
        BeanUtils.copyProperties(template,temp);
        Map<String, String> params = new HashMap<>();
        List<String> sendFails = new ArrayList<>();
        StringBuilder error = new StringBuilder();
        for (CapaPersonals capaPersonals : capaMessageParams.getCapaPersonals()) {
            CaseInfo caseInfo = caseInfoRepository.findOne(QCaseInfo.caseInfo.caseNumber.eq(capaPersonals.getCaseNumber()));
            Personal personal = personalRepository.findOne(capaPersonals.getPersonalId());
            if (Objects.isNull(personal)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "没有客户信息")).body(null);
            }
            if (Objects.equals(type, "0")) {
                SMSMessage sendSMSMessage = new SMSMessage();
                sendSMSMessage.setTemplate(template.getTemplateCode());
                sendSMSMessage.setParams(params);
                for (int i=0; i<capaPersonals.getConcatIds().size(); i++) {
                    sendSMSMessage.setPhoneNumber(capaPersonals.getConcatPhones().get(i));
                    SendMessageRecord result = messageService.saveMessage(caseInfo, personal, template, capaPersonals.getConcatIds().get(i), user, Integer.valueOf(capaMessageParams.getSendType()),SendMessageRecord.Flag.AUTOMATIC.getValue());
                    restTemplate.postForEntity("http://common-service/api/SearchMessageController/sendSmsMessage", sendSMSMessage, Void.class);
                }
            } else if(Objects.equals(type, "1")){
                SMSMessage message = new SMSMessage();
                template.setMessageContent(template.getMessageContent().replace("userName", personal.getName())
                        .replace("business", caseInfo.getPrincipalId().getName()).replace("money", caseInfo.getOverdueAmount().setScale(2).toString()));
                template.setMessageContent(template.getMessageContent().replace("{{", "").replace("}}", ""));
                message.setTemplate(template.getTemplateCode());
                params.put("userName", personal.getName());
                params.put("business", caseInfo.getPrincipalId().getName());
                params.put("money", caseInfo.getOverdueAmount().toString());
                message.setCompanyCode(user.getCompanyCode());
                message.setUserId(user.getId());
                message.setParams(params);
                for (int i=0; i<capaPersonals.getConcatIds().size(); i++) {
                    message.setPhoneNumber(capaPersonals.getConcatPhones().get(i));
                    String entity = restTemplate.postForObject("http://common-service/api/SearchMessageController/sendJGSmsMessage", message, String.class);
                    if (ZWStringUtils.isNotEmpty(entity)) {
                        sendFails.add(entity);
                        SendMessageRecord result = messageService.saveMessage(caseInfo, personal, template, capaPersonals.getConcatIds().get(i), user, Integer.valueOf(capaMessageParams.getSendType()),SendMessageRecord.Flag.MANUAL.getValue());
                        templateRepository.saveAndFlush(temp);
                    } else {
                        SendMessageRecord result = messageService.saveMessage(caseInfo, personal, template, capaPersonals.getConcatIds().get(i), user, Integer.valueOf(capaMessageParams.getSendType()),SendMessageRecord.Flag.AUTOMATIC.getValue());
                        templateRepository.saveAndFlush(temp);
                    }
                }
            }else if(Objects.equals(type,"2")){
                PaaSMessage message = new PaaSMessage();
                template.setMessageContent(template.getMessageContent().replace("userName", personal.getName())
                        .replace("business", caseInfo.getPrincipalId().getName()).replace("money", caseInfo.getOverdueAmount().setScale(2).toString()));
                template.setMessageContent(template.getMessageContent().replace("{{", "").replace("}}", ""));
                message.setTemplate(template.getTemplateCode());
                message.setContent(template.getMessageContent());
                params.put("userName", personal.getName());
                params.put("business", caseInfo.getPrincipalId().getName());
                params.put("money", caseInfo.getOverdueAmount().toString());
                message.setCompanyCode(user.getCompanyCode());
                message.setUserId(user.getId());
                message.setParams(params);
                for (int i=0; i<capaPersonals.getConcatIds().size(); i++) {
                    message.setPhoneNumber(capaPersonals.getConcatPhones().get(i));
                    String entity = restTemplate.postForObject("http://common-service/api/SearchMessageController/sendPaaSMessage", message, String.class);
                    if (ZWStringUtils.isNotEmpty(entity)) {
                        sendFails.add(entity);
                        SendMessageRecord result = messageService.saveMessage(caseInfo, personal, template, capaPersonals.getConcatIds().get(i), user, Integer.valueOf(capaMessageParams.getSendType()),SendMessageRecord.Flag.MANUAL.getValue());
                        templateRepository.saveAndFlush(temp);
                    } else {
                        SendMessageRecord result = messageService.saveMessage(caseInfo, personal, template, capaPersonals.getConcatIds().get(i), user, Integer.valueOf(capaMessageParams.getSendType()),SendMessageRecord.Flag.AUTOMATIC.getValue());
                        templateRepository.saveAndFlush(temp);
                    }
                }
            }
        }
        if(!sendFails.isEmpty()){
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "")).body(sendFails);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("发送成功", "")).body(null);
    }


}
