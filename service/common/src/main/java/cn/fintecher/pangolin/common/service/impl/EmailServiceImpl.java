package cn.fintecher.pangolin.common.service.impl;

import cn.fintecher.pangolin.common.model.EmailMessage;
import cn.fintecher.pangolin.common.respository.EmailMessageRepository;
import cn.fintecher.pangolin.common.service.EmailService;
import cn.fintecher.pangolin.entity.message.SendEmailMessage;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.Date;
import java.util.Map;


/**
 * @Author: sunyanping
 * @Description: 邮件发送
 * @Date 2017/2/28
 */
@Service("emailService")
public class EmailServiceImpl implements EmailService {

    @Value("${spring.mail.username}")
    private String from;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Configuration configuration;

    @Autowired
    private EmailMessageRepository repository;


    @Override
    public void sendMail(SendEmailMessage emailMessage) {
        sendStringTemplateMail(emailMessage.getSendTo(), emailMessage.getTitle(), emailMessage.getModel(), emailMessage.getTemplateContent());
        EmailMessage message = new EmailMessage();
        BeanUtils.copyProperties(emailMessage, message);
        repository.save(message);
    }

    private void sendSimpleMail(String sendTo, String title, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(sendTo);
        message.setSubject(title);
        message.setText(content);
        message.setSentDate(new Date());
        mailSender.send(message);
    }


    private void sendFileTemplateMail(String sendTo, String title, Map<String, Object> model, String templateName) {
        try {
            Template t = configuration.getTemplate(templateName);
            sendTemplateMail(sendTo, title, model, t);
        } catch (Exception e) {
            //TODO 这里异常的处理
            e.printStackTrace();
        }
    }

    private void sendStringTemplateMail(String sendTo, String title, Map<String, Object> model, String templateContent) {
        Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        stringLoader.putTemplate("template", templateContent);
        cfg.setTemplateLoader(stringLoader);
        try {
            Template t = cfg.getTemplate("template", "utf-8");
            sendTemplateMail(sendTo, title, model, t);
        } catch (Exception e) {
            //TODO 这里异常的处理
            e.printStackTrace();
        }
    }


    private void sendTemplateMail(String sendTo, String title, Map<String, Object> model, Template t) {
        try {
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
            sendSimpleMail(sendTo, title, html);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
