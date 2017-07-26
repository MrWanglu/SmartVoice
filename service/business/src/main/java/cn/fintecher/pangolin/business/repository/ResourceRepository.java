package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.QResource;
import cn.fintecher.pangolin.entity.Resource;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * @Author: LvGuoRong
 * @Description:资源信息
 * @Date:2017/7/18
 */
public interface ResourceRepository extends QueryDslPredicateExecutor<Resource>, JpaRepository<Resource, String>, QuerydslBinderCustomizer<QResource> {
    @Override
    default void customize(final QuerydslBindings bindings, final QResource root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(value).concat("%")));
    }
}
