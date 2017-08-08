package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : xiaqun
 * @Description : 催收员业绩进展基础模型
 * @Date : 11:47 2017/8/3
 */

@Data
public class PerformanceBasisModel {
    @ApiModelProperty(notes = "本部门code码")
    private String deptCode;

    @ApiModelProperty(notes = "本部门名称")
    private String deptName;

    @ApiModelProperty(notes = "父部门code码")
    private String parentDeptCode;

    @ApiModelProperty(notes = "父部门名称")
    private String parentDeptName;

    @ApiModelProperty(notes = "用户名")
    private String userName;

    @ApiModelProperty(notes = "用户姓名")
    private String realName;

    @ApiModelProperty(notes = "待催收案件数")
    private Integer waitCollectNum;

    @ApiModelProperty(notes = "待催收案件总金额")
    private BigDecimal waitCollectAmt;

    @ApiModelProperty(notes = "催收中案件数")
    private Integer collectingNum;

    @ApiModelProperty(notes = "催收中案件总金额")
    private BigDecimal collectingAmt;

    @ApiModelProperty(notes = "逾期还款中案件数")
    private Integer overdueNum;

    @ApiModelProperty(notes = "逾期还款中案件总金额")
    private BigDecimal overdueAmt;

    @ApiModelProperty(notes = "提前结清中案件数")
    private Integer advanceNum;

    @ApiModelProperty(notes = "提前结清中案件总金额")
    private BigDecimal advanceAmt;

    @ApiModelProperty(notes = "承诺还款案件数")
    private Integer promiseNum;

    @ApiModelProperty(notes = "承诺还款案件总金额")
    private BigDecimal promiseAmt;

    @ApiModelProperty(notes = "结案案件数")
    private Integer endNum;

    @ApiModelProperty(notes = "结案案件总金额")
    private BigDecimal endAmt;

    @ApiModelProperty(notes = "公司code码")
    private String companyCode;
}