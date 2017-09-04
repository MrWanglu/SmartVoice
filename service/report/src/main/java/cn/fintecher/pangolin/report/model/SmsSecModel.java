package cn.fintecher.pangolin.report.model;

import cn.fintecher.pangolin.report.entity.SmsReport;
import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 短信发送统计二级模型
 * @Date : 10:42 2017/9/4
 */

@Data
public class SmsSecModel {
    private String groupCode; //组别code码
    private String groupName; //组别名称
    private List<SmsReport> smsReports; //短息发送统计报表集合
}