package cn.fintecher.pangolin.report.model;

import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 催收员回款信息模型
 * @Date : 13:57 2017/8/2
 */

@Data
public class BackMoneyModel {
    private String deptCode; //部门code码
    private String deptName; //部门名称
    private List<BackMoneySecModel> backMoneySecModels; //回款信息二级模型集合
}