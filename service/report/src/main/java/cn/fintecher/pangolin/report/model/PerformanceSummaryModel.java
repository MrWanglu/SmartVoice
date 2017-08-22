package cn.fintecher.pangolin.report.model;

import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 催收员业绩排名小组汇总报表展示模型
 * @Date : 11:45 2017/8/22
 */

@Data
public class PerformanceSummaryModel {
    private String deptCode; //部门code码
    private String deptName; //部门名称
    private List<PerformanceSummarySecModel> performanceSummarySecModels; //催收员业绩排名报表二级模型集合
}