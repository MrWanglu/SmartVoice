package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 客户车产信息实体
 * @Date : 10:47 2017/7/26
 */

@Entity
@Table(name = "personal_car")
@Data
public class PersonalCar extends BaseEntity {
    @ApiModelProperty(notes = "车牌号码")
    private String no;

    @ApiModelProperty(notes = "购车价格")
    private BigDecimal prices;

    @ApiModelProperty(notes = "车辆类型")
    private String type;

    @ApiModelProperty(notes = "是否二手车 0-不是 1-是")
    private Integer secondHandFlag;

    @ApiModelProperty(notes = "初次登记时间")
    private Date registerTime;

    @ApiModelProperty(notes = "抵押登记次数")
    private Integer mortgageNum;

    @ApiModelProperty(notes = "车身颜色")
    private String color;

    @ApiModelProperty(notes = "保险单类型")
    private String insuranceType;

    @ApiModelProperty(notes = "购买方式")
    private String buyType;

    @ApiModelProperty(notes = "贷款方式")
    private String loanType;

    @ApiModelProperty(notes = "客户信息")
    private String personalId;

    @ApiModelProperty(notes = "操作员")
    private String operator;

    @ApiModelProperty(notes = "操作时间")
    private Date operatorTime;
}