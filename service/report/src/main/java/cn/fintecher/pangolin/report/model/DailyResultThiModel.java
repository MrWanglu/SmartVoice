package cn.fintecher.pangolin.report.model;

import cn.fintecher.pangolin.report.entity.DailyResultReport;
import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 催收押un每日催收结果报表三级模型
 * @Date : 9:54 2017/8/4
 */

@Data
public class DailyResultThiModel {
    private String deptCode; //部门code码
    private String userName; //用户名
    private List<DailyResultReport> dailyResultReports; //催收员每日催收结果报表集合
}