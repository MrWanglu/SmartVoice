package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author : gaobeibei
 * @Description : 移动定位实体
 * @Date : 11:15 2017/7/27
 */

@Entity
@Table(name = "mobile_position")
@Data
public class MobilePosition extends BaseEntity {
    @ApiModelProperty(notes = "主键ID")
    private String id;

    @ApiModelProperty(notes = "用户名")
    private String userName;

    @ApiModelProperty(notes = "部门")
    private String depCode;

    @ApiModelProperty(notes = "用户姓名")
    private String realName;

    @ApiModelProperty(notes = "经度")
    private BigDecimal longitude;

    @ApiModelProperty(notes = "纬度")
    private BigDecimal latitude;

    @ApiModelProperty(notes = "当前时间")
    private Date datetime;

    @ApiModelProperty(notes = "地址")
    private String address;
}