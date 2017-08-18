package cn.fintecher.pangolin.service.reminder.service.impl;

import cn.fintecher.pangolin.entity.message.SendReminderMessage;
import cn.fintecher.pangolin.service.reminder.model.ReminderMessage;
import cn.fintecher.pangolin.service.reminder.model.ReminderTiming;
import cn.fintecher.pangolin.service.reminder.model.ReminderWebSocketMessage;
import cn.fintecher.pangolin.service.reminder.repository.ReminderMessageRepository;
import cn.fintecher.pangolin.service.reminder.repository.ReminderTimingRepository;
import cn.fintecher.pangolin.service.reminder.service.ReminderTimingService;
import cn.fintecher.pangolin.service.reminder.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("reminderTimingService")
public class ReminderTimingServiceImpl implements ReminderTimingService {

    @Autowired
    ReminderTimingRepository reminderTimingRepository;
    @Autowired
    UserService userService;
    @Autowired
    ReminderMessageRepository reminderMessageRepository;
    @Override
    public ReminderTiming saveReminderTiming(SendReminderMessage reminderMessage){
        ReminderTiming reminderTiming=new ReminderTiming();
        BeanUtils.copyProperties(reminderMessage,reminderTiming);
        ReminderTiming result = reminderTimingRepository.save(reminderTiming);
        return result;
    }

    @Override
    public List<ReminderTiming> getAllReminderTiming() {
        return reminderTimingRepository.findAll();
    }

    @Override
    public void sendMessageForReminderTiming(ReminderTiming reminderTiming) {
        ReminderMessage reminderMessage = new ReminderMessage();
        BeanUtils.copyProperties(reminderTiming,reminderMessage);
        reminderMessage.setState(ReminderMessage.ReadStatus.UnRead);
        reminderMessage.setCreateTime(new Date());
        ReminderMessage result = reminderMessageRepository.save(reminderMessage);
        ReminderWebSocketMessage reminderWebSocketMessage = new ReminderWebSocketMessage();
        reminderWebSocketMessage.setData(result);
        userService.sendMessage(result.getUserId(), reminderWebSocketMessage);
        reminderTimingRepository.delete(reminderTiming);
    }


}
