package cn.fintecher.pangolin.dataimp.model;

import lombok.Data;

/**
 * Created by zzl029 on 2017/8/11.
 */
@Data
public class ScoreRuleModel {
    private Integer age;
    private Integer isWork;
    private Double overDueAmount;
    private Integer overDueDays;
    private Integer proId;
    private Double cupoScore = 0d;
}
