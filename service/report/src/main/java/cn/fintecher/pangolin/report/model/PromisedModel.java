package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

/**
 * @Author : huyanmin
 * @Description :
 * @Date : 2017/11/11.
 */
@Entity
@Data
public class PromisedModel {

    @ApiModelProperty(notes = "催收中总数量或金额")
    private String value;
    @ApiModelProperty(notes = "省份名称")
    private String name;
}
