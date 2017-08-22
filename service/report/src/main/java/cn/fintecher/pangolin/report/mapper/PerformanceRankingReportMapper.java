package cn.fintecher.pangolin.report.mapper;

import cn.fintecher.pangolin.report.entity.PerformanceRankingReport;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.BaseMapper;

import java.util.Date;
import java.util.List;

/**
 * @author : xiaqun
 * @Description : 催收员业绩排名报表
 * @Date : 10:51 2017/8/21
 */

public interface PerformanceRankingReportMapper extends BaseMapper<PerformanceRankingReport> {
    /**
     * @Description 查询催收员业绩排名实时报表
     */
    List<PerformanceRankingReport> getRealtimeReport(@Param("startDate") Date startDate,
                                                     @Param("endDate") Date endDate,
                                                     @Param("companyCode") String companyCode,
                                                     @Param("deptCode") String deptCode,
                                                     @Param("realName") String realName,
                                                     @Param("code") String code);

    /**
     * @Description 生成历史报表
     */
    List<PerformanceRankingReport> saveHistoryReport(@Param("startDate") Date startDate,
                                                     @Param("endDate") Date endDate,
                                                     @Param("historyDate") Date historyDate);

    /**
     * @Description 查询历史报表
     */
    List<PerformanceRankingReport> getHistoryReport(@Param("date") String date,
                                                    @Param("companyCode") String companyCode,
                                                    @Param("deptCode") String deptCode,
                                                    @Param("realName") String realName,
                                                    @Param("code") String code);

    /**
     * @Description 判断是否是管理者
     */
    Integer getManage(@Param("userName") String userName);
}
