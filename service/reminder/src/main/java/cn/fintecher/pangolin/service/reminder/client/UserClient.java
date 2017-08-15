package cn.fintecher.pangolin.service.reminder.client;

import cn.fintecher.pangolin.entity.User;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient("business-service")
public interface UserClient {
    @RequestMapping(method = RequestMethod.GET, value = "/api/userResource/getUserByToken")
    ResponseEntity<User> getUserByToken(@RequestParam(value = "token") String token);

    @RequestMapping(method = RequestMethod.GET, value = "/api/userResource/getUsersOnCompany")
    ResponseEntity<Integer> getUsersOnCompany(@RequestParam(value = "ids") @ApiParam("ids") List<String> ids,
                                              @RequestParam(value = "token") @ApiParam("token")String token);

    @RequestMapping(method = RequestMethod.GET, value = "/api/userResource/findUserById")
    ResponseEntity<User> findUserById(@RequestParam(value = "id") String id);
}
