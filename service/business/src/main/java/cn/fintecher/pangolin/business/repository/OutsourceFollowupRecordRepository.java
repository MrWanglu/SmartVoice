package cn.fintecher.pangolin.business.repository;


import cn.fintecher.pangolin.entity.OutsourceFollowRecord;
import cn.fintecher.pangolin.entity.QOutsourceFollowRecord;
import com.querydsl.core.types.dsl.SimpleExpression;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import java.util.Date;
import java.util.Iterator;

public interface OutsourceFollowupRecordRepository extends QueryDslPredicateExecutor<OutsourceFollowRecord>,QuerydslBinderCustomizer<QOutsourceFollowRecord>, JpaRepository<OutsourceFollowRecord, String> {

    @Override
    default void customize(final QuerydslBindings bindings, final QOutsourceFollowRecord root) {

        bindings.bind(root.feedback).first(SimpleExpression::eq); //催收反馈
        bindings.bind(root.followType).first(SimpleExpression::eq); //跟进方式

        //跟进日期
        bindings.bind(root.followTime).all((path, value) -> {
            Iterator<? extends Date> it = value.iterator();
            Date firstDelegationDate = it.next();
            if (it.hasNext()) {
                Date secondDelegationDate = it.next();
                return path.between(firstDelegationDate, secondDelegationDate);
            } else {
                //大于等于
                return path.goe(firstDelegationDate);
            }
        });

    }
}
