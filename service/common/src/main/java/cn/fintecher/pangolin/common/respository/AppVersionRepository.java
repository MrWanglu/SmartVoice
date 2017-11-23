package cn.fintecher.pangolin.common.respository;

import cn.fintecher.pangolin.common.model.AppVersion;
import cn.fintecher.pangolin.common.model.QAppVersion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 15:47 2017/7/18
 */
public interface AppVersionRepository extends MongoRepository<AppVersion, String>, QueryDslPredicateExecutor<AppVersion>,
        QuerydslBinderCustomizer<QAppVersion> {
    @Override
    default void customize(final QuerydslBindings bindings, final QAppVersion root) {
    }

}
