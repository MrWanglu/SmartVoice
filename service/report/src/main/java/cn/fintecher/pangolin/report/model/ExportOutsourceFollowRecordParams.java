package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by huyanmin on 2017/9/27.
 */
@Data
@ApiModel("导出跟进记录")
public class ExportOutsourceFollowRecordParams {

    @ApiModelProperty(notes = "导出案件标识符，0 案件导出 1 催收中跟踪记录 2 已结案跟进记录")
    private Integer type;
    @ApiModelProperty(notes = "选择的批次号集合")
    private List<String> batchNumberList;
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
    @ApiModelProperty(notes = "批次号")
    private String batchNumber;
    @ApiModelProperty(notes = "逾期天数开始")
    private Integer overDayStart;
    @ApiModelProperty(notes = "逾期天数结束")
    private Integer overDayEnd;
    @ApiModelProperty(notes = "案件金额")
    private BigDecimal outCaseAmountStart;
    @ApiModelProperty(notes = "案件金额")
    private BigDecimal outCaseAmountEnd;
    @ApiModelProperty(notes = "佣金比例开始")
    private BigDecimal commissionRateStart;
    @ApiModelProperty(notes = "佣金比例")
    private BigDecimal commissionRateEnd;
    @ApiModelProperty(notes = "委案日期")
    private String outTime;
    @ApiModelProperty(notes = "到期日期")
    private String overOutsourceTime;
    @ApiModelProperty(notes = "结案日期")
    private String endOutsourceTime;
    @ApiModelProperty(notes = "委托方")
    private List<String> outsNameList;
    @ApiModelProperty(notes = "公司Code")
    private String companyCode;
    @ApiModelProperty(notes = "委外方")
    private String outsName;
    @ApiModelProperty(notes = "还款状态")
    private String overduePeriods;

}
