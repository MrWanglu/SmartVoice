package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.entity.message.SendReminderMessage;
import cn.fintecher.pangolin.entity.util.Constants;
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
@RabbitListener(queues = Constants.FOLLOWUP_EXPORT_QE)
public class FollowupProgressMessageReceiver {

    private final Logger logger = LoggerFactory.getLogger(FollowupProgressMessageReceiver.class);

    @Autowired
    private ReminderService reminderService;

    @RabbitHandler
    public void receive(SendReminderMessage message) {
        try {
            logger.debug("接口处理消息 {}", message);
            reminderService.sendReminder(message);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }
}
