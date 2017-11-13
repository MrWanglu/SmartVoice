package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import java.math.BigDecimal;

/**
 * @Author : huyanmin
 * @Description :
 * @Date : 2017/11/11.
 */
@Entity
@Data
public class ProvinceDateModel {

    @ApiModelProperty(notes = "催收中总金额")
    private BigDecimal collectingAmt = new BigDecimal(0.00);
    @ApiModelProperty(notes = "催收中总数量")
    private Integer collectingCount;
}
