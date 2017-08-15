package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.MessagePush;
import cn.fintecher.pangolin.entity.QCaseFollowupRecord;
import cn.fintecher.pangolin.entity.QMessagePush;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by Administrator on 2017/8/1.
 */
public interface MessagePushRepository extends QueryDslPredicateExecutor<MessagePush>, JpaRepository<MessagePush, String>, QuerydslBinderCustomizer<QMessagePush> {
    @Override
    default void customize(final QuerydslBindings bindings, final QMessagePush root) {

    }
}
