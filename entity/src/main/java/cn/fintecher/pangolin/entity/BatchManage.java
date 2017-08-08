package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @Author : sunyanping
 * @Description : 批量管理
 * @Date : 2017/8/8.
 */
@Data
@Entity
@Table(name = "batch_manage")
@ApiModel(value = "BatchManage",description = "批量管理")
public class BatchManage extends BaseEntity{
    @ApiModelProperty("批量名称")
    private String batchName;
    @ApiModelProperty("批量代码")
    private String batchValue;
    @ApiModelProperty("开始时间")
    private Date startTime;
    @ApiModelProperty("结束时间")
    private Date endTime;
    @ApiModelProperty("状态 0-成功 1-失败")
    private Integer status;
    @ApiModelProperty("当前日期")
    private Date sysDate;
    @ApiModelProperty("处理数量")
    private Integer sum;
}
