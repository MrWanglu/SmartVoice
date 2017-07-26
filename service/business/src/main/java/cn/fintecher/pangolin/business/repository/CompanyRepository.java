package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.Company;
import cn.fintecher.pangolin.entity.QCompany;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-03-13:55
 */
public interface CompanyRepository extends QueryDslPredicateExecutor<Company>, JpaRepository<Company, String>, QuerydslBinderCustomizer<QCompany> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCompany root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(value).concat("%")));
        //公司状态
        bindings.bind(root.status).first((path, value) -> path.eq(value));
        //公司code
        bindings.bind(root.code).first((path, value) -> path.eq(value));
        //创建时间
    }
}
