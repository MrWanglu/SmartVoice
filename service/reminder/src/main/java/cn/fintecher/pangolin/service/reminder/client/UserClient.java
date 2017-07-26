package cn.fintecher.pangolin.service.reminder.client;

import cn.fintecher.pangolin.entity.User;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient("business-service")
public interface UserClient {
    @RequestMapping(method = RequestMethod.GET, value = "/api/userResource/getUserByToken")
    ResponseEntity<User> getUserByToken(@RequestParam(value = "token") String token);

}
