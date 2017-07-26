package cn.fintecher.pangolin.common.respository;

import cn.fintecher.pangolin.common.model.AppVersion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 15:47 2017/7/18
 */
public interface AppVersionRepository extends MongoRepository<AppVersion, String>, QueryDslPredicateExecutor<AppVersion> {
}
