package cn.fintecher.pangolin.business.model;

import lombok.Data;

/**
 * @author : xiaqun
 * @Description : 重新分配参数
 * @Date : 9:21 2017/7/18
 */

@Data
public class ReDistributionParams {
    private String caseId; //案件ID
    private String userName; //协催员用户名
    private Boolean isAssist; //是否是协催案件 true-是 false-不是
}