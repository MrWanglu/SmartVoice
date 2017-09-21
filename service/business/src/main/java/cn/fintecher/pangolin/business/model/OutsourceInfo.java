package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * Created by baizhangyu on 2017/7/26
 */
@Data
public class OutsourceInfo {

    private List<OutDistributeParam> outDistributes;//委外方分配信息
    private List<String> outCaseIds;//案件id集合
    private Integer rule;//分配原则(1共债优先；2案件数平均)

}
