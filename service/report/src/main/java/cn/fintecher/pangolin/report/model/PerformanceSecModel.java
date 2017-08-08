package cn.fintecher.pangolin.report.model;

import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 催收员业绩进展二级模型
 * @Date : 11:44 2017/8/3
 */

@Data
public class PerformanceSecModel {
    private String groupCode; //组别code码
    private String groupName; //组别名称
    private List<PerformanceBasisModel> performanceBasisModels; //业绩进展基础模型
}