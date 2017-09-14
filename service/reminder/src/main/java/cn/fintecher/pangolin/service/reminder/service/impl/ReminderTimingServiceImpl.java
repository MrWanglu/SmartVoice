package cn.fintecher.pangolin.service.reminder.service.impl;

import cn.fintecher.pangolin.entity.message.SendReminderMessage;
import cn.fintecher.pangolin.service.reminder.model.ReminderMessage;
import cn.fintecher.pangolin.service.reminder.model.ReminderTiming;
import cn.fintecher.pangolin.service.reminder.repository.ReminderTimingRepository;
import cn.fintecher.pangolin.service.reminder.service.ReminderMessageService;
import cn.fintecher.pangolin.service.reminder.service.ReminderTimingService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service("reminderTimingService")
public class ReminderTimingServiceImpl implements ReminderTimingService {

    @Autowired
    private ReminderTimingRepository reminderTimingRepository;

    @Autowired
    private ReminderMessageService reminderMessageService;

    @Override
    public ReminderTiming saveReminderTiming(SendReminderMessage reminderMessage){
        ReminderTiming reminderTiming=new ReminderTiming();
        BeanUtils.copyProperties(reminderMessage,reminderTiming);
        return reminderTimingRepository.save(reminderTiming);
    }

    @Override
    public List<ReminderTiming> getAllReminderTiming() {
        return reminderTimingRepository.findAll();
    }

    @Override
    public void sendMessageForReminderTiming(ReminderTiming reminderTiming) {
        ReminderMessage reminderMessage = new ReminderMessage();
        BeanUtils.copyProperties(reminderTiming,reminderMessage);
        if(Objects.nonNull(reminderTiming.getUserId())) {
            reminderMessageService.sendMessage(reminderMessage);
        }
        if(Objects.nonNull(reminderTiming.getCcUserIds())){
            for(String userId : reminderTiming.getCcUserIds()){
                reminderMessage.setUserId(userId);
                reminderMessageService.sendMessage(reminderMessage);
            }
        }
        reminderTimingRepository.delete(reminderTiming);
    }
}
