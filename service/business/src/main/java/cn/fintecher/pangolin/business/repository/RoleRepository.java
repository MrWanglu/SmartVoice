package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.QRole;
import cn.fintecher.pangolin.entity.Role;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 14:46 2017/7/14
 */
public interface RoleRepository extends QueryDslPredicateExecutor<Role>, JpaRepository<Role, String>, QuerydslBinderCustomizer<QRole> {

    @Override
    default void customize(final QuerydslBindings bindings, final QRole root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(value).concat("%")));
        bindings.bind(root.name).first((path, value) -> path.like(value));
        bindings.bind(root.status).first((path, value) -> path.eq(value));
    }
}