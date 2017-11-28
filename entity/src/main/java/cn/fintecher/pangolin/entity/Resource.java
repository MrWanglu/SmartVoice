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
@Table(name = "resource")
@Data
@ApiModel(value = "resource", description = "资源管理")
public class Resource extends BaseEntity {
    @ApiModelProperty(notes = "系统名称")
    private String sysName;

    @ApiModelProperty(notes = "资源名称")
    private String name;

    @ApiModelProperty(notes = "资源码")
    private String code;

    @ApiModelProperty(notes = "资源级别")
    private Integer level;

    @ApiModelProperty(notes = "状态")
    private Integer status;

    @ApiModelProperty(notes = "资源路径")
    private String path;

    @ApiModelProperty(notes = "资源图标")
    private String icon;

    @ApiModelProperty(notes = "资源类型")
    private Integer type;

    @ApiModelProperty(notes = "资源文件类型")
    private Integer fileType;

    @ApiModelProperty(notes = "备注")
    private String remark;

    @ApiModelProperty(notes = "创建人")
    private String operator;

    @ApiModelProperty(notes = "创建日期")
    private Date operateTime;

    @ApiModelProperty(notes = "备用字段")
    private String field;

    @ApiModelProperty(notes = "数据库排序标识")
    private Integer flag;

    @ApiModelProperty(notes = "父id")
    private String pid;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "role_resource", joinColumns = @JoinColumn(name = "reso_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @ApiModelProperty(notes = "关联的角色")
    private Set<Role> roles;
}
