package cn.fintecher.pangolin.common.service;


import cn.fintecher.pangolin.entity.message.SendSMSMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;


/**
 * 接受短信队列消息
 * Created by ChenChang on 2017/3/23.
 */
@Component
@RabbitListener(queues = "mr.cui.sms.send")
public class SmsReceiver {

    private final Logger log = LoggerFactory.getLogger(SmsReceiver.class);

    @Autowired
    private SmsMessageService smsMessageService;

    @RabbitHandler
    public void receive(SendSMSMessage message) {
        log.debug("entry the send message {}", message);
        message.setSendTime(new Date());
        smsMessageService.sendMessage(message);
    }
}
