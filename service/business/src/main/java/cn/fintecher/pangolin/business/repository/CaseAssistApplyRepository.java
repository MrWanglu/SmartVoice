package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseAssistApply;
import cn.fintecher.pangolin.entity.QCaseAssistApply;
import cn.fintecher.pangolin.util.ZWDateUtil;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Collection;
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
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(StringUtils.trim(value)).concat("%")));
//        bindings.bind(root.assistWay).first(SimpleExpression::eq); //协催方式
//        bindings.bind(root.approveStatus).first(SimpleExpression::eq); //协催审批状态
        bindings.bind(root.applyDate).all((DateTimePath<Date> path, Collection<? extends Date> value) -> { //申请时间
            Iterator<? extends Date> it = value.iterator();
            Date applyMinDate = it.next();
            if (it.hasNext()) {
                String date = ZWDateUtil.fomratterDate(it.next(), "yyyy-MM-dd");
                date = date + " 23:59:59";
                Date applyMaxDate = null;
                try {
                    applyMaxDate = ZWDateUtil.getFormatDate(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
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
