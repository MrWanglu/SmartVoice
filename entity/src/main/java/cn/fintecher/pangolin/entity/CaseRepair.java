package cn.fintecher.pangolin.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "case_repair")
public class CaseRepair extends BaseEntity{
  private String id;
  private Integer repairStatus;
  @ManyToOne
  @JoinColumn(name = "operator")
  private User operator;
  private Date operatorTime;
  private String companyCode;
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "case_id")
  private CaseInfo caseId;


  /**
   * 案件修复状态
   */
  public enum CaseRepairStatus{
    REPAIRING(187,"待修复"),REPAIRED(188,"已修复"), DISTRIBUTE(189,"已分配");

    Integer value;
    String code;
    CaseRepairStatus(Integer value,String code){
      this.value=value;
      this.code=code;
    }

    public Integer getValue() {
      return value;
    }

    public void setValue(Integer value) {
      this.value = value;
    }

    public String getCode() {
      return code;
    }

    public void setCode(String code) {
      this.code = code;
    }
  }

}
