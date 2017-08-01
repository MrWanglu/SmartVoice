package cn.fintecher.pangolin.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 案件异常池
 */
@Entity
@Table(name = "case_info_exception")
@Data
public class CaseInfoException extends BaseEntity {
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
    private BigDecimal leftCapital; //剩余本金
    private BigDecimal leftInterest; //剩余利息
    private String endRemark; //结案说明

    private Date operatorTime;
    private Integer caseMark;

    private String distributeRepeat;
    private String assignedRepeat;
    private Integer repeatStatus;


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

    /**
     * 异常数据处理类型
     */
    public enum RepeatStatusEnum{
        PENDING(182,"待处理"),UPDATE(183,"更新"),DELETE(184,"删除"),ADD(185,"新增");
        private Integer value;
        private String remark;

        RepeatStatusEnum(Integer value, String remark) {
            this.value = value;
            this.remark = remark;
        }

        public Integer getValue() {
            return value;
        }

        public String getRemark() {
            return remark;
        }
    }

}
