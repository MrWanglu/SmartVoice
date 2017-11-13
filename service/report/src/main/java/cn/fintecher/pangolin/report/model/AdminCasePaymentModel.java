package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by qijigui on 2017-11-11.
 */

@Data
public class AdminCasePaymentModel {

    @ApiModelProperty("每年/月/周案件金额")
    private BigDecimal caseAmount = new BigDecimal(0);
    @ApiModelProperty("案件总金额(万元)")
    private BigDecimal totalCaseAmount = new BigDecimal(0);
    @ApiModelProperty("每年/月/周案件数量")
    private Integer caesCount = 0;
    @ApiModelProperty("案件总数量")
    private Integer totalCaseCount = 0;
    @ApiModelProperty("查询类型 0 全部 1 内催 2 委外")
    private Integer queryType;
    @ApiModelProperty("月")
    private String queryMonth;
    @ApiModelProperty("日")
    private String queryDate;
    @ApiModelProperty("还款类型 0 已还款 1 还款审核中")
    private Integer collectionStatus;
}
