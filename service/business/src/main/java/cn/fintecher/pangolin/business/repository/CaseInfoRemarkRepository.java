package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseInfoRemark;
import cn.fintecher.pangolin.entity.QCaseInfoRemark;
import cn.fintecher.pangolin.util.ZWDateUtil;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * @author : xiaqun
 * @Description :
 * @Date : 13:49 2017/9/20
 */

public interface CaseInfoRemarkRepository extends QueryDslPredicateExecutor<CaseInfoRemark>, JpaRepository<CaseInfoRemark, String>, QuerydslBinderCustomizer<QCaseInfoRemark> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseInfoRemark root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(StringUtils.trim(value)).concat("%")));
        bindings.bind(root.operatorTime).all((DateTimePath<Date> path, Collection<? extends Date> value) -> { //操作时间
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
    }
}
