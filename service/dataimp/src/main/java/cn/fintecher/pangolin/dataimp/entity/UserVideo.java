package cn.fintecher.pangolin.dataimp.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by qijigui on 2017/6/1.
 */
@Data
@Document
public class UserVideo implements Serializable {

    @ApiModelProperty("唯一标识（主键）")
    @Id
    private String id;
    @ApiModelProperty("催收员")
    private String userName;
    @ApiModelProperty("催收员真名")
    private String userRealName;
    @ApiModelProperty("录音文件名")
    private String videoName;
    @ApiModelProperty("文件路径")
    private String videoUrl;
    @ApiModelProperty("文件长度")
    private String videoLength;
    @ApiModelProperty("操作时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operatorTime;
    @ApiModelProperty("公司Code")
    private String companyCode;
    @ApiModelProperty("部门Code")
    private String deptCode;
    @ApiModelProperty("部门名称")
    private String deptName;

}
