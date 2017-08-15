package cn.fintecher.pangolin.service.reminder.web;

import cn.fintecher.pangolin.entity.MessagePush;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.service.reminder.client.UserClient;
import cn.fintecher.pangolin.service.reminder.model.AppMsg;
import cn.fintecher.pangolin.service.reminder.repository.AppMsgRepository;
import cn.fintecher.pangolin.service.reminder.service.AppMsgService;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

/**
 * Created by  gaobeibei.
 * Description:
 * Date: 2017-8-1
 */
@RestController
@RequestMapping(value = "/api/appMsgController")
@Api(value = "app信息推送", description = "app信息推送")
public class AppMsgController{
    private final Logger log = LoggerFactory.getLogger(AppMsgController.class);

    @Autowired
    AppMsgRepository appMsgRepository;
    @Autowired
    AppMsgService appMsgService;
    @Autowired
    UserClient userClient;

    @PostMapping("/saveAppmsg")
    @ApiOperation(value = "新增app信息推送", notes = "新增app信息推送")
    @ResponseBody
    public ResponseEntity saveAppmsg(@RequestBody AppMsg request) {
        AppMsg returnAppMsg = appMsgRepository.save(request);
        appMsgService.sendPush(returnAppMsg);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("推送成功", "")).body(null);
    }
}
