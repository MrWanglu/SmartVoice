package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by sunyanping on 2017/10/14.
 */
@Data
@ApiModel("策略预览结果参数")
public class PreviewParams {
    @ApiModelProperty(notes = "策略JSON",required = true)
    private String jsonString;
    @ApiModelProperty(notes = "策略类型：230-案件导入分配策略，231-内催池分配策略，232-委外池分配策略", required = true)
    private Integer type;
    @ApiModelProperty("客户姓名")
    private String personalName;
    @ApiModelProperty("手机号")
    private String phone;
    @ApiModelProperty("身份证号")
    private String idCard;
    @ApiModelProperty("批次号")
    private String batchNumber;
    @ApiModelProperty("案件金额（最小）")
    private BigDecimal startAmount;
    @ApiModelProperty("案件金额（最大）")
    private BigDecimal endAmount;
    @ApiModelProperty("公司Code")
    private String companyCode;
}
