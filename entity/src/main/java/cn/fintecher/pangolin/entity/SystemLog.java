package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@ApiModel(value = "SystemLog",
        description = "系统日志信息",
        parent = BaseEntity.class)
@Document
@Data
public class SystemLog extends BaseEntity {

    @ApiModelProperty(notes = "操作类型")
    private String type;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("操作人")
    private String operator;

    @ApiModelProperty("描述")
    private String remark;

    @ApiModelProperty("请求执行时间")
    private String exeTime;

    @ApiModelProperty("客户端IP")
    private String reqIp;

    @ApiModelProperty("执行方法")
    private String methods;

    @ApiModelProperty("执行参数")
    private String params;
}
