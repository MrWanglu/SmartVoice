package cn.fintecher.pangolin.business.service.impl;

import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.service.ReminderService;
import cn.fintecher.pangolin.entity.message.SendReminderMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * @author : DuChao
 * @Description : 提醒接口实现
 * @Date : 2017/8/16.
 */
@Service("reminderService")
public class ReminderServiceImpl implements ReminderService{

    @Inject
    private RestTemplate restTemplate;
    @Inject
    private CaseInfoRepository caseInfoRepository;

    @Override
    public void sendReminder(SendReminderMessage sendReminderMessage) {
        restTemplate.postForLocation("http://reminder-service/api/reminderMessages/receiveMessage",sendReminderMessage);
    }

    @Override
    public void saveReminderTiming(SendReminderMessage sendReminderMessage){
        restTemplate.postForLocation("http://reminder-service/api/reminderTiming/saveReminderTiming",sendReminderMessage);
    }


    @Override
    public void sendReminderCalendarMessage(SendReminderMessage sendReminderMessage){
        restTemplate.postForLocation("http://reminder-service/api/reminderCalendars",sendReminderMessage);
    }

    @Override
    public List<SendReminderMessage> getAllReminderMessage(){
        return Collections.emptyList();
    }

}
