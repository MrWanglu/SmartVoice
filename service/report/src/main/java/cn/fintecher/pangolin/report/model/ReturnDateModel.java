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
public class ReturnDateModel {
    @ApiModelProperty(notes = "已还款年/月/周案件金额集合")
    private List<BigDecimal>  hadHAmtList = new ArrayList<>();
    @ApiModelProperty(notes = "已还款年/月/周案件数量集合")
    private List<Integer>  hadCountList = new ArrayList<>();
    @ApiModelProperty(notes = "还款审核中年/月/周金额集合")
    private List<BigDecimal>  AmtList = new ArrayList<>();
    @ApiModelProperty(notes = "已还款案件金额")
    private BigDecimal caseAmt = new BigDecimal(0.00);
    @ApiModelProperty(notes = "已还款案件数量")
    private Integer caseCount;
    @ApiModelProperty(notes = "还款审核中案件金额")
    private BigDecimal backCaseAmt = new BigDecimal(0.00);
    @ApiModelProperty(notes = "还款审核中案件数量")
    private Integer backCaseCount;
    @ApiModelProperty("还款类型 0 已还款 1 还款审核中")
    private Integer collectionStatus;
}
