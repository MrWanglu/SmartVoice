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
    @ApiModelProperty("每年/月/周案件数量")
    private Integer caseCount = 0;
    @ApiModelProperty("月")
    private String queryMonth;
    @ApiModelProperty("日")
    private String queryDate;
    @ApiModelProperty("周")
    private String queryWeek;
}
