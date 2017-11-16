package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import java.math.BigDecimal;
import java.util.List;

/**
 * @Author : huyanmin
 * @Description :
 * @Date : 2017/11/11.
 */
@Entity
@Data
public class FollowCalledDateModel {

    @ApiModelProperty(notes = "催记所选月份天数数量")
    private List<Integer> followCountList;
    @ApiModelProperty(notes = "外呼所选月份天数数量")
    private List<Integer> callCountList;
    @ApiModelProperty(notes = "催记总数量")
    private Integer followTotalCount;
    @ApiModelProperty(notes = "催记平均数量")
    private Integer  followAvgCount;
    @ApiModelProperty(notes = "呼叫总数量")
    private Integer callTotalCount;
    @ApiModelProperty(notes = "呼叫平均数量")
    private Integer callAvgCount;
    @ApiModelProperty(notes = "每个月的每一天")
    private List<String> dayList;//配合前段
}
