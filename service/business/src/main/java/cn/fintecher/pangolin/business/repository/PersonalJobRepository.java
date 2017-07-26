package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.PersonalJob;
import cn.fintecher.pangolin.entity.QPersonalJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * @author : xiaqun
 * @Description :
 * @Date : 11:03 2017/7/26
 */

public interface PersonalJobRepository extends QueryDslPredicateExecutor<PersonalJob>, JpaRepository<PersonalJob, String>, QuerydslBinderCustomizer<QPersonalJob> {
    @Override
    default void customize(final QuerydslBindings bindings, final QPersonalJob root) {

    }

    /**
     * @Dexcription 通过客户信息ID获取客户工作单位信息
     */
    PersonalJob findByPersonalId(String personalId);
}
