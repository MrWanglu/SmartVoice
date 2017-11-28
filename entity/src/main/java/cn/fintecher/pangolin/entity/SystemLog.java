package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@ApiModel(value = "SystemLog",
        description = "系统日志信息",
        parent = BaseEntity.class)
@Entity
@Table(name = "sys_operate_logs")
//@Document
@Data
public class SystemLog extends BaseEntity {

    @ApiModelProperty(notes = "公司code特定标识")
    private String companyCode;

    @ApiModelProperty(notes = "客户端IP")
    private String clientIp;

    @ApiModelProperty(notes = "操作人")
    private String operator;

    @ApiModelProperty(notes = "创建时间")
    private Date operateTime;

    @ApiModelProperty(notes = "描述")
    private String remark;

    @ApiModelProperty(notes = "请求执行时间")
    private String exeTime;

    @ApiModelProperty(notes = "执行方法")
    private String exeMethod;

    @ApiModelProperty(notes = "执行参数")
    private String exeParams;

    @ApiModelProperty(notes = "操作类型")
    private String exeType;

    @ApiModelProperty(notes = "备用字段")
    private String field;
}
