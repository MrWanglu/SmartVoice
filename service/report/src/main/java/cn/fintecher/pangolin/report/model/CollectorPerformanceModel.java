package cn.fintecher.pangolin.report.model;

import cn.fintecher.pangolin.report.entity.PerformanceRankingReport;
import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 催收员业绩排名报表展示模型
 * @Date : 19:35 2017/8/21
 */

@Data
public class CollectorPerformanceModel {
    private String deptCode; //部门ID
    private String deptName; //部门名称
    private List<PerformanceRankingReport> performanceRankingReports; //催收员业绩排名报表集合
}