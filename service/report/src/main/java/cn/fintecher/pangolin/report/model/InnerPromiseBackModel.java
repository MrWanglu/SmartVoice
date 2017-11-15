package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import java.math.BigDecimal;

/**
 * @Author : huyanmin
 * @Description :
 * @Date : 2017/11/10.
 */
@Entity
@Data
public class InnerPromiseBackModel {

    @ApiModelProperty(notes = "内催承诺还款总金额")
    private BigDecimal innerPromisedAmt= new BigDecimal("0.00");
    @ApiModelProperty(notes = "内催承诺还款总数")
    private Integer innerPromisedCount;
    @ApiModelProperty(notes = "内催协商跟进总金额")
    private BigDecimal innerFollowAmt= new BigDecimal("0.00");
    @ApiModelProperty(notes = "内催协商跟进总数")
    private Integer innerFollowCount;
    @ApiModelProperty(notes = "内催拒绝还款总金额")
    private BigDecimal innerRejectAmt= new BigDecimal("0.00");
    @ApiModelProperty(notes = "内催拒绝还款总数")
    private Integer innerRejectCount;
    @ApiModelProperty(notes = "内催其他总金额")
    private BigDecimal innerOtherAmt= new BigDecimal("0.00");
    @ApiModelProperty(notes = "内催其他总数")
    private Integer innerOtherCount;

    @ApiModelProperty(notes = "委外承诺还款总金额")
    private BigDecimal outsourcePromisedAmt= new BigDecimal("0.00");
    @ApiModelProperty(notes = "委外承诺还款总数")
    private Integer outsourcePromisedCount;
    @ApiModelProperty(notes = "委外协商跟进总金额")
    private BigDecimal outsourceFollowAmt= new BigDecimal("0.00");
    @ApiModelProperty(notes = "委外协商跟进总数")
    private Integer outsourceFollowCount;
    @ApiModelProperty(notes = "委外拒绝还款总金额")
    private BigDecimal outsourceRejectAmt= new BigDecimal("0.00");
    @ApiModelProperty(notes = "委外拒绝还款总数")
    private Integer outsourceRejectCount;
    @ApiModelProperty(notes = "委外其他总金额")
    private BigDecimal outsourceOtherAmt= new BigDecimal("0.00");
    @ApiModelProperty(notes = "委外其他总数")
    private Integer outsourceOtherCount;

    @ApiModelProperty(notes = "总承诺还款总金额")
    private BigDecimal totalPromisedAmt= new BigDecimal("0.00");
    @ApiModelProperty(notes = "总承诺还款总数")
    private Integer totalPromisedCount;
    @ApiModelProperty(notes = "总协商跟进总金额")
    private BigDecimal totalFollowAmt= new BigDecimal("0.00");
    @ApiModelProperty(notes = "总协商跟进总数")
    private Integer totalFollowCount;
    @ApiModelProperty(notes = "总拒绝还款总金额")
    private BigDecimal totalRejectAmt= new BigDecimal("0.00");
    @ApiModelProperty(notes = "总拒绝还款总数")
    private Integer totalRejectCount;
    @ApiModelProperty(notes = "总其他总金额")
    private BigDecimal totalOtherAmt= new BigDecimal("0.00");
    @ApiModelProperty(notes = "总其他总数")
    private Integer totalOtherCount;
}
