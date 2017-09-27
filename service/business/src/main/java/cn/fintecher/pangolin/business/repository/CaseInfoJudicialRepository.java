package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by yuanyanting on 2017/9/27.
 */
public interface CaseInfoJudicialRepository extends QueryDslPredicateExecutor<CaseInfoJudicial>, JpaRepository<CaseInfoJudicial, String>, QuerydslBinderCustomizer<QCaseInfoJudicial> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseInfoJudicial root) {
        // 客户姓名
        bindings.bind(root.caseInfo.personalInfo.name).first((path, value) -> path.contains(StringUtils.trim(value)));
        // 客户手机号
        bindings.bind(root.caseInfo.personalInfo.mobileNo).first((path, value) -> path.eq(StringUtils.trim(value)));
        // 身份证号
        bindings.bind(root.caseInfo.personalInfo.idCard).first((path, value) -> path.eq(StringUtils.trim(value)));
        // 批次号
        bindings.bind(root.caseInfo.batchNumber).first((path, value) -> path.eq(StringUtils.trim(value)));
        //还款状态
        List<String> list = new ArrayList<>();
        list.add("M1");
        list.add("M2");
        list.add("M3");
        list.add("M4");
        list.add("M5");
        bindings.bind(root.caseInfo.payStatus).first((path, value) -> {
            if (Objects.equals(StringUtils.trim(value), CaseInfo.PayStatus.M6_PLUS.getRemark())) {
                return path.notIn(list);
            } else {
                return path.eq(value);
            }
        });
        // 逾期天数
        bindings.bind(root.caseInfo.overdueDays).all((path, value) -> {
            Iterator<? extends Integer> it = value.iterator();
            Integer firstOverdueDays = it.next();
            if (it.hasNext()) {
                Integer secondOverdueDays = it.next();
                return path.between(firstOverdueDays, secondOverdueDays);
            } else {
                return path.goe(firstOverdueDays);
            }
        });
        // 催收员
        bindings.bind(root.caseInfo.currentCollector.realName).first((path, value) -> path.contains(StringUtils.trim(value)));
        // 案件金额
        bindings.bind(root.caseInfo.overdueAmount).all((path, value) -> {
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
        // 案件手数
        bindings.bind(root.caseInfo.handNumber).all((path, value) -> {
            Iterator<? extends Integer> it = value.iterator();
            Integer firstHandNumber = it.next();
            if (it.hasNext()) {
                Integer secondHandNumber = it.next();
                return path.between(firstHandNumber, secondHandNumber);
            } else {
                return path.goe(firstHandNumber);
            }
        });
        // 佣金比例%
        bindings.bind(root.caseInfo.commissionRate).all((path, value) -> {
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
        // 案件状态
        bindings.bind(root.caseInfo.collectionStatus).first((path, value) -> path.eq(value));
        // 委托方
        bindings.bind(root.caseInfo.principalId.id).first((path, value) -> path.eq(StringUtils.trim(value)));
        // 是否协催
        bindings.bind(root.caseInfo.assistFlag).first((path, value) -> path.eq(value));
        //催收反馈
        bindings.bind(root.caseInfo.followupBack).first((path, value) -> path.eq(value));
        //协催方式
        bindings.bind(root.caseInfo.assistWay).first((path, value) -> path.eq(value));
        //催收类型
        bindings.bind(root.caseInfo.collectionType).first((path, value) -> path.eq(value));
        //机构码搜索
        bindings.bind(root.caseInfo.department.code).first((path, value) -> path.startsWith(StringUtils.trim(value)));
    }
}
