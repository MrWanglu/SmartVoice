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

import javax.xml.ws.handler.MessageContext;
import java.util.*;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 11:24 2017/9/1
 */
@RestController
@RequestMapping("/api/sMSMessageController")
@Api(value = "角色资源管理", description = "角色资源管理")
public class SMSMessageController extends BaseController implements Runnable {
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
     * 电催 电催执行页 短信Icon和一键发送短息
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
        BeanUtils.copyProperties(template, temp);
        //获取短信发送系统参数
        SysParam sysParam = getSysParamByCondition(user, Constants.SMS_PUSH_CODE);
        if (Objects.isNull(sysParam)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "未找到发送短息的配置信息")).body(null);
        }
        String value = sysParam.getValue();
        //查询短信发送时间间隔
        SysParam sysParamInterval = getSysParamByCondition(user, Constants.SMS_PUSH_Interval);
        Integer interval = Objects.isNull(sysParamInterval) ? 0 : Integer.parseInt(sysParamInterval.getValue());

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
        final String messageContent = template.getMessageContent().replace("userName", personal.getName())
                .replace("business", caseInfo.getPrincipalId().getName()).replace("money", caseInfo.getOverdueAmount().setScale(2).toString()).replace("{{", "").replace("}}", "");
        message.setTemplate(template.getTemplateCode());
        message.setCompanyCode(user.getCompanyCode());
        message.setContent(messageContent);
        message.setUserId(user.getId());
        params.put("userName", personal.getName());
        params.put("business", caseInfo.getPrincipalId().getName());
        params.put("money", caseInfo.getOverdueAmount().toString());
        message.setParams(params);
        Thread thread = new Thread(() -> {
            //遍历所有联系人
            for (PersonalParams personalParams1 : personalParams) {
                message.setPhoneNumber(personalParams1.getPersonalPhone());
                //空号发送失败
                if (ZWStringUtils.isEmpty(message.getPhoneNumber())) {
                    sendFails.add(personalParams1);
                    //空号的话不用往数据库插入数据
                    continue;
                }
                run(interval);
                //0 ERPV3 1 极光 2 创蓝 3 数据宝
                try {
                    switch (value) {
                        case "0":
                            SMSMessage smsMessage = new SMSMessage();
                            BeanUtils.copyProperties(message, smsMessage);
                            restTemplate.postForObject(Constants.COMMON_SERVICE_SMS.concat("sendSmsMessage"), smsMessage, String.class);
                            break;
                        case "1":
                            SMSMessage JGMessage = new SMSMessage();
                            BeanUtils.copyProperties(message, JGMessage);
                            restTemplate.postForObject(Constants.COMMON_SERVICE_SMS.concat("sendJGSmsMessage"), JGMessage, String.class);
                            break;
                        case "2":
                            restTemplate.postForObject(Constants.COMMON_SERVICE_SMS.concat("sendPaaSMessage"), message, String.class);
                            break;
                        case "3":
                            restTemplate.postForObject(Constants.COMMON_SERVICE_SMS.concat("sendLookMessage"), message, String.class);
                            break;
                        case "4":
                            restTemplate.postForObject(Constants.COMMON_SERVICE_SMS.concat("sendAliyunMessage"), message, String.class);
                            break;
                    }
                    //发送成功
                    messageService.saveMessage(caseInfo, personal, template, personalParams1.getContId(), user, smsMessageParams.getSendType(), SendMessageRecord.Flag.AUTOMATIC.getValue(), messageContent);
                    templateRepository.saveAndFlush(temp);
                } catch (Exception ex) {
                    //发送失败
                    personalParams1.setReason(ex.getMessage());
                    sendFails.add(personalParams1);
                    messageService.saveMessage(caseInfo, personal, template, personalParams1.getContId(), user, smsMessageParams.getSendType(), SendMessageRecord.Flag.MANUAL.getValue(), messageContent);
                    templateRepository.saveAndFlush(temp);
                }
            }
        });
        thread.start();
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("发送成功", "")).body(sendFails);
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
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        Template template = templateRepository.findOne(capaMessageParams.getTesmId());
        if (Objects.isNull(template)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "没有找到模板")).body(null);
        }
        //获取短信发送系统参数
        SysParam sysParam = getSysParamByCondition(user, Constants.SMS_PUSH_CODE);
        if (Objects.isNull(sysParam)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请先配置系统参数")).body(null);
        }
        String type = sysParam.getValue();
        //查询短信发送时间间隔
        SysParam sysParamInterval = getSysParamByCondition(user, Constants.SMS_PUSH_Interval);
        Integer interval = Objects.isNull(sysParamInterval) ? 0 : Integer.parseInt(sysParamInterval.getValue());
        Map<String, String> params = new HashMap<>();
        List<PersonalParams> sendFails = new ArrayList<>();
        Thread thread = new Thread(() -> {
            //遍历每个客户
            for (CapaPersonals capaPersonals : capaMessageParams.getCapaPersonals()) {
                CaseInfo caseInfo = caseInfoRepository.findOne(QCaseInfo.caseInfo.caseNumber.eq(capaPersonals.getCaseNumber()));
                Personal personal = personalRepository.findOne(capaPersonals.getPersonalId());
                if (Objects.isNull(personal)) {
                    logger.error("没有查询到客户信息");
                    continue;
                }
                //初始化消息
                PaaSMessage message = new PaaSMessage();
                String messageContent = template.getMessageContent().replace("userName", personal.getName())
                        .replace("business", caseInfo.getPrincipalId().getName()).replace("money", caseInfo.getOverdueAmount().setScale(2).toString());
                messageContent = messageContent.replace("{{", "").replace("}}", "");
                message.setTemplate(template.getTemplateCode());
                message.setCompanyCode(user.getCompanyCode());
                message.setContent(messageContent);
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
                        personalParams.setReason("电话号码为空");
                        sendFails.add(personalParams);
                        continue;
                    }
                    run(interval);
                    try {
                        //0 ERPV3 1 极光 2 创蓝 3 数据宝 4 aliyun 5 沃动
                        switch (type) {
                            case "0":
                                SMSMessage smsMessage = new SMSMessage();
                                BeanUtils.copyProperties(message, smsMessage);
                                smsMessage.setParams(params);
                                restTemplate.postForObject(Constants.COMMON_SERVICE_SMS.concat("sendSmsMessage"), smsMessage, String.class);
                                break;
                            case "1":
                                SMSMessage JGMessage = new SMSMessage();
                                BeanUtils.copyProperties(message, JGMessage);
                                JGMessage.setParams(params);
                                restTemplate.postForObject(Constants.COMMON_SERVICE_SMS.concat("sendJGSmsMessage"), JGMessage, String.class);
                                break;
                            case "2":
                                restTemplate.postForObject(Constants.COMMON_SERVICE_SMS.concat("sendPaaSMessage"), message, String.class);
                                break;
                            case "3":
                                restTemplate.postForObject(Constants.COMMON_SERVICE_SMS.concat("sendLookMessage"), message, String.class);
                                break;
                            case "4":
                                message.setParams(params);
                                restTemplate.postForObject(Constants.COMMON_SERVICE_SMS.concat("sendAliyunMessage"), message, String.class);
                                break;
                        }
                        messageService.saveMessage(caseInfo, personal, template, capaPersonals.getConcatIds().get(i), user, Integer.valueOf(capaMessageParams.getSendType()), SendMessageRecord.Flag.AUTOMATIC.getValue(), messageContent);
                    } catch (Exception ex) {
                        PersonalParams personalParams = new PersonalParams();
                        personalParams.setPersonalName(capaPersonals.getConcatNames().get(i));
                        personalParams.setPersonalPhone(message.getPhoneNumber());
                        personalParams.setReason(ex.getMessage());
                        sendFails.add(personalParams);
                        messageService.saveMessage(caseInfo, personal, template, capaPersonals.getConcatIds().get(i), user, Integer.valueOf(capaMessageParams.getSendType()), SendMessageRecord.Flag.MANUAL.getValue(), messageContent);
                    }
                }
            }
        });
        thread.start();
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("发送成功", "")).body(sendFails);
    }

    private SysParam getSysParamByCondition(User user, String code) {
        BooleanBuilder exp = new BooleanBuilder();
        exp.and(QSysParam.sysParam.code.eq(code));
        exp.and(QSysParam.sysParam.companyCode.eq(user.getCompanyCode()));
        exp.and(QSysParam.sysParam.status.eq(SysParam.StatusEnum.Start.getValue()));
        return sysParamRepository.findOne(exp);
    }

    @Override
    public void run() {
    }

    public void run(Integer time) {
        try {
            new Thread().sleep(time);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }
}
