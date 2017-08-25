package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseAdvanceTurnApplay;
import cn.fintecher.pangolin.entity.QCaseAdvanceTurnApplay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;


/**
 * @Author : baizhangyu
 * @Description : 案件文件
 * @Date : 2017/8/15
 */
public interface CaseAdvanceTurnApplayRepository extends QueryDslPredicateExecutor<CaseAdvanceTurnApplay>, JpaRepository<CaseAdvanceTurnApplay, String>, QuerydslBinderCustomizer<QCaseAdvanceTurnApplay> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseAdvanceTurnApplay root) {

    }
}
