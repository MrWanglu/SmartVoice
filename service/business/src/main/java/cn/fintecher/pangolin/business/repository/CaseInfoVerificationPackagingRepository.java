package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseInfoVerificationPackaging;
import cn.fintecher.pangolin.entity.QCaseInfoVerificationPackaging;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by yuanyanting on 2017/9/21.
 * 核销案件打包
 */
public interface CaseInfoVerificationPackagingRepository extends QueryDslPredicateExecutor<CaseInfoVerificationPackaging>, JpaRepository<CaseInfoVerificationPackaging, String>, QuerydslBinderCustomizer<QCaseInfoVerificationPackaging> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseInfoVerificationPackaging root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(StringUtils.trim(value)).concat("%")));
        // 打包时间
        bindings.bind(root.caseInfoVerificationPackaging.packagingTime).all((path, value) -> {
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
        // 案件金额
        bindings.bind(root.caseInfoVerificationPackaging.totalAmount).all((path, value) -> {
            Iterator<? extends BigDecimal> it = value.iterator();
            BigDecimal firstOverdueAmount = it.next();
            if (it.hasNext()) {
                BigDecimal secondOverDueAmont = it.next();
                return path.between(firstOverdueAmount, secondOverDueAmont);
            } else {
                //大于等于
                return path.goe(firstOverdueAmount);
            }
        });
    }
}
