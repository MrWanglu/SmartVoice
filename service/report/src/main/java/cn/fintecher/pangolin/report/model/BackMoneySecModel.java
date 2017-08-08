package cn.fintecher.pangolin.report.model;

import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 催收员回款二级模型
 * @Date : 14:34 2017/8/2
 */

@Data
public class BackMoneySecModel {
    private String groupCode; //组别code码
    private String groupName; //组别名称
    private List<BackMoneyThiModel> backMoneyThiModels; //客户回款三级模型
}