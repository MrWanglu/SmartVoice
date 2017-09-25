package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-09-21-10:21
 */
@Entity
@Table(name = "system_backup")
@Data
@ApiModel(value = "SystemBackup", description = "系统备份实体")
public class SystemBackup extends BaseEntity {
    @ApiModelProperty("特定公司的标识")
    private String companyCode;
    @ApiModelProperty("备份类型 0：自动 1：手动")
    private Integer type;
    @ApiModelProperty("mysql数据库文件名称")
    private String mysqlName;
    @ApiModelProperty("mongdb数据库名称")
    private String mongdbName;
    @ApiModelProperty("操作人")
    private String operator;
    @ApiModelProperty("备份时间")
    private Date operateTime;
}
