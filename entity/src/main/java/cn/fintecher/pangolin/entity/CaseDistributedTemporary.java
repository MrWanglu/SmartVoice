package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 案件分配结果临时表实体
 * @Date : 11:34 2017/10/17
 */

@Data
@Entity
@Table(name = "case_distributed_temporary")
public class CaseDistributedTemporary extends BaseEntity {
    @ApiModelProperty(notes = "案件ID")
    private String caseId;

    @ApiModelProperty(notes = "案件编号")
    private String caseNumber;

    @ApiModelProperty(notes = "批次号")
    private String batchNumber;

    @ApiModelProperty(notes = "客户姓名")
    private String personalName;

    @ApiModelProperty(notes = "案件金额")
    private BigDecimal overdueAmt;

    @ApiModelProperty(notes = "流转记录ID")
    private Integer caseTurnRecord;

    @ApiModelProperty(notes = "案件修复ID")
    private String caseRepairId;

    @ApiModelProperty(notes = "案件备注ID")
    private String caseRemark;

    @ApiModelProperty(notes = "上一个催收员ID")
    private String lastCollector;

    @ApiModelProperty(notes = "上一个催收员持案天数")
    private Integer lastCollectorHasDays;

    @ApiModelProperty(notes = "上一个催收员姓名")
    private String lastCollectorName;

    @ApiModelProperty(notes = "当前催收员ID")
    private String currentCollector;

    @ApiModelProperty(notes = "当前催收姓名")
    private String currentCollectorName;

    @ApiModelProperty(notes = "案件上一个所在部门ID")
    private String lastDepartment;

    @ApiModelProperty(notes = "案件上一个所在部门名称")
    private String lastDepartmentName;

    @ApiModelProperty(notes = "案件原催收状态")
    private Integer lastCollectionStatus;

    @ApiModelProperty(notes = "原案件催收类型")
    private Integer lastCollectionType;

    @ApiModelProperty(notes = "案件协催标识")
    private Integer lastAssistFlag;

    @ApiModelProperty(notes = "案件当前所在部门ID")
    private String currentDepartment;

    @ApiModelProperty(notes = "案件当前所在部门名称")
    private String currentDepartmentName;

    @ApiModelProperty(notes = "委托方名称")
    private String principalName;

    @ApiModelProperty(notes = "分配类型")
    private Integer type;

    @ApiModelProperty(notes = "操作人用户名")
    private String operatorUserName;

    @ApiModelProperty(notes = "操作人姓名")
    private String operatorRealName;

    @ApiModelProperty(notes = "操作时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operatorTime;

    @ApiModelProperty(notes = "公司code码")
    private String companyCode;

    public enum Type {
        BIG_IN(0, "大分案-内催"),
        BIG_OUT(1, "大分案-委外"),
        //首次分案
        FIRST(2, "内催首次分案"),
        //其他分案
        OTHER(3, "内催其他分案");

        private Integer value;
        private String remark;

        Type(Integer value, String remark) {
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