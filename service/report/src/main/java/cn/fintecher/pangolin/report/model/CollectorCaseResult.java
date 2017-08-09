package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author : sunyanping
 * @Description : 催收员首页案件情况、案件金额、在线情况
 * @Date : 2017/8/3.
 */
@Data
@ApiModel("催收员首页案件情况、案件金额、在线情况")
public class CollectorCaseResult {
    @ApiModelProperty(notes = "今日流入案件数")
    private Integer flowInCaseToday;

    @ApiModelProperty(notes = "今日结清案件数")
    private Integer finishCaseToday;

    @ApiModelProperty(notes = "今日流出案件数")
    private Integer flowOutCaseToday;

    @ApiModelProperty(notes = "回款总额")
    private BigDecimal moneySumResult = new BigDecimal("0.00");

    @ApiModelProperty(notes = "本月累计回款")
    private BigDecimal monthMoneyResult = new BigDecimal("0.00");

    @ApiModelProperty(notes = "今日累计回款")
    private BigDecimal dayMoneyResult = new BigDecimal("0.00");

    @ApiModelProperty(notes = "今日累计催收")
    private Integer dayFollowCount;

    @ApiModelProperty(notes = "本月累计催收")
    private Integer monthFollowCount;
}
