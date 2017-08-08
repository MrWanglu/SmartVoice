package cn.fintecher.pangolin.report.model;

import cn.fintecher.pangolin.report.entity.DailyProcessReport;
import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 催收员每日催收过程三级模型
 * @Date : 17:16 2017/8/3
 */

@Data
public class DailyProcessThiModel {
    private String deptCode; //部门code码
    private String userName; //用户名
    private List<DailyProcessReport> dailyProcessReports; //催收员每日催收过程报表集合
}