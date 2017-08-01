package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/7/31.
 */
@Data
@ApiModel(value = "周回款统计")
public class WeekCountResult {
    @ApiModelProperty("周回款总金额")
    private BigDecimal amount;
    @ApiModelProperty("周催计数/结案数")
    private Integer num;
    @ApiModelProperty("天回款金额")
    private Integer dayOfWeek;
}
