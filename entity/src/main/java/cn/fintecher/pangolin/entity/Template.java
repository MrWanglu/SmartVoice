package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Created by luqiang on 2017/7/24.
 */
@Data
@Entity
@ApiModel
@Table(name = "acc_template")
public class Template extends BaseEntity {

    @ApiModelProperty(notes = "不是默认")
    public static final Boolean DEFAULT_NO = false;
    @ApiModelProperty(notes = "默认")
    public static final Boolean DEFAULT_YES = true;

    @ApiModelProperty(notes = "语音文件ID")
    private String fileId;

    @NotNull
    @Pattern(regexp = "[A-Za-z0-9]+", message = "编号只能包含是字母,数字")
    @Size(min = 1, max = 40, message = "编号长度不能小于1位大于40字符")
    @ApiModelProperty(notes = "编号")
    private String templateCode;

    @NotNull
    @Pattern(regexp = "^([\\u4e00-\\u9fa5]+|([a-zA-Z]+\\s?)+)$", message = "名称只能汉字或者英文")
    @Size(min = 1, max = 200, message = "名称不能超过200个字符")
    @ApiModelProperty(notes = "名称")
    private String templateName;

    @ApiModelProperty(notes = "是否是默认模板 false: 否 true: 是")
    private Boolean isDefault;

    @ApiModelProperty(notes = "模板形式 29: 短信 30: 语音 31: 电子邮件 32: 信函 33: 电催话术")
    private Integer templateStyle;

    @ApiModelProperty(notes = "模板类别 34: 提醒 35: 催收 ")
    private Integer templateType;

    @ApiModelProperty(notes = "状态 0: 启用 1: 停用")
    private Integer templateStatus;

    @ApiModelProperty(notes = "短信内容")
    private String messageContent;

    @ApiModelProperty(notes = "创建时间")
    private Date createTime;

    @ApiModelProperty(notes = "操作人 人员ID")
    private String creator;

    @ApiModelProperty(notes = "更新时间")
    private Date updateTime;

}
