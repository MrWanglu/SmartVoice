package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.QUserDevice;
import cn.fintecher.pangolin.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

/**
 * @author yuanyanting
 * @version Id:CaseRepairRepository.java,v 0.1 2017/8/8 15:15 yuanyanting Exp $$
 * 案件修复
 */


public interface UserDeviceRepository extends QueryDslPredicateExecutor<UserDevice>, JpaRepository<UserDevice, String>, QuerydslBinderCustomizer<QUserDevice> {
    @Override
    default void customize(final QuerydslBindings bindings, final QUserDevice root) {
    }
}
