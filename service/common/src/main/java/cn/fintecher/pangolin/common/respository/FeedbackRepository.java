package cn.fintecher.pangolin.common.respository;

import cn.fintecher.pangolin.common.model.Feedback;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * @Author: gaobeibei
 * @Description:
 * @Date 15:47 2017/8/7
 */
public interface FeedbackRepository extends MongoRepository<Feedback, String>, QueryDslPredicateExecutor<Feedback> {
}
