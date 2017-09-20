package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseInfoRemark;
import cn.fintecher.pangolin.entity.QCaseInfoRemark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * @author : xiaqun
 * @Description :
 * @Date : 13:49 2017/9/20
 */

public interface CaseInfoRemarkRepository extends QueryDslPredicateExecutor<CaseInfoRemark>, JpaRepository<CaseInfoRemark, String>, QuerydslBinderCustomizer<QCaseInfoRemark> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseInfoRemark root) {
    }
}
