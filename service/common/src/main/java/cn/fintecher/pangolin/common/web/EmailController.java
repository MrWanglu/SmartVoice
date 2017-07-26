package cn.fintecher.pangolin.common.web;

import cn.fintecher.pangolin.entity.message.SendEmailMessage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@Api(value = "邮件发送", description = "邮件发送")
@RequestMapping("/api/emailController")
@RestController
public class EmailController {

    private final Logger logger = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    RabbitTemplate rabbitTemplate;


    @RequestMapping(value = "/sendEmail", method = RequestMethod.POST)
    @ApiOperation(value = "发送短信", notes = "发送短信")
    public ResponseEntity<Void> sendSmsMessage(@RequestBody SendEmailMessage message) {
        logger.debug("发送短信：{}", message.toString());
        rabbitTemplate.convertAndSend("mr.cui.mail.send", message);
        return ResponseEntity.ok().build();
    }


}
