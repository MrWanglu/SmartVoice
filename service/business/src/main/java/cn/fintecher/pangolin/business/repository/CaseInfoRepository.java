package cn.fintecher.pangolin.business.repository;


import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.QCaseInfo;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ChenChang on 2017/7/11.
 */
public interface CaseInfoRepository extends QueryDslPredicateExecutor<CaseInfo>, JpaRepository<CaseInfo, String>, QuerydslBinderCustomizer<QCaseInfo> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseInfo root) {

        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(value).concat("%")));
        bindings.bind(root.product.prodcutName).first((path, value) -> path.eq(value));
        //机构码搜索
        bindings.bind(root.department.code).first((path, value) -> path.startsWith(value));
        //公司码
        bindings.bind(root.companyCode).first((path, value) -> path.eq(value));
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
        bindings.bind(root.principalId.id).first((path, value) -> path.eq(value));
        //案件状态
        bindings.bind(root.collectionStatus).first((path, value) -> path.eq(value));
        //案件类型
        bindings.bind(root.caseType).first((path, value) -> path.eq(value));
        //催收类型
        bindings.bind(root.collectionType).first((path, value) -> path.eq(value));
        //产品系列
        bindings.bind(root.product.productSeries.id).first((path, value) -> path.eq(value));
        //客户姓名
        bindings.bind(root.personalInfo.name).first((path, value) -> path.eq(value));
        //客户手机号
        bindings.bind(root.personalInfo.mobileNo).first((path, value) -> path.eq(value));
        //批次号
        bindings.bind(root.batchNumber).first((path, value) -> path.eq(value));
        //申请省份
        bindings.bind(root.area.id).first((path, value) -> path.eq(value));
        //申请城市
        bindings.bind(root.area.areaName).first((path, value) -> path.eq(value));
    }

    /**
     * @Description 获得指定用户所持有的未结案案件总数
     */
    @Query(value = "select count(*) from case_info where current_collector = :userId and collection_status in (20,21,22,23,25)", nativeQuery = true)
    Integer getCaseCount(@Param("userId") String userId);

    /**
     * @Description 获得指定用户的待催收金额
     */
    @Query(value = "select sum(overdue_amount) from case_info where current_collector = :id or assist_collector = :id and collection_status = :collectionStatus", nativeQuery = true)
    BigDecimal getCollectionAmt(@Param("id") String id, @Param("collectionStatus") Integer collectionStatus);

    /**
     * 获取所有批次号
     * @param companyCode
     * @return
     */
    @Query(value = "select distinct(batch_number) from case_info where company_code = ?1 and batch_number is not null", nativeQuery = true)
    List<String> findDistinctByBatchNumber(@Param("companyCode") String companyCode);

    /**
     * 部门下案件总金额
     * @param deptCode
     * @return
     */
    @Query(value = "select sum(amo.overdue_amount) as case_sum " +
                    "from (select distinct(cinfo.case_number),overdue_amount " +
                        "from " +
                        " case_info cinfo " +
                        "left join department dept " +
                        " on cinfo.depart_id = dept.id " +
                        "where dept.`code` like concat(?1,'%')) amo ", nativeQuery = true)
    BigDecimal getCaseSumAmt (@Param("deptCode") String deptCode);

    /**
     * 部门下案件已还款总额
     * @param deptCode
     * @return
     */
    @Query(value = "select sum(cpd.payamt) as payamt from\n" +
            "(select caseamt.case_id,caseamt.payamt,cade.dcode from \n" +
            "(select case_id,sum(apply_pay_amt) as payamt from case_pay_apply where approve_status = '58' group by case_id) as caseamt \n" +
            "left join \n" +
            "(select case_info.id as cid,department.code as dcode from\n" +
            "case_info left join department on case_info.depart_id = department.id) as cade\n" +
            "on cade.cid = caseamt.case_id) as cpd where cpd.dcode like concat(?1,'%')", nativeQuery = true)
    BigDecimal getRepaySumAmt(@Param("deptCode") String deptCode);
}
