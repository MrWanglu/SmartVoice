package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 还款信息展示模型
 * @Date : 9:30 2017/7/29
 */

@Data
public class PaymentModel {
    private String casePayApplyId; //还款审批ID
    private String caseId; //案件ID
    private String name; //客户姓名
    private String idCardNum; //客户身份证号
    private String phone; //客户手机号
    private BigDecimal contractAmount; //合同金额
    private Integer periods; //期数
    private Integer overdueDays; //逾期天数
    private BigDecimal overdueAmount; //逾期应还总金额
    private BigDecimal overdueCapital; //逾期应支付本金
    private BigDecimal overdueInterest; //应支付利息
    private BigDecimal overdueFine; //应支付逾期罚息
    private BigDecimal overdueDelayFine; //应支付逾期还款合同违约金
    private BigDecimal overdueManageFee; //应支付分期服务费
    private BigDecimal derateAmt = new BigDecimal(0); //减免费用
    private String derateRemark; //减免备注
    private String batchNumber; //批次号
    private String principalName; //委托方
    private BigDecimal paymentAmt = new BigDecimal(0); //还款金额
    private String paymentRemark; //还款备注
    private Integer payType; //还款类型
    private Integer payWay; //还款方式
    private Date applyDate; //申请日期
    private Integer fileCount; //附件个数

}