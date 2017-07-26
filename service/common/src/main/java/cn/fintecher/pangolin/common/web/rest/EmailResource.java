package cn.fintecher.pangolin.common.web.rest;

import cn.fintecher.pangolin.common.service.EmailService;
import cn.fintecher.pangolin.entity.message.SendEmailMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Created by ChenChang on 2017/3/2.
 */
@RestController
@RequestMapping("/api/email")
@Api(value = "", description = "发送邮件")
public class EmailResource {
    @Autowired
    private EmailService emailService;

    @PostMapping("/sendMail")
    @ApiOperation(value = "发送邮件同步", notes = "发送邮件同步")
    public void sendMail(@RequestBody SendEmailMessage message) {
        emailService.sendMail(message);
    }
}
