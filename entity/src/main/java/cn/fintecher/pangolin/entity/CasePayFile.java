package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "case_pay_file")
@Data
public class CasePayFile extends BaseEntity {
    @ApiModelProperty(notes = "还款记录ID")
    private String payId;

    @ApiModelProperty(notes = "案件编号")
    private String caseNumber;

    @ApiModelProperty(notes = "文件ID")
    private String fileid;

    @ApiModelProperty(notes = "创建人")
    private String operator;

    @ApiModelProperty(notes = "创建人名称")
    private String operatorName;

    @ApiModelProperty(notes = "创建时间")
    private Date operatorTime;

    @ApiModelProperty(notes = "案件id")
    private String caseId;
}
