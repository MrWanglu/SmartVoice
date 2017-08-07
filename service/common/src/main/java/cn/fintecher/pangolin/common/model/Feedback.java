package cn.fintecher.pangolin.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.io.Serializable;
import java.util.Date;

/**
 * 反馈信息表
 * Created by gaobeibei on 2017/8/7.
 */
@Data
@Document
@ApiModel(value = "Feedback",
        description = "反馈信息")
public class Feedback implements Serializable {
    @Id
    private String id;
    @ApiModelProperty(notes = "反馈人姓名")
    private String feedbackName;
    @ApiModelProperty(notes = "反馈时间")
    private Date feedbackTime;
    @ApiModelProperty(notes = "反馈内容")
    private String feedbackDescription;
    @ApiModelProperty(notes = "反馈预留字段")
    private String feedbackExtra;
}
