package cn.fintecher.server.repository;

import cn.fintecher.entity.AuthUser;
import cn.fintecher.entity.QAuthUser;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;


/**
 * Created by qijigui on 2018-05-10.
 */
public interface AuthUserRepository extends QueryDslPredicateExecutor<AuthUser>, JpaRepository<AuthUser, String>, QuerydslBinderCustomizer<QAuthUser> {

    @Override
    default void customize(final QuerydslBindings bindings, final QAuthUser root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(value).concat("%")));
    }

}
