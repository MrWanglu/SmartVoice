package cn.fintecher.pangolin.report.entity.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by ChenChang on 2017/11/28.
 */
@Data
public class CaseInfoDistributedListResponse {
    private String id;
    @ApiModelProperty("案件编号")
    private String caseNumber;
    @ApiModelProperty("批次号")
    private String batchNumber;
    @ApiModelProperty("委托方")
    private String principalName;
    @ApiModelProperty("申请城市")
    private String areaAreaName;
    @ApiModelProperty("客户姓名")
    private String personalInfoName;
    @ApiModelProperty("手机号")
    private String personalInfoMobileNo;
    @ApiModelProperty("逾期天数")
    private Integer overdueDays;
    @ApiModelProperty("逾期期数")
    private Integer overduePeriods;
    @ApiModelProperty("案件金额")
    private BigDecimal overdueAmount = new BigDecimal(0);
    @ApiModelProperty("案件手数")
    private Integer handNumber;
    @ApiModelProperty("佣金比例")
    private BigDecimal commissionRate = new BigDecimal(0);
    @ApiModelProperty("案件评分")
    private BigDecimal score = new BigDecimal(0);
    @ApiModelProperty("委案日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date delegationDate;
    @ApiModelProperty("结案日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date closeDate;
}
