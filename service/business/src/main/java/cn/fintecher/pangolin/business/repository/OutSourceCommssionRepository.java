package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.OutSourceCommssion;
import cn.fintecher.pangolin.entity.QOutSourceCommssion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-15-11:58
 */
public interface OutSourceCommssionRepository extends QueryDslPredicateExecutor<OutSourceCommssion>, JpaRepository<OutSourceCommssion, String>, QuerydslBinderCustomizer<QOutSourceCommssion> {
    @Override
    default void customize(final QuerydslBindings bindings, final QOutSourceCommssion root) {

    }
}
