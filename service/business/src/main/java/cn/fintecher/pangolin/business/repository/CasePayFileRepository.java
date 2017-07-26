package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CasePayFile;
import cn.fintecher.pangolin.entity.QCasePayFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by Administrator on 2017/7/20.
 */
public interface CasePayFileRepository extends QueryDslPredicateExecutor<CasePayFile>, JpaRepository<CasePayFile, String>, QuerydslBinderCustomizer<QCasePayFile> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCasePayFile root) {
    }
}
