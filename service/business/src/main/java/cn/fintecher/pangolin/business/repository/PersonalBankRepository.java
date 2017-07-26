package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.PersonalBank;
import cn.fintecher.pangolin.entity.QPersonalBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * @author : xiaqun
 * @Description :
 * @Date : 10:28 2017/7/26
 */

public interface PersonalBankRepository extends QueryDslPredicateExecutor<PersonalBank>, JpaRepository<PersonalBank, String>, QuerydslBinderCustomizer<QPersonalBank> {
    @Override
    default void customize(final QuerydslBindings bindings, final QPersonalBank root) {

    }
}
