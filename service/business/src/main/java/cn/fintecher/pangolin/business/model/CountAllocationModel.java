package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunyanping on 2017/10/9.
 */
@Data
@ApiModel(description = "策略分配统计Model")
public class CountAllocationModel {
    @ApiModelProperty("催收模块：0-内催，1-委外")
    private Integer type;
    @ApiModelProperty("分配案件数")
    private Integer total = 0;
    @ApiModelProperty("分配案件金额")
    private BigDecimal amount = new BigDecimal(0);
    @ApiModelProperty("分配案件ID集合")
    private List<String> ids = new ArrayList<>();
}
