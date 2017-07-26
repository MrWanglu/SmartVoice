package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @Author: hukaijia
 * @Description:
 * @Date 2017/3/27
 */
@Data
public class UpdatePassword {
    @ApiModelProperty(notes = "userId用户id")
    private String userId;
    @Size(min = 6, max = 64, message = "密码长度不能小于6位大于64位")
    @ApiModelProperty(notes = "原始密码")
    private String newPassword;
    @Size(min = 6, max = 64, message = "密码长度不能小于6位大于64位")
    @ApiModelProperty(notes = "修改密码")
    private String oldPassword;
}
