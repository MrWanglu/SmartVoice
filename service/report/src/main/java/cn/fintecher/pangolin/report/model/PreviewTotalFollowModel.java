package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import java.math.BigDecimal;

/**
 * @Author : huyanmin
 * @Description :
 * @Date : 2017/11/8.
 */
@Entity
@Data
public class PreviewTotalFollowModel {

    // 第三部分 跟催量总览
    @ApiModelProperty(notes = "今日外呼")
    private Integer currentDayCalled;
    @ApiModelProperty(notes = "本周外呼")
    private Integer currentWeekCalled;
    @ApiModelProperty(notes = "本月外呼")
    private Integer currentMonthCalled;
    @ApiModelProperty(notes = "今日催记数")
    private Integer currentDayCount;
    @ApiModelProperty(notes = "本周催记数")
    private Integer currentWeekCount;
    @ApiModelProperty(notes = "本月催记数")
    private Integer currentMonthCount;
    @ApiModelProperty(notes = "在线时长")
    private Integer onlineTime;
    @ApiModelProperty(notes = "离线时长")
    private Integer offlineTime;

}
