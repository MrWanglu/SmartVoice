package cn.fintecher.pangolin.dataimp.repository;

import cn.fintecher.pangolin.entity.strategy.CaseStrategy;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by luqiang on 2017/8/2.
 */
public interface CaseStrategyRepository extends MongoRepository<CaseStrategy, String> {
}
