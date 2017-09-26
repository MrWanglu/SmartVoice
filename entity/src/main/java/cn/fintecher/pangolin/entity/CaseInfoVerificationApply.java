package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author yuanyanting
 * @version Id:CaseInfoVerificationModel.java,v 0.1 2017/8/31 15:52 yuanyanting Exp $$
 */
@Entity
@Table(name = "case_info_verification_apply")
@Data
public class CaseInfoVerificationApply extends BaseEntity {


    @ApiModelProperty("公司code码")
    private String companyCode;

    @ApiModelProperty("当前催收员")
    private String operator;

    @ApiModelProperty("案件编号")
    private String caseId;

    @ApiModelProperty("操作时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operatorTime;

    @ApiModelProperty(notes = "申请日期")
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
    private String approvalResult;

    /**
     * 审批状态的枚举类
     */
    public enum ApprovalStatus {
        // 待审批
        approval_pending(213,"待审批"),

        // 通过
        approve(214,"审批通过"),

        // 拒绝
        disapprove(215,"审批拒绝");

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

}