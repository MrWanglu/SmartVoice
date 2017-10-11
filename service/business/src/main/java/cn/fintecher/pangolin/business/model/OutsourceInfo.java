package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * Created by baizhangyu on 2017/7/26
 */
@Data
public class OutsourceInfo {

    private List<OutDistributeParam> outDistributes;//选择委外方分配信息
    private List<String> outId; //委外方的Id
    private List<Integer> distributionCount;//分配数量
    private List<String> outCaseIds;//委外案件id集合
    private Integer isDebt; //("是否共债优先 0 停用 1 启用")
    private Integer isNumAvg;//("是否数量平均 0 停用 1 启用")
    private Integer rule;//("是否数量平均 0 停用 1 启用")

}
