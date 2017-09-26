package cn.fintecher.pangolin.report.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Administrator on 2017/9/25.
 */
@Data
public class CollectingCaseInfo {
    private String batchNumber;
    private String name;
    private Date caseFollowInTime;
    private Date delegationDate;
    private Date closeDate;
    private Integer leftDays;
    private BigDecimal caseAmt;
    private Integer caseNum;
    private BigDecimal endCaseAmt;
    private Integer endCaseNum;
    private BigDecimal numRate;
    private BigDecimal amtRate;
}
