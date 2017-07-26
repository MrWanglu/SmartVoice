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
    @ApiModelProperty("公司中文名称")
    private String chinaName;
    @ApiModelProperty("公司英文名称")
    private String engName;
    @ApiModelProperty("状态")
    private Integer status;
    @ApiModelProperty("公司code")
    private String code;
    @ApiModelProperty("公司法人")
    private String legPerson;
    @ApiModelProperty("公司地址")
    private String address;
    @ApiModelProperty("公司城市")
    private String city;
    @ApiModelProperty("公司电话")
    private String phone;
    @ApiModelProperty("公司传真")
    private String fax;
    @ApiModelProperty("公司联系人")
    private String contactPerson;
    @ApiModelProperty("创建人")
    private String operator;
    @ApiModelProperty("创建时间")
    private Date operateTime;
    @ApiModelProperty("备用字段")
    private String field;
}
