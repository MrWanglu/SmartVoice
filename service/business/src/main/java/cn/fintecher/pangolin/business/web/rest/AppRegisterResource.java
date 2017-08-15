package cn.fintecher.pangolin.business.web.rest;

import cn.fintecher.pangolin.business.repository.MessagePushRepository;
import cn.fintecher.pangolin.entity.MessagePush;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by  gaobeibei.
 * Description: 新增app消息推送注册服务
 * Date: 2017-8-1
 */
@RestController
@RequestMapping("/api/appRegisterResource")
@Api(value = "APP注册服务及消息推送", description = "APP注册服务及消息推送")
public class AppRegisterResource {
    private final Logger log = LoggerFactory.getLogger(AppRegisterResource.class);

    @Autowired
    MessagePushRepository messagePushRepository;

    @PostMapping
    @ApiOperation(value = "新增app注册服务", notes = "新增app注册服务")
    public ResponseEntity createAppRegister(@RequestBody MessagePush messagePush) {
        log.debug("REST request to save AppRegister  : {}", messagePush);
        messagePush.setPushStatus(MessagePush.PushStatus.Enable.getValue());
        messagePush.setOperateTime(ZWDateUtil.getNowDateTime());
        MessagePush result = messagePushRepository.save(messagePush);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("绑定成功", "")).body(null);
    }

    @GetMapping("/getAppRegisterById")
    @ApiOperation(value = "查询app注册服务通过id", notes = "查询app注册服务通过id")
    public ResponseEntity<MessagePush> getAppRegisterById(@RequestParam(value = "id") String id) {
        MessagePush messagePush = messagePushRepository.findOne(id);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("绑定成功", "")).body(messagePush);
    }
}
