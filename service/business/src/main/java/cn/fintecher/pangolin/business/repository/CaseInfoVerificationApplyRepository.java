package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseInfoVerificationApply;
import cn.fintecher.pangolin.entity.QCaseInfoVerificationApply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by ChenChang on 2017/7/11.
 */
public interface CaseInfoVerificationApplyRepository extends QueryDslPredicateExecutor<CaseInfoVerificationApply>, JpaRepository<CaseInfoVerificationApply, String>, QuerydslBinderCustomizer<QCaseInfoVerificationApply> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseInfoVerificationApply root) {
    }
}
