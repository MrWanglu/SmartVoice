package cn.fintecher.pangolin.dataimp.repository;

import cn.fintecher.pangolin.dataimp.entity.QUserVideo;
import cn.fintecher.pangolin.dataimp.entity.UserVideo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;


/**
 * Created by qijigui on 2017-11-08.
 */
public interface UserVideoRepository extends MongoRepository<UserVideo, String>, QueryDslPredicateExecutor<UserVideo>, QuerydslBinderCustomizer<QUserVideo> {

    @Override
    default void customize(final QuerydslBindings bindings, final QUserVideo root) {
        bindings.bind(root.userName).first((path, value) -> path.eq(StringUtils.trim(value)));
    }

}
