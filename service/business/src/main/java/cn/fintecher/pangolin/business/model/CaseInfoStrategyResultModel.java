package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunyanping on 2017/10/23.
 */
@Data
@ApiModel("案件策略分配结果")
public class CaseInfoStrategyResultModel {
    @ApiModelProperty("部门ID")
    private String departId;
    @ApiModelProperty("部门")
    private String departName;
    @ApiModelProperty("用户名")
    private String username;
    @ApiModelProperty("催收员")
    private String realName;
    @ApiModelProperty("当前案件数")
    private Integer handNum = 0;
    @ApiModelProperty("当前案件总金额")
    private BigDecimal handAmount = new BigDecimal(0);
    @ApiModelProperty("分配案件总数")
    private Integer distributeNum = 0;
    @ApiModelProperty("分配案件总金额")
    private BigDecimal distributeAmount = new BigDecimal(0);
    @ApiModelProperty("要分配的案件ID集合")
    private List<String> distributeIds = new ArrayList<>();
}
