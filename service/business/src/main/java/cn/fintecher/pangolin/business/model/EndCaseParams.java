package cn.fintecher.pangolin.business.model;

import lombok.Data;

/**
 * @author : xiaqun
 * @Description : 结案参数
 * @Date : 22:54 2017/7/19
 */

@Data
public class EndCaseParams {
    private String caseId; //案件ID
    private String endRemark; //结案说明
    private Integer endType; //结案方式
    private Boolean isAssist; //是否是协催案件 true-是 false-否
}
