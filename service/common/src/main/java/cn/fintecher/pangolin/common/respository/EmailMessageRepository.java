package cn.fintecher.pangolin.common.respository;


import cn.fintecher.pangolin.common.model.EmailMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;


public interface EmailMessageRepository extends MongoRepository<EmailMessage, String>, QueryDslPredicateExecutor<EmailMessage> {

}
