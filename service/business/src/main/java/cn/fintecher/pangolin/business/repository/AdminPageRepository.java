package cn.fintecher.pangolin.business.repository;

import cn.fintecher.pangolin.entity.CaseInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author : sunyanping
 * @Description : 管理员主页统计
 * @Date : 2017/8/2.
 */
public interface AdminPageRepository extends JpaRepository<CaseInfo, String> {
    /**
     * 部门下案件总金额
     * @param deptCode
     * @return
     */
    @Query(value = "SELECT SUM(case_info.overdue_amount) AS amt FROM case_info " +
                    "LEFT JOIN department " +
                    "ON case_info.depart_id = department.id " +
                    "WHERE department.`code` LIKE concat(?1,'%')", nativeQuery = true)
    BigDecimal getCaseSumAmt (@Param("deptCode") String deptCode);

    /**
     * 部门下案件已还款总额
     * @param deptCode
     * @return
     */
    @Query(value = "SELECT SUM(case_pay_apply.apply_pay_amt) AS amt FROM case_pay_apply " +
                    "LEFT JOIN department " +
                    "ON case_pay_apply.depart_id = department.id " +
                    "WHERE department.`code` LIKE concat(?1,'%') " +
                    "AND case_pay_apply.approve_result = 179", nativeQuery = true)
    BigDecimal getRepaySumAmt(@Param("deptCode") String deptCode);

    /**
     * 部门下客户总数
     * @param deptCode
     * @return
     */
    @Query(value =  "SELECT COUNT(DISTINCT(personal_id)) FROM case_info " +
            "LEFT JOIN department " +
            "ON case_info.depart_id = department.id " +
            "WHERE department.`code` LIKE concat(?1,'%')", nativeQuery = true)
    Integer getCustNum(@Param("deptCode") String deptCode);

    /**
     * 部门下客户在案总数
     * @param deptCode
     * @return
     */
    @Query(value =  "SELECT COUNT(DISTINCT(personal_id)) FROM case_info " +
                    "LEFT JOIN department " +
                    "ON case_info.depart_id = department.id " +
                    "WHERE department.`code` LIKE concat(?1,'%') " +
                    "AND case_info.collection_status NOT IN (24,166)", nativeQuery = true)
    Integer getCustNumIN(@Param("deptCode") String deptCode);

    /**
     * 周回款统计
     * @param deptCode
     * @return
     */
    @Query(value = "SELECT SUM(apply_pay_amt) AS amt,WEEKDAY(approve_pay_datetime) AS dayOfWeek FROM case_pay_apply " +
                    "LEFT JOIN department " +
                    "ON case_pay_apply.depart_id = department.id " +
                    "WHERE department.`code` LIKE concat(?1,'%') " +
                    "AND YEARWEEK(DATE_FORMAT(approve_pay_datetime,'%Y-%m-%d'),1) = YEARWEEK(now(),1) " +
                    "GROUP BY WEEKDAY(approve_pay_datetime) " +
                    "ORDER BY dayOfWeek ",  nativeQuery = true)
    List<Object[]> getWeekRepaySumAmt(@Param("deptCode") String deptCode);

    /**
     * 周催计数
     * @param deptCode
     * @return
     */
    @Query(value = "SELECT COUNT(*) AS num,WEEKDAY(operator_time) AS dayOfWeek FROM case_followup_record " +
            "LEFT JOIN `user` " +
            "ON case_followup_record.operator = `user`.id " +
            "LEFT JOIN department " +
            "ON `user`.dept_id = department.id " +
            "WHERE department.`code` LIKE concat(?1,'%') " +
            "AND YEARWEEK(DATE_FORMAT(operator_time,'%Y-%m-%d'),1) = YEARWEEK(now(),1) " +
            "GROUP BY WEEKDAY(operator_time) " +
            "ORDER BY dayOfWeek", nativeQuery = true)
    List<Object[]> getWeekFollCount(@Param("deptCode") String deptCode);

