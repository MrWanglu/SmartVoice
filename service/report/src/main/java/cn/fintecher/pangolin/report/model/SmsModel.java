package cn.fintecher.pangolin.report.model;

import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 短信发送统计展示模型
 * @Date : 10:41 2017/9/4
 */

@Data
public class SmsModel {
    private String deptCode; //部门code码
    private String deptName; //部门名称
    List<SmsSecModel> smsSecModels; //二级模型集合
}