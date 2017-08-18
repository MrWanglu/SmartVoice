package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-17-14:23
 */
@Data
public class SmaDetailReport {
    @ApiModelProperty(notes = "坐席工号")
    private String agNum;

    @ApiModelProperty(notes = "业务组ID")
    private String groupId;

    @ApiModelProperty(notes = "坐席名称")
    private String agName;

    @ApiModelProperty(notes = "坐席号码")
    private String agPhone;

    @ApiModelProperty(notes = "客户号码")
    private String cusPhone;

    @ApiModelProperty(notes = "技能组名称")
    private String queName;

    @ApiModelProperty(notes = "呼叫类型")
    private String callType;

    @ApiModelProperty(notes = "呼叫ID")
    private String callId;

    @ApiModelProperty(notes = "开始时间")
    private Date startTime;

    @ApiModelProperty(notes = "客户号码区号")
    private String cusPhoneAreacode;

    @ApiModelProperty(notes = "客户号码地区名")
    private String cusPhoneAreaname;

    @ApiModelProperty(notes = "结束时间")
    private Date endTime;

    @ApiModelProperty(notes = "通话时长")
    private Integer connSecs;

    @ApiModelProperty(notes = "坐席号码区号")
    private String agPhoneAreacode;

    @ApiModelProperty(notes = "坐席号码地区名")
    private String agPhoneAreaname;

    @ApiModelProperty(notes = "呼叫结果")
    private Integer result;

    @ApiModelProperty(notes = "结果")
    private String endresult;
}
