package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.OutSourceCommssion;
import cn.fintecher.pangolin.entity.QOutSourceCommssion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-15-11:58
 */
public interface OutSourceCommssionRepository extends QueryDslPredicateExecutor<OutSourceCommssion>, JpaRepository<OutSourceCommssion, String>, QuerydslBinderCustomizer<QOutSourceCommssion> {
    @Override
    default void customize(final QuerydslBindings bindings, final QOutSourceCommssion root) {

    }

    @Query(value = "SELECT aa.outs_name,aa.overdue_time,aa.return_money,(aa.return_money * aa.huankuan) AS money,aa.return_households,(aa.return_households * aa.hushu) AS nummoney " +
            "FROM " +
            "(SELECT b.outs_name,d.overdue_time,d.return_money,d.return_households,sum(c.contract_amount) AS huankuan,count(*) AS hushu " +
            " FROM out_operate_record a,outsource b,outsource_pool c,outsource_commission d" +
            " WHERE " +
            "    a.outs_id = b.id " +
            "    AND a.outcase_id = c.id " +
            "    AND d.outs_id = b.id" +
            "    AND d.overdue_time = c.overdue_periods " +
            "    AND b.company_code = :companyCode" +
            "    AND a.operation_type = :operationType" +
            "    AND b.outs_name = :outsName " +
            "    GROUP BY a.outs_id,d.overdue_time,d.return_money,d.return_households " +
            " ) AS aa;", nativeQuery = true)
    Object[] outsourceCommissionReturn(@Param("companyCode") String companyCode, @Param("operationType") Integer operationType, @Param("outsName") String outsName);

    @Query(value = "SELECT aa.outs_name,aa.overdue_time,aa.rollback_money,(aa.rollback_money * aa.huankuan) AS money,aa.rollback_households,(aa.rollback_households * aa.hushu) AS nummoney " +
            "FROM " +
            "(SELECT b.outs_name,d.overdue_time,d.rollback_money,d.rollback_households,sum(c.contract_amount) AS huankuan,count(*) AS hushu " +
            " FROM out_operate_record a,outsource b,outsource_pool c,outsource_commission d" +
            " WHERE " +
            "    a.outs_id = b.id " +
            "    AND a.outcase_id = c.id " +
            "    AND d.outs_id = b.id" +
            "    AND d.overdue_time = c.overdue_periods " +
            "    AND b.company_code = :companyCode" +
            "    AND a.operation_type = :operationType" +
            "    AND b.outs_name = :outsName " +
            "    GROUP BY a.outs_id,d.overdue_time,d.rollback_money,d.rollback_households " +
            " ) AS aa;", nativeQuery = true)
    Object[] outsourceCommissionRollback(@Param("companyCode") String companyCode, @Param("operationType") Integer operationType, @Param("outsName") String outsName);

    @Query(value = "SELECT aa.outs_name,aa.overdue_time,aa.repair_money,(aa.repair_money * aa.huankuan) AS money,aa.repair_households,(aa.repair_households * aa.hushu) AS nummoney " +
            "FROM " +
            "(SELECT b.outs_name,d.overdue_time,d.repair_money,d.repair_households,sum(c.contract_amount) AS huankuan,count(*) AS hushu " +
            " FROM out_operate_record a,outsource b,outsource_pool c,outsource_commission d" +
            " WHERE " +
            "    a.outs_id = b.id " +
            "    AND a.outcase_id = c.id " +
            "    AND d.outs_id = b.id" +
            "    AND d.overdue_time = c.overdue_periods " +
            "    AND b.company_code = :companyCode" +
            "    AND a.operation_type = :operationType" +
            "    AND b.outs_name = :outsName " +
            "    GROUP BY a.outs_id,d.overdue_time,d.repair_money,d.repair_households " +
            " ) AS aa;", nativeQuery = true)
    Object[] outsourceCommissionRepair(@Param("companyCode") String companyCode, @Param("operationType") Integer operationType, @Param("outsName") String outsName);
}
