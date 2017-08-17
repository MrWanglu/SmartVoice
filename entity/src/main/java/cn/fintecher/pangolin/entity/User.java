package cn.fintecher.pangolin.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * Created by ChenChang on 2017/7/10.
 */
@Entity
@Table(name = "user")
@Data
@ApiModel(value = "user", description = "用户信息管理")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User extends BaseEntity {
    @ApiModelProperty("特定公司的标识")
    private String companyCode;
    @ApiModelProperty("用户的催收类型（1.电话 2.外访3.修复...)")
    private Integer type;
    @ApiModelProperty("用户采用的登录方式（1密码登录 2.二维码登录..)")
    private Integer loginType;
    @ApiModelProperty("用户的登录设备限制（1.pc登录 2.手机登录）")
    private Integer loginDevice;
    @ApiModelProperty("用户的最后一次登录地址（登录地址改变给出提醒）")
    private String loginAddress;
    @ApiModelProperty("密码的定时修改（比如3个月后提醒修改密码）")
    private Date passwordInvalidTime;
    @ApiModelProperty("电话呼叫绑定的电话号码")
    private String callPhone;
    @ApiModelProperty("用户绑定的消息推送的注册标识")
    private String messagePushId;
    @ApiModelProperty("是否具有查看下级用户的权限")
    private Integer manager;
    @ApiModelProperty("用户名")
    private String userName;
    @ApiModelProperty("姓名")
    private String realName;
    @ApiModelProperty("密码")
    private String password;
    @ApiModelProperty("性别")
    private Integer sex;
    @ApiModelProperty("电话")
    private String phone;
    @ApiModelProperty("邮箱")
    private String email;
    @ApiModelProperty("状态")
    private Integer status;
    @ApiModelProperty("签名")
    private String signature;
    @ApiModelProperty("特定公司的标识")
    private String remark;
    @ApiModelProperty("备注")
    private String photo;
    @ApiModelProperty("创建人用户名")
    private String operator;
    @ApiModelProperty("创建时间")
    private Date operateTime;
    @ApiModelProperty("备用字段")
    private String field;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dept_id")
    @ApiModelProperty("用户所在部门的id")
    private Department department;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @ApiModelProperty("用户所拥有的角色")
    private Set<Role> roles;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @ApiModelProperty("用户的登陆设备")
    private Set<UserDevice> userDevices;


    /**
     * 是否有数据权限
     */
    public enum MANAGER_TYPE {
        NO_DATA_AUTH(0), DATA_AUTH(1);
        Integer value;

        MANAGER_TYPE(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }
    }

    /**
     * 用户催收类型
     */
    public enum Type {
        TEL(1, "电话催收"),
        VISIT(2, "外访催收"),
        JUD(3, "司法催收"),
        OUT(4, "委外催收"),
        INTILL(5, "智能催收"),
        REMINDER(6, "提醒催收"),
        REPAIR(7, "修复管理"),
        SYNTHESIZE(196, "综合管理");

        private Integer value;
        private String name;

        Type(Integer value, String name) {
            this.value = value;
            this.name = name;
        }

        public Integer getValue() {
            return value;
        }
    }
}
