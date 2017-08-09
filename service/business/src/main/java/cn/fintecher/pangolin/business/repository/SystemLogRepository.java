package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.QSystemLog;
import cn.fintecher.pangolin.entity.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-09-16:39
 */
public interface SystemLogRepository extends QueryDslPredicateExecutor<SystemLog>, JpaRepository<SystemLog, String>, QuerydslBinderCustomizer<QSystemLog> {
    @Override
    default void customize(final QuerydslBindings bindings, final QSystemLog root) {
    }
}
