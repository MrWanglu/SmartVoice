package cn.fintecher.pangolin.business.repository;


import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.CaseInfoHistory;
import cn.fintecher.pangolin.entity.QCaseInfoHistory;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by ChenChang on 2017/7/11.
 */
public interface CaseInfoHistoryRepository extends QueryDslPredicateExecutor<CaseInfoHistory>, JpaRepository<CaseInfoHistory, String>, QuerydslBinderCustomizer<QCaseInfoHistory> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseInfoHistory root) {

        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(StringUtils.trim(value)).concat("%")));
        bindings.bind(root.product.prodcutName).first((path, value) -> path.eq(StringUtils.trim(value)));
        //机构码搜索
        bindings.bind(root.department.code).first((path, value) -> path.startsWith(StringUtils.trim(value)));
        //公司码
        bindings.bind(root.companyCode).first((path, value) -> path.eq(StringUtils.trim(value)));
        //案件金额
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
        //逾期天数
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
        //案件手数
        bindings.bind(root.handNumber).all((path, value) -> {
            Iterator<? extends Integer> it = value.iterator();
            Integer firstHandNumber = it.next();
            if (it.hasNext()) {
                Integer secondHandNumber = it.next();
                return path.between(firstHandNumber, secondHandNumber);
            } else {
                return path.goe(firstHandNumber);
            }
        });
        //佣金比例%
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
        //委案日期
        bindings.bind(root.delegationDate).all((path, value) -> {
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
        //结案日期
        bindings.bind(root.closeDate).all((path, value) -> {
            Iterator<? extends Date> it = value.iterator();
            Date firstCloseDate = it.next();
            if (it.hasNext()) {
                Date secondCloseDate = it.next();
                return path.between(firstCloseDate, secondCloseDate);
            } else {
                //大于等于
                return path.goe(firstCloseDate);
            }
        });
        //委托方
        bindings.bind(root.principalId.id).first((path, value) -> path.eq(StringUtils.trim(value)));
        //案件状态
        bindings.bind(root.collectionStatus).first((path, value) -> path.eq(value));
        //案件类型
        bindings.bind(root.caseType).first((path, value) -> path.eq(value));
        //催收类型
        bindings.bind(root.collectionType).first((path, value) -> path.eq(value));
        //产品系列
        bindings.bind(root.product.productSeries.id).first((path, value) -> path.eq(StringUtils.trim(value)));
        //客户姓名
        bindings.bind(root.personalInfo.name).first((path, value) -> path.contains(StringUtils.trim(value)));
        //客户手机号
        bindings.bind(root.personalInfo.mobileNo).first((path, value) -> path.eq(StringUtils.trim(value)));
        //批次号
        bindings.bind(root.batchNumber).first((path, value) -> path.eq(StringUtils.trim(value)));
        //申请省份
        bindings.bind(root.area.parent.id).first((path, value) -> path.eq(value));
        //申请城市
        bindings.bind(root.area.id).first((path, value) -> path.eq(value));
        //标记颜色
        bindings.bind(root.caseMark).first((path, value) -> path.eq(value));
        //还款状态
        List<String> list = new ArrayList<>();
        list.add("M1");
        list.add("M2");
        list.add("M3");
        list.add("M4");
        list.add("M5");
        bindings.bind(root.payStatus).first((path, value) -> {
            if (Objects.equals(StringUtils.trim(value), CaseInfo.PayStatus.M6_PLUS.getRemark())) {
                return path.notIn(list);
            } else {
                return path.eq(value);
            }
        });
        //催收反馈
        bindings.bind(root.followupBack).first((path, value) -> path.eq(value));
    }
}
