package cn.fintecher.server.web;

import cn.fintecher.entity.AuthUser;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.server.repository.AuthUserRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by qijigui on 2018-05-10.
 */
@RestController
@RequestMapping("/api/userController")
@Api(value = "用户管理", description = "用户管理")
public class UserController {

    final private Logger logger = LoggerFactory.getLogger(UserController.class);

    @Inject
    AuthUserRepository authUserRepository;

    /**
     * @Description 获取所有客户信息
     */
    @PostMapping("/getAllUsers")
    @ApiOperation(value = "获取所有客户信息", notes = "获取所有客户信息")
    public ResponseEntity<List<AuthUser>> getAllUsers() {
        try {
            List<AuthUser> authUserRepositoryAll = authUserRepository.findAll();
            return ResponseEntity.ok().body(authUserRepositoryAll);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "查询失败")).body(null);
        }
    }

}
