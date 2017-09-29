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
    private String batchNumber;
    private String caseNumber;
    private Personal personalInfo;
    private List<OutsourceFollowRecord> outsourceFollowRecords;
    private AreaCode areaCode;
    private String contractNumber;
    private String outsName;
    private BigDecimal outsourceTotalAmount;
    private BigDecimal hasPayAmount = new BigDecimal(0);
    private BigDecimal leftAmount;
    private Integer leftDays;
    private Date outTime;
    private Date endOutTime;
    private Date overOutTime;
    private Integer outsourceCaseStatus;
    private BigDecimal commissionRate;

}
