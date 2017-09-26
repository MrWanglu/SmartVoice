package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by Administrator on 2017/9/20.
 */
@Data
public class OutDistributeInfo {
    private String outCode; //委外方code
    private String outName; //委外方name
    private Integer caseCount;//案件数量
    private Integer endCount;//结案数量
    private BigDecimal endAmt;//结案金额
    private BigDecimal caseAmt;//案件金额
}
