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
    private BigDecimal amount = new BigDecimal(0);
    @ApiModelProperty("案件总金额(万元)")
    private BigDecimal totalAmount = new BigDecimal(0);
    @ApiModelProperty("每年/月/周案件数量")
    private Integer caesCount = 0;
    @ApiModelProperty("案件总数量")
    private Integer totalCaseCount = 0;
    @ApiModelProperty("查询类型 0 全部 1 内催 2 委外")
    private Integer queryType;
    @ApiModelProperty("还款类型 0 已还款 1 还款审核中")
    private Integer collectionStatus;
}
