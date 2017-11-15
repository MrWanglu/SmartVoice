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
public class BackAmtModel {

    @ApiModelProperty(notes = "催收员姓名")
    private String collectionName;
    @ApiModelProperty(notes = "回款率")
    private Double backRate;
    @ApiModelProperty(notes = "回款金额")
    private BigDecimal backMoney= new BigDecimal("0.00");
    @ApiModelProperty(notes = "回款户数")
    private Integer backCount;

}
