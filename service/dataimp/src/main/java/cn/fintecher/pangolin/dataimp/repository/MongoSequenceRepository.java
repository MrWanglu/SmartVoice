package cn.fintecher.pangolin.dataimp.repository;

import cn.fintecher.pangolin.dataimp.entity.MongoSequence;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 15:56 2017/7/19
 */
public interface MongoSequenceRepository extends MongoRepository<MongoSequence, String>,QueryDslPredicateExecutor<MongoSequence> {
}
