package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-26-13:31
 */
@Entity
@Table(name = "batch_seq")
@Data
@ApiModel(value = "BatchSeq",description = "序列号")
public class BatchSeq extends BaseEntity{
    @ApiModelProperty("序列名称")
    private String seqName;
    @ApiModelProperty("当前值")
    private Integer currentSeq;
}
