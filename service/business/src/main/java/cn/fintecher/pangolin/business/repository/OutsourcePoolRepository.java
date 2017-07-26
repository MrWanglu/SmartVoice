package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.OutsourcePool;
import cn.fintecher.pangolin.entity.QOutsourcePool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by  baizhangyu.
 * Description:
 * Date: 2017-07-26-11:18
 */
public interface OutsourcePoolRepository extends QueryDslPredicateExecutor<OutsourcePool>, JpaRepository<OutsourcePool, String>, QuerydslBinderCustomizer<QOutsourcePool> {
    @Override
    default void customize(final QuerydslBindings bindings, final QOutsourcePool root) {

    }
}