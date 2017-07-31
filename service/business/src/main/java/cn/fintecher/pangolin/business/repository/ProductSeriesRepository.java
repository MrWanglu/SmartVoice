package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.ProductSeries;
import cn.fintecher.pangolin.entity.QProductSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 16:50 2017/7/26
 */
public interface ProductSeriesRepository extends QueryDslPredicateExecutor<ProductSeries>, JpaRepository<ProductSeries, String>,
        QuerydslBinderCustomizer<QProductSeries> {
    @Override
    default void customize(final QuerydslBindings bindings, final QProductSeries root) {


    }
}
