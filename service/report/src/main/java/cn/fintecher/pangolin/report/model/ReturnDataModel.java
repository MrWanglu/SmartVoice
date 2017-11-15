package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author : huyanmin
 * @Description :
 * @Date : 2017/11/11.
 */
@Entity
@Data
public class ReturnDataModel {
    @ApiModelProperty(notes = "已还款年/月/周案件金额集合")
    private List<BigDecimal> hadAmountList = new ArrayList<>();
    @ApiModelProperty(notes = "已还款年/月/周案件数量集合")
    private List<Integer> hadCountList = new ArrayList<>();
    @ApiModelProperty(notes = "还款审核中年/月/周金额集合")
    private List<BigDecimal> applyAmountList = new ArrayList<>();
    @ApiModelProperty(notes = "还款审核中年/月/周案件数量集合")
    private List<Integer> applyCountList = new ArrayList<>();
    @ApiModelProperty(notes = "已还款案件金额")
    private BigDecimal hadTotalCaseAmount = new BigDecimal(0.00);
    @ApiModelProperty(notes = "已还款案件数量")
    private Integer hadTotalCaseCount = 0;
    @ApiModelProperty(notes = "还款审核中案件金额")
    private BigDecimal applyTotalCaseAmount = new BigDecimal(0.00);
    @ApiModelProperty(notes = "还款审核中案件数量")
    private Integer applyTotalCaseCount = 0;
}
