package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.QUser;
import cn.fintecher.pangolin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 10:45 2017/7/17
 */
public interface UserRepository extends QueryDslPredicateExecutor<User>, JpaRepository<User, String>, QuerydslBinderCustomizer<QUser> {
    @Override
    default void customize(final QuerydslBindings bindings, final QUser root) {

    }

    User findByUserName(String username);
}
