package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseAssist;
import cn.fintecher.pangolin.entity.QCaseAssist;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;


/**
 * @author : xiaqun
 * @Description :
 * @Date : 10:11 2017/7/18
 */

public interface CaseAssistRepository extends QueryDslPredicateExecutor<CaseAssist>, JpaRepository<CaseAssist, String>, QuerydslBinderCustomizer<QCaseAssist> {

    @Override
    default void customize(final QuerydslBindings bindings, final QCaseAssist root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(value).concat("%")));
        bindings.bind(root.caseId.collectionType).first((path, value) -> path.eq(value));
    }
}
