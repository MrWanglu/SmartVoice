package cn.fintecher.pangolin.report.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 短信统计报表实体
 * @Date : 9:24 2017/9/4
 */

@Data
public class SmsReport extends BaseEntity {
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

    @ApiModelProperty(notes = "排名")
    private Integer rank;

    @ApiModelProperty(notes = "发送短信数量")
    private Integer smsNum;

    @ApiModelProperty(notes = "发送成功数量")
    private Integer successNum;

    @ApiModelProperty(notes = "发送失败数量")
    private Integer failureNum;

    @ApiModelProperty(notes = "当前日期")
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