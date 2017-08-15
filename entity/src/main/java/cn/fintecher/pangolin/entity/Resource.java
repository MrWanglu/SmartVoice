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
    @ApiModelProperty("系统名称")
    private String sysName;
    @ApiModelProperty("资源名称")
    private String name;
    @ApiModelProperty("资源码")
    private String code;
    @ApiModelProperty("资源级别")
    private Integer level;
    @ApiModelProperty("状态")
    private Integer status;
    @ApiModelProperty("资源路径")
    private String path;
    @ApiModelProperty("资源图标")
    private String icon;
    @ApiModelProperty("资源类型")
    private Integer type;
    @ApiModelProperty("资源文件类型")
    private Integer fileType;
    @ApiModelProperty("备注")
    private String remark;
    @ApiModelProperty("创建人")
    private String operator;
    @ApiModelProperty("创建日期")
    private Date operateTime;
    @ApiModelProperty("备用字段")
    private String field;
    @ApiModelProperty("数据库排序标识")
    private Integer flag;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pid")
    @ApiModelProperty("父id")
    private Resource parent;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "role_resource", joinColumns = @JoinColumn(name = "reso_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @ApiModelProperty("关联的角色")
    private Set<Role> roles;
}
