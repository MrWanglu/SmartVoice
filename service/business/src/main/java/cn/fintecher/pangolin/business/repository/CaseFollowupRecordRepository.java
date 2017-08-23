package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseFollowupRecord;
import cn.fintecher.pangolin.entity.QCaseFollowupRecord;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author : xiaqun
 * @Description :
 * @Date : 16:15 2017/7/19
 */

public interface CaseFollowupRecordRepository extends QueryDslPredicateExecutor<CaseFollowupRecord>, JpaRepository<CaseFollowupRecord, String>, QuerydslBinderCustomizer<QCaseFollowupRecord> {
    @Override
    default void customize(final QuerydslBindings bindings, final QCaseFollowupRecord root) {
        bindings.bind(String.class).first((StringPath path, String value) -> path.like("%".concat(value).concat("%")));
        bindings.bind(root.collectionFeedback).first(SimpleExpression::eq); //催收反馈
        bindings.bind(root.type).first(SimpleExpression::eq); //跟进方式
        bindings.bind(root.source).first(SimpleExpression::eq); //跟进来源
        bindings.bind(root.operatorTime).all((path, value) -> { //跟进时间
            Iterator<? extends Date> it = value.iterator();
            Date operatorMinTime = it.next();
            if (it.hasNext()) {
                Date operatorMaxTime = it.next();
                return path.between(operatorMinTime, operatorMaxTime);
            } else {
                return path.goe(operatorMinTime);
            }
        });
    }

    /**
     @Description 获得周跟催榜
     */
    @Query(value = "select count(distinct(c.case_id)) as rank,u.real_name,u.id,u.photo from " +
            "( " +
            "select operator,case_id from case_followup_record " +
            "where operator_time <= :endDate " +
            "and operator_time >= :startDate " +
            ") as c " +
            "inner join " +
            "( " +
            "select id,real_name,photo,user_name from `user` " +
            "where type = :type " +
            "and companyCode = :companyCode " +
            ") as u " +
            "on u.user_name = c.operator " +
            "group by " +
            "u.id " +
            "order by " +
            "rank desc",nativeQuery = true)
    List<Object[]> getFlowupCaseList(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("type") Integer type, @Param("companyCode") String companyCode);

    /**
     @Description 获得周催计榜
     */
    @Query(value = "select count(*) as rank,u.real_name,u.id,u.photo from " +
            "( " +
            "select operator from case_followup_record " +
            "where operator_time <= :endDate " +
            "and operator_time >= :startDate " +
            ") as c " +
            "inner join " +
            "( " +
            "select id,real_name,photo,user_name from `user` " +
            "where type = :type " +
            "and companyCode = :companyCode " +
            ") as u " +
            "on u.user_name = c.operator " +
            "group by " +
            "u.id " +
            "order by " +
            "rank desc",nativeQuery = true)
    List<Object[]> getCollectionList(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("type") Integer type, @Param("companyCode") String companyCode);
    /**
     @Description 获得指定用户催计数
     */
    @Query(value = "select count(*) from case_followup_record where operator = :name and type = :type and operator_time <= :endDate and operator_time >= :startDate",nativeQuery = true)
    Integer  getCollectionNum(@Param("name")String name, @Param("type") int type, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query(value = "select * from case_followup_record where case_id = :id and promise_flag = :flag order by operator_time desc limit 1",nativeQuery = true)
    CaseFollowupRecord getPayPromise(@Param("id")String id, @Param("flag") Integer flag);
    /**
     * @Description : 中通天鸿 164 双向外呼通话个数统计
     */

    @Query(value = "select count(*) a,operator,operator_name from case_followup_record where operator_time>:startTime and operator_time<:endTime and company_code =:companyCode and call_type ='164' GROUP BY operator,operator_name ORDER BY a DESC",nativeQuery = true)
    List<Object[]> getCountSmaRecord(@Param("startTime") String startTime, @Param("endTime") String endTime,@Param("companyCode") String companyCode);
    /**
     * @Description : 中通天鸿 164 双向外呼通话时长统计
     */

    @Query(value = "select sum(conn_secs) a ,operator,operator_name from case_followup_record where operator_time>:startTime and operator_time<:endTime and company_code =:companyCode and call_type ='164' GROUP BY operator,operator_name ORDER BY a DESC",nativeQuery = true)
    List<Object[]> getCountTimeSmaRecord(@Param("startTime") String startTime, @Param("endTime") String endTime,@Param("companyCode") String companyCode);
}
