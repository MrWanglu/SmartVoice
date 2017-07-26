package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author : sunyanping
 * @Description : 协催案件结案model
 * @Date : 2017/7/26.
 */
@Data
@ApiModel(value = "协催案件结案model")
public class CloseAssistCaseModel {
    @ApiModelProperty("协催案件ID")
    private String assistId;
    @ApiModelProperty("结案说明")
    private String remark;
    @ApiModelProperty("结案方式")
    private Integer type;
}
