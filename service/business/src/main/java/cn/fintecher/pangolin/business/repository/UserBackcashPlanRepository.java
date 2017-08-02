package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.QUserBackcashPlan;
import cn.fintecher.pangolin.entity.UserBackcashPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-02-10:24
 */
public interface UserBackcashPlanRepository extends QueryDslPredicateExecutor<UserBackcashPlan>, JpaRepository<UserBackcashPlan, String>, QuerydslBinderCustomizer<QUserBackcashPlan> {
    @Override
    default void customize(final QuerydslBindings bindings, final QUserBackcashPlan root) {

    }
}
