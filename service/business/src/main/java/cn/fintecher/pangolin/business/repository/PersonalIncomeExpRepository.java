package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.PersonalIncomeExp;
import cn.fintecher.pangolin.entity.QPersonalIncomeExp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * @author : xiaqun
 * @Description :
 * @Date : 17:24 2017/7/26
 */

public interface PersonalIncomeExpRepository extends QueryDslPredicateExecutor<PersonalIncomeExp>, JpaRepository<PersonalIncomeExp, String>, QuerydslBinderCustomizer<QPersonalIncomeExp> {
    @Override
    default void customize(final QuerydslBindings bindings, final QPersonalIncomeExp root) {

    }

    /**
     * @Description 通过客户信息ID获取客户收支信息
     */
    PersonalIncomeExp findByPersonalId(String personalId);
}
