package cn.fintecher.pangolin.report.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 每日催收过程报表
 * @Date : 16:40 2017/8/3
 */

@Entity
@Table(name = "daily_process_report")
@Data
public class DailyProcessReport extends BaseEntity {
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

    @ApiModelProperty(notes = "案件金额")
    private BigDecimal caseAmt = new BigDecimal(0);

    @ApiModelProperty(notes = "当日处理案件数量")
    private Integer handleNum = 0;

    @ApiModelProperty(notes = "承诺还款案件数量")
    private Integer promiseNum = 0;

    @ApiModelProperty(notes = "协商跟进案件数量")
    private Integer consultNum = 0;

    @ApiModelProperty(notes = "他人转告案件数量")
    private Integer otherTellNum = 0;

    @ApiModelProperty(notes = "查找案件数量")
    private Integer findNum = 0;

    @ApiModelProperty(notes = "无人应答案件数量")
    private Integer noAnswerNum = 0;

    @ApiModelProperty(notes = "每日呼叫数量")
    private Integer callNum = 0;

    @ApiModelProperty(notes = "每日有效呼叫数量")
    private Integer effectiveCallNum = 0;

    @ApiModelProperty(notes = "每日通话时长")
    private Integer callDuration = 0;

    @ApiModelProperty(notes = "沟通记录条数")
    private Integer communicateNum = 0;

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