package cn.fintecher.pangolin.dataimp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.io.Serializable;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 15:52 2017/7/19
 */
@Data
@Document
@ApiModel(value = "mongoSequence", description = "系统序列信息")
public class MongoSequence implements Serializable{

    @Id
    @ApiModelProperty(notes = "序列ID")
    private String id;
    @ApiModelProperty(notes = "序列CODE值")
    private String code;
    @ApiModelProperty(notes = "序列当前值")
    private Integer currentValue;
    @ApiModelProperty(notes = "数据长度")
    private Integer length;
    @ApiModelProperty(notes = "公司Code码")
    private String companyCode;
}
