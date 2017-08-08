package cn.fintecher.pangolin.report.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 催收员每日催收结果报表
 * @Date : 9:26 2017/8/4
 */

@Entity
@Table(name = "daily_result_report")
@Data
public class DailyResultReport extends BaseEntity {
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

    @ApiModelProperty(notes = "案件数量")
    private Integer caseNum = 0;

    @ApiModelProperty(notes = "案件总金额")
    private BigDecimal caseAmt = new BigDecimal(0);

    @ApiModelProperty(notes = "当日回款户数")
    private Integer dayNum = 0;

    @ApiModelProperty(notes = "当日回款金额")
    private BigDecimal dayAmt = new BigDecimal(0);

    @ApiModelProperty(notes = "月累计回款户数")
    private Integer monthNum = 0;

    @ApiModelProperty(notes = "月累计回款金额")
    private BigDecimal monthAmt = new BigDecimal(0);

    @ApiModelProperty(notes = "当日回款户数比")
    private BigDecimal dayNumRate = new BigDecimal(0);

    @ApiModelProperty(notes = "当日回款金额比")
    private BigDecimal dayAmtRate = new BigDecimal(0);

    @ApiModelProperty(notes = "回款比")
    private BigDecimal backMoneyRate = new BigDecimal(0);

    @ApiModelProperty(notes = "月度回款金额目标")
    private BigDecimal target = new BigDecimal(0);

    @ApiModelProperty(notes = "当前报表日期")
    private Date nowDate;

    @ApiModelProperty(notes = "操作人")
    private String operatorUserName;

    @ApiModelProperty(notes = "操作人姓名")
    private String operatorRealName;

    @ApiModelProperty(notes = "操作时间")
    private Date operatorDate;

    @ApiModelProperty(notes = "公司code码")
    private String companyCode;
}