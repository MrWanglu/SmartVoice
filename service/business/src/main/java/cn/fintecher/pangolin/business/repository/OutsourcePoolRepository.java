package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.OutsourcePool;
import cn.fintecher.pangolin.entity.QOutsourcePool;
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
 * Created by  baizhangyu.
 * Description:
 * Date: 2017-07-26-11:18
 */
public interface OutsourcePoolRepository extends QueryDslPredicateExecutor<OutsourcePool>, JpaRepository<OutsourcePool, String>, QuerydslBinderCustomizer<QOutsourcePool> {
    @Override
    default void customize(final QuerydslBindings bindings, final QOutsourcePool root) {
        /*
        * Added by huyanmin at 2017/09/20
        * */
        //客户姓名模糊查询
        bindings.bind(root.caseInfo.personalInfo.name).first((path, value) -> path.like("%".concat(StringUtils.trim(value)).concat("%")));
        //客户手机号
        bindings.bind(root.caseInfo.personalInfo.mobileNo).first((path, value) -> path.eq(StringUtils.trim(value)).or(root.caseInfo.personalInfo.personalContacts.any().phone.eq(StringUtils.trim(value))));
        //客户身份证号
        bindings.bind(root.caseInfo.personalInfo.idCard).first((path, value) -> path.contains(StringUtils.trim(value)));
        //批次号
        bindings.bind(root.caseInfo.batchNumber).first((path, value) -> path.eq(StringUtils.trim(value)));
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
        //案件金额
        bindings.bind(root.contractAmt).all((path, value) -> {
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
        //案件类型
        bindings.bind(root.caseInfo.caseType).first((path, value) -> path.eq(value));
        //委外方
        bindings.bind(root.outsource.id).first((path, value) -> path.eq(StringUtils.trim(value)));

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
    }

    /**
     * 获取特定委外方下的特定案件的个数（共债案件）
     *
     * @param name,idCard,outIds
     * @return
     */
    @Query(value = "select a.out_id,COUNT(*) from outsource_pool a " +
            "LEFT JOIN case_info b on a.case_id=b.id " +
            "LEFT JOIN personal c on b.personal_id=c.id " +
            "where c.`name`=:personalName " +
            "and c.id_card=:idCard " +
            "and a.company_code=:companyCode " +
            "and a.out_status=168 " +
            "GROUP BY a.out_id", nativeQuery = true)
    Object getGzNum(@Param("personalName") String personalName, @Param("idCard") String idCard, @Param("companyCode") String companyCode);


    /**
     * 获取分配时的委外信息
     *
     * @param
     * @return
     */
    @Query(value = "select b.outs_code,b.outs_name,b.id,IFNULL(c.case_count,0),IFNULL(c.end_amt,0.00),IFNULL(c.end_count,0),IFNULL(c.success_rate,0.00) from outsource b left JOIN (select a.out_id, count(case when a.out_status<>167 then a.id end) as case_count,count( case when  a.out_status=170 then a.id end) as end_count,(count( case when  a.out_status=170 then a.id end)/count(case when a.out_status <>167 then a.id end)) as success_rate,sum(case when a.out_status = 170 then a.contract_amt end) as end_amt from outsource_pool a group by a.out_id ) c on b.id =c.out_id where b.company_code=:companyCode", nativeQuery = true)
    Object[] getAllOutSourceByCase(@Param("companyCode") String companyCode);

    @Query(value = "SELECT count(*) FROM `outsource_pool` where out_status=168 and out_id= :outsourceId", nativeQuery = true)
    Integer getOutsourceCaseCount(@Param("outsourceId") String outsouceId);

    @Query(value = "SELECT ifnull(sum(contract_amt),0) FROM `outsource_pool` where out_status=168 and out_id= :outsourceId", nativeQuery = true)
    BigDecimal getOutsourceAmtCount(@Param("outsourceId") String outsouceId);

}