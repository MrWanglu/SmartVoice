package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-16-11:37
 */
@Data
public class UserModel {
    @ApiModelProperty("用户名")
    private String userName;
    @ApiModelProperty("姓名")
    private String realName;
    @ApiModelProperty("所属部门")
    private String department;
    @ApiModelProperty("用户的催收类型（1.电话 2.外访3.修复...)")
    private String type;
    @ApiModelProperty("是否具有查看下级用户的权限")
    private String manager;
    @ApiModelProperty("电话")
    private String phone;
    @ApiModelProperty("邮箱")
    private String email;
    @ApiModelProperty("备注")
    private String remark;
    @ApiModelProperty("创建人")
    private String operator;
}
