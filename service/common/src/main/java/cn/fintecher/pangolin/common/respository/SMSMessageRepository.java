package cn.fintecher.pangolin.common.respository;


import cn.fintecher.pangolin.common.model.SMSMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * Created by qijigui on 2017/3/27.
 * 短信记录
 */
public interface SMSMessageRepository extends MongoRepository<SMSMessage, String>, QueryDslPredicateExecutor<SMSMessage> {

}
