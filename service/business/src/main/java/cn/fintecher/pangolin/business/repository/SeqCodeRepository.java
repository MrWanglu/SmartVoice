package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.QSeqCode;
import cn.fintecher.pangolin.entity.SeqCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 17:29 2017/7/18
 */
public interface SeqCodeRepository extends QueryDslPredicateExecutor<SeqCode>, JpaRepository<SeqCode, String>, QuerydslBinderCustomizer<QSeqCode> {
    @Override
    default void customize(final QuerydslBindings bindings, final QSeqCode root) {

    }

    @Query(value = "SELECT next_seq(?1,?2)", nativeQuery = true)
    String getBatchSeq(String name, Integer length);

}
