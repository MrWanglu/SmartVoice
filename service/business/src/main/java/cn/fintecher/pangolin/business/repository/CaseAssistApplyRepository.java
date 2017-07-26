package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseAssistApply;
import cn.fintecher.pangolin.entity.QCaseAssistApply;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;

/**
 * @author : xiaqun
 * @Description :
 * @Date : 10:50 2017/7/18
 */

public interface CaseAssistApplyRepository extends QueryDslPredicateExecutor<CaseAssistApply>, JpaRepository<CaseAssistApply, String>, QuerydslBinderCustomizer<QCaseAssistApply> {

    @Override
    default void customize(final QuerydslBindings bindings, final QCaseAssistApply root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(value).concat("%")));
//        bindings.bind(root.assistWay).first(SimpleExpression::eq); //协催方式
//        bindings.bind(root.approveStatus).first(SimpleExpression::eq); //协催审批状态
        bindings.bind(root.applyDate).all((path, value) -> { //申请时间
            Iterator<? extends Date> it = value.iterator();
            Date applyMinDate = it.next();
            if (it.hasNext()) {
                Date applyMaxDate = it.next();
                return path.between(applyMinDate, applyMaxDate);
            } else {
                return path.goe(applyMinDate);
            }
        });
        // 逾期总金额（案件金额）
        bindings.bind(root.overdueAmount).all((path, value) -> {
            Iterator<? extends BigDecimal> it = value.iterator();
            BigDecimal firstOverdueAmount = it.next();
            if (it.hasNext()) {
                BigDecimal secondOverDueAmount = it.next();
                return path.between(firstOverdueAmount, secondOverDueAmount);
            } else {
                //大于等于
                return path.goe(firstOverdueAmount);
            }
        });
    }
}
