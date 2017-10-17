package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by yuanyanting on 2017/7/31.
 */

@Entity
@Table(name = "user_device")
@Data
@ApiModel(value = "user_device",description = "用户登录设备管理")
public class UserDevice extends BaseEntity{
    @ApiModelProperty("登录设备的id")
    private String id;

    @ApiModelProperty("特定公司的标识")
    @JoinColumn(name = "company_code")
    private String companyCode;

    @ApiModelProperty("设备编号")
    private String code;

    @ApiModelProperty("设备类型:移动端、PC端")
    private Integer type;

    @ApiModelProperty("设备名称")
    private String name;

    @ApiModelProperty("是否开启设备验证：是、否")
    private Integer validate;

    @ApiModelProperty("状态：启用、停用")
    private Integer status;

    @ApiModelProperty("创建人")
    private String operator;

    @ApiModelProperty("创建日期")
    @JoinColumn(name = "operate_time")
    private Date operateTime;

    @ApiModelProperty("备用字段")
    private String field;

    @ApiModelProperty("MAC地址")
    private String mac;

    @Column(name = "user_id")
    private String userId;


}
