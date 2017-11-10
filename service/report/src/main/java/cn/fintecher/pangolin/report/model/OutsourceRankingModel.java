package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by huyanmin on 2017/11/9.
 */
@Data
@ApiModel("管理员首页委外方排名Model")
public class OutsourceRankingModel {
    @ApiModelProperty("姓名")
    private String name;
    @ApiModelProperty("案件总金额(万元)")
    private BigDecimal amount;
    @ApiModelProperty("案件总数")
    private Integer count;
    @ApiModelProperty("回款案件总金额(万元)")
    private BigDecimal payAmount;
    @ApiModelProperty("回款案件数")
    private Integer payCount;
    @ApiModelProperty("回款率(%)")
    private Double payRate;
}
