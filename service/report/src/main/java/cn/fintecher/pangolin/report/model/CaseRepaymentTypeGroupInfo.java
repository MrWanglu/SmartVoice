package cn.fintecher.pangolin.report.model;


import lombok.Data;

import java.math.BigDecimal;

/*
* Auther: huangrui
* Date: 2017年11月11日
* Desc: 不同日期类型下各种回款类型的统计
* */
@Data
public class CaseRepaymentTypeGroupInfo {

    //还款类型
    private String rePaymentType;
    //共计还款金额
    private BigDecimal totalRePaymentMoney = new BigDecimal(0);
    //总共案件数量
    private  Integer totalCaseNumber = 0;

}


