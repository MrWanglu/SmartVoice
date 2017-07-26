package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.DataDict;
import cn.fintecher.pangolin.entity.QDataDict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-09-10:07
 */
public interface DataDictRepository extends QueryDslPredicateExecutor<DataDict>, JpaRepository<DataDict, String>, QuerydslBinderCustomizer<QDataDict> {
    @Override
    default void customize(final QuerydslBindings bindings, final QDataDict root) {

    }
}
