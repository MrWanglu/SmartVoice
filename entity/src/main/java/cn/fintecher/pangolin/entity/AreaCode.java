package cn.fintecher.pangolin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
/**
 * Created by ChenChang on 2017/7/12.
 */
@Entity
@Table(name = "area_code")
@Data
public class AreaCode implements Serializable {
    @Id
    @ApiModelProperty(notes = "主键ID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    @ApiModelProperty(notes = "父级节点")
    private AreaCode parent;

    @ApiModelProperty(notes = "地域路由")
    private String treePath;

    @ApiModelProperty(notes = "地域编码")
    private String areaCode;

    @ApiModelProperty(notes = "地域名称")
    private String areaName;

    @ApiModelProperty(notes = "地区名称拼音")
    private String areaEnglishName;

    @ApiModelProperty(notes = "银行地域支付编码")
    private String bankCode;

    @ApiModelProperty(notes = "地区邮编")
    private String zipCode;

    @ApiModelProperty(notes = "区号")
    private String zoneCode;

    @ApiModelProperty(notes = "操作员")
    private String operator;

    @ApiModelProperty(notes = "操作时间")
    private Date operatorTime;
}
