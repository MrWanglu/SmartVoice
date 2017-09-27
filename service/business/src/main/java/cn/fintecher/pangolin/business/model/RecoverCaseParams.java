package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by sunyanping on 2017/9/27.
 */
@Data
@ApiModel(description = "回收案件传值参数")
public class RecoverCaseParams {
    @ApiModelProperty("选择的案件ID集合")
    private List<String> ids;
    @ApiModelProperty("回收说明")
    private String reason;
}
