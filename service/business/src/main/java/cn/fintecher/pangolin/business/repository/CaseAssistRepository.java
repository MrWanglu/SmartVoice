package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseAssist;
import cn.fintecher.pangolin.entity.QCaseAssist;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;

import java.util.List;
import org.springframework.data.repository.query.Param;


/**
 * @author : xiaqun
 * @Description :
 * @Date : 10:11 2017/7/18
 */

public interface CaseAssistRepository extends QueryDslPredicateExecutor<CaseAssist>, JpaRepository<CaseAssist, String>, QuerydslBinderCustomizer<QCaseAssist> {

    @Override
    default void customize(final QuerydslBindings bindings, final QCaseAssist root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(value).concat("%")));
        bindings.bind(root.caseId.collectionType).first((path, value) -> path.eq(value));
    }

    @Query(value = "SELECT a.numa+b.numb FROM ( " +
            "SELECT COUNT(*) AS numa FROM case_info " +
            "WHERE current_collector = :userId " +
            "AND leave_case_flag = 1 " +
            "AND collection_status IN (20, 21, 22, 23, 25) " +
            ") AS a, " +
            "( " +
            "SELECT COUNT(*) AS numb FROM case_assist " +
            "WHERE assist_collector = :userId " +
            "AND leave_case_flag = 1 " +
            "AND assist_status IN (28,118) " +
            ") AS b ", nativeQuery = true)
    long leaveCaseAssistCount(@Param("userId") String userId);

    /**
     * 查询协催案件流转
     * @param holdDays
     * @param companyCode
     * @return
     */
    @Query(value = "SELECT a.* FROM case_assist a LEFT JOIN case_followup_record b ON a.case_id = b.case_id  WHERE " +
            " a.assist_way = ?1 AND (b.type is null or b.type = 186) AND a.hold_days >= ?2 AND (b.operator_time is null or b.operator_time >= a.case_flowin_time)" +
            " AND a.company_code=?3 AND a.assist_collector is NOT NULL AND a.leave_case_flag=?4 AND b.id IS NULL AND a.assist_status in (28,117,118)", nativeQuery = true)
    List<CaseAssist> queryAssitForce(@Param("assistWay") Integer assistWay, @Param("holdDays")Integer holdDays, @Param("companyCode") String companyCode,
                                     @Param("leaveCaseFlag") Integer leaveCaseFlag);
}
