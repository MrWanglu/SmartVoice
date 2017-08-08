package cn.fintecher.pangolin.report.model;

import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 催收员每日催收过程二级模型
 * @Date : 17:16 2017/8/3
 */

@Data
public class DailyProcessSecModel {
    private String groupCode; //组别code码
    private String groupName; //组别名称
    private List<DailyProcessThiModel> dailyProcessThiModels; //催收员每日催收过程三级模型集合
}