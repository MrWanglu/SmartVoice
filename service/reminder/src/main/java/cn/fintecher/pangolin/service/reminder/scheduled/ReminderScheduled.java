package cn.fintecher.pangolin.service.reminder.scheduled;

import cn.fintecher.pangolin.service.reminder.model.AppMsg;
import cn.fintecher.pangolin.service.reminder.model.ReminderCalendar;
import cn.fintecher.pangolin.service.reminder.model.ReminderMessage;
import cn.fintecher.pangolin.service.reminder.model.ReminderWebSocketMessage;
import cn.fintecher.pangolin.service.reminder.repository.ReminderCalendarRepository;
import cn.fintecher.pangolin.service.reminder.repository.ReminderMessageRepository;
import cn.fintecher.pangolin.service.reminder.service.AppMsgService;
import cn.fintecher.pangolin.service.reminder.service.ReminderCalendarService;
import cn.fintecher.pangolin.service.reminder.service.ReminderMessageService;
import cn.fintecher.pangolin.service.reminder.service.UserService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Component
@EnableScheduling
@Lazy(value = false)
public class ReminderScheduled {
    private final Logger log = LoggerFactory.getLogger(ReminderScheduled.class);
    @Autowired
    ReminderCalendarRepository reminderCalendarRepository;
    @Autowired
    ReminderMessageRepository reminderMessageRepository;

    @Autowired
    ReminderMessageService reminderMessageService;
    @Autowired
    ReminderCalendarService reminderCalendarService;
    @Autowired
    AppMsgService appMsgService;
    @Autowired
    UserService userService;

    @Scheduled(cron = "1 0/1 * * * ?")
    void checkReminderCalendar() throws IOException {
        log.debug("定时调度 检查是否有定时提醒" + new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
        List<ReminderCalendar> list = reminderCalendarService.findByReminderTime();
        for (ReminderCalendar calendar : list) {
            ReminderMessage reminderMessage = new ReminderMessage();
            BeanUtils.copyProperties(calendar, reminderMessage);
            reminderMessage.setState(ReminderMessage.ReadStatus.UnRead);
            reminderMessage.setCreateTime(new Date());
            reminderMessageRepository.save(reminderMessage);
            reminderCalendarRepository.delete(calendar);
            ReminderWebSocketMessage reminderWebSocketMessage = new ReminderWebSocketMessage();
            reminderWebSocketMessage.setData(reminderMessage);
            Long count = reminderMessageService.countUnRead(calendar.getUserId());
            reminderWebSocketMessage.setUnReadeCount(count);
            userService.sendMessage(calendar.getUserId(), reminderWebSocketMessage);
            //app推送信息
            AppMsg request = new AppMsg();
            BeanUtils.copyProperties(calendar, request);
            request.setId(null);
            request.setAppMsgUnRead(new Long(count).intValue());
            request.setContent(calendar.getContent());
            appMsgService.sendPush(request);

        }

    }
}
