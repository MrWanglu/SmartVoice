package cn.fintecher.pangolin.business.repository;


import cn.fintecher.pangolin.entity.CaseInfoException;
import cn.fintecher.pangolin.entity.QCaseInfoException;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import java.math.BigDecimal;
import java.util.Iterator;

/**
 * Created by ChenChang on 2017/7/11.
 */
public interface CaseInfoExceptionRepository extends QueryDslPredicateExecutor<CaseInfoException>, JpaRepository<CaseInfoException, String>, QuerydslBinderCustomizer<QCaseInfoException> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseInfoException root) {

        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(StringUtils.trim(value)).concat("%")));
        //客户手机号
        bindings.bind(root.mobileNo).first((path, value) -> path.eq(StringUtils.trim(value)));
        //批次号
        bindings.bind(root.batchNumber).first((path, value) -> path.eq(StringUtils.trim(value)));
        //公司码
        bindings.bind(root.companyCode).first((path, value) -> path.eq(StringUtils.trim(value)));
        //案件金额
        bindings.bind(root.overdueAmount).all((path, value) -> {
            Iterator<? extends BigDecimal> it=value.iterator();
            BigDecimal firstOverdueAmount=it.next();
            if(it.hasNext()){
                BigDecimal secondOverDueAmont=it.next();
                return path.between(firstOverdueAmount,secondOverDueAmont);
            }else{
                //大于等于
               return path.goe(firstOverdueAmount);
            }
        });
        //逾期天数
        bindings.bind(root.overDueDays).all((path, value) -> {
            Iterator<? extends Integer> it=value.iterator();
            Integer firstOverdueDays=it.next();
            if(it.hasNext()){
                Integer secondOverdueDays=it.next();
                return path.between(firstOverdueDays,secondOverdueDays);
            }else{
                return path.goe(firstOverdueDays);
            }
        });
        //委托方
        bindings.bind(root.prinName).first((path, value) -> path.eq(StringUtils.trim(value)));

        //客户姓名
        bindings.bind(root.personalName).first((path, value) -> path.contains(StringUtils.trim(value)));
    }

}
