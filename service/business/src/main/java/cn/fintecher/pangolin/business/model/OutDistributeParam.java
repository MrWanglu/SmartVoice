package cn.fintecher.pangolin.business.model;

import lombok.Data;

/**
 * Created by Administrator on 2017/9/20.
 */
@Data
public class OutDistributeParam {
    private String outId; //委外案件id
    private Integer distributionCount;//分配数量
}
