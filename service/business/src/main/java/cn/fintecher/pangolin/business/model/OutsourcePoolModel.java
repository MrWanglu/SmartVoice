package cn.fintecher.pangolin.business.model;

import lombok.Data;

import javax.persistence.Entity;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * Created by huyanmin on 2017/9/20.
 *
 * 委外催收中Model
 *
 */
@Entity
@Data
public class OutsourcePoolModel {

    /*受托方名称*/
    private String outsourceName;
    /*案件流入时间*/
    private Date comeOutsourceTime;
    /*委案日期*/
    private Date delegationDate;
    /*结案日期*/
    private Date closedDate;
    /*剩余委托时间(天)*/
    private BigInteger leftDay;
    /*案件总金额*/
    private BigDecimal outcaseTotalAmt;
    /*案件数量*/
    private String outcaseTotalCount;
    /*已催回案件数*/
    private String outcaseClosedCount;
    /*已催回案件数比例*/
    private String outcaseCountRatio;
    /*已催回案件金额*/
    private BigDecimal outcaseClosedAmt;
    /*已催回案件金额比例*/
    private String outcaseAmtRatio;
    /*案件批次号*/
    private String batchNumber;
}
