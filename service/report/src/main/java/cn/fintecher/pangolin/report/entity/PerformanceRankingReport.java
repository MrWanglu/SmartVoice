package cn.fintecher.pangolin.report.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 催收员业绩排名报表
 * @Date : 9:54 2017/8/21
 */

@Data
public class PerformanceRankingReport extends BaseEntity {
    @ApiModelProperty(notes = "部门code码")
    private String deptCode;

    @ApiModelProperty(notes = "部门名称")
    private String deptName;

    @ApiModelProperty(notes = "父部门code码")
    private String parentDeptCode;

    @ApiModelProperty(notes = "父部门名称")
    private String parentDeptName;

    @ApiModelProperty(notes = "用户名")
    private String userName;

    @ApiModelProperty(notes = "用户名称")
    private String realName;

    @ApiModelProperty(notes = "案件数量")
    private Integer caseNum = 0;

    @ApiModelProperty(notes = "当日回款金额")
    private BigDecimal dayBackMoney = new BigDecimal(0);

    @ApiModelProperty(notes = "月累计回款金额")
    private BigDecimal monthBackMoney = new BigDecimal(0);

    @ApiModelProperty(notes = "排名")
    private Integer rank;

    @ApiModelProperty(notes = "月度回款金额目标")
    private BigDecimal target = new BigDecimal(0);

    @ApiModelProperty(notes = "月度目标差距")
    private BigDecimal targetDisparity = new BigDecimal(0);

    @ApiModelProperty(notes = "统计日期")
    private Date nowDate;

    @ApiModelProperty(notes = "操作人")
    private String operatorUserName;

    @ApiModelProperty(notes = "操作人姓名")
    private String operatorRealName;

    @ApiModelProperty(notes = "操作日期")
    private Date operatorDate;

    @ApiModelProperty(notes = "公司code码")
    private String companyCode;

    @ApiModelProperty(notes = "组长名称")
    @Transient
    private String manageName;
}