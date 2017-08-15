package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author : gaobeibei
 * @Description : APP新增客户社交信息
 * @Date : 16:01 2017/7/20
 */
@Data
public class SocialRepairInfo {
    @ApiModelProperty("社交账号类型")
    private Integer socialType;
    @ApiModelProperty("社交账号")
    private String socialValue;
}
