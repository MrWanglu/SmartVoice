package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseTurnRecord;
import cn.fintecher.pangolin.entity.QCaseTurnRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * @author : xiaqun
 * @Description :
 * @Date : 16:40 2017/7/18
 */

public interface CaseTurnRecordRepository extends QueryDslPredicateExecutor<CaseTurnRecord>, JpaRepository<CaseTurnRecord, String>, QuerydslBinderCustomizer<QCaseTurnRecord> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseTurnRecord root) {

    }
}
