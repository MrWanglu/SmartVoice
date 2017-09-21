package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.QSystemBackup;
import cn.fintecher.pangolin.entity.SystemBackup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-09-21-11:49
 */
public interface SystemBackupRepository extends QueryDslPredicateExecutor<SystemBackup>, JpaRepository<SystemBackup, String>, QuerydslBinderCustomizer<QSystemBackup> {
    @Override
    default void customize(final QuerydslBindings bindings, final QSystemBackup root) {
    }
}
