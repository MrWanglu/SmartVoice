package cn.fintecher.pangolin.dataimp.repository;

import cn.fintecher.pangolin.dataimp.entity.CaseStrategy;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * Created by luqiang on 2017/8/2.
 */
public interface CaseStrategyRepository extends MongoRepository<CaseStrategy, String>,QueryDslPredicateExecutor<CaseStrategy>{
}
