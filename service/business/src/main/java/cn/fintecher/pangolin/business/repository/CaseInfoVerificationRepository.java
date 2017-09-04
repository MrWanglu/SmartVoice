package cn.fintecher.pangolin.business.repository;


import cn.fintecher.pangolin.entity.CaseInfoVerificationModel;
import cn.fintecher.pangolin.entity.QCaseInfoVerificationModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by ChenChang on 2017/7/11.
 */
public interface CaseInfoVerificationRepository extends QueryDslPredicateExecutor<CaseInfoVerificationModel>, JpaRepository<CaseInfoVerificationModel, String>, QuerydslBinderCustomizer<QCaseInfoVerificationModel> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseInfoVerificationModel root) {
    }
}
