package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author : sunyanping
 * @Description : 某部门下/用户下正在协催的协催案件
 * @Date : 2017/7/25.
 */
@Data
public class AssistingStatisticsModel {
    @ApiModelProperty("总数")
    private Integer num;
    @ApiModelProperty("协催案件ID集合")
    private List<String> assistList;
}
