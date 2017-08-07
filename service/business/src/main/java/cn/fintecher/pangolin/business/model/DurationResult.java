package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author : sunyanping
 * @Description : 用户在线/离线时长
 * @Date : 2017/8/7.
 */
@Data
@ApiModel("用户在线时长")
public class DurationResult {
    @ApiModelProperty("用户ID")
    private String userId;
    @ApiModelProperty("在线时长")
    private Long duration;
    @ApiModelProperty("离线时长")
    private Long offLineDuration;
}
