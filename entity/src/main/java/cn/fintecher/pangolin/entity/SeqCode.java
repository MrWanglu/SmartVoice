package cn.fintecher.pangolin.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 17:25 2017/7/18
 */
@Data
@Entity
@Table
@ApiModel(value = "seqCode", description = "系统序列信息")
public class SeqCode implements Serializable {
    @Id
    @ApiModelProperty(notes = "序列名称")
    private String name;

    @ApiModelProperty(notes = "序列当前值")
    private Integer currentValue;
}
