package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "AccOutsideFinExportModel", description = "财务数据导出时模板数据接收对象")
public class AccOutsideFinExportModel {
    @ApiModelProperty(notes = "委外批次号")
    private String oupoOutbatch;
    @ApiModelProperty(notes = "案件编号")
    private String oupoCasenum;
    @ApiModelProperty(notes = "客户姓名")
    private String custName;
    @ApiModelProperty(notes = "客户身份证号")
    private String oupoIdcard;
    @ApiModelProperty(notes = "委外状态")
    private String oupoStatus;
    @ApiModelProperty(notes = "委外方")
    private String outsName;
    @ApiModelProperty(notes = "案件金额（逾期应还金额）")
    private String oupoAmt;
    @ApiModelProperty(notes = "已还款金额(逾期还款金额)")
    private String oupoPaynum;
}
