package cn.fintecher.pangolin.service.reminder.repository;


import cn.fintecher.pangolin.service.reminder.model.ReminderCalendar;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * Created by ChenChang on 2017/3/20.
 */
public interface ReminderCalendarRepository extends MongoRepository<ReminderCalendar, String>, QueryDslPredicateExecutor<ReminderCalendar> {
}
