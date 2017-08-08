package cn.fintecher.pangolin.report.model;

import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 每日催收过程报表展示模型
 * @Date : 17:15 2017/8/3
 */

@Data
public class DailyProcessModel {
    private String deptCode; //部门code码
    private String deptName; //部门名称
    private List<DailyProcessSecModel> dailyProcessSecModels; //催收员每日催收过程二级模型集合
}