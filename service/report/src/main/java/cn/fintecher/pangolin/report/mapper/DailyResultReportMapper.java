package cn.fintecher.pangolin.report.mapper;

import cn.fintecher.pangolin.report.entity.DailyResultReport;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.BaseMapper;

import java.util.Date;
import java.util.List;

/**
 * @author : xiaqun
 * @Description : 催收员每日催收结果报表
 * @Date : 10:03 2017/8/4
 */

public interface DailyResultReportMapper extends BaseMapper<DailyResultReport> {
    /**
     * @Description 查询催收员每日催收结果实时报表
     */
    List<DailyResultReport> getRealTimeReport(@Param("deptCode") String deptCode,
                                              @Param("code") String code,
                                              @Param("realName") String realName,
                                              @Param("companyCode") String companyCode);

    /**
     * @Description 生成催收员每日催收结果历史报表
     */
    List<DailyResultReport> saveHistoryReport(@Param("historyDate")Date historyDate);

    /**
     * @Description 查询催收员每日催收结果历史报表
     */
    List<DailyResultReport> getHistoryReport(@Param("startDate") Date startDate,
                                             @Param("endDate") Date endDate,
                                             @Param("deptCode") String deptCode,
                                             @Param("code") String code,
                                             @Param("realName") String realName,
                                             @Param("companyCode") String companyCode);
}
