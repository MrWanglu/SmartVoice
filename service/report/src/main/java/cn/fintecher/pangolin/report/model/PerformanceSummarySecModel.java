package cn.fintecher.pangolin.report.model;

import cn.fintecher.pangolin.report.entity.PerformanceRankingReport;
import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 催收员业绩排名小组汇总报表二级模型
 * @Date : 11:46 2017/8/22
 */

@Data
public class PerformanceSummarySecModel {
    private String groupCode; //组别code码
    private String groupName; //组别名称
    private List<PerformanceRankingReport> performanceRankingReports; //催收员业绩排名报表集合
}