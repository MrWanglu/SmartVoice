package cn.fintecher.pangolin.business.repository;


import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.QCaseInfo;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by ChenChang on 2017/7/11.
 */
public interface CaseInfoRepository extends QueryDslPredicateExecutor<CaseInfo>, MongoRepository<CaseInfo, String>, QuerydslBinderCustomizer<QCaseInfo> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseInfo root) {

        bindings.bind(String.class).first((StringPath path, String value) -> path.containsIgnoreCase(value));
//        bindings.bind(root.domain.id).first((path, value) -> path.like(value));
    }
}
