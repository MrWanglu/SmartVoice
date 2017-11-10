package cn.fintecher.pangolin.report.mapper;

import cn.fintecher.pangolin.report.model.BackAmtModel;
import cn.fintecher.pangolin.report.model.CaseCountResult;
import cn.fintecher.pangolin.report.model.FollowCountModel;
import cn.fintecher.pangolin.report.model.WeekCountResult;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author : huyanmin
 * @Description :
 * @Date : 2017/11/07.
 */
public interface CollectPageMapper {

    /**
     * 本周流入案件总数（包含已结案的）
     *
     */
    Integer getCaseInfoWeekAllCount(String userId);

    /**
     * 本周已结案案件总数
     *
     */
    Integer getCaseInfoWeekClosedCount(String userId);

    /**
     * 本月流入案件总数（包含已结案的）
     *
     */
    Integer getCaseInfoMonthAllCount(String userId);

    /**
     * 本月完成已结案案件总数
     *
     */
    Integer getCaseInfoMonthClosedCount(String userId);

    /**
     * 本周需回款总案件个数
     *
     */
    Integer getWeekTotalBackCash(String userName);

    /**
     * 本周已回款案件个数
     *
     */
    Integer getWeekHadBackCash(String userName);

    /**
     * 本月需回款总金额
     *
     */
    Integer getMonthTotalBackCash(String userName);

    /**
     * 本月已回款总金额
     *
     */
    Integer getMonthHadBackCash(String userName);

    /**
     * 今日外呼
     * @param userName
     * @return
     */
    Integer getCalledDay(String userName);

    /**
     * 本周外呼
     * @param userName
     * @return
     */
    Integer getCalledWeek(String userName);

    /**
     * 本月外呼
     * @param userName
     * @return
     */
    Integer getCalledMonth(String userName);

    /**
     * 今日催计数
     * @param userName
     * @return
     */
    Integer getFollowDay(String userName);

    /**
     * 本周催计数
     * @param userName
     * @return
     */
    Integer getFollowWeek(String userName);

    /**
     * 本月催计数
     * @param userName
     * @return
     */
    Integer getFollowMonth(String userName);


    /**
     * 用户在线时长
     * @param userId
     * @return
     */
    Double getUserOnlineTime(String userId);

    /**
     * 今日流入案件数
     *
     */
    Integer getFlowInCaseToday(String userId);

    /**
     * 今日结清案件数
     *
     */
    Integer getFinishCaseToday(String userId);

    /**
     * 今日流出案件数
     *
     */
    Integer getFlowOutCaseToday(String userId);

    /**
     * 未催收案件数
     *
     */
    Integer getCaseInfoToFollowCount(String userId);

    /**
     * 催收中案件数
     *
     */
    Integer getCaseInfoFollowingCount(String userId);

    /**
     * 承诺还款案件数
     *
     */
    Integer getCaseInfoPromisedCount(String userName);

    /**
     * 回款金额排名
     *
     */
    List<BackAmtModel> getCaseInfoBackRank(String depCode);

    /**
     * 跟催量排名
     *
     */
    List<FollowCountModel> getCaseInfoFollowRank(String depName);
}
