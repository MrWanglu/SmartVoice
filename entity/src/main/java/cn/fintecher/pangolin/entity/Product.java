package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by ChenChang on 2017/7/12.
 */
@Entity
@Table(name = "product")
@Data
public class Product extends BaseEntity {
    @ApiModelProperty(notes = "产品code")
    private String prodcutCode;

    @ApiModelProperty(notes = "产品名称")
    private String prodcutName;

    @ApiModelProperty(notes = "期数")
    private Integer periods;

    @ApiModelProperty(notes = "合同利率")
    private BigDecimal contractRate;

    @ApiModelProperty(notes = "综合利率")
    private BigDecimal multipleRate;

    @ApiModelProperty(notes = "还款方式")
    private Integer payWay;

    @ApiModelProperty(notes = "产品状态")
    private Integer productStatus;

    @ApiModelProperty(notes = "罚息利率")
    private BigDecimal interestRate;

    @ApiModelProperty(notes = "操作员")
    private String operator;

    @ApiModelProperty(notes = "操作时间")
    private Date operatorTime;

    @ApiModelProperty(notes = "公司code码")
    private String companyCode;

    @ApiModelProperty(notes = "年化利率")
    private BigDecimal yearRate;

    @ApiModelProperty(notes = "罚息金额")
    private BigDecimal interestAmt;

    @ApiModelProperty(notes = "提前还款违约金")
    private BigDecimal prepaymentAmount;

    @ApiModelProperty(notes = "提前还款违约金费率")
    private BigDecimal prepaymentRate;

    @ApiModelProperty(notes = "分期服务费")
    private BigDecimal insServiceFee;

    @ApiModelProperty(notes = "分期服务费率")
    private BigDecimal insServiceRate;

    @ManyToOne
    @JoinColumn(name = "series_id")
    @ApiModelProperty(notes = "产品系列ID")
    private ProductSeries productSeries;
}
