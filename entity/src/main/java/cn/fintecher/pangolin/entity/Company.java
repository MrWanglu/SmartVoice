package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-03-11:59
 */
@Entity
@Table(name = "company")
@Data
@ApiModel(value = "company", description = "注册公司的信息")
public class Company extends BaseEntity {
    @ApiModelProperty(notes = "公司中文名称")
    private String chinaName;

    @ApiModelProperty(notes = "公司英文名称")
    private String engName;

    @ApiModelProperty(notes = "状态")
    private Integer status;

    @ApiModelProperty(notes = "公司code")
    private String code;

    @ApiModelProperty(notes = "公司法人")
    private String legPerson;

    @ApiModelProperty(notes = "公司地址")
    private String address;

    @ApiModelProperty(notes = "公司城市")
    private String city;

    @ApiModelProperty(notes = "公司电话")
    private String phone;

    @ApiModelProperty(notes = "公司传真")
    private String fax;

    @ApiModelProperty(notes = "公司联系人")
    private String contactPerson;

    @ApiModelProperty(notes = "创建人")
    private String operator;

    @ApiModelProperty(notes = "创建时间")
    private Date operateTime;

    @ApiModelProperty(notes = "备用字段")
    private String field;

    @ApiModelProperty(notes = "公司序列码")
    private String sequence;

    @ApiModelProperty(notes = "注册天数")
    private Integer registerDay;
}
