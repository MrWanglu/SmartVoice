package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.ExportItem;
import cn.fintecher.pangolin.entity.QExportItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by Administrator on 2017/9/26.
 */
public interface ExportItemRepository extends QueryDslPredicateExecutor<ExportItem>, JpaRepository<ExportItem, String>, QuerydslBinderCustomizer<QExportItem> {
    default void customize(final QuerydslBindings bindings, final QExportItem root) {

    }
}
