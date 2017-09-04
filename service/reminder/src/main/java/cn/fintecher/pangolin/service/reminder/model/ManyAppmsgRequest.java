package cn.fintecher.pangolin.service.reminder.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by zzl029 on 2017/9/1.
 */
@Data
public class ManyAppmsgRequest {
    List<String> ids;
    @ApiModelProperty(notes = "app信息Title")
    private String title;
    @ApiModelProperty(notes = "app信息content")
    private String content;
    @ApiModelProperty(notes = "app未读消息数量")
    private Integer appmsgNoRead;
}
