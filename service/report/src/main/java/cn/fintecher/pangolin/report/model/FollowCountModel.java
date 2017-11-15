package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import java.math.BigDecimal;

/**
 * @Author : huyanmin
 * @Description :
 * @Date : 2017/11/8.
 */
@Entity
@Data
public class FollowCountModel {
    //第六部分 跟催排名
    @ApiModelProperty(notes = "跟催催收员姓名")
    private String collectionFollowName;
    @ApiModelProperty(notes = "外呼数")
    private Integer calledCount;
    @ApiModelProperty(notes = "催计数")
    private Integer followedCount;
    @ApiModelProperty(notes = "跟单量")
    private Integer followedNumber;
}
