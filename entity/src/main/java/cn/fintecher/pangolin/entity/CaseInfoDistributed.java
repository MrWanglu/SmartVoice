package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 案件待分配池
 */
@Entity
@Table(name = "case_info_distributed")
@Data
public class CaseInfoDistributed extends BaseEntity {
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
    @ApiModelProperty("委案日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date delegationDate;
    @ApiModelProperty("结案日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date closeDate;
    private BigDecimal commissionRate = new BigDecimal(0);
    @ApiModelProperty("案件手数")
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
    @ApiModelProperty("导入案件时Excel中的备注")
    private String memo;
    @ApiModelProperty("首次还款日期")
    private Date firstPayDate;
    @ApiModelProperty("账龄")
    private String accountAge;
    @ApiModelProperty("案件到期回收方式：0-自动回收，1-手动回收")
    private Integer recoverWay;
    @ApiModelProperty("案件到期回收说明")
    private String recoverMemo;
    @ApiModelProperty("回收标志：0-未回收，1-已回收")
    private Integer recoverRemark;



    @ManyToOne
    @JoinColumn(name = "personal_id")
    private Personal personalInfo;

    @ManyToOne
    @JoinColumn(name = "depart_id")
    private Department department;
    @ManyToOne
    @JoinColumn(name = "area_id")
    private AreaCode area;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "principal_id")
    private Principal principalId;

    @ManyToOne
    @JoinColumn(name = "operator")
    private User operator;


}
