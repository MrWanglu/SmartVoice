package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.Product;
import cn.fintecher.pangolin.entity.QProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 16:53 2017/7/26
 */
public interface ProductRepository extends QueryDslPredicateExecutor<Product>, JpaRepository<Product, String>,
        QuerydslBinderCustomizer<QProduct> {
    @Override
    default void customize(final QuerydslBindings bindings, final QProduct root) {


    }
}
