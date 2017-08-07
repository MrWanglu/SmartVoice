package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.QUserLoginLog;
import cn.fintecher.pangolin.entity.UserLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/8/7.
 */
public interface UserLoginLogRepository extends QueryDslPredicateExecutor<UserLoginLog>, JpaRepository<UserLoginLog, String>, QuerydslBinderCustomizer<QUserLoginLog> {
    @Override
    default void customize(final QuerydslBindings bindings, final QUserLoginLog root) {

    }
}
