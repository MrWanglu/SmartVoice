package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author yuanyanting
 * @version Id:CaseInfoVerificationModel.java,v 0.1 2017/8/31 15:52 yuanyanting Exp $$
 */
@Entity
@Table(name = "case_info_verification_apply")
@Data
public class CaseInfoVerificationApply extends BaseEntity {

    @ApiModelProperty(notes = "公司code码")
    private String companyCode;

    @ApiModelProperty(notes = "审批人")
    private String operator;

    @ApiModelProperty(notes = "案件Id")
    private String caseId;

    @ApiModelProperty(notes = "批次号")
    private String batchNumber;

    @ApiModelProperty(notes = "案件编号")
    private String caseNumber;

    @ApiModelProperty(notes = "部门code")
    private String deptCode;

    @ApiModelProperty(notes = "操作时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operatorTime;

    @ApiModelProperty(notes = "申请日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date applicationDate;

    @ApiModelProperty(notes = "申请人")
    private String applicant;

    @ApiModelProperty(notes = "申请理由")
    private String applicationReason;

    @ApiModelProperty(notes = "审批状态")
    private Integer approvalStatus;

    @ApiModelProperty(notes = "审批意见")
    private String approvalOpinion;

    @ApiModelProperty(notes = "审批结果")
    private Integer approvalResult;

    @ApiModelProperty(notes = "逾期金额")
    private BigDecimal overdueAmount;

    @ApiModelProperty(notes = "还款状态")
    private String payStatus;

    @ApiModelProperty(notes = "逾期天数")
    private Integer overdueDays;

    @ApiModelProperty(notes = "委托方名称")
    private String principalName;

    @ApiModelProperty(notes = "客户姓名")
    private String personalName;

    @ApiModelProperty(notes = "手机号码")
    private String mobileNo;

    @ApiModelProperty(notes = "身份证号")
    private String IdCard;

    @ApiModelProperty(notes = "省份")
    private Integer province;

    @ApiModelProperty(notes = "城市")
    private Integer city;

    @ApiModelProperty(notes = "合同编号")
    private String contractNumber;

    @ApiModelProperty(notes = "合同金额")
    private BigDecimal contractAmount;

    @ApiModelProperty(notes = "逾期本金")
    private BigDecimal overdueCapital;

    @ApiModelProperty(notes = "逾期利息")
    private BigDecimal overdueInterest;

    @ApiModelProperty(notes = "逾期罚息")
    private BigDecimal overdueFine;

    @ApiModelProperty(notes = "逾期滞纳金")
    private BigDecimal overdueDelayFine;

    @ApiModelProperty(notes = "已还款金额")
    private BigDecimal hasPayAmount;

    @ApiModelProperty(notes = "已还款期数")
    private Integer hasPayPeriods;

    @ApiModelProperty(notes = "最近还款日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date latelyPayDate;

    @ApiModelProperty(notes = "最近还款金额")
    private BigDecimal latelyPayAmount;

    @ApiModelProperty(notes = "还款期数")
    private Integer periods;

    @ApiModelProperty(notes = "佣金比例")
    private BigDecimal commissionRate;

    @ApiModelProperty(notes = "内催 225 委外 226 司法 227 核销 228")
    private Integer source;

    /**
     * 审批状态的枚举类
     */
    public enum ApprovalStatus {
        // 待审批
        approval_pending(222,"待审批"),

        // 通过
        approval_approve(220,"审批通过"),

        // 拒绝
        approval_disapprove(221,"审批拒绝");

        private Integer value;

        private String remark;

        ApprovalStatus(Integer value, String remark) {
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

    /**
     * 审批状态的枚举类
     */
    public enum ApprovalResult {

        // 通过
        approve(223,"通过"),

        // 拒绝
        disapprove(224,"拒绝");

        private Integer value;

        private String remark;

        ApprovalResult(Integer value, String remark) {
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