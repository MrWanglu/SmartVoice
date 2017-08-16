package cn.fintecher.pangolin.dataimp.repository;

import cn.fintecher.pangolin.dataimp.entity.ScoreRule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * Created by luqiang on 2017/8/10.
 */
public interface ScoreRuleRepository extends MongoRepository<ScoreRule, String>,QueryDslPredicateExecutor<ScoreRule> {

}
