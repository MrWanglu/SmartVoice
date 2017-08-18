package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.BatchManage;
import cn.fintecher.pangolin.entity.QBatchManage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * @author : xiaqun
 * @Description :
 * @Date : 16:57 2017/5/17
 */

public interface BatchManageRepository extends QueryDslPredicateExecutor<BatchManage>, JpaRepository<BatchManage, String>, QuerydslBinderCustomizer<QBatchManage> {
    @Override
    default void customize(final QuerydslBindings bindings, final QBatchManage root) {

    }

}
