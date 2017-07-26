package cn.fintecher.pangolin.service.reminder.model;

import cn.fintecher.pangolin.entity.ReminderType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by  hukaijia.
 * Description:app信息推送
 * Date: 2017-05-02-11:24
 */
@Data
@Document
@ApiModel(value = "AppMsg",
        description = "app信息推送")
public class AppMsg implements Serializable {
    @Id
    private String id;
    @ApiModelProperty(notes = "用户id")
    private String userId;
    @ApiModelProperty(notes = "用户名")
    private String userName;
    @ApiModelProperty(notes = "String type")
    private ReminderType type;
    @ApiModelProperty(notes = "app信息Title")
    private String title;
    @ApiModelProperty(notes = "app信息content")
    private String content;
    @ApiModelProperty(notes = "相关属性")
    private Map<String, Object> params;
    @ApiModelProperty(notes = "app未读消息数量")
    private Integer appMsgUnRead;

}

