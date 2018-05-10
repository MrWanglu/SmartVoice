package cn.fintecher.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by qijigui  on 2018-05-10.
 */

@Entity
@Table(name = "auth_user")
@Data
@ApiModel(value = "auth_user", description = "用户信息管理")
public class AuthUser extends BaseEntity {

    @ApiModelProperty("密码")
    private String password;
    @ApiModelProperty("上次登录时间")
    private Date last_login;
    @ApiModelProperty("是否超级管理员")
    private Integer is_superuser;
    @ApiModelProperty("用户名")
    private String username;
    @ApiModelProperty("姓")
    private String first_name;
    @ApiModelProperty("名")
    private String last_name;
    @ApiModelProperty("邮箱")
    private String email;
    @ApiModelProperty("是否工作组")
    private Integer is_staff;
    @ApiModelProperty("日期")
    private Date date_joined;
}
