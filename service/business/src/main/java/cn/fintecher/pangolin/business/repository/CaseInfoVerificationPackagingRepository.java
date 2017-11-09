package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseInfoVerificationPackaging;
import cn.fintecher.pangolin.entity.QCaseInfoVerificationPackaging;
import cn.fintecher.pangolin.util.ZWDateUtil;
import com.querydsl.core.types.dsl.DateTimePath;
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
 * Created by yuanyanting on 2017/9/21.
 * 核销案件打包
 */
public interface CaseInfoVerificationPackagingRepository extends QueryDslPredicateExecutor<CaseInfoVerificationPackaging>, JpaRepository<CaseInfoVerificationPackaging, String>, QuerydslBinderCustomizer<QCaseInfoVerificationPackaging> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseInfoVerificationPackaging root) {
        //bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(StringUtils.trim(value)).concat("%")));
        // 打包时间
        bindings.bind(root.packagingTime).all((DateTimePath<Date> path, Collection<? extends Date> value) -> {
            Iterator<? extends Date> it = value.iterator();
            Date operatorMinTime = it.next();
            if (it.hasNext()) {
                String date = ZWDateUtil.fomratterDate(it.next(), "yyyy-MM-dd");
                date = date + " 23:59:59";
                Date operatorMaxTime = null;
                try {
                    operatorMaxTime = ZWDateUtil.getFormatDateTime(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return path.between(operatorMinTime, operatorMaxTime);
            } else {
                return path.goe(operatorMinTime);
            }
        });
        // 案件金额
        bindings.bind(root.totalAmount).all((path, value) -> {
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
