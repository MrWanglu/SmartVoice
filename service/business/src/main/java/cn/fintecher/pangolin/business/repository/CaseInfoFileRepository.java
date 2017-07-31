package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseInfoFile;
import cn.fintecher.pangolin.entity.QCaseInfoFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 10:24 2017/7/29
 */
public interface CaseInfoFileRepository extends QueryDslPredicateExecutor<CaseInfoFile>, JpaRepository<CaseInfoFile, String>, QuerydslBinderCustomizer<QCaseInfoFile> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseInfoFile root) {

    }
}
