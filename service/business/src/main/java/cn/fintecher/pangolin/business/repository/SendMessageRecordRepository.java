package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.QSendMessageRecord;
import cn.fintecher.pangolin.entity.SendMessageRecord;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * @author : xiaqun
 * @Description :
 * @Date : 14:26 2017/7/20
 */

public interface SendMessageRecordRepository extends QueryDslPredicateExecutor<SendMessageRecord>, JpaRepository<SendMessageRecord, String>, QuerydslBinderCustomizer<QSendMessageRecord> {
    @Override
    default void customize(final QuerydslBindings bindings, final QSendMessageRecord root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(value).concat("%")));
        bindings.bind(root.messageType).first(SimpleExpression::eq); //发送信息类别
        bindings.bind(root.sendWay).first(SimpleExpression::eq); //发送方式
        bindings.bind(root.tempelateType).first(SimpleExpression::eq); //模版类别
    }
}
