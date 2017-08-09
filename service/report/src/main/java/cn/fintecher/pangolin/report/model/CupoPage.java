package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/8/2.
 */
@Data
public class CupoPage {
    // 第一部分
    @ApiModelProperty(notes = "本周完成度")
    private Double taskFinished;

    // 第二部分
    @ApiModelProperty(notes = "案件情况")
    private List<CaseCountResult> caseCountResultList;

    @ApiModelProperty(notes = "今日流入案件")
    private Integer flowInCaseToday;

    @ApiModelProperty(notes = "今日结清案件")
    private Integer finishCaseToday;

    @ApiModelProperty(notes = "今日流出案件")
    private Integer flowOutCaseToday;

    // 第三部分
/*    @ApiModelProperty(notes = "案件金额")
    private CaseAmtResult caseAmtResult;*/

    @ApiModelProperty(notes = "回款总额")
    private BigDecimal moneySumResult = new BigDecimal("0.00");

    @ApiModelProperty(notes = "本月累计回款")
    private BigDecimal monthMoneyResult = new BigDecimal("0.00");

    @ApiModelProperty(notes = "今日累计回款")
    private BigDecimal dayMoneyResult = new BigDecimal("0.00");

    // 第四部分
    @ApiModelProperty(notes = "在线时长")
    private Integer onlineTime;

    @ApiModelProperty(notes = "离线时长")
    private Integer offlineTime;

    @ApiModelProperty(notes = "今日累计催收")
    private Integer dayFollowCount;

    @ApiModelProperty(notes = "本月累计催收")
    private Integer monthFollowCount;

    // 第五部分
    @ApiModelProperty(notes = "本周回款")
    private List<WeekCountResult> weekRepaySum;

    // 第六部分
    @ApiModelProperty(notes = "本周催计数")
    private List<WeekCountResult> weekFollCount;

    // 第七部分
    @ApiModelProperty(notes = "本周结案数")
    private List<WeekCountResult> weekCaseEndCount;
}
