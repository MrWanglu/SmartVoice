package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @Author : sunyanping
 * @Description : 排行榜
 * @Date : 2017/7/31.
 */
@Data
public class PageSortResult {
    @ApiModelProperty(notes = "姓名")
    private String name;

    @ApiModelProperty(notes = "总金额")
    private BigDecimal amount;

    @ApiModelProperty(notes = "入账金额")
    private BigDecimal payed;

    @ApiModelProperty(notes = "比率")
    private Double rate;

    public void initRate(){
        this.rate = Objects.isNull(this.rate) ? 0D : this.rate > 1D ? 1D : this.rate;
    }
}
