package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Administrator on 2017/9/25.
 */
@Data
public class CollectingCaseInfo {
    @ApiModelProperty(notes = "批次号")
    private String batchNumber;
    @ApiModelProperty(notes = "委托方名称")
    private String name;
    @ApiModelProperty(notes = "案件流入日期")
    private Date caseFollowInTime;
    @ApiModelProperty(notes = "委案日期")
    private Date delegationDate;
    @ApiModelProperty(notes = "结案日期")
    private Date closeDate;
    @ApiModelProperty(notes = "剩余天数")
    private Integer leftDays;
    @ApiModelProperty(notes = "案件数量")
    private BigDecimal caseAmt;
    @ApiModelProperty(notes = "案件金额")
    private Integer caseNum;
    @ApiModelProperty(notes = "已催回案件金额")
    private BigDecimal endCaseAmt;
    @ApiModelProperty(notes = "已催回案件数量")
    private Integer endCaseNum;
    @ApiModelProperty(notes = "已催回数量比例")
    private BigDecimal numRate;
    @ApiModelProperty(notes = "已催回金额比例")
    private BigDecimal amtRate;
}
