package cn.fintecher.pangolin.service.reminder.web;

import cn.fintecher.pangolin.service.reminder.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/onlineUserController")
@Api(value = "在线用户", description = "在线用户")
public class OnlineUserController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/getOnlineUsers", method = RequestMethod.GET)
    @ApiOperation(value = "获取当前登陆用户数", notes = "获取当前登陆用户数")
    public ResponseEntity<Map<String, Object>> getOnlineUsers() {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("onlineUsers", userService.getOnlineUsers());
        resultMap.put("currentTimeMillis", System.currentTimeMillis());
        return ResponseEntity.ok(resultMap);
    }

}
