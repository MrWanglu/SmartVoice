package cn.fintecher.pangolin.service.reminder.web.rest;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Created by gaobeibei.
 * Description:
 * Date: 2017-8-1
 */
@RestController
@RequestMapping("/api/appmsgResource")
@Api(value = "app信息推送", description = "app信息推送")
public class AppMsgResource {
    private final Logger log = LoggerFactory.getLogger(AppMsgResource.class);

    @Autowired
    UserClient userClient;
    @Autowired
    AppMsgRepository appMsgRepository;
    @Autowired
    AppMsgService appMsgService;

    @PostMapping("/appmsg")
    @ApiOperation(value = "app信息推送", notes = "app信息推送")
    public ResponseEntity<AppMsg> appmsg(@RequestBody AppMsg request){
        log.debug("REST request to save Appmsg : {}", request);
        ResponseEntity<User> userResult = userClient.findUserById(request.getUserId());
        User user = userResult.getBody();
        if (Objects.isNull(user)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createAlert("无法获取用户", "")).body(null);
        }
        if(Objects.nonNull(user.getMessagePushId())) {
            RestTemplate restTemplate = new RestTemplate();
            MessagePush messagePush = restTemplate.getForEntity("http://business-service/api/appRegisterResource/getAppRegisterById?id="+user.getMessagePushId(),MessagePush.class).getBody();
            if(Objects.nonNull(messagePush)) {
                request.setUserId(messagePush.getPushRegid());
                request.setUserName(user.getUserName());
                appMsgService.sendPush(request);
                appMsgRepository.save(request);
            }
        }
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("推送成功", "")).body(null);
    }
}
