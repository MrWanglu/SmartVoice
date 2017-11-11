package cn.fintecher.pangolin.report.mapper;

import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.report.model.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author : sunyanping
 * @Description : 管理员首页
 * @Date : 2017/8/8.
 */
public interface AdminPageMapper {


    /**
     * 催收员排行榜
     *
     * @param params
     * @return
     */
    List<CollectorRankingModel> collectorRanking(CollectorRankingParams params);

    /**
     * 委外方排行榜
     * @param params
     * @return
     */
    List<OutsourceRankingModel> OutsourceRanking(CollectorRankingParams params);
    /**
     * 内催分布于各省份的金额和数量
     * @param params
     * @return
     */
    ProvinceCollectionDateModel getInnerCollectionDate(CollectorRankingParams params);

    /**
     * 内催分布于各省份的金额和数量
     * @param params
     * @return
     */
    ProvinceCollectionDateModel getOutsourceCollectionDate(CollectorRankingParams params);

    /**
     * 内催分布于各省份的金额和数量
     * @param params
     * @return
     */
    List<ProvinceCollectionDateModel> getProvinceInnerCollectionDate(CollectorRankingParams params);

    /**
     * 委外分布于各省份的金额和数量
     * @param params
     * @return
     */
    List<ProvinceCollectionDateModel> getProvinceOutsourceCollectionDate(CollectorRankingParams params);

    /**以上是新版本*/
    /**############################################################################################################*/
    /**以下是旧版本*/

    /**
     * 获取部门下所有用户
     *
     * @param deptCode
     * @return
     */
    List<User> getAllUserOnDepartment(String deptCode);

    /**
     * 获取公司下所有用户
     *
     * @param companyCode
     * @return
     */
    List<User> getAllUserOnCompany(String companyCode);

    /**
     * 部门下案件总金额
     *
     * @param deptCode
     * @return
     */
    BigDecimal getCaseSumAmt(String deptCode);

    /**
     * 部门下案件已还款总额
     *
     * @param deptCode
     * @return
     */
    BigDecimal getRepaySumAmt(String deptCode);

    /**
     * 部门下客户总数
     *
     * @param deptCode
     * @return
     */
    Integer getCustNum(String deptCode);

    /**
     * 部门下客户在案总数
     *
     * @param deptCode
     * @return
     */
    Integer getCustNumIN(String deptCode);

    /**
     * 周回款统计
     *
     * @param deptCode
     * @return
     */
    List<WeekCountResult> getWeekRepaySumAmt(String deptCode);

    /**
     * 周催计数
     *
     * @param deptCode
     * @return
     */
    List<WeekCountResult> getWeekFollCount(String deptCode);

    /**
     * 周结案数
     *
     * @param deptCode
     * @return
     */
    List<WeekCountResult> getWeekCaseEndCount(String deptCode);

    /**
     * 催收员排名
     *
     * @param deptCode
     * @return
     */
    List<PageSortResult> getCupoSort(String deptCode);

    /**
     * 客户排名
     *
     * @param deptCode
     * @return
     */
    List<PageSortResult> getCustSort(String deptCode);

    /**
     * 获取所有的案件时间
     */
    CaseDateModel getCaseDate(CollectorRankingParams collectorRankingParams);

    /**
     * 获取所有的案件时间
     */
    CaseDateModel getCaseAmtAndCount(CollectorRankingParams collectorRankingParams);

    /**
     * 某个年度不同催收方式的每月催记数量和外呼数量
     */
    List<GroupMonthFollowRecord> getRecordReport(CollectorRankingParams collectorrankingparams);

    /**
     * 根据不同的日期类型获取案件当前情况。包含不同还款类型的金额和数量
     */
    List<CaseRepaymentTypeGroupInfo> getCaseGroupInfo(CollectorRankingParams parm);
}
