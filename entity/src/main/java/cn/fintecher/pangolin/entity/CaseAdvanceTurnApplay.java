package cn.fintecher.pangolin.entity;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "case_advance_turn_applay")
@Data
public class CaseAdvanceTurnApplay extends BaseEntity {
  @ApiModelProperty(notes = "案件ID")
  private String caseId;

  @ApiModelProperty(notes = "案件编号")
  private String caseNumber;

  @ApiModelProperty(notes = "客户姓名")
  private String personalName;

  @ApiModelProperty(notes = "客户信息ID")
  private String personalId;

  @ApiModelProperty(notes = "催收类型(电催、外访、司法、委外、提醒)")
  private Integer collectionType;

  @ApiModelProperty(notes = "部门Code")
  private String deptCode;

  @ApiModelProperty(notes = "委托方ID")
  private String principalId;

  @ApiModelProperty(notes = "委托方名称")
  private String principalName;

  @ApiModelProperty(notes = "逾期总金额")
  private BigDecimal overdueAmount;

  @ApiModelProperty(notes = "逾期天数")
  private Integer overdueDays;

  @ApiModelProperty(notes = "逾期期数")
  private Integer overduePeriods;

  @ApiModelProperty(notes = "持案天数")
  private Integer holdDays;

  @ApiModelProperty(notes = "剩余天数")
  private Integer leftDays;

  @ApiModelProperty(notes = "省份编号")
  private Integer areaId;

  @ApiModelProperty(notes = "城市名称")
  private String areaName;

  @ApiModelProperty(notes = "申请人")
  private String applayUserName;

  @ApiModelProperty(notes = "申请人姓名")
  private String applayRealName;

  @ApiModelProperty(notes = "申请人部门名称")
  private String applayDeptName;

  @ApiModelProperty(notes = "申请原因")
  private String applayReason;

  @ApiModelProperty(notes = "申请时间")
  private Date applayDate;

  @ApiModelProperty(notes = "申请失效日期")
  private Date applayDeadTime;

  @ApiModelProperty(notes = "审批人")
  private String approveUserName;

  @ApiModelProperty(notes = "审批名称")
  private String approveRealName;

  @ApiModelProperty(notes = "审批说明")
  private String approveMemo;

  @ApiModelProperty(notes = "审批时间")
  private Date approveDatetime;

  @ApiModelProperty(notes = "审批结果")
  private Integer approveResult;

  @ApiModelProperty(notes = "公司code码")
  private String companyCode;

  /**
   * @Description 流转审批状态
   */
  public enum CirculationStatus {
    //待审批
    PHONE_WAITING(213, "待审批"),
    //通过
    PHONE_PASS(214, "通过"),
    //拒绝
    PHONE_REFUSE(215, "拒绝");
    private Integer value;

    private String remark;

    CirculationStatus(Integer value, String remark) {
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
