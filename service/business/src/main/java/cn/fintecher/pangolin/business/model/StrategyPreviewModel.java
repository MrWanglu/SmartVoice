package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by sunyanping on 2017/10/11.
 */
@Data
@ApiModel("策略结果预览Model")
public class StrategyPreviewModel {
    @ApiModelProperty("案件编号")
    private String caseNumber;
    @ApiModelProperty("批次号")
    private String batchNumber;
    @ApiModelProperty("委托方")
    private String principalName;
    @ApiModelProperty("客户姓名")
    private String personalName;
    @ApiModelProperty("手机号")
    private String phone;
    @ApiModelProperty("身份证号")
    private String idCard;
    @ApiModelProperty("省份")
    private String province;
    @ApiModelProperty("城市")
    private String city;
    @ApiModelProperty("还款状态")
    private String payStatus;
    @ApiModelProperty("逾期天数")
    private Integer overdueDays;
    @ApiModelProperty("案件金额")
    private BigDecimal overdueAmount;
    @ApiModelProperty("佣金比例")
    private BigDecimal commissionRate;
    @ApiModelProperty("案件评分")
    private BigDecimal score;
    @ApiModelProperty("委案日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date delegationDate;
    @ApiModelProperty("结案日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date closeDate;
}
