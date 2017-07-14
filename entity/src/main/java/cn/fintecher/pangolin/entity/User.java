package cn.fintecher.pangolin.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * Created by ChenChang on 2017/7/10.
 */
@Entity
@Table
@Data
public class User extends BaseEntity {
    private String companyCode;
    private Integer type;
    private Integer loginType;
    private Integer loginDevice;
    private String loginAddress;
    private Date passwordInvalidTime;

    private String callPhone;
    private String messagePushId;
    private Integer manager;
    private String userName;
    private String realName;
    private String password;
    private Integer sex;
    private String phone;
    private String email;
    private Integer status;
    private String signature;
    private String remark;
    private String photo;
    private String operator;
    private Date operateTime;

    @ManyToOne
    @JoinColumn(name = "dept_id")
    private Department department;
    @ManyToMany
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;
}
