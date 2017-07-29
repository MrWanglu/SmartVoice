package cn.fintecher.pangolin.entity;

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
    private String prodcutCode;
    private String prodcutName;
    private Integer periods;
    private BigDecimal contractRate;
    private BigDecimal multipleRate;
    private Integer payWay;
    private Integer productStatus;
    private BigDecimal interestRate;
    private String operator;
    private Date operatorTime;
    private String companyCode;
    private BigDecimal yearRate; //年化利率
    private BigDecimal interestAmt; //罚息金额
    private BigDecimal prepaymentAmount; //提前还款违约金
    private BigDecimal prepaymentRate; //提前还款违约金费率
    private BigDecimal insServiceFee; //分期服务费
    private BigDecimal insServiceRate; //分期服务费率
    @ManyToOne
    @JoinColumn(name = "series_id")
    private ProductSeries productSeries;

}
