package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by sunyanping on 2017/9/26.
 */
@Data
@ApiModel(description = "手动分案案件统计")
public class AllocationCountModel {
    @ApiModelProperty("案件总个数")
    private Integer caseTotal;
    @ApiModelProperty("案件总金额")
    private Double caseAmount;
}
