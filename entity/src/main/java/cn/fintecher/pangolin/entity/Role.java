package cn.fintecher.pangolin.entity;

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
@Table(name = "role")
@Data
@ApiModel(value = "role", description = "角色信息管理")
public class Role extends BaseEntity {
    @ApiModelProperty("特定公司的标识")
    private String companyCode;
    @ApiModelProperty("角色名称")
    private String name;
    @ApiModelProperty("角色状态 0：启用 1：停用")
    private Integer status;
    @ApiModelProperty("描述")
    private String remark;
    @ApiModelProperty("创建人用户名")
    private String operator;
    @ApiModelProperty("创建时间")
    private Date operateTime;
    @ManyToMany
    @JoinTable(name = "role_resource", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "reso_id"))
    @Transient
    private Set<Resource> resources;

    @ManyToMany
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    @Transient
    private Set<User> users;

}
