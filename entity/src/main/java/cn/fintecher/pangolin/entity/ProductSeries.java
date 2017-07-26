package cn.fintecher.pangolin.entity;

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
    private String seriesName;
    private Integer seriesStatus;
    private Integer seriesFlag;
    private String operator;
    private Date operatorTime;
    //Fixme 修改成关系的
    private String principal_id;
    private String companyCode;

}
