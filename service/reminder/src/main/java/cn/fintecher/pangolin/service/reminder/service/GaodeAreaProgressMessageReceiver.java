package cn.fintecher.pangolin.service.reminder.service;

import cn.fintecher.pangolin.entity.message.ProgressMessage;
import cn.fintecher.pangolin.service.reminder.model.ProgressWebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 高德地图接口处理进度
 * Created by SunYanPing on 2017/6/16.
 */
@Component
@RabbitListener(queues = "mr.cui.data.area.progress")
public class GaodeAreaProgressMessageReceiver {

    private final Logger logger = LoggerFactory.getLogger(GaodeAreaProgressMessageReceiver.class);

    @Autowired
    private UserService userService;

    @RabbitHandler
    public void receive(ProgressMessage message) {
        try {
            logger.debug("接口处理进度消息 {}", message);
            ProgressWebSocketMessage progressWebSocketMessage = new ProgressWebSocketMessage();
            progressWebSocketMessage.setData(message);
            userService.sendMessage(message.getUserId(), progressWebSocketMessage);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }
}
