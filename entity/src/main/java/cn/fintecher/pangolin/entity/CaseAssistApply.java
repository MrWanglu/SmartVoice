package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author : sunyanping
 * @Description : 案件协催申请
 * @Date : 2017/7/17.
 */
@Data
@Entity
@Table(name = "case_assist_apply")
@ApiModel(value = "CaseAssistApply", description = "案件协催申请")
public class CaseAssistApply extends BaseEntity {
    @ApiModelProperty("案件ID")
    private String caseId;
    @ApiModelProperty("案件编号")
    private String caseNumber;
    @ApiModelProperty("客户姓名")
    private String personalName;
    @ApiModelProperty("客户手机号")
    private String personalPhone;
    @ApiModelProperty("客户信息ID")
    private String personalId;
    @ApiModelProperty("催收类型(电催、外访、司法、委外、提醒)")
    private Integer collectionType;
    @ApiModelProperty("部门ID")
    private String departId;
    @ApiModelProperty("委托方ID")
    private String principalId;
    @ApiModelProperty("委托方名称")
    private String principalName;
    @ApiModelProperty("逾期总金额")
    private BigDecimal overdueAmount;
    @ApiModelProperty("逾期天数")
    private Integer overdueDays;
    @ApiModelProperty("逾期期数")
    private Integer overduePeriods;
    @ApiModelProperty("持案天数")
    private Integer holdDays;
    @ApiModelProperty("剩余天数")
    private Integer leftDays;
    @ApiModelProperty("省份编号")
    private Integer areaId;
    @ApiModelProperty("城市名称")
    private String areaName;
    @ApiModelProperty("申请人")
    private String applyUserName;
    @ApiModelProperty("申请人姓名")
    private String applyRealName;
    @ApiModelProperty("申请人部门名称")
    private String applyDeptName;
    @ApiModelProperty("申请原因")
    private String applyReason;
    @ApiModelProperty("申请时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date applyDate;
    @ApiModelProperty("申请失效日期")
    private Date applyInvalidTime;
    @ApiModelProperty("协催方式")
    private Integer assistWay;
    @ApiModelProperty("产品系列ID")
    private String productSeries;
    @ApiModelProperty("产品ID")
    private String productId;
    @ApiModelProperty("产品系列名称")
    private String productSeriesName;
    @ApiModelProperty("产品名称")
    private String productName;
    @ApiModelProperty("审批状态(电催待审批、电催审批完成、外访待审批、外访审批完成)")
    private Integer approveStatus;
    @ApiModelProperty("电催审批结果")
    private Integer approvePhoneResult;
    @ApiModelProperty("电催审批人")
    private String approvePhoneUser;
    @ApiModelProperty("电催审批人姓名")
    private String approvePhoneName;
    @ApiModelProperty("电催审批时间")
    private Date approvePhoneDatetime;
    @ApiModelProperty("电催审批意见")
    private String approvePhoneMemo;
    @ApiModelProperty("外访审批人")
    private String approveOutUser;
    @ApiModelProperty("外访审批人姓名")
    private String approveOutName;
    @ApiModelProperty("外访审批时间")
    private Date approveOutDatetime;
    @ApiModelProperty("外访审批意见")
    private String approveOutMemo;
    @ApiModelProperty("外访审批结果")
    private Integer approveOutResult;
    @ApiModelProperty("公司Code码")
    private String companyCode;

    /**
     * @Description 协催审批状态枚举类
     */
    public enum ApproveStatus {

        TEL_APPROVAL(32, "电催待审批"),
        TEL_COMPLETE(33, "电催审批完成"),
        VISIT_APPROVAL(34, "外访待审批"),
        VISIT_COMPLETE(35, "外访审批完成"),
        FAILURE(210, "审批失效");

        private Integer value;
        private String name;

        ApproveStatus(Integer value, String name) {
            this.value = value;
            this.name = name;
        }

        public Integer getValue() {
            return value;
        }

        public String getName()

        {
            return name;
        }
    }

    /**
     * @Description 协催审批结果枚举类
     */
    public enum ApproveResult {

        TEL_REJECT(36, "电催审批拒绝"),
        TEL_PASS(37, "电催审批通过"),
        VISIT_REJECT(38, "外访审批拒绝"),
        VISIT_PASS(39, "外访审批通过"),
        FORCED_REJECT(40, "流转强制拒绝"),
        PAY_REJECT(246, "还款强制拒绝"),
        OUT_DATE(211, "过期");

        private Integer value;
        private String name;

        ApproveResult(Integer value, String name) {
            this.value = value;
            this.name = name;
        }

        public Integer getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }
}
