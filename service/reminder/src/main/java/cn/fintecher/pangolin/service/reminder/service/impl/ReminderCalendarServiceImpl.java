package cn.fintecher.pangolin.service.reminder.service.impl;

import cn.fintecher.pangolin.service.reminder.model.ReminderCalendar;
import cn.fintecher.pangolin.service.reminder.repository.ReminderCalendarRepository;
import cn.fintecher.pangolin.service.reminder.service.ReminderCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by ChenChang on 2017/3/20.
 */
@Service("reminderCalendarService")
public class ReminderCalendarServiceImpl implements ReminderCalendarService {
    private final ReminderCalendarRepository reminderCalendarRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    public ReminderCalendarServiceImpl(ReminderCalendarRepository reminderCalendarRepository) {

        this.reminderCalendarRepository = reminderCalendarRepository;
    }

    @Override
    public List<ReminderCalendar> findByReminderTime() {
        Query query = new Query();
        query.addCriteria(Criteria.where("remindTime").lt(new Date()));
        List<ReminderCalendar> list = mongoTemplate.find(query, ReminderCalendar.class);
        return list;
    }
}
