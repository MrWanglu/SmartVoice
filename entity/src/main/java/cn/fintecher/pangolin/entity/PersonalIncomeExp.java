package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 客户收支信息实体
 * @Date : 10:59 2017/7/26
 */

@Entity
@Table(name = "personal_income_exp")
@Data
public class PersonalIncomeExp extends BaseEntity{
    @ApiModelProperty(notes = "客户ID")
    private String personalId;

    @ApiModelProperty(notes = "年收入")
    private BigDecimal annualIncome;

    @ApiModelProperty(notes = "每月工作收入")
    private BigDecimal monthIncome;

    @ApiModelProperty(notes = "公积金账号")
    private String providentAccount;

    @ApiModelProperty(notes = "公积金密码")
    private String providentPwd;

    @ApiModelProperty(notes = "每月其他收入")
    private BigDecimal monthOtherIncome;

    @ApiModelProperty(notes = "月均支出")
    private BigDecimal monthExp;

    @ApiModelProperty(notes = "社保账号")
    private String socialSecurityAccount;

    @ApiModelProperty(notes = "社保密码")
    private String socialSecurityPwd;

    @ApiModelProperty(notes = "供养人数")
    private Integer dependentsNumber;

    @ApiModelProperty(notes = "收入来源说明")
    private String incomeMemo;

    @ApiModelProperty(notes = "操作员")
    private String operator;

    @ApiModelProperty(notes = "操作时间")
    private Date operatorTime;
}
