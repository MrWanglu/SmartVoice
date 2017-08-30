package cn.fintecher.pangolin.dataimp.repository;

import cn.fintecher.pangolin.dataimp.entity.CaseStrategy;
import cn.fintecher.pangolin.dataimp.entity.QCaseStrategy;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by luqiang on 2017/8/2.
 */
public interface CaseStrategyRepository extends MongoRepository<CaseStrategy, String>,QueryDslPredicateExecutor<CaseStrategy>
        ,QuerydslBinderCustomizer<QCaseStrategy> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseStrategy root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(StringUtils.trim(value)).concat("%")));
    }
}
