package cn.fintecher.pangolin.report.mapper;

import cn.fintecher.pangolin.report.entity.SmsReport;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.BaseMapper;

import java.util.Date;
import java.util.List;

/**
 * @author : xiaqun
 * @Description : 发短信统计报表
 * @Date : 9:22 2017/9/4
 */

public interface SmsReportMapper extends BaseMapper<SmsReport> {
    /**
     * @Description 查询实时报表
     */
    List<SmsReport> getRealtimeReport(@Param("deptCode") String deptCode,
                                      @Param("code") String code,
                                      @Param("realName") String realName,
                                      @Param("companyCode") String companyCode);

    /**
     * @Description 生成历史报表
     */
    List<SmsReport> saveHistoryReport(@Param("historyDate") Date historyDate);

    /**
     * @Description 查询历史报表
     */
    List<SmsReport> getHistoryReport(@Param("deptCode") String deptCode,
                                     @Param("startDate") Date startDate,
                                     @Param("endDate") Date endDate,
                                     @Param("code") String code,
                                     @Param("realName") String realName,
                                     @Param("companyCode") String companyCode);
}
