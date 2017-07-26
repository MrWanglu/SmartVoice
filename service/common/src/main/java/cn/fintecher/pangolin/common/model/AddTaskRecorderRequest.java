package cn.fintecher.pangolin.common.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by ChenChang on 2017/4/1.
 */
@Data
public class AddTaskRecorderRequest {
    @ApiModelProperty(notes = "被叫号码")
    private String callee;
    @ApiModelProperty(notes = "客户ID")
    private String customer;
    @ApiModelProperty(notes = "绑定和返回的taskId")
    private String taskId;
    @ApiModelProperty(notes = "主叫号码")
    private String caller;
}
