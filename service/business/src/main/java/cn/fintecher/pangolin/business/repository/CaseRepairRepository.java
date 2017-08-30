package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.CaseRepair;
import cn.fintecher.pangolin.entity.QCaseRepair;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @Author: PeiShouWen
 * @Description: 案件修复
 * @Date 19:45 2017/8/7
 */
public interface CaseRepairRepository extends QueryDslPredicateExecutor<CaseRepair>, JpaRepository<CaseRepair, String>, QuerydslBinderCustomizer<QCaseRepair> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseRepair root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(StringUtils.trim(value)).concat("%")));
        // 客户姓名
        bindings.bind(root.caseId.personalInfo.name).first((path, value) -> path.contains(StringUtils.trim(value)));
        // 还款状态
        bindings.bind(root.caseId.payStatus).first((path, value) -> {
            List<String> list = new ArrayList<>();
            list.add("M1");
            list.add("M2");
            list.add("M3");
            list.add("M4");
            list.add("M5");
            if (Objects.equals(StringUtils.trim(value), CaseInfo.PayStatus.M6_PLUS.getRemark())) {
                return path.notIn(list);
            } else {
                return path.eq(value);
            }
        });
        // 案件金额
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
        // 状态
        bindings.bind(root.repairStatus).first((path, value) -> path.eq(value));
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
        //委托方
        bindings.bind(root.caseId.principalId.id).first((path, value) -> path.eq(StringUtils.trim(value)));
    }
}
