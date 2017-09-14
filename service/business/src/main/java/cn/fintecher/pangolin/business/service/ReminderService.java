package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.entity.message.SendReminderMessage;

import java.util.List;

public interface ReminderService {

    /**
     * 发送消息提醒
     * @param sendReminderMessage
     */
    void sendReminder(SendReminderMessage sendReminderMessage);

    /**
     * 保存消息提醒
     * @param sendReminderMessage
     */
    void saveReminderTiming(SendReminderMessage sendReminderMessage);

    /**
     * 获取消息提醒内容
     * @return
     */
    List<SendReminderMessage> getAllReminderMessage();

    /**
     * 发送定时提醒
     * @param sendReminderMessage
     */
    void sendReminderCalendarMessage(SendReminderMessage sendReminderMessage);
}
