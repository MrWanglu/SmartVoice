package cn.fintecher.pangolin.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by ChenChang on 2017/7/12.
 */
@Entity
@Table(name = "personal_address")
@Data
public class PersonalAddress extends BaseEntity {
    @ApiModelProperty(notes = "关系：145-本人，146-配偶，147-父母，148-子女，149-亲属，150-同事，151-朋友，152-其他，153-亲兄弟姐妹，154-单位")
    private Integer relation;

    @ApiModelProperty(notes = "姓名")
    private String name;

    @ApiModelProperty(notes = "地址类型")
    private Integer type;

    @ApiModelProperty(notes = "地址状态")
    private Integer status;

    @ApiModelProperty(notes = "数据来源")
    private Integer source;

    @ApiModelProperty(notes = "详细地址")
    private String detail;

    @ApiModelProperty(notes = "客户ID")
    private String personalId;

    @ApiModelProperty(notes = "操作员")
    private String operator;

    @ApiModelProperty(notes = "操作时间")
    private Date operatorTime;

    @ApiModelProperty(notes = "经度")
    private BigDecimal longitude;

    @ApiModelProperty(notes = "纬度")
    private BigDecimal latitude;
}

