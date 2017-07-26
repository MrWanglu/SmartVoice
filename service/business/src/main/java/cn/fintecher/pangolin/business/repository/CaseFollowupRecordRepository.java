package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseFollowupRecord;
import cn.fintecher.pangolin.entity.QCaseFollowupRecord;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;

import java.util.Date;
import java.util.Iterator;

/**
 * @author : xiaqun
 * @Description :
 * @Date : 16:15 2017/7/19
 */

public interface CaseFollowupRecordRepository extends QueryDslPredicateExecutor<CaseFollowupRecord>, JpaRepository<CaseFollowupRecord, String>, QuerydslBinderCustomizer<QCaseFollowupRecord> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseFollowupRecord root) {
        bindings.bind(String.class).first((SingleValueBinding<StringPath, String>) StringExpression::like);
        bindings.bind(root.collectionFeedback).first(SimpleExpression::eq); //催收反馈
        bindings.bind(root.type).first(SimpleExpression::eq); //跟进方式
        bindings.bind(root.source).first(SimpleExpression::eq); //跟进来源
        bindings.bind(root.operatorTime).all((path, value) -> { //跟进时间
            Iterator<? extends Date> it = value.iterator();
            Date operatorMinTime = it.next();
            if (it.hasNext()) {
                Date operatorMaxTime = it.next();
                return path.between(operatorMinTime, operatorMaxTime);
            } else {
                return path.goe(operatorMinTime);
            }
        });
    }

    @Query(value = "select operator as realName, count(distinct(case_id)) as totalFllowupCase from case_followup_record", nativeQuery = true)
    String getFlowupCaseRank();
}
