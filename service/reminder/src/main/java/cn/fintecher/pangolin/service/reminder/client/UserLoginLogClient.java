package cn.fintecher.pangolin.service.reminder.client;

import cn.fintecher.pangolin.entity.UserLoginLog;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/8/7.
 */
@FeignClient("business-service")
public interface UserLoginLogClient {
    @RequestMapping(method = RequestMethod.GET, value = "/api/userLoginLogResource/{id}")
    ResponseEntity<UserLoginLog> get(@PathVariable("id") String id);

    @RequestMapping(method = RequestMethod.POST, value = "/api/userLoginLogResource")
    ResponseEntity<UserLoginLog> save(@RequestBody UserLoginLog userLoginLog);
}
