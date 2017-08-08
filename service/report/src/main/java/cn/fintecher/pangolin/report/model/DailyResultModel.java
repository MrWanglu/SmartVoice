package cn.fintecher.pangolin.report.model;

import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 催收员每日催收结果报表展示模型
 * @Date : 9:52 2017/8/4
 */

@Data
public class DailyResultModel {
    private String deptCode; //部门code码
    private String deptName; //部门名称
    private List<DailyResultSecModel> dailyResultSecModels; //催收员每日催收结果报表二级模型集合
}