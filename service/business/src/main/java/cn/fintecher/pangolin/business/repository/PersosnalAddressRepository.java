package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.PersonalAddress;
import cn.fintecher.pangolin.entity.QPersonalAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by Administrator on 2017/7/21.
 */
public interface PersosnalAddressRepository extends QueryDslPredicateExecutor<PersonalAddress>, JpaRepository<PersonalAddress, String>, QuerydslBinderCustomizer<QPersonalAddress> {
    @Override
    default void customize(final QuerydslBindings bindings, final QPersonalAddress root) {
    }
}
