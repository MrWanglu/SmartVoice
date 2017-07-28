package cn.fintecher.pangolin.business.client;

import cn.fintecher.pangolin.entity.message.ReminderMessage;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @Author: LvGuoRong
 * @Description:
 * @Date: 2017/7/26
 */
@FeignClient("bussines-service")
public interface ReminderClient {
    @RequestMapping(method = RequestMethod.POST, value = "/api/reminderMessages")
    ResponseEntity<ReminderMessage> createReminderMessage(@RequestBody ReminderMessage reminderMessage);
}
