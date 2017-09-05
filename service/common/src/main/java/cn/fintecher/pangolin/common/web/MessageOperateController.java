package cn.fintecher.pangolin.common.web;

import cn.fintecher.pangolin.common.client.UserClient;
import cn.fintecher.pangolin.common.service.SmsMessageService;
import cn.fintecher.pangolin.entity.message.PaaSMessage;
import cn.fintecher.pangolin.common.model.SMSMessage;
import cn.fintecher.pangolin.util.ZWDateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
    @Autowired
    SmsMessageService smsMessageService;
    @Autowired
    UserClient userClient;

    @RequestMapping(value = "/sendSmsMessage", method = RequestMethod.POST)
    @ApiOperation(value = "发送短信", notes = "发送短信")
    public ResponseEntity<Void> sendSmsMessage(@RequestBody SMSMessage message) {
        logger.debug("发送短信：{}", message.toString());
        rabbitTemplate.convertAndSend("mr.cui.sms.send", message);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/sendJGSmsMessage", method = RequestMethod.POST)
    @ApiOperation(value = "极光发送短信", notes = "极光发送短信")
    public ResponseEntity<String> sendSmsJGMessage(@RequestBody SMSMessage message) {
        message.setSendTime(ZWDateUtil.getNowDateTime());
        String result = smsMessageService.sendMessageJiGuang(message);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/sendPaaSMessage",method = RequestMethod.POST)
    @ApiOperation(value = "发送云通信短息", notes = "发送云通信短息")
    public ResponseEntity<String> sendPaaSMessage(@RequestBody PaaSMessage message){
        message.setSendTime(ZWDateUtil.getNowDateTime());
        String result = smsMessageService.sendMessagePaaS(message);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
