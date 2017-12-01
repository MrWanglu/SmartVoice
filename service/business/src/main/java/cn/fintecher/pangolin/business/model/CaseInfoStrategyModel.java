package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by sunyanping on 2017/10/23.
 */
@Data
@ApiModel("策略结果")
public class CaseInfoStrategyModel {
    @ApiModelProperty("策略结果")
    private List<CaseInfoStrategyResultModel> modelList;
}
