package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by sunyanping on 2017/11/8.
 */
@Data
@ApiModel("管理员首页催收员排名Model")
public class CollectorRankingModel {
    @ApiModelProperty("姓名")
    private String name;
    @ApiModelProperty("案件总金额(万元)")
    private BigDecimal amount = new BigDecimal(0);
    @ApiModelProperty("案件总数")
    private Integer count = 0;
    @ApiModelProperty("回款案件总金额(万元)")
    private BigDecimal payAmount = new BigDecimal(0);
    @ApiModelProperty("回款案件数")
    private Integer payCount = 0;
    @ApiModelProperty("回款率(%)")
    private Double payRate = new Double("0");
    @ApiModelProperty("查询年份")
    private String queryYear;
    @ApiModelProperty("查询月份")
    private String queryMonth;
}
