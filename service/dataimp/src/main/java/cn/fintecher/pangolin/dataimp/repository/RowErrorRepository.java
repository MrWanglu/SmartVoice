package cn.fintecher.pangolin.dataimp.repository;

import cn.fintecher.pangolin.dataimp.entity.QRowError;
import cn.fintecher.pangolin.dataimp.entity.RowError;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by sun on 2017/9/22.
 */
public interface RowErrorRepository extends MongoRepository<RowError, String>, QueryDslPredicateExecutor<RowError>, QuerydslBinderCustomizer<QRowError> {
    @Override
    default void customize(final QuerydslBindings bindings, final QRowError root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(StringUtils.trim(value)).concat("%")));
    }
}
