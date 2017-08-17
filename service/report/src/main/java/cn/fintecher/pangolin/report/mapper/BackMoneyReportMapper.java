package cn.fintecher.pangolin.report.mapper;

import cn.fintecher.pangolin.report.entity.BackMoneyReport;
import cn.fintecher.pangolin.report.model.DeptModel;
import cn.fintecher.pangolin.report.util.MyMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @author : xiaqun
 * @Description : 回款报表
 * @Date : 19:37 2017/8/1
 */

public interface BackMoneyReportMapper extends MyMapper<BackMoneyReport> {
    /**
     * @Description 查询实时回款报表
     */
    List<BackMoneyReport> getRealTimeReport(@Param("deptCode") String deptCode,
                                            @Param("code") String code,
                                            @Param("realName") String realName,
                                            @Param("companyCode") String companyCode);

    /**
     * @Description 生成历史报表
     */
    List<BackMoneyReport> saveHistoryReport(@Param("historyDate") Date historyDate);

    /**
     * @Description 查询用户的部门code码和名称
     */
    DeptModel getDept(@Param("userName") String userName);

    /**
     * @Descritpion 查询历史报表
     */
    List<BackMoneyReport> getHistoryReport(@Param("deptCode") String deptCode,
                                           @Param("startDate") Date startDate,
                                           @Param("endDate") Date endDate,
                                           @Param("code") String code,
                                           @Param("realName") String realName,
                                           @Param("companyCode") String companyCode);
}
