package cn.fintecher.pangolin.report.model;

import cn.fintecher.pangolin.entity.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by huyanmin on 2017/9/27.
 */
@Data
public class ExcportOutsourceResultModel {
    private String id;
    private String batchNumberOutsource;
    private String caseNumber;
    private Personal personalInfo;
    private List<CaseFollowupRecord> outsourceFollowRecords;
    private AreaCode areaCode;
    private Principal principal;
    private Product product;
    private String contractNumber;
    private String outsName;
    private BigDecimal outsourceTotalAmount;
    private BigDecimal hasPayAmountOutsource = new BigDecimal(0);
    private BigDecimal leftAmount;
    private Integer leftDaysOutsource;
    private Date outTime;
    private Date endOutTime;
    private Date overOutTime;
    private Integer outStatus;
    private BigDecimal commissionRateOutsource;
    private String batchNumber;
    private BigDecimal contractAmount;
    private BigDecimal overdueAmount;
    private BigDecimal overdueCapital;
    private BigDecimal leftCapital;
    private Integer periods;
    private BigDecimal overdueInterest;
    private Date perDueDate;
    private Integer overduePeriods;
    private Integer overdueDays;
    private BigDecimal hasPayAmount;
    private Integer hasPayPeriods;
    private Integer leftDays;
    private String payStatus;
    private Integer collectionStatus;
    private BigDecimal commissionRate;
    private Date loanDate;
    private BigDecimal overdueManageFee;
    private Integer followupBack;
    private String principalName;
    private String productName;
}
