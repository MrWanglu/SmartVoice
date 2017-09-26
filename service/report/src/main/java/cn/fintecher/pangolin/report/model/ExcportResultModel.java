package cn.fintecher.pangolin.report.model;

import cn.fintecher.pangolin.entity.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by sunyanping on 2017/9/18.
 */
@Data
public class ExcportResultModel {
    private String id;
    private String batchNumber;
    private String caseNumber;
    private Personal personalInfo;
    private List<CaseFollowupRecord> caseFollowupRecords;
    private AreaCode areaCode;
    private Principal principal;
    private Integer handNumber;
    private String contractNumber;
    private Date loanDate;
    private BigDecimal contractAmount;
    private BigDecimal leftCapital;
    private BigDecimal leftInterest;
    private BigDecimal overdueAmount;
    private BigDecimal overdueCapital;
    private BigDecimal overdueInterest;
    private BigDecimal overdueFine;
    private Integer periods;
    private BigDecimal perPayAmount;
    private BigDecimal otherAmt;
    private Date overDueDate;
    private Integer overduePeriods;
    private Integer overdueDays;
    private BigDecimal hasPayAmount = new BigDecimal(0);
    private Integer hasPayPeriods;
    private Date latelyPayDate;
    private BigDecimal latelyPayAmount;
    private BigDecimal commissionRate;
    private Product product;

}
