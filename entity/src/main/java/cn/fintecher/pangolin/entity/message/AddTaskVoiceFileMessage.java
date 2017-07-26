package cn.fintecher.pangolin.entity.message;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by ChenChang on 2017/4/1.
 */
@Data
public class AddTaskVoiceFileMessage {
    @ApiModelProperty(notes = "绑定和返回的 taskid")
    private String taskid;
    @ApiModelProperty(notes = "申请任务记录 ID")
    private String recorderId;
    @ApiModelProperty(notes = "坐席号 ID")
    private String taskcallerId;
}
