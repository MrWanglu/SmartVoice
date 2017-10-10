package cn.fintecher.pangolin.dataimp.repository;

import cn.fintecher.pangolin.entity.strategy.ScoreRule;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by luqiang on 2017/8/10.
 */
public interface ScoreRuleRepository extends MongoRepository<ScoreRule, String> {

}
