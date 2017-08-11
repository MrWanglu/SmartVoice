package cn.fintecher.pangolin.entity;

import lombok.Data;

import javax.persistence.Entity;
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
    private String personalName;
    private String idCard;
    private String mobileNo;
    private String productSeriesName;
    private String productName;
    private String contractNumber;
    private Date loanDate;
    private Integer periods;
    private Date perDueDate;
    private BigDecimal perPayAmount;
    private BigDecimal contractAmount;
    private BigDecimal leftCapital;
    private BigDecimal leftInterest;
    private BigDecimal overdueAmount;
    private BigDecimal overdueCapital;
    private BigDecimal overDueInterest;
    private BigDecimal overdueFine;
    private BigDecimal overdueDelayFine;
    private BigDecimal otherAmt;
    private Date overDueDate;
    private Integer overDuePeriods;
    private Integer overDueDays;
    private BigDecimal hasPayAmount;
    private Integer hasPayPeriods;
    private Date latelyPayDate;
    private BigDecimal latelyPayAmount;
    private String depositBank;
    private String cardNumber;
    private String province;
    private String city;
    private String homeAddress;
    private String homePhone;
    private String idCardAddress;
    private String companyName;
    private String companyAddr;
    private String companyPhone;
    private String contactName1;
    private String contactRelation1;
    private String contactWorkUnit1;
    private String contactUnitPhone1;
    private String contactPhone1;
    private String contactHomePhone1;
    private String contactCurrAddress1;
    private String contactName2;
    private String contactRelation2;
    private String contactWorkUnit2;
    private String contactUnitPhone2;
    private String contactPhone2;
    private String contactHomePhone2;
    private String contactName3;
    private String contactRelation3;
    private String contactWorkUnit3;
    private String contactUnitPhone3;
    private String contactPhone3;
    private String contactHomePhone3;
    private String contactName4;
    private String contactRelation4;
    private String contactWorkUnit4;
    private String contactUnitPhone4;
    private String contactPhone4;
    private String contactHomePhone4;
    private String memo;
    private BigDecimal commissionRate;
    private BigDecimal overdueManageFee;
    private String paymentStatus;
    private String batchNumber;
    private String prinCode;
    private String prinName;
    private Date delegationDate;
    private Date closeDate;
    private String operator;
    private Date operatorTime;
    private String operatorName;
    private Integer dataSources;
    private Integer caseHandNum;
    private String companyCode;
    private String caseNumber;
    private String distributeRepeat;
    private String assignedRepeat;
    private Integer repeatStatus;

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
