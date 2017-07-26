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
    @ManyToOne
    @JoinColumn(name = "series_id")
    private ProductSeries productSeries;

}
