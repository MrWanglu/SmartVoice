package cn.fintecher.pangolin.report.mapper;

import cn.fintecher.pangolin.report.model.PerformanceBasisModel;
import cn.fintecher.pangolin.report.util.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 业绩进展报表
 * @Date : 15:32 2017/8/3
 */

public interface PerformanceReportMapper extends MyMapper<PerformanceBasisModel> {
    /**
     * @Description 查询催收员业绩进展报表
     */
    List<PerformanceBasisModel> getPerformanceReport(@Param("deptCode") String deptCode,
                                                     @Param("code") String code,
                                                     @Param("realName") String realName,
                                                     @Param("companyCode") String companyCode);
}
