package cn.fintecher.pangolin.business.web.rest;

import cn.fintecher.pangolin.business.session.SessionStore;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-05-9:17
 */
@RestController
@RequestMapping("/api/userResource")
@Api(value = "userResource", description = "用户信息")
public class UserResource {
    private static final String ENTITY_NAME = "User";

    @GetMapping("/getUserByToken")
    @ApiOperation(value = "通过token获取用户", notes = "通过token获取用户")
    public ResponseEntity<User> getUserByToken(@RequestParam @ApiParam("token") String token) {
        HttpSession session = SessionStore.getInstance().getSession(token);
        if (session == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        return ResponseEntity.ok().body(user);
    }
}
