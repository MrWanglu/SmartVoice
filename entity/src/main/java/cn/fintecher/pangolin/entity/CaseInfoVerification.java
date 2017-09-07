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
 * @author yuanyanting
 * @version Id:CaseInfoVerificationModel.java,v 0.1 2017/8/31 15:52 yuanyanting Exp $$
 */
@Entity
@Table(name = "case_info_verification")
@Data
public class CaseInfoVerification extends BaseEntity{
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
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date delegationDate;
    @ApiModelProperty("结案日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
    @JoinColumn(name = "lately_collector")
    private User latelyCollector;
    @ManyToOne
    @JoinColumn(name = "current_collector")
    private User currentCollector;
    @ManyToOne
    @JoinColumn(name = "assist_collector")
    private User assistCollector;

    @ManyToOne
    @JoinColumn(name = "principal_id")
    private Principal principalId;

    @ManyToOne
    @JoinColumn(name = "operator")
    private User operator;

}