    /**
     * 周结案数
     * @param deptCode
     * @return
     */
    @Query(value = "SELECT SUM(m.num),m.closeDate AS dayOfWeek FROM ( " +
            "SELECT COUNT(*) AS num,WEEKDAY(close_date) AS closeDate FROM case_info " +
            "LEFT JOIN department " +
            "ON case_info.depart_id = department.id " +
            "WHERE department.`code` LIKE concat(:deptCode,'%') " +
            "AND case_info.collection_status = 24 " +
            "AND YEARWEEK(DATE_FORMAT(close_date,'%Y-%m-%d'),1) = YEARWEEK(now(),1) " +
            "GROUP BY WEEKDAY(close_date) " +
            "UNION ALL " +
            "SELECT COUNT(*) AS num,WEEKDAY(close_date) AS closeDate FROM case_info " +
            "LEFT JOIN `user` " +
            "ON `user`.id = case_info.assist_collector " +
            "LEFT JOIN department " +
            "ON `user`.dept_id = department.id " +
            "WHERE department.`code` LIKE concat(:deptCode,'%') " +
            "AND case_info.collection_status = 24 " +
            "AND YEARWEEK(DATE_FORMAT(close_date,'%Y-%m-%d'),1) = YEARWEEK(now(),1) " +
            "GROUP BY WEEKDAY(close_date) " +
            ") AS m " +
            "GROUP BY m.closeDate " +
            "ORDER BY dayOfWeek", nativeQuery = true)
    List<Object[]> getWeekCaseEndCount(@Param("deptCode") String deptCode);

    /**
     * 催收员排名
     * @param deptCode
     * @return
     */
    @Query(value = "select n.uname as uname,n.rname as rname,m.amt as amt,n.pamt as pamt,pamt/amt as rate from " +
            "(select apply.apply_user_name as uname,apply.apply_real_name as rname,sum(apply.apply_pay_amt) as pamt from case_pay_apply as apply " +
            "left join department " +
            "on apply.depart_id = department.id " +
            "where apply.approve_status = '58' " +
            "and department.code like concat(?1,'%') " +
            "and year(apply.approve_pay_datetime) = year(NOW()) " +
            "and month(apply.approve_pay_datetime) = month(NOW()) " +
            "group by apply.apply_user_name,apply.apply_real_name) as n " +
            "left join " +
            " ( " +
            " select sum(amt) as amt,coll from ( " +
            " ( " +
            " select sum(case_info.overdue_amount) as amt,`user`.user_name as coll from case_info " +
            " left join `user` " +
            " on `user`.id = case_info.current_collector " +
            " group by case_info.current_collector " +
            " ) " +
            " union all " +
            " ( " +
            " select sum(case_info.overdue_amount) as amt,`user`.user_name as coll from case_info " +
            " left join `user` " +
            " on `user`.id = case_info.assist_collector " +
            " group by case_info.assist_collector " +
            " ) " +
            " ) as c " +
            " group by c.coll " +
            " ) as m " +
            "on n.uname = m.coll " +
            "order by rate desc,amt desc,pamt desc " +
            "limit 0,3", nativeQuery = true)
    List<Object[]> getCupoSort(@Param("deptCode") String deptCode);

    /**
     * 客户排名
     * @param deptCode
     * @return
     */
    @Query(value = "select m.pid,m.pname,n.amt,m.pamt,m.pamt/n.amt as rate from " +
            "( " +
            " select apply.personal_id as pid,apply.personal_name as pname,sum(apply.apply_pay_amt) as pamt from case_pay_apply as apply " +
            " left join department " +
            " on apply.depart_id = department.id " +
            " where apply.approve_status = '58' " +
            " and department.`code` like concat(?1,'%') " +
            " and year(apply.approve_pay_datetime) = year(NOW()) " +
            " and month(apply.approve_pay_datetime) = month(NOW()) " +
            " group by apply.personal_id,apply.personal_name " +
            ") as m " +
            "left join " +
            "( " +
            " select sum(case_info.overdue_amount) as amt,case_info.personal_id as pid from case_info " +
            " group by case_info.personal_id " +
            ") as n " +
            "on m.pid = n.pid " +
            "order by rate desc,amt desc,pamt desc " +
            "limit 0,3", nativeQuery = true)
    List<Object[]> getCustSort(@Param("deptCode") String deptCode);
}
