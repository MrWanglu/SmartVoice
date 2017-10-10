package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by sunyanping on 2017/10/10.
 */
@Data
@ApiModel("回收案件重新分配传值")
public class ReDisRecoverCaseParams {
    @ApiModelProperty("案件ID集合")
    private List<String> ids;
    @ApiModelProperty(value = "分配目标：0-内催待分配池，1-委外待分配池", required = true)
    private Integer type;
}
