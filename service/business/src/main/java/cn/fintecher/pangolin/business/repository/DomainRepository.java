package cn.fintecher.pangolin.business.repository;


import cn.fintecher.pangolin.entity.Domain;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * Created by ChenChang on 2017/7/11.
 */
public interface DomainRepository extends QueryDslPredicateExecutor<Domain>, MongoRepository<Domain, String> {
}
