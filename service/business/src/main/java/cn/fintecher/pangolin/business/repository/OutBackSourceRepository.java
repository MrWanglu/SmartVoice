package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.OutBackSource;
import cn.fintecher.pangolin.entity.QOutBackSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by huyanmin
 * Description:
 * Date: 2017-08-31
 *
 */

public interface OutBackSourceRepository extends QueryDslPredicateExecutor<OutBackSource>, JpaRepository<OutBackSource, String>, QuerydslBinderCustomizer<QOutBackSource> {

        @Override
        default void customize(final QuerydslBindings bindings, final QOutBackSource root) {

        }
}