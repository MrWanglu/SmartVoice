package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.*;
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
    public ResponseEntity<List<PersonalParams>> addAccSMSMessageByHand(@RequestBody @ApiParam("短息信息") SMSMessageParams smsMessageParams,
                                                                       @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(smsMessageParams.getPersonalParamsList());
        }
        Template template = templateRepository.findOne(smsMessageParams.getTesmId());
        if (Objects.isNull(template)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "没有找到模板")).body(smsMessageParams.getPersonalParamsList());
        }
        Personal personal = personalRepository.findOne(smsMessageParams.getPersonalId());
        if (Objects.isNull(personal)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "没有客户信息")).body(smsMessageParams.getPersonalParamsList());
        }
        Template temp = new Template();
        BeanUtils.copyProperties(template, temp);
        //获取短信发送系统参数
        BooleanBuilder exp = new BooleanBuilder();
        exp.and(QSysParam.sysParam.code.eq(Constants.SMS_PUSH_CODE));
        exp.and(QSysParam.sysParam.companyCode.eq(user.getCompanyCode()));
        exp.and(QSysParam.sysParam.status.eq(SysParam.StatusEnum.Start.getValue()));
        SysParam sysParam = sysParamRepository.findOne(exp);
        if(Objects.isNull(sysParam)){
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请先配置系统参数")).body(smsMessageParams.getPersonalParamsList());
        }
        String type = sysParam.getValue();
        //联系人列表
        List<PersonalParams> personalParams = smsMessageParams.getPersonalParamsList();
        List<PersonalParams> sendFails = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        if (Objects.isNull(personalParams) || personalParams.isEmpty()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "没有客户信息")).body(null);
        }
        //相关案件
        CaseInfo caseInfo = caseInfoRepository.findOne(QCaseInfo.caseInfo.caseNumber.eq(smsMessageParams.getCaseNumber()));
        //初始化消息
        PaaSMessage message = new PaaSMessage();
        template.setMessageContent(template.getMessageContent().replace("userName", personal.getName())
                .replace("business", caseInfo.getPrincipalId().getName()).replace("money", caseInfo.getOverdueAmount().setScale(2).toString()));
        template.setMessageContent(template.getMessageContent().replace("{{", "").replace("}}", ""));
        message.setTemplate(template.getTemplateCode());
        message.setCompanyCode(user.getCompanyCode());
        message.setContent(template.getMessageContent());
        message.setUserId(user.getId());
        params.put("userName", personal.getName());
        params.put("business", caseInfo.getPrincipalId().getName());
        params.put("money", caseInfo.getOverdueAmount().toString());
        //遍历所有联系人
        for (PersonalParams personalParams1 : personalParams) {
            String entity = null;
            message.setPhoneNumber(personalParams1.getPersonalPhone());
            //空号发送失败
            if (ZWStringUtils.isEmpty(message.getPhoneNumber())) {
                sendFails.add(personalParams1);
                SendMessageRecord result = messageService.saveMessage(caseInfo, personal, template, personalParams1.getContId(), user, smsMessageParams.getSendType(), SendMessageRecord.Flag.MANUAL.getValue());
                templateRepository.saveAndFlush(temp);
                continue;
            }
            //0 ERPV3 1 极光 2 创蓝 3 数据宝
            switch (type) {
                case "0":
                    SMSMessage smsMessage = new SMSMessage();
                    BeanUtils.copyProperties(message, smsMessage);
                    smsMessage.setParams(params);
                    entity = restTemplate.postForObject("http://common-service/api/SearchMessageController/sendSmsMessage", smsMessage, String.class);
                    break;
                case "1":
                    SMSMessage JGMessage = new SMSMessage();
                    BeanUtils.copyProperties(message, JGMessage);
                    JGMessage.setParams(params);
                    entity = restTemplate.postForObject("http://common-service/api/SearchMessageController/sendJGSmsMessage", JGMessage, String.class);
                    break;
                case "2":
                    entity = restTemplate.postForObject("http://common-service/api/SearchMessageController/sendPaaSMessage", message, String.class);
                    break;
                case "3":
                    entity = restTemplate.postForObject("http://common-service/api/SearchMessageController/sendLookMessage", message, String.class);
                    break;
            }
            if (ZWStringUtils.isNotEmpty(entity)) {
                //发送失败
                sendFails.add(personalParams1);
                SendMessageRecord result = messageService.saveMessage(caseInfo, personal, template, personalParams1.getContId(), user, smsMessageParams.getSendType(), SendMessageRecord.Flag.MANUAL.getValue());
                templateRepository.saveAndFlush(temp);
            } else {
                //发送成功
                SendMessageRecord result = messageService.saveMessage(caseInfo, personal, template, personalParams1.getContId(), user, smsMessageParams.getSendType(), SendMessageRecord.Flag.AUTOMATIC.getValue());
                templateRepository.saveAndFlush(temp);
            }
        }
        if (!sendFails.isEmpty()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "")).body(sendFails);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("发送成功", "")).body(null);
    }

    @PostMapping("/SendCapaMessageSingle")
    @ApiOperation(value = "智能短信记录", notes = "智能短信记录")
    public ResponseEntity<List<PersonalParams>> sendCapaMessageSingle(@RequestBody @ApiParam("短息信息") CapaMessageParams capaMessageParams,
                                                                      @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(messageService.parseErrorList(capaMessageParams.getCapaPersonals()));
        }
        Template template = templateRepository.findOne(capaMessageParams.getTesmId());
        if (Objects.isNull(template)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "没有找到模板")).body(messageService.parseErrorList(capaMessageParams.getCapaPersonals()));
        }
        //获取短信发送系统参数
        BooleanBuilder exp = new BooleanBuilder();
        exp.and(QSysParam.sysParam.code.eq(Constants.SMS_PUSH_CODE));
        exp.and(QSysParam.sysParam.companyCode.eq(user.getCompanyCode()));
        exp.and(QSysParam.sysParam.status.eq(SysParam.StatusEnum.Start.getValue()));
        SysParam sysParam = sysParamRepository.findOne(exp);
        if(Objects.isNull(sysParam)){
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请先配置系统参数")).body(messageService.parseErrorList(capaMessageParams.getCapaPersonals()));
        }
        String type = sysParam.getValue();
        Template temp = new Template();
        BeanUtils.copyProperties(template, temp);
        Map<String, String> params = new HashMap<>();
        List<PersonalParams> sendFails = new ArrayList<>();
        //遍历每个客户
        for (CapaPersonals capaPersonals : capaMessageParams.getCapaPersonals()) {
            CaseInfo caseInfo = caseInfoRepository.findOne(QCaseInfo.caseInfo.caseNumber.eq(capaPersonals.getCaseNumber()));
            Personal personal = personalRepository.findOne(capaPersonals.getPersonalId());
            if (Objects.isNull(personal)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "没有客户信息")).body(null);
            }
            //初始化消息
            PaaSMessage message = new PaaSMessage();
            template.setMessageContent(template.getMessageContent().replace("userName", personal.getName())
                    .replace("business", caseInfo.getPrincipalId().getName()).replace("money", caseInfo.getOverdueAmount().setScale(2).toString()));
            template.setMessageContent(template.getMessageContent().replace("{{", "").replace("}}", ""));
            message.setTemplate(template.getTemplateCode());
            message.setCompanyCode(user.getCompanyCode());
            message.setContent(template.getMessageContent());
            message.setUserId(user.getId());
            params.put("userName", personal.getName());
            params.put("business", caseInfo.getPrincipalId().getName());
            params.put("money", caseInfo.getOverdueAmount().toString());
            message.setParams(params);
            //遍历每个联系人
            for (int i = 0; i < capaPersonals.getConcatIds().size(); i++) {
                String entity = null;
                message.setPhoneNumber(capaPersonals.getConcatPhones().get(i));
                //空号发送失败
                if (ZWStringUtils.isEmpty(message.getPhoneNumber())) {
                    PersonalParams personalParams = new PersonalParams();
                    personalParams.setPersonalName(capaPersonals.getConcatNames().get(i));
                    personalParams.setPersonalPhone(message.getPhoneNumber());
                    sendFails.add(personalParams);
                    SendMessageRecord result = messageService.saveMessage(caseInfo, personal, template, capaPersonals.getConcatIds().get(i), user, Integer.valueOf(capaMessageParams.getSendType()), SendMessageRecord.Flag.MANUAL.getValue());
                    templateRepository.saveAndFlush(temp);
                    continue;
                }
                //0 ERPV3 1 极光 2 创蓝 3 数据宝
                switch (type){
                    case "0":
                        SMSMessage smsMessage = new SMSMessage();
                        BeanUtils.copyProperties(message, smsMessage);
                        smsMessage.setParams(params);
                        entity = restTemplate.postForObject("http://common-service/api/SearchMessageController/sendSmsMessage", smsMessage, String.class);
                        break;
                    case "1":
                        SMSMessage JGMessage = new SMSMessage();
                        BeanUtils.copyProperties(message, JGMessage);
                        JGMessage.setParams(params);
                        entity = restTemplate.postForObject("http://common-service/api/SearchMessageController/sendJGSmsMessage", JGMessage, String.class);
                        break;
                    case "2":
                        entity = restTemplate.postForObject("http://common-service/api/SearchMessageController/sendPaaSMessage", message, String.class);
                        break;
                    case "3":
                        entity = restTemplate.postForObject("http://common-service/api/SearchMessageController/sendLookMessage", message, String.class);
                        break;
                }
                if (ZWStringUtils.isNotEmpty(entity)) {
                    //发送失败
                    PersonalParams personalParams = new PersonalParams();
                    personalParams.setPersonalName(capaPersonals.getConcatNames().get(i));
                    personalParams.setPersonalPhone(message.getPhoneNumber());
                    sendFails.add(personalParams);
                    SendMessageRecord result = messageService.saveMessage(caseInfo, personal, template, capaPersonals.getConcatIds().get(i), user, Integer.valueOf(capaMessageParams.getSendType()), SendMessageRecord.Flag.MANUAL.getValue());
                    templateRepository.saveAndFlush(temp);
                } else {
                    //发送成功
                    SendMessageRecord result = messageService.saveMessage(caseInfo, personal, template, capaPersonals.getConcatIds().get(i), user, Integer.valueOf(capaMessageParams.getSendType()), SendMessageRecord.Flag.AUTOMATIC.getValue());
                    templateRepository.saveAndFlush(temp);
                }
            }
        }
        if (!sendFails.isEmpty()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "")).body(sendFails);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("发送成功", "")).body(null);
    }

}
