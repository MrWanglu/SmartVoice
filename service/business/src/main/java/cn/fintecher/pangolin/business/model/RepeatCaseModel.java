package cn.fintecher.pangolin.business.model;

import cn.fintecher.pangolin.entity.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
    private BigDecimal contractAmount;
    private BigDecimal overdueAmount;
    private BigDecimal overdueCapital;
    private BigDecimal overdueInterest;
    private BigDecimal overdueFine;
    private BigDecimal overdueDelayFine;
    private Integer periods;
    private Date perDueDate; //逾期日期
    private BigDecimal perPayAmount;
    private Integer overduePeriods;
    private Integer overdueDays;
    private Date overDueDate;
    private BigDecimal hasPayAmount = new BigDecimal(0); //逾期已还款金额
    private Integer hasPayPeriods;
    private Date latelyPayDate;
    private BigDecimal latelyPayAmount;
    @ApiModelProperty("协催标识：0-未协催，1-协催")
    private Integer assistFlag;
    private Integer assistStatus;
    private Integer assistWay;
    private Integer holdDays;
    private Integer leftDays;
    private Integer caseType;
    @ApiModelProperty("0-未留案，1-留案")
    private Integer leaveCaseFlag = 0;
    private Date leaveDate;
    private Integer hasLeaveDays;
    private Integer followUpNum = 0;
    private Date caseFollowInTime;
    private String payStatus;
    private String orderId;
    private Integer collectionStatus;
    @ApiModelProperty("委案日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date delegationDate;
    @ApiModelProperty("结案日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date closeDate;
    private BigDecimal commissionRate;
    private Integer handNumber;
    private Date loanDate;
    private BigDecimal overdueManageFee;
    private Integer handUpFlag;
    private BigDecimal derateAmt = new BigDecimal(0); //逾期减免金额
    private BigDecimal realPayAmount = new BigDecimal(0); //逾期实际还款金额
    private BigDecimal earlySettleAmt = new BigDecimal(0); //提前结清已还款金额
    private BigDecimal earlyRealSettleAmt = new BigDecimal(0); //提前结清实际还款金额
    private BigDecimal earlyDerateAmt = new BigDecimal(0); //提前结清减免金额
    private BigDecimal otherAmt;
    private BigDecimal score;
    private String companyCode;
    private BigDecimal leftCapital; //剩余本金
    private BigDecimal leftInterest; //剩余利息
    private String endRemark; //结案说明
    private Integer endType; //结案方式
    private Date followupTime; //最新跟进时间
    private Integer followupBack; //催收反馈
    private BigDecimal promiseAmt; //承诺还款金额
    private Date promiseTime; //承诺还款日期
    private BigDecimal creditAmount; //授信金额
    private Integer circulationStatus; //流转审批状态

    private Date operatorTime;
    private Integer caseMark = 126;
    @ApiModelProperty("备注")
    private String memo;
    @ApiModelProperty("首次还款日期")
    private Date firstPayDate;
    @ApiModelProperty("账龄")
    private String accountAge;
    @ApiModelProperty("案件到期回收方式：0-自动回收，1-手动回收")
    private Integer recoverWay;
    @ApiModelProperty("回收标志：0-未回收，1-已回收")
    private Integer recoverRemark;
    @ApiModelProperty("内催 225 委外 226 司法 227 核销 228")
    private Integer casePoolType;
    private Personal personalInfo;
    private Department department;
    private AreaCode area;
    private Product product;
    private User latelyCollector;
    private User currentCollector;
    private User assistCollector;
    private Principal principalId;
    private User operator;
    private Integer repeatType;
}
