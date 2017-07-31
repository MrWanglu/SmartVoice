package cn.fintecher.pangolin.business.repository;


import cn.fintecher.pangolin.entity.CaseInfoException;
import cn.fintecher.pangolin.entity.QCaseInfoException;
import com.querydsl.core.types.dsl.StringPath;
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

        bindings.bind(String.class).first((StringPath path, String value) -> path.like(value));
        bindings.bind(root.product.prodcutName).first((path,value) -> path.eq(value));
        //机构码搜索
        bindings.bind(root.department.code).first((path, value) -> path.startsWith(value));
        //公司码
        bindings.bind(root.companyCode).first((path, value) -> path.eq(value));
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
        bindings.bind(root.overdueDays).all((path, value) -> {
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
        bindings.bind(root.principalId.id).first((path, value) -> path.eq(value));
        //案件状态
        bindings.bind(root.collectionStatus).first((path, value) -> path.eq(value));
        //案件类型
        bindings.bind(root.caseType).first((path, value) -> path.eq(value));
        //催收类型
        bindings.bind(root.collectionType).first((path, value) -> path.eq(value));
        //产品系列
        bindings.bind(root.product.productSeries.id).first((path, value) -> path.eq(value));
    }

}
