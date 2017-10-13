package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by sunyanping on 2017/9/18.
 */
@Data
@ApiModel("导出跟进记录")
public class ExportFollowRecordParams {
    @ApiModelProperty(notes = "选择的案件编号集合")
    private List<String> caseNumberList;
    @ApiModelProperty(notes = "导出项集合")
    private List<String> exportItemList;
    @ApiModelProperty(notes = "客户姓名")
    private String personalName;
    @ApiModelProperty(notes = "客户手机号")
    private String phone;
    @ApiModelProperty(notes = "申请省份")
    private Integer provinceId;
    @ApiModelProperty(notes = "申请城市")
    private Integer cityId;
    @ApiModelProperty(notes = "批次号集合")
    private List<String> batchNumberList;
    @ApiModelProperty(notes = "还款状态")
    private String payStatus;
    @ApiModelProperty(notes = "逾期天数开始")
    private Integer overDayStart;
    @ApiModelProperty(notes = "逾期天数结束")
    private Integer overDayEnd;
    @ApiModelProperty(notes = "催收员")
    private String currentCollector;
    @ApiModelProperty(notes = "案件金额")
    private BigDecimal overDueAmountStart;
    @ApiModelProperty(notes = "案件金额")
    private BigDecimal overDueAmountEnd;
    @ApiModelProperty(notes = "回款金额")
    private BigDecimal payAmtStart;
    @ApiModelProperty(notes = "回款金额")
    private BigDecimal payAmtEnd;
    @ApiModelProperty(notes = "手数")
    private Integer handNumberStart;
    @ApiModelProperty(notes = "手数")
    private Integer handNumberEnd;
    @ApiModelProperty(notes = "佣金比例")
    private BigDecimal commissionRateStart;
    @ApiModelProperty(notes = "佣金比例")
    private BigDecimal commissionRateEnd;
    @ApiModelProperty(notes = "催收状态")
    private Integer collectionStatus;
    @ApiModelProperty(notes = "委托方ID")
    private String principalId;
    @ApiModelProperty(notes = "是否协催")
    private Integer assistFlag;
    @ApiModelProperty("催收反馈")
    private Integer followupBack;
    @ApiModelProperty(notes = "协催方式")
    private Integer assistWay;
    @ApiModelProperty(notes = "催收类型")
    private Integer collectionType;
    @ApiModelProperty(notes = "机构Code")
    private String departmentCode;
    @ApiModelProperty(notes = "公司Code")
    private String companyCode;
    @ApiModelProperty(notes = "委案日期")
    private String delegationDate;
    @ApiModelProperty(notes = "结案日期")
    private String closeDate;
    @ApiModelProperty(notes = "标志：0 催收中 1 已结案")
    private Integer type;
}
