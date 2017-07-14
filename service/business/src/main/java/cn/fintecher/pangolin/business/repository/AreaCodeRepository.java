package cn.fintecher.pangolin.business.repository;


import cn.fintecher.pangolin.entity.AreaCode;
import cn.fintecher.pangolin.entity.QAreaCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by ChenChang on 2017/7/11.
 */
public interface AreaCodeRepository extends QueryDslPredicateExecutor<AreaCode>, JpaRepository<AreaCode, Integer>, QuerydslBinderCustomizer<QAreaCode> {
    @Override
    default void customize(final QuerydslBindings bindings, final QAreaCode root) {

    }
}
