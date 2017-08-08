package cn.fintecher.pangolin.report.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 回款报表实体
 * @Date : 19:31 2017/8/1
 */

@Entity
@Table(name = "back_money_report")
@Data
public class BackMoneyReport extends BaseEntity {
    @ApiModelProperty(notes = "本部门code码")
    private String deptCode;

    @ApiModelProperty(notes = "本部门名称")
    private String deptName;

    @ApiModelProperty(notes = "上级部门code码")
    private String parentDeptCode;

    @ApiModelProperty(notes = "上级部门名称")
    private String parentDeptName;

    @ApiModelProperty(notes = "用户名")
    private String userName;

    @ApiModelProperty(notes = "用户姓名")
    private String realName;

    @ApiModelProperty(notes = "回款金额")
    private BigDecimal backMoney = new BigDecimal(0);

    @ApiModelProperty(notes = "回款时间")
    private Date backDate;

    @ApiModelProperty(notes = "操作人")
    private String operatorUserName;

    @ApiModelProperty(notes = "操作人姓名")
    private String operatorRealName;

    @ApiModelProperty(notes = "操作时间")
    private Date operatorDate;

    @ApiModelProperty(notes = "公司code码")
    private String companyCode;
}