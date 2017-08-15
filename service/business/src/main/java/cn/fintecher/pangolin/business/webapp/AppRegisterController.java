package cn.fintecher.pangolin.business.webapp;

import cn.fintecher.pangolin.business.repository.MessagePushRepository;
import cn.fintecher.pangolin.business.repository.UserRepository;
import cn.fintecher.pangolin.business.web.BaseController;
import cn.fintecher.pangolin.entity.MessagePush;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.util.ZWStringUtils;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.Objects;

/**
 * Created by gaobeibei
 * Description:
 * Date: 2017-8-1
 */
@RestController
@RequestMapping("/api/appRegisterController")
@Api(value = "APP注册服务及消息推送", description = "APP注册服务及消息推送")
public class AppRegisterController extends BaseController {
    private final Logger log = LoggerFactory.getLogger(AppRegisterController.class);

    @Inject
    MessagePushRepository messagePushRepository;
    @Inject
    UserRepository userRepository;

    @PostMapping("/saveAppRegister")
    @ApiOperation(value = "新增app消息推送注册服务", notes = "新增app消息推送注册服务")
    public ResponseEntity saveAppRegister(@ApiParam("新增app注册服务") @RequestBody MessagePush messagePush, @RequestHeader(value="X-UserToken") @ApiParam("操作者的token") String token) {
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        if (Objects.isNull(user.getMessagePushId()) || ZWStringUtils.isEmpty(user.getMessagePushId())) {
            messagePush.setPushStatus(MessagePush.PushStatus.Enable.getValue());
            messagePush.setCompanyCode(user.getCompanyCode());
            messagePush.setOperateTime(ZWDateUtil.getNowDateTime());
            messagePush.setOperator(user.getUserName());
            MessagePush result = messagePushRepository.save(messagePush);
            user.setMessagePushId(result.getId());
            userRepository.saveAndFlush(user);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("绑定成功", "")).body(null);
        } else {
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("已绑定", "")).body(null);
        }
    }
}
