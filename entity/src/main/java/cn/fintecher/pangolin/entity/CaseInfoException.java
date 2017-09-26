package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
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
    private BigDecimal perPayAmount = new BigDecimal(0);
    private BigDecimal contractAmount = new BigDecimal(0);
    private BigDecimal leftCapital = new BigDecimal(0);
    private BigDecimal leftInterest = new BigDecimal(0);
    private BigDecimal overdueAmount = new BigDecimal(0);
    private BigDecimal overdueCapital = new BigDecimal(0);
    private BigDecimal overDueInterest = new BigDecimal(0) ;
    private BigDecimal overdueFine = new BigDecimal(0);
    private BigDecimal overdueDelayFine = new BigDecimal(0);
    private BigDecimal otherAmt = new BigDecimal(0);
    private Date overDueDate;
    private Integer overDuePeriods;
    private Integer overDueDays;
    private BigDecimal hasPayAmount = new BigDecimal(0);
    private Integer hasPayPeriods;
    private Date latelyPayDate;
    private BigDecimal latelyPayAmount = new BigDecimal(0);
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
    private String contactCurrAddress2;
    private String contactName3;
    private String contactRelation3;
    private String contactWorkUnit3;
    private String contactUnitPhone3;
    private String contactPhone3;
    private String contactHomePhone3;
    private String contactCurrAddress3;
    private String contactName4;
    private String contactRelation4;
    private String contactWorkUnit4;
    private String contactUnitPhone4;
    private String contactPhone4;
    private String contactHomePhone4;
    private String contactCurrAddress4;
    @ApiModelProperty("联系人5姓名")
    private String contactName5;

    @ApiModelProperty("联系人5与客户关系")
    private String contactRelation5;

    @ApiModelProperty("联系人5工作单位")
    private String contactWorkUnit5;

    @ApiModelProperty("联系人5单位电话")
    private String contactUnitPhone5;

    @ApiModelProperty("联系人5手机号码")
    private String contactPhone5;

    @ApiModelProperty("联系人5住宅电话")
    private String contactHomePhone5;

    @ApiModelProperty("联系人5现居地址")
    private String contactCurrAddress5;

    @ApiModelProperty("联系人6姓名")
    private String contactName6;

    @ApiModelProperty("联系人6与客户关系")
    private String contactRelation6;

    @ApiModelProperty("联系人6工作单位")
    private String contactWorkUnit6;

    @ApiModelProperty("联系人6单位电话")
    private String contactUnitPhone6;

    @ApiModelProperty("联系人6手机号码")
    private String contactPhone6;

    @ApiModelProperty("联系人6住宅电话")
    private String contactHomePhone6;

    @ApiModelProperty("联系人6现居地址")
    private String contactCurrAddress6;

    @ApiModelProperty("联系人7姓名")
    private String contactName7;

    @ApiModelProperty("联系人7与客户关系")
    private String contactRelation7;

    @ApiModelProperty("联系人7工作单位")
    private String contactWorkUnit7;

    @ApiModelProperty("联系人7单位电话")
    private String contactUnitPhone7;

    @ApiModelProperty("联系人7手机号码")
    private String contactPhone7;

    @ApiModelProperty("联系人7住宅电话")
    private String contactHomePhone7;

    @ApiModelProperty("联系人7现居地址")
    private String contactCurrAddress7;

    @ApiModelProperty("联系人8姓名")
    private String contactName8;

    @ApiModelProperty("联系人8与客户关系")
    private String contactRelation8;

    @ApiModelProperty("联系人8工作单位")
    private String contactWorkUnit8;

    @ApiModelProperty("联系人8单位电话")
    private String contactUnitPhone8;

    @ApiModelProperty("联系人8手机号码")
    private String contactPhone8;

    @ApiModelProperty("联系人8住宅电话")
    private String contactHomePhone8;

    @ApiModelProperty("联系人8现居地址")
    private String contactCurrAddress8;

    @ApiModelProperty("联系人9姓名")
    private String contactName9;

    @ApiModelProperty("联系人9与客户关系")
    private String contactRelation9;

    @ApiModelProperty("联系人9工作单位")
    private String contactWorkUnit9;

    @ApiModelProperty("联系人9单位电话")
    private String contactUnitPhone9;

    @ApiModelProperty("联系人9手机号码")
    private String contactPhone9;

    @ApiModelProperty("联系人9住宅电话")
    private String contactHomePhone9;

    @ApiModelProperty("联系人9现居地址")
    private String contactCurrAddress9;

    @ApiModelProperty("联系人10姓名")
    private String contactName10;

    @ApiModelProperty("联系人10与客户关系")
    private String contactRelation10;

    @ApiModelProperty("联系人10工作单位")
    private String contactWorkUnit10;

    @ApiModelProperty("联系人10单位电话")
    private String contactUnitPhone10;

    @ApiModelProperty("联系人10手机号码")
    private String contactPhone10;

    @ApiModelProperty("联系人10住宅电话")
    private String contactHomePhone10;

    @ApiModelProperty("联系人10现居地址")
    private String contactCurrAddress10;

    @ApiModelProperty("联系人11姓名")
    private String contactName11;

    @ApiModelProperty("联系人11与客户关系")
    private String contactRelation11;

    @ApiModelProperty("联系人11工作单位")
    private String contactWorkUnit11;

    @ApiModelProperty("联系人11单位电话")
    private String contactUnitPhone11;

    @ApiModelProperty("联系人11手机号码")
    private String contactPhone11;

    @ApiModelProperty("联系人11住宅电话")
    private String contactHomePhone11;

    @ApiModelProperty("联系人11现居地址")
    private String contactCurrAddress11;

    @ApiModelProperty("联系人12姓名")
    private String contactName12;

    @ApiModelProperty("联系人12与客户关系")
    private String contactRelation12;

    @ApiModelProperty("联系人12工作单位")
    private String contactWorkUnit12;

    @ApiModelProperty("联系人12单位电话")
    private String contactUnitPhone12;

    @ApiModelProperty("联系人12手机号码")
    private String contactPhone12;

    @ApiModelProperty("联系人12住宅电话")
    private String contactHomePhone12;

    @ApiModelProperty("联系人12现居地址")
    private String contactCurrAddress12;

    @ApiModelProperty("联系人13姓名")
    private String contactName13;

    @ApiModelProperty("联系人13与客户关系")
    private String contactRelation13;

    @ApiModelProperty("联系人13工作单位")
    private String contactWorkUnit13;

    @ApiModelProperty("联系人13单位电话")
    private String contactUnitPhone13;

    @ApiModelProperty("联系人13手机号码")
    private String contactPhone13;

    @ApiModelProperty("联系人13住宅电话")
    private String contactHomePhone13;

    @ApiModelProperty("联系人13现居地址")
    private String contactCurrAddress13;

    @ApiModelProperty("联系人14姓名")
    private String contactName14;

    @ApiModelProperty("联系人14与客户关系")
    private String contactRelation14;

    @ApiModelProperty("联系人14工作单位")
    private String contactWorkUnit14;

    @ApiModelProperty("联系人14单位电话")
    private String contactUnitPhone14;

    @ApiModelProperty("联系人14手机号码")
    private String contactPhone14;

    @ApiModelProperty("联系人14住宅电话")
    private String contactHomePhone14;

    @ApiModelProperty("联系人14现居地址")
    private String contactCurrAddress14;

    @ApiModelProperty("联系人15姓名")
    private String contactName15;

    @ApiModelProperty("联系人15与客户关系")
    private String contactRelation15;

    @ApiModelProperty("联系人15工作单位")
    private String contactWorkUnit15;

    @ApiModelProperty("联系人15单位电话")
    private String contactUnitPhone15;

    @ApiModelProperty("联系人15手机号码")
    private String contactPhone15;

    @ApiModelProperty("联系人15住宅电话")
    private String contactHomePhone15;

    @ApiModelProperty("联系人15现居地址")
    private String contactCurrAddress15;

    private String memo;
    private BigDecimal commissionRate = new BigDecimal(0);
    private BigDecimal overdueManageFee = new BigDecimal(0);
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
    @ApiModelProperty("客户号")
    private String personalNumber;

    @ApiModelProperty("账户号")
    private String accountNumber;

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
