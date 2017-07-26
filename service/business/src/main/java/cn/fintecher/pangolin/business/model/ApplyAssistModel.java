package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/7/18.
 */
@Data
@ApiModel(value = "ApplyAssistModel", description = "申请协催信息对象")
public class ApplyAssistModel {
    @ApiModelProperty("协催方式")
    private Integer assistWay;
    @ApiModelProperty("申请原因")
    private String applyReason;
}
