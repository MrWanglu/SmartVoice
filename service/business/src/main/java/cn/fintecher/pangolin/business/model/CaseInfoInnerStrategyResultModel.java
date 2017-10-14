package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * Created by qijigui on 2017-10-14.
 */

@Data
public class CaseInfoInnerStrategyResultModel {

    private List<CaseInfoInnerDistributeModel> innerDistributeUserModelList;//分配给催收员的List

    private List<CaseInfoInnerDistributeModel> innerDistributeDepartModelList;//分配给机构的List

}
