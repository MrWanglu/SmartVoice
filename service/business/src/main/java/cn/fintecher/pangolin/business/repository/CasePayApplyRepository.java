package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CasePayApply;
import cn.fintecher.pangolin.entity.QCasePayApply;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author : xiaqun
 * @Description :
 * @Date : 9:13 2017/7/19
 */

public interface CasePayApplyRepository extends QueryDslPredicateExecutor<CasePayApply>, JpaRepository<CasePayApply, String>, QuerydslBinderCustomizer<QCasePayApply> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCasePayApply root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(value).concat("%")));
        bindings.bind(root.personalPhone).first(SimpleExpression::eq); //手机号
        bindings.bind(root.principalId).first((SimpleExpression::eq)); //委托方
        bindings.bind(root.payType).first(SimpleExpression::eq); //还款类型
        bindings.bind(root.payWay).first(SimpleExpression::eq); //还款方式
        bindings.bind(root.approveStatus).first(SimpleExpression::eq); //还款审批状态
        bindings.bind(root.applayDate).all((path, value) -> { //申请时间
            Iterator<? extends Date> it = value.iterator();
            Date applayMinDate = it.next();
            if (it.hasNext()) {
                Date applayMaxDate = it.next();
                return path.between(applayMinDate, applayMaxDate);
            } else {
                return path.goe(applayMinDate);
            }
        });
        bindings.bind(root.approveResult).first(SimpleExpression::eq); //审核结果
        bindings.bind(root.personalName).first(SimpleExpression::eq);//客户姓名
        bindings.bind(root.batchNumber).first(SimpleExpression::eq);//案件批次号
        bindings.bind(root.applyDerateAmt).all((path, value) -> { //减免金额
            Iterator<? extends BigDecimal> it = value.iterator();
            BigDecimal applyDerateMinAmt = it.next();
            if (it.hasNext()) {
                BigDecimal applyDerateMaxAmt = it.next();
                return path.between(applyDerateMinAmt, applyDerateMaxAmt);
            } else {
                return path.goe(applyDerateMinAmt);
            }
        });
        bindings.bind(root.approveType).first(SimpleExpression::eq);//减免类型
        bindings.bind(root.approveCostresult).first(SimpleExpression::eq);//减免审批状态
        bindings.bind(root.applayUserName).first(SimpleExpression::eq);//申请人

    }

    /**
     @Description 获得指定用户的待审核回款金额
     */
    @Query(value = "select sum(applyPayAmt) from CasePayApply where applayUserName = :username and  approveStatus=:approveStatu")
    public BigDecimal queryApplyAmtByUserName(@Param("username") String username, @Param("approveStatu") Integer approveStatu);

    /**
     @Description 获得周回款榜
     */
    @Query(value = "select sum(apply_pay_amt) as amt, applay_real_name, u.id from case_pay_apply c,user u where c.applay_user_name = u.user_name and applay_date >= :startDate and applay_date <= :endDate " +
            "and approve_status=:approveStatu group by id, applay_real_name order by amt desc",nativeQuery = true)
    List<Object[]> queryPayList(@Param("approveStatu") Integer approveStatu, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
}