package cn.fintecher.pangolin.business.repository;


import cn.fintecher.pangolin.entity.Personal;
import cn.fintecher.pangolin.entity.QPersonal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by ChenChang on 2017/7/11.
 */
public interface PersonalRepository extends QueryDslPredicateExecutor<Personal>, JpaRepository<Personal, String>, QuerydslBinderCustomizer<QPersonal> {
    @Override
    default void customize(final QuerydslBindings bindings, final QPersonal root) {
    }
}