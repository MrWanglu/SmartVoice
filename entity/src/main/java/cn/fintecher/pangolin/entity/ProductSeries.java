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
@Table(name = "product_series")
@Data
public class ProductSeries extends BaseEntity {
    @ApiModelProperty(notes = "产品系列名称")
    private String seriesName;

    @ApiModelProperty(notes = "产品系列状态")
    private Integer seriesStatus;

    @ApiModelProperty(notes = "产品内外部标识0本部1外部")
    private Integer seriesFlag;

    @ApiModelProperty(notes = "操作员")
    private String operator;

    @ApiModelProperty(notes = "操作时间")
    private Date operatorTime;

    @ApiModelProperty(notes = "委托方ID")
    private String principal_id;

    @ApiModelProperty(notes = "公司code码")
    private String companyCode;

}
