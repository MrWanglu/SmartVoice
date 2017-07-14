package cn.fintecher.pangolin.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by ChenChang on 2017/7/10.
 */
@Entity
@Table(name = "case_info")
@Data
public class CaseInfo extends BaseEntity {
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
    private Date perDueDate;
    private BigDecimal perPayAmount;
    private Integer overduePeriods;
    private Integer overdueDays;
    private BigDecimal hasPayAmount;
    private Integer hasPayPeriods;
    private Date latelyPayDate;
    private BigDecimal latelyPayAmount;
    private Integer assistFlag;
    private Integer assistStatus;
    private Integer assistWay;
    private Integer holdDays;
    private Integer leftDays;
    private Integer caseType;
    private Integer leaveCaseFlag;
    private Date leaveDate;
    private Integer hasLeaveDays;
    private Integer followUpNum;
    private Date caseFollowInTime;
    private String payStatus;
    private String orderId;
    private Integer collectionStatus;
    private Date delegationDate;
    private Date closeDate;
    private BigDecimal commissionRate;
    private Integer handNumber;
    private Date loanDate;
    private BigDecimal overdueManageFee;
    private Integer handUpFlag;
    private BigDecimal derateAmt;
    private BigDecimal realPayAmount;
    private BigDecimal earlySettleAmt;
    private BigDecimal earlyRealSettleAmt;
    private BigDecimal earlyDerateAmt;
    private BigDecimal otherAmt;
    private BigDecimal score;
    private String companyCode;
    private String operator;
    private Date operatorTime;
    private Integer caseMark;
    private String principalId;
    @ManyToOne
    @JoinColumn(name = "personal_id")
    private PersonalInfo personalInfo;

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

}
