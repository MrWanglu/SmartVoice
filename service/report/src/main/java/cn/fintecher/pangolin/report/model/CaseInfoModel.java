package cn.fintecher.pangolin.report.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 展示用案件模型
 * @Date : 10:19 2017/11/1
 */

@Data
public class CaseInfoModel {
    private String caseId; //案件ID
    private String caseNumber; //案件编号
    private String batchNumber; //批次号
    private String personalName; //客户姓名
    private String mobileNo; //手机号
    private String idCard; //身份证号
    private String overdueAmount; //案件金额
    private String payStatus; //还款状态
    private Integer overdueDays; //逾期天数
    private String principalName; //委托方名称
    private String collectorName; //催收员姓名
    private Integer collectionStatus; //催收状态
    private Date followupTime; //跟进时间
    private Date caseFollowInTime; //案件流入日期
    private Integer followupBack; //催收反馈
    private Integer assistWay; //协催方式
    private Integer holdDays; //持案天数
    private Integer leftDays; //剩余天数
    private Integer leaveCaseFlag; //留案标识
    private String deptCode; //部门code码
    private Integer collectionType; //催收类型
    private Integer caseMark; //案件标记
    private String personalId; //客户信息ID
    private BigDecimal promiseAmt; //承诺还款金额
    private Date promiseTime; //承诺还款日期
}