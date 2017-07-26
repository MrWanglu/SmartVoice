package cn.fintecher.pangolin.service.reminder.repository;


import cn.fintecher.pangolin.service.reminder.model.MobilePosition;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * @author : xiaqun
 * @Description : 移动定位
 * @Date : 11:23 2017/5/31
 */

public interface MobilePositionRepository extends MongoRepository<MobilePosition, String>, QueryDslPredicateExecutor<MobilePosition> {
}
