package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CasePayApply;
import cn.fintecher.pangolin.entity.QCasePayApply;
import com.querydsl.core.types.dsl.SimpleExpression;
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
        bindings.bind(root.applyDate).all((path, value) -> { //申请时间
            Iterator<? extends Date> it = value.iterator();
            Date applyMinDate = it.next();
            if (it.hasNext()) {
                Date applyMaxDate = it.next();
                return path.between(applyMinDate, applyMaxDate);
            } else {
                return path.goe(applyMinDate);
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
        bindings.bind(root.applyUserName).first(SimpleExpression::eq);//申请人
        bindings.bind(root.batchNumber).first((SimpleExpression::eq)); //批次号
    }

    /**
     @Description 获得指定用户的待审核回款金额
     */
    @Query(value = "select sum(applyPayAmt) from CasePayApply where applyUserName = :username and  approveStatus=:approveStatu")
    BigDecimal queryApplyAmtByUserName(@Param("username") String username, @Param("approveStatu") Integer approveStatu);

    /**
     @Description 获得周回款榜
     */
    @Query(value = "select sum(a.apply_pay_amt) as amt,u.real_name,u.id,u.photo from " +
            "( " +
            "select apply_pay_amt,apply_user_name from case_pay_apply " +
            "where approve_pay_datetime >=:startDate " +
            "and approve_pay_datetime <=:endDate " +
            "and approve_status=:approveStatu " +
            ") as a " +
            "inner join " +
            "( " +
            "select id,real_name,photo,user_name from `user` " +
            "where type = :type " +
            ") as u " +
            "on u.user_name = a.apply_user_name " +
            "GROUP BY " +
            "id " +
            "order by " +
            "amt desc",nativeQuery = true)
    List<Object[]> queryPayList(@Param("approveStatu") Integer approveStatu, @Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("type") Integer type);

    /**
     * @Description 本月催收佣金
     */
    @Query(value = "select sum(a.apply_pay_amt*c.commission_rate/100) from " +
            "( " +
            "select commission_rate,id from case_info " +
            ") as c " +
            "inner join " +
            "( " +
            "select apply_pay_amt,case_id from case_pay_apply " +
            "where approve_status=:approveStatu " +
            "and apply_user_name =:username " +
            "and approve_pay_datetime >=:startDate " +
            "and approve_pay_datetime <=:endDate " +
            ") as a " +
            "on a.case_id = c.id",nativeQuery = true)
    BigDecimal queryCommission(@Param("username") String username,@Param("approveStatu") Integer approveStatu, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
