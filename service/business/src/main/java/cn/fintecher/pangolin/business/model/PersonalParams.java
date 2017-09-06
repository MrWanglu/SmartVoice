package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: PeiShouWen
 * @Description: 客户信息(短信参数）
 * @Date 11:34 2017/9/1
 */
@Data
public class PersonalParams {
    @ApiModelProperty("联系人姓名")
    private String personalName;
    @ApiModelProperty("联系人手机号")
    private String personalPhone;
    @ApiModelProperty("联系人ID")
    private String contId;
}
