package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 客户房产信息实体
 * @Date : 10:36 2017/7/28
 */

@Entity
@Table(name = "personal_property")
@Data
public class PersonalProperty extends BaseEntity {
    @ApiModelProperty(notes = "客户信息ID")
    private String personalId;

    @ApiModelProperty(notes = "房产状况")
    private String status;

    @ApiModelProperty(notes = "房产归属")
    private String attribution;

    @ApiModelProperty(notes = "房产性质")
    private String kind;

    @ApiModelProperty(notes = "已购年限")
    private Integer years;

    @ApiModelProperty(notes = "房贷余额")
    private BigDecimal over;

    @ApiModelProperty(notes = "房产地址")
    private String address;

    @ApiModelProperty(notes = "中介姓名")
    private String agencyName;

    @ApiModelProperty(notes = "中介手机号")
    private String agencyPhone;

    @ApiModelProperty(notes = "征信编号")
    private String referenceNo;

    @ApiModelProperty(notes = "征信密码")
    private String referencePwd;

    @ApiModelProperty(notes = "征信验证码")
    private String referenceAuthCode;

    @ApiModelProperty(notes = "操作人")
    private String operator;

    @ApiModelProperty(notes = "操作时间")
    private Date operatorTime;
}