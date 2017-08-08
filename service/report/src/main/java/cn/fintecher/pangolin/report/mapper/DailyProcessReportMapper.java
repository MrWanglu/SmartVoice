package cn.fintecher.pangolin.report.mapper;

import cn.fintecher.pangolin.report.entity.DailyProcessReport;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.BaseMapper;

import java.util.Date;
import java.util.List;

/**
 * @author : xiaqun
 * @Description : 催收员每日催收过程报表
 * @Date : 19:11 2017/8/3
 */

public interface DailyProcessReportMapper extends BaseMapper<DailyProcessReport> {
    /**
     * @Description 查询每日催收过程实时报表
     */
    List<DailyProcessReport> getRealTimeReport(@Param("deptCode") String deptCode,
                                               @Param("code") String code,
                                               @Param("realName") String realName,
                                               @Param("companyCode") String companyCode);

    /**
     * @Description 生成每日催收过程历史报表
     */
    List<DailyProcessReport> saveHistoryReport(@Param("historyDate") Date historyDate);

    /**
     * @Description 查询每日催收过程历史报表
     */
    List<DailyProcessReport> getHistoryReport(@Param("startDate") Date startDate,
                                              @Param("endDate") Date endDate,
                                              @Param("deptCode") String deptCode,
                                              @Param("code") String code,
                                              @Param("realName") String realName,
                                              @Param("companyCode") String companyCode);
}
