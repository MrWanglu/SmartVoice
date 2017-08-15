package cn.fintecher.pangolin.business.web.rest;

import cn.fintecher.pangolin.business.repository.UserLoginLogRepository;
import cn.fintecher.pangolin.entity.UserLoginLog;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

/**
 * @Author : sunyanping
 * @Description : 用户登录日志
 * @Date : 2017/8/7.
 */
@ApiIgnore
@RestController
@RequestMapping("/api/userLoginLogResource")
@Api(value = "UserLoginLogResource", description = "用户登录日志")
public class UserLoginLogResource {

    private final Logger log = LoggerFactory.getLogger(UserLoginLogResource.class);

    @Inject
    private UserLoginLogRepository userLoginLogRepository;

    @PostMapping
    @ApiOperation(value = "保存用户登录日志", notes = "保存用户登录日志")
    public ResponseEntity<UserLoginLog> save(@RequestBody UserLoginLog userLoginLog) throws URISyntaxException {
        log.debug("REST request to save UserLoginLog : {}", userLoginLog);
        userLoginLog = userLoginLogRepository.save(userLoginLog);
        return ResponseEntity.created(new URI("/api/userLoginLogResource/" + userLoginLog.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(UserLoginLog.class.getSimpleName(), userLoginLog.getId()))
                .body(userLoginLog);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取用户登录日志", notes = "获取用户登录日志")
    public ResponseEntity<UserLoginLog> get(@PathVariable @ApiParam(value = "日志ID/SessionID") String id) {
        log.debug("REST request to get CustomerInformation : {}", id);
        UserLoginLog userLoginLog = userLoginLogRepository.findOne(id);
        return Optional.ofNullable(userLoginLog)
                .map(result -> new ResponseEntity<>(
                        result,
                        HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
