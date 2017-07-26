package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.PersonalCar;
import cn.fintecher.pangolin.entity.QPersonalCar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * @author : xiaqun
 * @Description :
 * @Date : 10:56 2017/7/26
 */

public interface PersonalCarRepository extends QueryDslPredicateExecutor<PersonalCar>, JpaRepository<PersonalCar, String>, QuerydslBinderCustomizer<QPersonalCar> {
    @Override
    default void customize(final QuerydslBindings bindings, final QPersonalCar root) {

    }
}
