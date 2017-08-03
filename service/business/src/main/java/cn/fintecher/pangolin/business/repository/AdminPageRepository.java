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
    @Query(value = "select sum(amo.overdue_amount) as case_sum " +
            "from (select distinct(cinfo.case_number),overdue_amount " +
            "from " +
            " case_info cinfo " +
            "left join department dept " +
            " on cinfo.depart_id = dept.id " +
            "where dept.`code` like concat(?1,'%')) amo ", nativeQuery = true)
    BigDecimal getCaseSumAmt (@Param("deptCode") String deptCode);

    /**
     * 部门下案件已还款总额
     * @param deptCode
     * @return
     */
    @Query(value = "select sum(cpd.payamt) as payamt from " +
            "(select caseamt.case_id,caseamt.payamt,cade.dcode from " +
            "(select case_id,sum(apply_pay_amt) as payamt from case_pay_apply where approve_status = '58' group by case_id) as caseamt " +
            "left join " +
            "(select case_info.id as cid,department.code as dcode from " +
            "case_info left join department on case_info.depart_id = department.id) as cade " +
            "on cade.cid = caseamt.case_id) as cpd where cpd.dcode like concat(?1,'%')", nativeQuery = true)
    BigDecimal getRepaySumAmt(@Param("deptCode") String deptCode);

    /**
     * 部门下客户总数
     * @param deptCode
     * @return
     */
    @Query(value =  "select count(distinct(personal_id)) as psum from case_info " +
            " left join  " +
            " department " +
            " on case_info.depart_id = department.id " +
            " where department.`code` like concat(?1,'%')", nativeQuery = true)
    Integer getCustNum(@Param("deptCode") String deptCode);

    /**
     * 部门下客户在案总数
     * @param deptCode
     * @return
     */
    @Query(value =  "select count(distinct(personal_id)) as psum from case_info " +
            " left join  " +
            " department " +
            " on case_info.depart_id = department.id " +
            " where department.`code` like concat(?1,'%') " +
            " and case_info.collection_status not in (24,166)", nativeQuery = true)
    Integer getCustNumIN(@Param("deptCode") String deptCode);

    /**
     * 周回款统计
     * @param deptCode
     * @return
     */
    @Query(value = "select sum(wk.apply_pay_amt) as amount,weekday(wk.approve_pay_datetime) as dayOfWeek " +
            "from " +
            " (select case_id,apply_pay_amt,approve_pay_datetime " +
            " from case_pay_apply as apply " +
            " left join department on apply.depart_id = department.id " +
            " where approve_status = '58' " +
            " and department.code like concat(?1,'%') " +
            " and yearweek(DATE_FORMAT(apply.approve_pay_datetime,'%Y-%m-%d'),1) = YEARWEEK(NOW(),1)) wk " +
            "group by weekday(approve_pay_datetime) " +
            "order by dayOfWeek",  nativeQuery = true)
    List<Object[]> getWeekRepaySumAmt(@Param("deptCode") String deptCode);

    /**
     * 周催计数
     * @param deptCode
     * @return
     */
    @Query(value = "select count(*) as num,weekday(wk.operator_time) as dayOfWeek " +
            "from ( " +
            " select record.case_id,record.operator_time from case_followup_record as record " +
            " left join ( " +
            " select case_info.id,department.`code` from case_info " +
            " join department " +
            " on case_info.depart_id = department.id) as cdept " +
            " on record.case_id = cdept.id " +
            " where yearweek(DATE_FORMAT(record.operator_time,'%Y-%m-%d'),1) = YEARWEEK(NOW(),1) " +
            " and cdept.code like concat(?1,'%')) wk " +
            "group by weekday(wk.operator_time) " +
            "order by dayOfWeek", nativeQuery = true)
    List<Object[]> getWeekFollCount(@Param("deptCode") String deptCode);

    /**
     * 周结案数
     * @param deptCode
     * @return
     */
    @Query(value = "select count(*) as num,weekday(wk.close_date) as dayOfWeek from ( " +
            " select case_info.id,case_info.close_date from case_info " +
            " join department " +
            " on case_info.depart_id = department.id " +
            " where yearweek(DATE_FORMAT(case_info.close_date,'%Y-%m-%d'),1) = YEARWEEK(NOW(),1) " +
            " and department.code like concat(?1,'%')) as wk " +
            "group by weekday(wk.close_date) " +
            "order by dayOfWeek", nativeQuery = true)
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
