package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by sun on 2017/10/31.
 */
@Data
@ApiModel(description = "回收案件核销参数")
public class VerifiyParams {
    @ApiModelProperty("选择的案件ID集合")
    private List<String> ids;
    @ApiModelProperty(value = "核销说明", required = true)
    private String memo;
}
