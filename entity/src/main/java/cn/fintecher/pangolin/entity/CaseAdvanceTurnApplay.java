package cn.fintecher.pangolin.entity;


import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "case_advance_turn_applay")
@Data
public class CaseAdvanceTurnApplay extends BaseEntity {

  private String caseId;
  private String caseNumber;
  private String personalName;
  private String personalId;
  private Integer collectionType;
  private String departId;
  private String principalId;
  private String principalName;
  private BigDecimal overdueAmount;
  private Integer overdueDays;
  private Integer overduePeriods;
  private Integer holdDays;
  private Integer leftDays;
  private Integer areaId;
  private String areaName;
  private String applayUserName;
  private String applayRealName;
  private String applayDeptName;
  private String applayReason;
  private Date applayDate;
  private Date applayDeadTime;
  private String approveUserName;
  private String approveRealName;
  private String approveMemo;
  private Date approveDatetime;
  private Integer approveResult;
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
