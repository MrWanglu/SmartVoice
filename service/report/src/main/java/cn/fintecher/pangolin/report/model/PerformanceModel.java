package cn.fintecher.pangolin.report.model;

import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 催收员业绩进展模型
 * @Date : 11:43 2017/8/3
 */

@Data
public class PerformanceModel {
    private String deptCode; //部门code码
    private String deptName; //部门名称
    private List<PerformanceSecModel> performanceSecModels; //业绩进展二级模型集合
}