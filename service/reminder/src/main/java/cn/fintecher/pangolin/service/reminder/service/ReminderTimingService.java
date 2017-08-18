package cn.fintecher.pangolin.service.reminder.service;

import cn.fintecher.pangolin.entity.message.SendReminderMessage;
import cn.fintecher.pangolin.service.reminder.model.ReminderTiming;

import java.util.List;

public interface ReminderTimingService {

    /**
     * 保存消息至定时任务
     * @param sendReminderMessage
     * @return
     */
    ReminderTiming saveReminderTiming(SendReminderMessage sendReminderMessage);

    /**
     * 获取定时提醒列表
     * @return
     */
    List<ReminderTiming> getAllReminderTiming();

    /**
     * 发送定时消息
     * @param reminderTiming
     */
    void sendMessageForReminderTiming(ReminderTiming reminderTiming);
}
