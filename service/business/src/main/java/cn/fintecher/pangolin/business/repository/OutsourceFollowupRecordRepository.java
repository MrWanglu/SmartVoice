package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.OutsourceFollowRecord;
import cn.fintecher.pangolin.entity.QOutsourceFollowRecord;
import cn.fintecher.pangolin.util.ZWDateUtil;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.SimpleExpression;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public interface OutsourceFollowupRecordRepository extends QueryDslPredicateExecutor<OutsourceFollowRecord>,QuerydslBinderCustomizer<QOutsourceFollowRecord>, JpaRepository<OutsourceFollowRecord, String> {

    @Override
    default void customize(final QuerydslBindings bindings, final QOutsourceFollowRecord root) {

        bindings.bind(root.feedback).first(SimpleExpression::eq); //催收反馈
        bindings.bind(root.followType).first(SimpleExpression::eq); //跟进方式
        bindings.bind(root.followTime).all((DateTimePath<Date> path, Collection<? extends Date> value) -> { //跟进时间
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
