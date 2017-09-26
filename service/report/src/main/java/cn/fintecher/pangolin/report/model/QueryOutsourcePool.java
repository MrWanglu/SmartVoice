package cn.fintecher.pangolin.report.model;

import lombok.Data;

import javax.persistence.Entity;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * @author : huyanmin
 * @Description : 委外催收
 * @Date : 2017/9/25
 */

@Entity
@Data
public class QueryOutsourcePool {

    /*受托方名称*/
    private String outsName;
    /*案件流入时间*/
    private Date comeOutsourceTime;
    /*案件批次号*/
    private String batchNumber;
    /*委案日期*/
    private Date outsourceTime;
    /*委外到期日期*/
    private Date overOutsourceTime;
    /*剩余委托时间(天)*/
    private BigInteger leftDay;
    /*案件总金额*/
    private BigDecimal outcaseTotalAmt;
    /*案件数量*/
    private String outcaseTotalCount;
    /*已催回案件数*/
    private String outcaseClosedCount;
    /*已催回案件数比例*/
    private String outcaseCountRate;
    /*已催回案件金额*/
    private BigDecimal outcaseClosedAmt;
    /*已催回案件金额比例*/
    private String outcaseAmtRate;
    /*公司标识码*/
    private String companyCode;


}