package cn.fintecher.pangolin.business.repository;


import cn.fintecher.pangolin.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * Created by ChenChang on 2017/7/11.
 */
public interface DepartmentRepository extends QueryDslPredicateExecutor<Department>, JpaRepository<Department, String> {
}
