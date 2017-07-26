package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author xiaqun
 * @Description : 案件还款审批实体
 * @Date : 16:01 2017/7/17
 * @Modified by LvGuoRong on 2017/7/5 增加减免类型字段
 */

@Entity
@Table(name = "CasePayApply")
@Data
public class CasePayApply extends BaseEntity {
    @ApiModelProperty(notes = "案件ID")
    private String caseId;

    @ApiModelProperty(notes = "案件编号")
    private String caseNumber;

    @ApiModelProperty(notes = "客户姓名")
    private String personalName;

    @ApiModelProperty(notes = "客户信息ID")
    private String personalId;

    @ApiModelProperty(notes = "催收类型")
    private Integer collectionType;

    @ApiModelProperty(notes = "部门ID")
    private String departId;

    @ApiModelProperty(notes = "申请还款金额")
    private BigDecimal applyPayAmt;

    @ApiModelProperty(notes = "申请减免金额")
    private BigDecimal applyDerateAmt;

    @ApiModelProperty(notes = "还款类型")
    private Integer payType;

    @ApiModelProperty(notes = "还款方式")
    private Integer payWay;

    @ApiModelProperty(notes = "减免标识 0-没有 1-有")
    private Integer derateFlag;

    @ApiModelProperty(notes = "审批状态")
    private Integer approveStatus;

    @ApiModelProperty(notes = "还款说明")
    private String payMemo;

    @ApiModelProperty(notes = "审批结果")
    private Integer approveResult;

    @ApiModelProperty(notes = "申请人")
    private String applayUserName;

    @ApiModelProperty(notes = "申请人姓名")
    private String applayRealName;

    @ApiModelProperty(notes = "申请人部门")
    private String applayDeptName;

    @ApiModelProperty(notes = "申请时间")
    private Date applayDate;

    @ApiModelProperty(notes = "减免审批人")
    private String approveDerateUser;

    @ApiModelProperty(notes = "减免审批人姓名")
    private String approveDerateName;

    @ApiModelProperty(notes = "减免审批时间")
    private Date approveDerateDatetime;

    @ApiModelProperty(notes = "减免审批意见")
    private String approveDerateMemo;

    @ApiModelProperty(notes = "减免费用备注")
    private String approveDerateRemark;

    @ApiModelProperty(notes = "还款审批人")
    private String approvePayUser;

    @ApiModelProperty(notes = "还款审批人姓名")
    private String approvePayName;

    @ApiModelProperty(notes = "还款审批时间")
    private Date approvePayDatetime;

    @ApiModelProperty(notes = "还款审批意见")
    private String approvePayMemo;

    @ApiModelProperty(notes = "公司code码")
    private String companyCode;

    @ApiModelProperty(notes = "操作人")
    private String operatorUserName;

    @ApiModelProperty(notes = "操作人名称")
    private String operatorRealName;

    @ApiModelProperty(notes = "操作时间")
    private Date operatorDate;

    @ApiModelProperty(notes = "客户手机号")
    private String personalPhone;

    @ApiModelProperty(notes = "案件批次号")
    private String batchNumber;

    @ApiModelProperty(notes = "委托方ID")
    private String principalId;

    @ApiModelProperty(notes = "委托方名称")
    private String principalName;

    @ApiModelProperty(notes = "减免类型 0逾期还款  1提前结清")
    private Integer approveType;

    @ApiModelProperty(notes = "费用减免审批状态 0已撤回 1待审批 2 审批同意 3审批拒绝")
    private Integer approveCostresult;

    /**
     * @Description 还款类型枚举类
     */
    public enum PayType {
        //部分逾期还款
        PARTOVERDUE(41, "部分逾期还款"),
        //全额逾期还款
        ALLOVERDUE(42, "全额逾期还款"),
        //减免逾期还款
        DERATEOVERDUE(43, "减免逾期还款"),
        //部分提前结清
        PARTADVANCE(44, "部分提前结清"),
        //全额提前结清
        ALLADVANCE(45, "全额提前结清"),
        //减免提前结清
        DERATEADVANCE(46, "减免提前结清");
        private Integer value;

        private String remark;

        PayType(Integer value, String remark) {
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
     * @Description 还款方式枚举类
     */
    public enum PayWay {
        //对公
        PUBLIC(47, "对公"),
        //代扣
        WITHHOLD(48, "代扣"),
        //微信
        WECHAT(49, "微信"),
        //支付宝
        ALIPAY(50, "支付宝"),
        //其他
        OTHER(51, "其他");
        private Integer value;

        private String remark;

        PayWay(Integer value, String remark) {
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
     * @Description 还款审批状态枚举类
     */
    public enum ApproveStatus {
        //撤回
        REVOKE(54, "撤回"),
        //减免待审核
        DERATE_TO_AUDIT(55, "减免待审核"),
        //减免审核驳回
        DERATE_AUDIT_REJECT(56, "减免审核驳回"),
        //还款待审核(66)
        PAY_TO_AUDIT(57, "还款待审核"),
        //审核通过(入账)
        AUDIT_AGREE(58, "审核通过(入账)"),
        //审核拒绝(驳回)
        AUDIT_REJECT(59, "审核拒绝(驳回)"),
        //还款审核中
        PAY_TO_REVIEW(124, "还款审核中"),
        //失效
        FAILURE(125, "失效");

        private Integer value;

        private String remark;

        ApproveStatus(Integer value, String remark) {
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
     * @Description 还款审批结果枚举类
     */
    public enum ApproveResult {
        //减免同意
        DERATE_AGREE(177, "减免同意"),
        //减免拒绝
        DERATE_REJECT(178, "减免拒绝"),
        //入账
        AGREE(179, "入账"),
        //驳回
        REJECT(180, "驳回");
        private Integer value;

        private String remark;

        ApproveResult(Integer value, String remark) {
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
     * 费用减免审批个状态的枚举
     */
    //费用减免审批各状态枚举
    public enum ApproveType {
        OVERDULE_PAY(115, "逾期还款"),
        EARLY_SETTLE(116, "提前结清");


        private Integer value;

        private String remark;

        ApproveType(Integer value, String remark) {
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
     * 费用减免审批个状态的枚举
     */
    //费用减免审批各状态枚举
    public enum ApproveCostresult {
        REVOCATION(119, "已撤回"),
        TO_AUDIT(120, "待审批"),
        AUDIT_AGREE(121, "审批同意"),
        AUDIT_REJECT(122, "审批拒绝");

        private Integer value;

        private String remark;

        ApproveCostresult(Integer value, String remark) {
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

