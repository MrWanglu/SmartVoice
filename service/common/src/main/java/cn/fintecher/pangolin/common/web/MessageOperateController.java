package cn.fintecher.pangolin.common.web;

import cn.fintecher.pangolin.entity.message.SendSMSMessage;
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

/**
 * Created by qijigui on 2017/3/24.
 */

@Api(value = "短信发送", description = "短信发送")
@RequestMapping("/api/SearchMessageController")
@RestController
public class MessageOperateController {

    private final Logger logger = LoggerFactory.getLogger(MessageOperateController.class);

    @Autowired
    RabbitTemplate rabbitTemplate;


    @RequestMapping(value = "/sendSmsMessage", method = RequestMethod.POST)
    @ApiOperation(value = "发送短信", notes = "发送短信")
    public ResponseEntity<Void> sendSmsMessage(@RequestBody SendSMSMessage message) {
        logger.debug("发送短信：{}", message.toString());
        rabbitTemplate.convertAndSend("mr.cui.sms.send", message);
        return ResponseEntity.ok().build();
    }


}
