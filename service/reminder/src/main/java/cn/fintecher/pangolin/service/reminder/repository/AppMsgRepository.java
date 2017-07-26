package cn.fintecher.pangolin.service.reminder.repository;


import cn.fintecher.pangolin.service.reminder.model.AppMsg;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-05-02-11:56
 */
public interface AppMsgRepository extends MongoRepository<AppMsg, String>, QueryDslPredicateExecutor<AppMsg> {

}
