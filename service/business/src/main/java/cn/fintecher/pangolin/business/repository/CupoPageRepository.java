package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @Author : sunyanping
 * @Description : 催收员主页统计
 * @Date : 2017/8/2.
 */
public interface CupoPageRepository extends JpaRepository<CaseInfo, String> {

    /**
     * 月度任务完成度
     * @param username
     * @return
     */
    @Query(value = "select m.pamt/n.back_cash from " +
            "( " +
            " select sum(apply.apply_pay_amt) as pamt,apply.apply_user_name from case_pay_apply as apply " +
            " where apply.approve_status = '58' " +
            " and apply.apply_user_name = :username " +
            " and year(apply.apply_date) = year(NOW()) " +
            " and month(apply.apply_date) = month(NOW()) " +
            ") as m " +
            "left join " +
            "( " +
            " select plan.user_name,plan.back_cash from user_backcash_plan as plan " +
            " where plan.user_name = :username " +
            " and plan.year = year(now()) " +
            " and plan.month = month(now()) " +
            ") as n " +
            "on m.apply_user_name = n.user_name", nativeQuery = true)
    Double getTodyTashFinished(@Param("username") String username);

    /**
     * 案件情况/案件金额总计
     * @param userId
     * @return
     */
    @Query(value = "SELECT collection_status AS status,count(collection_status) AS num,sum(overdue_amount) AS amount FROM case_info " +
            "WHERE (current_collector = :userId OR assist_collector = :userId) " +
            "GROUP BY collection_status", nativeQuery = true)
    List<Object[]> getCaseCountResult(@Param("userId") String userId);

    /**
     * 本周回款
     * @param userName
     * @return
     */
    @Query(value = "SELECT sum(apply_pay_amt) AS amount,WEEKDAY(approve_pay_datetime) AS dayOfWeek FROM case_pay_apply " +
            "WHERE apply_user_name = :username " +
            "AND YEARWEEK(date_format(approve_pay_datetime,'%Y-%m-%d'),1) = YEARWEEK(now(),1) " +
            "AND approve_status = 58 " +
            "GROUP BY WEEKDAY(approve_pay_datetime) " +
            "ORDER BY dayOfWeek", nativeQuery = true)
    List<Object[]> getRepayWeek(@Param("username")String userName);

    /**
     * 本周催计数
     * @param userId
     * @return
     */
    @Query(value = "SELECT count(id) AS num,WEEKDAY(operator_time) AS dayOfWeek FROM case_followup_record " +
            "WHERE operator = :userId " +
            "AND YEARWEEK(date_format(operator_time,'%Y-%m-%d'),1) = YEARWEEK(now(),1) " +
            "GROUP BY WEEKDAY(operator_time) " +
            "ORDER BY dayOfWeek", nativeQuery = true)
    List<Object[]> getFolWeek(@Param("userId")String userId);

    /**
     * 本周结案数
     * @param userId
     * @return
     */
    @Query(value = "SELECT count(id) AS num,WEEKDAY(close_date) AS dayOfWeek FROM case_info " +
            "WHERE (current_collector = :userId OR assist_collector = :userId) " +
            "AND collection_status = 24 " +
            "AND YEARWEEK(date_format(close_date,'%Y-%m-%d'),1) = YEARWEEK(now(),1) " +
            "GROUP BY WEEKDAY(close_date) " +
            "ORDER BY dayOfWeek", nativeQuery = true)
    List<Object[]> getCaseEndWeek(@Param("userId")String userId);
}
