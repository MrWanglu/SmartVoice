package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseRepair;
import cn.fintecher.pangolin.entity.QCaseRepair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * @Author: PeiShouWen
 * @Description: 案件修复
 * @Date 19:45 2017/8/7
 */
public interface CaseRepairRepository extends QueryDslPredicateExecutor<CaseRepair>, JpaRepository<CaseRepair, String>, QuerydslBinderCustomizer<QCaseRepair> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseRepair root) {
    }
}
