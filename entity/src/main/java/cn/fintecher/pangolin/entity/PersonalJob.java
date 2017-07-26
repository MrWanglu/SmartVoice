package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 客户工作单位信息
 * @Date : 10:59 2017/7/26
 */

@Entity
@Table(name = "personal_job")
public class PersonalJob extends BaseEntity {
    @ApiModelProperty(notes = "客户信息ID")
    private String personalId;

    @ApiModelProperty(notes = "单位名称")
    private String companyName;

    @ApiModelProperty(notes = "部门")
    private String department;

    @ApiModelProperty(notes = "职务")
    private String position;

    @ApiModelProperty(notes = "职级")
    private String rank;

    @ApiModelProperty(notes = "单位性质")
    private String nature;

    @ApiModelProperty(notes = "单位规模")
    private String scale;

    @ApiModelProperty(notes = "单位固定电话")
    private String phone;

    @ApiModelProperty(notes = "单位成立时间")
    private Date createTime;

    @ApiModelProperty(notes = "单位地址")
    private String address;

    @ApiModelProperty(notes = "何时进入公司")
    private Date joinTime;

    @ApiModelProperty(notes = "基本月薪")
    private BigDecimal monthSalary;

    @ApiModelProperty(notes = "每月发薪日")
    private Integer payDay;

    @ApiModelProperty(notes = "所属行业")
    private String industry;

    @ApiModelProperty(notes = "发薪方式")
    private String payWay;

    @ApiModelProperty(notes = "现单位工作年限")
    private Integer workYear;

    @ApiModelProperty(notes = "操作员")
    private String operator;

    @ApiModelProperty(notes = "操作时间")
    private Date operatorTime;
}