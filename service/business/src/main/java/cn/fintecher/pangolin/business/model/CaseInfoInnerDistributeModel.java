package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by qijigui on 2017-10-10.
 */

@Data
public class CaseInfoInnerDistributeModel {
    private String userName; //催收员名称
    private String userRealName; //催收员真实名称
    private String departmentName; //机构名称
    private Integer distributeType; //分配类型 0 分配给机构 1 分配给用户
    private Integer caseCurrentCount; //当前案件数
    private BigDecimal caseMoneyCurrentCount; //当前案件总金额
    private Integer caseDistributeCount; //确认分配案件数
    private BigDecimal caseDistributeMoneyCount; //确认分配案件总金额
    private Integer caseTotalCount; //分后后案件总数
    private BigDecimal caseMoneyTotalCount; //分配后案件金额总数
}
