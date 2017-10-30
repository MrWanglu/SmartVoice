package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseDistributedTemporary;
import cn.fintecher.pangolin.entity.QCaseDistributedTemporary;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * @author : xiaqun
 * @Description :
 * @Date : 11:50 2017/10/17
 */

public interface CaseDistributedTemporaryRepository extends QueryDslPredicateExecutor<CaseDistributedTemporary>, JpaRepository<CaseDistributedTemporary, String>, QuerydslBinderCustomizer<QCaseDistributedTemporary> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseDistributedTemporary root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(StringUtils.trim(value)).concat("%")));
        bindings.bind(root.caseNumber).first((path, value) -> path.eq("%".concat(StringUtils.trim(value)).concat("%"))); //案件编号
        bindings.bind(root.batchNumber).first((path, value) -> path.eq("%".concat(StringUtils.trim(value)).concat("%"))); //批次号
        bindings.bind(root.type).first(SimpleExpression::eq); //分案类型
        bindings.bind(root.operatorTime).all((DateTimePath<Date> path, Collection<? extends Date> value) -> { //操作时间
            Iterator<? extends Date> it = value.iterator();
            Date operatorMinTime = it.next();
            if (it.hasNext()) {
                Date operatorMaxTime = it.next();
                return path.between(operatorMinTime, operatorMaxTime);
            } else {
                return path.goe(operatorMinTime);
            }
        });
        bindings.bind(root.overdueAmt).all((path, value) -> {
            Iterator<? extends BigDecimal> it = value.iterator();
            BigDecimal overdueMinAmt = it.next();
            if (it.hasNext()) {
                BigDecimal overdueMaxAmt = it.next();
                return path.between(overdueMinAmt, overdueMaxAmt);
            } else {
                return path.goe(overdueMinAmt);
            }
        });
    }

}
