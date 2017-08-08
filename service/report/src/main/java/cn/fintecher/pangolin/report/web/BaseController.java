package cn.fintecher.pangolin.report.web;

import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.util.Constants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;

/**
 * @author : xiaqun
 * @Description : 报表服务基础接口
 * @Date : 9:04 2017/8/3
 */

public class BaseController {
    @Inject
    RestTemplate restTemplate;

    public User getUserByToken(String token) throws Exception {
        ResponseEntity<User> userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        if (!userResponseEntity.hasBody()) {
            throw new Exception(Constants.SYS_EXCEPTION_NOSESSION);
        }
        return userResponseEntity.getBody();
    }
}