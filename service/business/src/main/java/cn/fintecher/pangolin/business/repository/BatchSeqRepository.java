package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.BatchSeq;
import cn.fintecher.pangolin.entity.QBatchSeq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-26-13:34
 */
public interface BatchSeqRepository extends QueryDslPredicateExecutor<BatchSeq>, JpaRepository<BatchSeq, String>, QuerydslBinderCustomizer<QBatchSeq> {
    @Override
    default void customize(final QuerydslBindings bindings, final QBatchSeq root) {

    }
    @Query(value = "SELECT next_seq(?1,?2)", nativeQuery = true)
    String getBatchSeq(String name, Integer length);
}
