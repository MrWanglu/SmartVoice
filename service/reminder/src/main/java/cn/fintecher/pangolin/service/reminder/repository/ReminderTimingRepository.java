package cn.fintecher.pangolin.service.reminder.repository;

import cn.fintecher.pangolin.service.reminder.model.ReminderTiming;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * @Author duchao
 * @Description
 * @Date : 2017/8/18.
 */
public interface ReminderTimingRepository extends MongoRepository<ReminderTiming, String>, QueryDslPredicateExecutor<ReminderTiming> {
}
