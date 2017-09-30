package cn.fintecher.pangolin.business.model;

import cn.fintecher.pangolin.entity.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Administrator on 2017/9/29.
 */
@Data
public class RepeatCaseModel {
    private String id;
    private String batchNumber;
    private String caseNumber;
    private Integer collectionType;
    private String contractNumber;
    private BigDecimal contractAmount = new BigDecimal(0);
    private BigDecimal overdueAmount = new BigDecimal(0);
    private BigDecimal overdueCapital = new BigDecimal(0);
    private BigDecimal overdueInterest = new BigDecimal(0);
    private BigDecimal overdueFine = new BigDecimal(0);
    private BigDecimal overdueDelayFine = new BigDecimal(0);
    private Integer periods;
    private Date perDueDate;
    private BigDecimal perPayAmount = new BigDecimal(0);
    private Integer overduePeriods;
    private Integer overdueDays;
    private BigDecimal hasPayAmount = new BigDecimal(0);
    private Integer hasPayPeriods;
    private Date latelyPayDate;
    private BigDecimal latelyPayAmount = new BigDecimal(0);
    private Integer assistFlag;
    private Integer assistStatus;
    private Integer assistWay;
    private Integer holdDays = 0;
    private Integer leftDays;
    private Integer caseType;
    private Integer leaveCaseFlag;
    private Date leaveDate;
    private Integer hasLeaveDays;
    private Integer followUpNum = 0;
    private Date caseFollowInTime;
    private String payStatus;
    private String orderId;
    private Integer collectionStatus;
    private Date delegationDate;
    private Date closeDate;
    private BigDecimal commissionRate = new BigDecimal(0);
    private Integer handNumber;
    private Date loanDate;
    private BigDecimal overdueManageFee = new BigDecimal(0);
    private Integer handUpFlag;
    private BigDecimal derateAmt = new BigDecimal(0);
    private BigDecimal realPayAmount = new BigDecimal(0);
    private BigDecimal earlySettleAmt = new BigDecimal(0);
    private BigDecimal earlyRealSettleAmt = new BigDecimal(0);
    private BigDecimal earlyDerateAmt = new BigDecimal(0);
    private BigDecimal otherAmt = new BigDecimal(0);
    private BigDecimal score = new BigDecimal(0);
    private String companyCode;
    private BigDecimal leftCapital = new BigDecimal(0); //剩余本金
    private BigDecimal leftInterest = new BigDecimal(0); //剩余利息
    private String endRemark; //结案说明
    private Date overDueDate; //逾期日期
    private Date operatorTime;
    private Integer caseMark;
    private String memo;
    private Date firstPayDate;
    private String accountAge;
    private Integer recoverWay;
    private Integer recoverRemark;
    private Personal personalInfo;
    private Department department;
    private AreaCode area;
    private Product product;
    private Principal principalId;
    private User operator;


}
