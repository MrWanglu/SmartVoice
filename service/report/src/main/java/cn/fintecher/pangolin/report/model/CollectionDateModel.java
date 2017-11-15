package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Author : huyanmin
 * @Description :
 * @Date : 2017/11/11.
 */
@Entity
@Data
public class CollectionDateModel {

    @ApiModelProperty(notes = "内催案件催收中总金额")
    private BigDecimal innerCollectingAmt= new BigDecimal("0.00");
    @ApiModelProperty(notes = "内催案件催收中总数量")
    private Integer innerCollectingCount;
    @ApiModelProperty(notes = "委外案件催收中总金额")
    private BigDecimal outsourceCollectingAmt= new BigDecimal("0.00");
    @ApiModelProperty(notes = "委外案件催收中总数")
    private Integer outsourceCollectingCount;
    @ApiModelProperty(notes = "内催和委外总金额")
    private BigDecimal totalCollectionAmt= new BigDecimal("0.00");
    @ApiModelProperty(notes = "内催和委外总数")
    private Integer totalCollectionCount;
    @ApiModelProperty(notes = "内催各省份总金额和数量")
    private List<ProvinceCollectionDateModel> innerProvinceCollectionCount;
    @ApiModelProperty(notes = "委外各省份总金额和数量")
    private List<ProvinceCollectionDateModel> outsourceProvinceCollectionCount;
    @ApiModelProperty(notes = "内催+委外总金额和数量")
    private List<ProvinceCollectionDateModel> totalProvinceCollectionCount;
}
