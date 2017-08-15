package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseFile;
import cn.fintecher.pangolin.entity.QCaseFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * @Author : gaobeibei
 * @Description : 案件文件
 * @Date : 2017/8/15
 */
public interface CaseFileRepository extends QueryDslPredicateExecutor<CaseFile>, JpaRepository<CaseFile, String>, QuerydslBinderCustomizer<QCaseFile> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseFile root) {

    }
}
