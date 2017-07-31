package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/7/31.
 */
public class PageSortResult {
    @ApiModelProperty(notes = "姓名")
    private String name;

    @ApiModelProperty(notes = "总金额")
    private BigDecimal amount;

    @ApiModelProperty(notes = "入账金额")
    private BigDecimal payed;

    @ApiModelProperty(notes = "比率")
    private Double rate;
}
