package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunyanping on 2017/10/9.
 */
@Data
@ApiModel(description = "策略分配统计Model")
public class CountStrategyAllocationModel {
    private List<CountAllocationModel> modelList = new ArrayList<>();
}
