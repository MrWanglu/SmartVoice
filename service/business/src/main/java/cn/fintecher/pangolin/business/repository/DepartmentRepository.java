package cn.fintecher.pangolin.business.repository;


import cn.fintecher.pangolin.entity.Department;
import cn.fintecher.pangolin.entity.QDepartment;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * Created by ChenChang on 2017/7/11.
 */
public interface DepartmentRepository extends QueryDslPredicateExecutor<Department>, JpaRepository<Department, String>,QuerydslBinderCustomizer<QDepartment> {
    @Override
    default void customize(final QuerydslBindings bindings, final QDepartment root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(StringUtils.trim(value)).concat("%")));
        //机构类型
        bindings.bind(root.type).first((path, value) -> path.eq(value));
        //机构等级
        bindings.bind(root.level).first((path, value) -> path.eq(value));
        //机构状态（0是启用  1 是停用）
        bindings.bind(root.status).first((path, value) -> path.eq(value));
        //机构code
        bindings.bind(root.code).first((path, value) -> path.like(StringUtils.trim(value)));
        //机构company_code
        bindings.bind(root.companyCode).first((path, value) -> path.eq(StringUtils.trim(value)));
    }
}
