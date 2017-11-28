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
    @ApiModelProperty(notes = "特定公司的标识")
    private String companyCode;

    @ApiModelProperty(notes = "角色名称")
    private String name;

    @ApiModelProperty(notes = "角色状态 0：启用 1：停用")
    private Integer status;

    @ApiModelProperty(notes = "描述")
    private String remark;

    @ApiModelProperty(notes = "创建人用户名")
    private String operator;

    @ApiModelProperty(notes = "创建时间")
    private Date operateTime;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "role_resource", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "reso_id"))
    @Transient
    private Set<Resource> resources;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    @Transient
    private Set<User> users;

}
