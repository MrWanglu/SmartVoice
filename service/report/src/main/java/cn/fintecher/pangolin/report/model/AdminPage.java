package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/7/31.
 */
@Data
public class AdminPage {

    // 第一部分
    @ApiModelProperty(notes = "案件总金额")
    private BigDecimal caseSumAmt;

    @ApiModelProperty(notes = "人均金额")
    private BigDecimal caseSumAmtPerson;

    // 第二部分
    @ApiModelProperty(notes = "回款总金额")
    private BigDecimal repaySumAmt;

    @ApiModelProperty(notes = "人均金额")
    private BigDecimal repaySumAmtPerson;

    // 第三部分
    @ApiModelProperty(notes = "催收员总人数")
    private Integer cupoSum;

    @ApiModelProperty(notes = "在线人数")
    private Integer cupoOnlineSum;

    @ApiModelProperty(notes = "离线人数")
    private Integer cupoOfflineSum;

    // 第四部分
    @ApiModelProperty(notes = "客户总数")
    private Integer custSum;

    @ApiModelProperty(notes = "在案客户总数")
    private Integer custSumIn;

    // 第五部分
    @ApiModelProperty(notes = "本周回款")
    private List<WeekCountResult> weekRepayList;

    // 第六部分
    @ApiModelProperty(notes = "本周催计数")
    private List<WeekCountResult> weekFollList;

    // 第七部分
    @ApiModelProperty(notes = "本周结案数")
    private List<WeekCountResult> weekCaseEndList;

    // 第八部分
    @ApiModelProperty(notes = "催收员排行榜")
    private List<PageSortResult> cupoSortList;

    // 第九部分
    @ApiModelProperty(notes = "客户排行榜")
    private List<PageSortResult> custSortList;

    // 第十部分
    @ApiModelProperty(notes = "系统公告")
    private List<SysNotice> sysNotice;

    public void initRate(){
        this.custSortList.forEach(PageSortResult::initRate);
        this.cupoSortList.forEach(PageSortResult::initRate);
    }
}
