package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.OutsourceRecord;
import cn.fintecher.pangolin.entity.QOutsourceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-26-11:18
 */
public interface OutsourceRecordRepository extends QueryDslPredicateExecutor<OutsourceRecord>, JpaRepository<OutsourceRecord, String>, QuerydslBinderCustomizer<QOutsourceRecord> {
    @Override
    default void customize(final QuerydslBindings bindings, final QOutsourceRecord root) {

    }
}