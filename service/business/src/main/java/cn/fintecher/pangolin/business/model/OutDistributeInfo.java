package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by huyanmin on 2017/9/20.
 */
@Data
public class OutDistributeInfo {
    private String outCode; //委外方code
    private String outName; //委外方name
    private Integer caseCount = new Integer(0);//案件数量
    private BigDecimal endAmt = new BigDecimal(0);//结案金额
    private Integer endCount = new Integer(0);;//结案数量
    private BigDecimal caseAmt= new BigDecimal(0);//案件金额
    private Integer collectionCount = new Integer(0);//当前案件数量
    private BigDecimal collectionAmt;//当前案件金额
    private BigDecimal successRate;//历史催收成功率 此实体用于分配策略选择委外方用
    private String outId; //委外方ID 此实体用于分配策略选择委外方用
    private Integer caseDistributeCount = new Integer(0); //确认分配案件数
    private BigDecimal caseDistributeMoneyCount = new BigDecimal(0); //确认分配案件总金额
    private Integer caseTotalCount = new Integer(0); //分后后案件总数
    private BigDecimal caseMoneyTotalCount = new BigDecimal(0); //分配后案件金额总数
}
