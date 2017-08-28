package cn.fintecher.pangolin.report.mapper;

import cn.fintecher.pangolin.report.model.CaseCountResult;
import cn.fintecher.pangolin.report.model.WeekCountResult;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/8/8.
 */
public interface CupoPageMapper {


    /**
     * 月度任务完成度
     * @param username
     * @return
     */
    Double getTodyTashFinished(String username);

    /**
     * 案件情况/案件金额总计
     * @param userId
     * @return
     */
    List<CaseCountResult> getCaseCountResult(String userId);

    /**
     * 本周回款
     * @param userName
     * @return
     */
    List<WeekCountResult> getRepayWeek(String userName);

    /**
     * 本周催计数
     * @param username
     * @return
     */
    List<WeekCountResult> getFolWeek(String username);

    /**
     * 本周结案数
     * @param userId
     * @return
     */
    List<WeekCountResult> getCaseEndWeek(String userId);

    /**
     * 今日流入案件数
     * @param userId
     * @return
     */
    Integer getFlowInCaseToday(String userId);

    /**
     * 今日结清案件数
     * @param userId
     * @return
     */
    Integer getFinishCaseToday(String userId);

    /**
     * 今日流出案件数
     * @param userId
     * @return
     */
    Integer getFlowOutCaseToday(String userId);

    /**
     * 催收员回款总额
     * @param username
     * @return
     */
    BigDecimal getMoneySumResult(String username);

    /**
     * 本月回款总额
     * @param username
     * @return
     */
    BigDecimal getMonthMoneyResult(String username);

    /**
     * 今天回款总额
     * @param username
     * @return
     */
    BigDecimal getDayMoneyResult(String username);

    /**
     * 今日累计催收次数
     * @param username
     * @return
     */
    Integer getDayFollowCount(String username);

    /**
     * 本月累计催收次数
     * @param username
     * @return
     */
    Integer getMonthFollowCount(String username);

    /**
     * 催收员当前催收案件数
     * @param userId
     * @return
     */
    Integer getCaseInfoCount(String userId);

    /**
     * 催收员案件总数（包含已结案的）
     * @return
     */
    Integer getCaseInfoAllCount(String userId);
}
