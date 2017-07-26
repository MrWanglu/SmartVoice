package cn.fintecher.pangolin.service.reminder.service;

import cn.fintecher.pangolin.service.reminder.model.ReminderCalendar;

import java.util.List;

/**
 * Created by ChenChang on 2017/3/20.
 */
public interface ReminderCalendarService {
    List<ReminderCalendar> findByReminderTime();
}
