package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.QUser;
import cn.fintecher.pangolin.entity.User;
import com.querydsl.core.types.dsl.StringPath;
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
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(value).concat("%")));
    }

    User findByUserName(String username);
}
