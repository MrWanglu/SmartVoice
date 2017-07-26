package cn.fintecher.pangolin.common.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by ChenChang on 2017/4/1.
 */
@Data
public class BindCallNumberRequest {
    @ApiModelProperty(notes = "固定话机ID")
    private String callerId;
    @ApiModelProperty(notes = "主叫号码")
    private String caller;
}
