package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by yuanyanting on 2017/9/26.
 */
public interface CaseInfoJudicialApplyRepository extends QueryDslPredicateExecutor<CaseInfoJudicialApply>, JpaRepository<CaseInfoJudicialApply, String>, QuerydslBinderCustomizer<QCaseInfoJudicialApply> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseInfoJudicialApply root) {
      /*  // 客户姓名
        bindings.bind(root.personalName).first((path, value) -> path.contains(StringUtils.trim(value)));
        // 客户手机号
        bindings.bind(root.mobileNo).first((path, value) -> path.eq(StringUtils.trim(value)));
        // 身份证号
        bindings.bind(root.IdCard).first((path, value) -> path.eq(StringUtils.trim(value)));
        // 批次号
        bindings.bind(root.batchNumber).first((path, value) -> path.eq(StringUtils.trim(value)));
        // 案件金额
        bindings.bind(root.overdueAmount).all((path, value) -> {
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
        // 逾期天数
        bindings.bind(root.overdueDays).all((path, value) -> {
            Iterator<? extends Integer> it = value.iterator();
            Integer firstOverdueDays = it.next();
            if (it.hasNext()) {
                Integer secondOverdueDays = it.next();
                return path.between(firstOverdueDays, secondOverdueDays);
            } else {
                return path.goe(firstOverdueDays);
            }
        });
        // 佣金比例%
        bindings.bind(root.commissionRate).all((path, value) -> {
            Iterator<? extends BigDecimal> it = value.iterator();
            BigDecimal firstCommissionRate = it.next();
            if (it.hasNext()) {
                BigDecimal secondCommissionRate = it.next();
                return path.between(firstCommissionRate, secondCommissionRate);
            } else {
                //大于等于
                return path.goe(firstCommissionRate);
            }
        });
        // 申请省份
        bindings.bind(root.province).first((path, value) -> path.eq(value));
        // 申请城市
        bindings.bind(root.city).first((path, value) -> path.eq(value));
        // 申请日期
        bindings.bind(root.applicationDate).all((path, value) -> {
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
        // 委托方
        bindings.bind(root.principalName).first((path, value) -> path.eq(StringUtils.trim(value)));
        // 审批状态
        bindings.bind(root.approvalStatus).first((path, value) -> path.eq(value));*/
    }
}
