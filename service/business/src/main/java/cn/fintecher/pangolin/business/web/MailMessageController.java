package cn.fintecher.pangolin.business.web;


import cn.fintecher.pangolin.business.model.EmailBatchSendParams;
import cn.fintecher.pangolin.entity.message.EmailMessage;
import cn.fintecher.pangolin.business.model.EmailSendParams;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.TemplateRepository;
import cn.fintecher.pangolin.business.service.MessageService;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.Template;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;


/**
 * Created by Administrator on 2017/10/31.
 */

@RestController
@RequestMapping("/api/mailMessageController")
@Api(value = "邮件发送", description = "邮件发送")
public class MailMessageController extends BaseController{
    private final static Logger log = LoggerFactory.getLogger(MailMessageController.class);
    @Autowired
    TemplateRepository templateRepository;
    @Autowired
    CaseInfoRepository caseInfoRepository;
    @Autowired
    MessageService messageService;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @RequestMapping(value = "/sendMail", method = RequestMethod.POST)
    @ApiOperation(value = "发送邮件催收", notes = "发送邮件催收")
    public ResponseEntity sendMailMessage(@RequestBody EmailBatchSendParams emailBatchSendParams,
                                  @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "User is not login", "用户未登录")).body(null);
        }
        try {
            if (emailBatchSendParams != null && !CollectionUtils.isEmpty(emailBatchSendParams.getEmailSendParamList())) {
                //根据邮件模板编号获取邮件模板
                Template template = templateRepository.findOne(emailBatchSendParams.getTesmId());
                Template tmp = new Template();
                BeanUtils.copyProperties(template,tmp);
                //邮件发送人员集合
                List<EmailSendParams> emailSendParamList = emailBatchSendParams.getEmailSendParamList();
                EmailMessage emailMessage = null;
                //邮箱地址
                Date currentDate = ZWDateUtil.getNowDateTime();
                for (EmailSendParams emailSendParam : emailSendParamList) {
                    //获取案件集合
                    if (emailSendParam != null && StringUtils.hasText(emailSendParam.getEmail())) {
                        //对内容进行处理
                        CaseInfo caseInfo = caseInfoRepository.findOne(emailSendParam.getCupoId());
                        template.setMessageContent(template.getMessageContent().replace("userName", emailSendParam.getCustName())
                                .replace("business", caseInfo.getPrincipalId().getName()).replace("money", caseInfo.getOverdueAmount().setScale(2).toString()));
                        template.setMessageContent(template.getMessageContent().replace("{{", "").replace("}}", ""));
                        //发送邮件
                        emailMessage = new EmailMessage();
                        emailMessage.setTemplateContent(template.getMessageContent());
                        emailMessage.setSendTo(emailSendParam.getEmail());
                        emailMessage.setSendTime(currentDate);
                        emailMessage.setTitle("邮件催收");
                        rabbitTemplate.convertAndSend("mr.cui.mail.send", emailMessage);
                        messageService.saveMessage(caseInfo,caseInfo.getPersonalInfo(),template,emailSendParam.getCustId(),user,emailBatchSendParams.getMereStyle(),0);
                    }
                }
                templateRepository.save(tmp);
            } else {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "邮箱为空")).body(null);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("发送成功","")).body(null);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "发送失败")).body(null);
        }
    }
}
