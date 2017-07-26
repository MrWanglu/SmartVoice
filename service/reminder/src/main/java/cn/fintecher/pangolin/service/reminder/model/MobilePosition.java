package cn.fintecher.pangolin.service.reminder.model;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 移动定位实体
 * @Date : 11:15 2017/5/31
 */
@Data
@Document
@ApiModel(value = "MobilePosition",
        description = "app移动定位")
public class MobilePosition implements Serializable {
    @ApiModelProperty(notes = "主键ID")
    @Id
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