package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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

    /**
     * 今日流入案件数
     * @param userId
     * @return
     */
    @Query(value = "SELECT SUM(m.sum) FROM ( " +
            "SELECT COUNT(id) AS sum,current_collector AS coll FROM case_info " +
            "WHERE to_days(case_follow_in_time) = to_days(now()) " +
            "AND current_collector = :userId " +
            "UNION ALL " +
            "SELECT COUNT(id) AS sum,assist_collector AS coll FROM case_assist " +
            "WHERE to_days(case_flowin_time) = to_days(now()) " +
            "AND assist_collector = :userId " +
            ") AS m ", nativeQuery = true)
    Integer getFlowInCaseToday(@Param("userId")String userId);

    /**
     * 今日结清案件数
     * @param userId
     * @return
     */
    @Query(value = "SELECT COUNT(*) AS num FROM case_info " +
            "WHERE collection_status = 24 " +
            "AND to_days(operator_time) = to_days(now()) " +
            "AND (current_collector = :userId OR assist_collector = :userId)", nativeQuery = true)
    Integer getFinishCaseToday(@Param("userId")String userId);

    /**
     * 今日流出案件数
     * @param userId
     * @return
     */
    @Query(value = "SELECT COUNT(*) AS num FROM case_turn_record " +
            "LEFT JOIN case_info " +
            "ON case_turn_record.case_id = case_info.id " +
            "WHERE to_days(case_turn_record.operator_time) = to_days(now()) " +
            "AND (case_info.current_collector != :userId " +
            "OR case_info.assist_collector != :userId)", nativeQuery = true)
    Integer getFlowOutCaseToday(@Param("userId")String userId);

    /**
     * 催收员回款总额
     * @param username
     * @return
     */
    @Query(value = "SELECT SUM(apply_pay_amt) FROM case_pay_apply WHERE approve_result = 179 AND apply_user_name = :username", nativeQuery = true)
    BigDecimal getMoneySumResult(@Param("username")String username);

    /**
     * 本月回款总额
     * @param username
     * @return
     */
    @Query(value = "SELECT SUM(apply_pay_amt) FROM case_pay_apply " +
            "WHERE approve_result = 179 " +
            "AND approve_status = 58 " +
            "AND DATE_FORMAT(approve_pay_datetime, '%Y%m') = DATE_FORMAT(curdate(),'%Y%m') " +
            "AND apply_user_name = :username", nativeQuery = true)
    BigDecimal getMonthMoneyResult(@Param("username")String username);

    /**
     * 今天回款总额
     * @param username
     * @return
     */
    @Query(value = "SELECT SUM(apply_pay_amt) FROM case_pay_apply " +
            "WHERE approve_result = 179 " +
            "AND approve_status = 58 " +
            "AND to_days(approve_pay_datetime) = to_days(now()) " +
            "AND apply_user_name = :username", nativeQuery = true)
    BigDecimal getDayMoneyResult(@Param("username")String username);

    /**
     * 今日累计催收次数
     * @param userId
     * @return
     */
    @Query(value = "SELECT COUNT(*) FROM case_followup_record " +
            " WHERE to_days(operator_time) = to_days(now()) " +
            " AND operator = :userId", nativeQuery = true)
    Integer getDayFollowCount(@Param("userId")String userId);

    /**
     * 本月累计催收次数
     * @param userId
     * @return
     */
    @Query(value = "SELECT COUNT(*) FROM case_followup_record " +
            "WHERE DATE_FORMAT(operator_time, '%Y%m') = DATE_FORMAT(curdate(),'%Y%m') " +
            "AND operator = :userId", nativeQuery = true)
    Integer getMonthFollowCount(@Param("userId")String userId);
}
