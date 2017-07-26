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
 * 接受文件上传成功队列消息
 * Created by ChenChang on 2017/3/31.
 */
@Component
@RabbitListener(queues = "mr.cui.file.unReduce.progress")
public class UnReduceFileProgressMessageReceiver {

    private final Logger logger = LoggerFactory.getLogger(UnReduceFileProgressMessageReceiver.class);

    @Autowired
    private UserService userService;

    @RabbitHandler
    public void receive(ProgressMessage message) {
        try {
            logger.debug("收到处理文件进度消息 {}", message);
            ProgressWebSocketMessage progressWebSocketMessage = new ProgressWebSocketMessage();
            progressWebSocketMessage.setData(message);
            userService.sendMessage(message.getUserId(), progressWebSocketMessage);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }
}
