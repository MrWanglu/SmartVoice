package cn.fintecher.pangolin.common.web;

import cn.fintecher.pangolin.common.client.UserClient;
import cn.fintecher.pangolin.common.model.SMSMessage;
import cn.fintecher.pangolin.common.service.SmsMessageService;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.message.SendSMSMessage;
import cn.fintecher.pangolin.util.ZWDateUtil;
import com.netflix.discovery.converters.Auto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<Void> sendSmsMessage(@RequestBody SendSMSMessage message) {
        logger.debug("发送短信：{}", message.toString());
        rabbitTemplate.convertAndSend("mr.cui.sms.send", message);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/sendSmsMessage", method = RequestMethod.POST)
    @ApiOperation(value = "极光发送短信", notes = "极光发送短信")
    public ResponseEntity<Void> sendSmsMessage(@RequestBody SMSMessage message,
                                                 @RequestHeader(value="X-UserToken") @ApiParam("操作者的token") String token) {
        User user = userClient.getUserByToken(token).getBody();
        message.setCompanyCode(user.getCompanyCode());
        message.setUserId(user.getId());
        message.setSendTime(ZWDateUtil.getNowDateTime());
        smsMessageService.sendMessageJiGuang(message);
        return ResponseEntity.ok().build();
    }
}
