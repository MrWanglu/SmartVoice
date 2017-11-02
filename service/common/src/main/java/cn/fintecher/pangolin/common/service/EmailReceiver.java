package cn.fintecher.pangolin.common.service;

import cn.fintecher.pangolin.entity.message.EmailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/10/31.
 */

@Component
@RabbitListener(queues = "mr.cui.mail.send")
public class EmailReceiver {

    private final Logger log = LoggerFactory.getLogger(EmailMessage.class);

    @Autowired
    EmailService emailService;

    @RabbitHandler
    public void receive(EmailMessage message) {
        log.debug("entry the send message {}", message);
        emailService.sendMail(message);
    }
}
