package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "AccOutsourcePoolModel", description = "委外导出时模板数据接收对象")
public class AccOutsourcePoolModel {
    @ApiModelProperty("案件编号")
    private String caseCode;

    @ApiModelProperty("客户姓名")
    private String custName;

    @ApiModelProperty("身份证号码")
    private String idCardNumber;

    @ApiModelProperty("手机号码")
    private String phoneNumber;

    @ApiModelProperty("产品系列")
    private String productSeries;

    @ApiModelProperty("产品名称")
    private String productName;

    @ApiModelProperty("合同金额")
    private String contractAmount;

    @ApiModelProperty("逾期总金额")
    private String overDueAmount;

    @ApiModelProperty("逾期本金（元）")
    private String overDueCapital;

    @ApiModelProperty("逾期利息（元）")
    private String overDueInterest;

    @ApiModelProperty("逾期罚息（元）")
    private String overDueDisincentive;

    @ApiModelProperty("逾期滞纳金（元）")
    private String overDueFine;

    @ApiModelProperty("还款期数")
    private String payPeriods;

    @ApiModelProperty("每期还款日")
    private String currentPayDate;

    @ApiModelProperty("每期还款金额（元）")
    private String currentAmount;

    @ApiModelProperty("逾期期数")
    private String overDuePeriods;

    @ApiModelProperty("逾期天数")
    private String overDueDays;

    @ApiModelProperty("已还款金额（元）")
    private String hasPayAmount;

    @ApiModelProperty("已还款期数")
    private String hasPayPeriods;

    @ApiModelProperty("最近还款日期")
    private String lastPayDate;

    @ApiModelProperty("最近还款金额（元）")
    private String lastPayAmount;

    @ApiModelProperty("家庭住址")
    private String homeAddress;

    @ApiModelProperty("工作单位名称")
    private String employerName;

    @ApiModelProperty("工作单位住址")
    private String employerAddress;

    @ApiModelProperty("工作单位电话")
    private String employerPhone;

    @ApiModelProperty("佣金比例（%）")
    private String commissionRate;

}
