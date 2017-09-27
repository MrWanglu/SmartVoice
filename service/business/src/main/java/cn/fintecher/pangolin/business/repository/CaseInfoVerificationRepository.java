package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.business.model.CaseInfoVerModel;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.CaseInfoVerification;
import cn.fintecher.pangolin.entity.QCaseInfoVerification;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by ChenChang on 2017/7/11.
 */
public interface CaseInfoVerificationRepository extends QueryDslPredicateExecutor<CaseInfoVerification>, JpaRepository<CaseInfoVerification, String>, QuerydslBinderCustomizer<QCaseInfoVerification> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseInfoVerification root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(StringUtils.trim(value)).concat("%")));
        bindings.bind(root.caseInfo.product.prodcutName).first((path, value) -> path.eq(StringUtils.trim(value)));
        //机构码搜索
        bindings.bind(root.caseInfo.department.code).first((path, value) -> path.startsWith(StringUtils.trim(value)));
        //公司码
        bindings.bind(root.caseInfo.companyCode).first((path, value) -> path.eq(StringUtils.trim(value)));
        //案件金额
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
        //逾期天数
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
        //案件手数
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
        //佣金比例%
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
        //委案日期
        bindings.bind(root.caseInfo.delegationDate).all((path, value) -> {
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
        bindings.bind(root.caseInfo.closeDate).all((path, value) -> {
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
        bindings.bind(root.caseInfo.principalId.id).first((path, value) -> path.eq(StringUtils.trim(value)));
        //案件状态
        bindings.bind(root.caseInfo.collectionStatus).first((path, value) -> path.eq(value));
        //案件类型
        bindings.bind(root.caseInfo.caseType).first((path, value) -> path.eq(value));
        //催收类型
        bindings.bind(root.caseInfo.collectionType).first((path, value) -> path.eq(value));
        //产品系列
        //bindings.bind(root.caseInfo.product.productSeries.id).first((path, value) -> path.eq(StringUtils.trim(value)));
        //客户姓名
        bindings.bind(root.caseInfo.personalInfo.name).first((path, value) -> path.contains(StringUtils.trim(value)));
        //客户手机号
        bindings.bind(root.caseInfo.personalInfo.mobileNo).first((path, value) -> path.eq(StringUtils.trim(value)).or(root.caseInfo.personalInfo.personalContacts.any().phone.eq(StringUtils.trim(value))));
        //批次号
        bindings.bind(root.caseInfo.batchNumber).first((path, value) -> path.eq(StringUtils.trim(value)));
        //申请省份
        //bindings.bind(root.caseInfo.area.parent.id).first((path, value) -> path.eq(value));
        //申请城市
        bindings.bind(root.caseInfo.area.id).first((path, value) -> path.eq(value));
        //标记颜色
        bindings.bind(root.caseInfo.caseMark).first((path, value) -> path.eq(value));
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
        //催收反馈
        bindings.bind(root.caseInfo.followupBack).first((path, value) -> path.eq(value));
        //协催
        bindings.bind(root.caseInfo.assistFlag).first((path, value) -> path.eq(value));
        //协催方式
        bindings.bind(root.caseInfo.assistWay).first((path, value) -> path.eq(value));
        // 催收员
        bindings.bind(root.caseInfo.currentCollector.realName).first((path, value) -> path.contains(StringUtils.trim(value)));
        //根据id数组获取查询结果list
        bindings.bind(root.id).all((path, value) -> {
            Set<String> idSets = new HashSet<>();
            if(value.iterator().hasNext()){
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
        bindings.bind(root.caseInfo.collectionType).first((path, value) -> path.eq(value));
        bindings.bind(root.caseInfo.department.code).first((path, value) -> path.startsWith(value));
    }

    /**
     * 查询核销案件数
     *
     * @param
     * @return
     */
    @Query(value = "select count(*) from case_info_verification", nativeQuery = true)
    Integer getTotalCount();

    /**
     * 查询核销管理报表
     *
     * @param
     * @return
     */
    @Query(value = "SELECT c.area_name,sum(b.overdue_amount),count(a.id) from (case_info_verification a LEFT JOIN case_info b on a.case_id = b.id)LEFT JOIN " +
            "area_code c ON b.area_id = c.id where company_code = :companyCode and b.case_follow_inTime >= :startTime and b.case_follow_inTime <= :endTime" +
            " GROUP BY c.area_name", nativeQuery = true)
   List<CaseInfoVerModel> findCaseInfoVerificationReport(@Param("startTime") Date startTime,
                                                         @Param("endTime") Date endTime,
                                                         @Param("companyCode") String companyCode);
    /**
     * 导出核销管理
     *
     * @param ids
     * @param companyCode
     * @return
     */
    @Query(value = "SELECT b.case_number,c.`name`,c.mobile_no,c.id_card,b.batch_number,b.overdue_days,b.overdue_amount,d.`name` AS pname,e.real_name,b.collection_status FROM (SELECT id,case_id FROM case_info_verification WHERE id in (:ids) AND company_code = :companyCode) AS a LEFT JOIN case_info b ON a.case_id = b.id LEFT JOIN personal c ON b.personal_id = c.id LEFT JOIN principal d ON b.principal_id = d.id LEFT JOIN `user` e ON b.current_collector = e.id ", nativeQuery = true)
    List<Object[]> findCaseInfoVer(@Param("ids") List<String> ids, @Param("companyCode") String companyCode);
}
