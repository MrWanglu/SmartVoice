package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/8/7.
 */
@Data
@Entity
@Table(name = "user_login_log")
@ApiModel(value = "UserLoginLog", description = "用户登录日志")
public class UserLoginLog implements Serializable{

    @Id
    private String id;
    @ApiModelProperty("登陆时间")
    private Date loginTime;
    @ApiModelProperty("登出时间")
    private Date logoutTime;
    @ApiModelProperty("用户ID")
    private String userId;
    @ApiModelProperty("用户账号")
    private String userName;
    @ApiModelProperty("持续时间")
    private Long duration;
}
