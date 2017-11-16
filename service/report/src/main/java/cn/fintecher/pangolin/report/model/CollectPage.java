package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author : huyanmin
 * @Description :
 * @Date : 2017/11/7.
 */
@Data
public class CollectPage {

    // 第一部分 周完成进度
    @ApiModelProperty(notes = "案件总数")
    private Integer caseWeekTotalCount;
    @ApiModelProperty(notes = "案件完成总数")
    private Integer caseWeekFinishedCount;
    @ApiModelProperty(notes = "回款案件金额")
    private BigDecimal caseWeekBackTotalAmt;
    @ApiModelProperty(notes = "回款完成案件金额")
    private BigDecimal caseWeekBackFinishedAmt;

    // 第二部分 月完成进度
    @ApiModelProperty(notes = "案件总数")
    private Integer caseMonthTotalCount;
    @ApiModelProperty(notes = "案件完成总数")
    private Integer caseMonthFinishedCount;
    @ApiModelProperty(notes = "回款案件总金额")
    private BigDecimal caseMonthBackTotalAmt;
    @ApiModelProperty(notes = "回款完成案件金额")
    private BigDecimal caseMonthBackFinishedAmt;
}
