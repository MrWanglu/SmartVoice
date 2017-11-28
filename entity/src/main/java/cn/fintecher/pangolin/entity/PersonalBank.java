package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by ChenChang on 2017/7/12.
 */
@Entity
@Table(name = "personal_bank")
@Data
public class PersonalBank extends BaseEntity {
    @ApiModelProperty(notes = "账户类型")
    private String accountType;

    @ApiModelProperty(notes = "开户银行")
    private String depositBank;

    @ApiModelProperty(notes = "开户支行")
    private String depositBranch;

    @ApiModelProperty(notes = "银行卡号")
    private String cardNumber;

    @ApiModelProperty(notes = "开户省份")
    private String depositProvince;

    @ApiModelProperty(notes = "开户城市")
    private String depositCity;

    @ApiModelProperty(notes = "操作员")
    private String operator;

    @ApiModelProperty(notes = "操作时间")
    private Date operatorTime;

    @ApiModelProperty(notes = "客户ID")
    private String personalId;

    @ApiModelProperty(notes = "账户号")
    private String accountNumber;

}
