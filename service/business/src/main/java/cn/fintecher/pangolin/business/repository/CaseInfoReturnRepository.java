package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.CaseInfoReturn;
import cn.fintecher.pangolin.entity.QCaseInfoReturn;
import cn.fintecher.pangolin.util.ZWDateUtil;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

/**
 * @author : xiaqun
 * @Description :
 * @Date : 17:23 2017/9/19
 */

public interface CaseInfoReturnRepository extends QueryDslPredicateExecutor<CaseInfoReturn>, JpaRepository<CaseInfoReturn, String>, QuerydslBinderCustomizer<QCaseInfoReturn> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseInfoReturn root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(StringUtils.trim(value)).concat("%")));
        bindings.bind(root.caseId.product.prodcutName).first((path, value) -> path.eq(StringUtils.trim(value)));
        //机构码搜索
        bindings.bind(root.caseId.department.code).first((path, value) -> path.startsWith(StringUtils.trim(value)));
        //公司码
        bindings.bind(root.caseId.companyCode).first((path, value) -> path.eq(StringUtils.trim(value)));
        //案件金额
        bindings.bind(root.caseId.overdueAmount).all((path, value) -> {
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
        bindings.bind(root.caseId.overdueDays).all((path, value) -> {
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
        bindings.bind(root.caseId.handNumber).all((path, value) -> {
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
        bindings.bind(root.caseId.commissionRate).all((path, value) -> {
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
        bindings.bind(root.caseId.delegationDate).all((path, value) -> {
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
        bindings.bind(root.caseId.closeDate).all((path, value) -> {
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
        bindings.bind(root.operatorTime).all((DateTimePath<Date> path, Collection<? extends Date> value) -> { //跟进时间
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
        //委托方
        bindings.bind(root.caseId.principalId.id).first((path, value) -> path.eq(StringUtils.trim(value)));
        //案件状态
        bindings.bind(root.caseId.collectionStatus).first((path, value) -> path.eq(value));
        //案件类型
        bindings.bind(root.caseId.caseType).first((path, value) -> path.eq(value));
        //催收类型
        bindings.bind(root.caseId.collectionType).first((path, value) -> path.eq(value));
        //产品系列
        //bindings.bind(root.caseInfo.product.productSeries.id).first((path, value) -> path.eq(StringUtils.trim(value)));
        //客户姓名
        bindings.bind(root.caseId.personalInfo.name).first((path, value) -> path.contains(StringUtils.trim(value)));
        //客户手机号
        bindings.bind(root.caseId.personalInfo.mobileNo).first((path, value) -> path.eq(StringUtils.trim(value))
                .or(root.caseId.personalInfo.personalContacts.any().phone.eq(StringUtils.trim(value))));
        //批次号
        bindings.bind(root.caseId.batchNumber).first((path, value) -> path.eq(StringUtils.trim(value)));
        //申请省份
        //bindings.bind(root.caseInfo.area.parent.id).first((path, value) -> path.eq(value));
        //申请城市
        bindings.bind(root.caseId.area.id).first((path, value) -> path.eq(value));
        //标记颜色
        bindings.bind(root.caseId.caseMark).first((path, value) -> path.eq(value));
        //还款状态
        List<String> list = new ArrayList<>();
        list.add("M0");
        list.add("M1");
        list.add("M2");
        list.add("M3");
        list.add("M4");
        list.add("M5");
        bindings.bind(root.caseId.payStatus).first((path, value) -> {
            if (Objects.equals(StringUtils.trim(value), CaseInfo.PayStatus.M6_PLUS.getRemark())) {
                return path.notIn(list);
            } else {
                return path.eq(value);
            }
        });
        //催收反馈
        bindings.bind(root.caseId.followupBack).first((path, value) -> path.eq(value));
        //协催
        bindings.bind(root.caseId.assistFlag).first((path, value) -> path.eq(value));
        //协催方式
        bindings.bind(root.caseId.assistWay).first((path, value) -> path.eq(value));
        //根据id数组获取查询结果list
        bindings.bind(root.id).all((path, value) -> {
            Set<String> idSets = new HashSet<>();
            if (value.iterator().hasNext()) {
                StringBuilder sb = new StringBuilder(value.iterator().next());
                sb.deleteCharAt(0);
                sb.deleteCharAt(sb.length() - 1);
                List<String> idArray = Arrays.asList(sb.toString().split(","));
                Iterator<? extends String> it = idArray.iterator();
                while (it.hasNext()) {
                    idSets.add(it.next().trim());
                }
            }
            return path.in(idSets);
        });
        bindings.bind(root.caseId.collectionType).first((path, value) -> path.eq(value));
        bindings.bind(root.outBatch).first((path, value) -> path.eq(value));
        bindings.bind(root.outsName).first((path, value) -> path.eq(value));
    }
}
