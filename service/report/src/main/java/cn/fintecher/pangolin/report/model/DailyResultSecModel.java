package cn.fintecher.pangolin.report.model;

import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 催收员每日催收结果报表二级模型
 * @Date : 9:53 2017/8/4
 */

@Data
public class DailyResultSecModel {
    private String groupCode; //组别code码
    private String groupName; //组别名称
    private List<DailyResultThiModel> dailyResultThiModels; //催收员每日催收结果报表三级模型集合
}