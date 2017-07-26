package cn.fintecher.pangolin.business.client;

import cn.fintecher.pangolin.entity.User;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-05-9:24
 */
@FeignClient("user-service")
public interface UserClient {
    @RequestMapping(method = RequestMethod.GET, value = "/api/userResource/getUserByToken")
    ResponseEntity<User> getUserByToken(@RequestParam @ApiParam("token") String token);
}
