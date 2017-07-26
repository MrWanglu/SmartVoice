package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.QSysParam;
import cn.fintecher.pangolin.entity.SysParam;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-04-9:30
 */
public interface SysParamRepository extends QueryDslPredicateExecutor<SysParam>, JpaRepository<SysParam, String>, QuerydslBinderCustomizer<QSysParam> {
    @Override
    default void customize(final QuerydslBindings bindings, final QSysParam root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like(value));
        //公司的特定标识
        bindings.bind(root.companyCode).first((path, value) -> path.eq(value));
        //参数的自定义code
        bindings.bind(root.code).first((path, value) -> path.eq(value));
        //参数名称
        bindings.bind(root.name).first((path, value) -> path.eq(value));
        //参数是否启用（0是启用 1 是停用）
        bindings.bind(root.status).first((path, value) -> path.eq(value));
        //参数类型（服务的端口号）
        bindings.bind(root.type).first((path, value) -> path.eq(value));
        //参数值
        bindings.bind(root.value).first((path, value) -> path.eq(value));
        //标识（0是可以修改 1是不能修改）
        bindings.bind(root.sign).first((path, value) -> path.eq(value));
        //创建时间
    }
}
