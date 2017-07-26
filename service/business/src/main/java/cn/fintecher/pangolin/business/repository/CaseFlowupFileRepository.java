package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseFlowupFile;
import cn.fintecher.pangolin.entity.QCaseFlowupFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by Administrator on 2017/7/20.
 */
public interface CaseFlowupFileRepository extends QueryDslPredicateExecutor<CaseFlowupFile>, JpaRepository<CaseFlowupFile, String>, QuerydslBinderCustomizer<QCaseFlowupFile> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseFlowupFile root) {
    }
}
