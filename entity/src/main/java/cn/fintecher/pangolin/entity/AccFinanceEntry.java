package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "acc_finance_entry")
@ApiModel(value = "AccFinanceEntry", description = "财务数据信息")
public class AccFinanceEntry {

  @ApiModelProperty(notes = "外键:文件ID")
  private String fileId;
  @ApiModelProperty(notes = "外键：委外方ID")
  private String fienFgid;
  @ApiModelProperty(notes = "委外方名称")
  private String fienFgname;
  @ApiModelProperty(notes = "案件编号")
  private String fienCasenum;
  @ApiModelProperty(notes = "客户姓名")
  private String fienCustname;
  @ApiModelProperty(notes = "身份证号码")
  private String fienIdcard;
  @ApiModelProperty(notes = "批次")
  private String fienBatchnum;
  @ApiModelProperty(notes = "外部还款金额")
  private BigDecimal fienOutpaysum;
  @ApiModelProperty(notes = "案件金额")
  private BigDecimal fienCount = new BigDecimal(0);
  @ApiModelProperty(notes = "已还款金额")
  private BigDecimal fienPayback = new BigDecimal(0);
  @ApiModelProperty(notes = "数据来源  0-委托方1-委外方")
  private Integer fienDatasource;
  @ApiModelProperty(notes = "数据状态  0-未确认 1-已确认")
  private Integer fienStatus;
  @ApiModelProperty(notes = "备注")
  private String fienRemark;
  @ApiModelProperty(notes = "操作人")
  private String creator;
  @ApiModelProperty(notes = "操作时间")
  private Date createTime;

}
