package cn.fintecher.pangolin.business.model;

import lombok.Data;

/**
 * @author : xiaqun
 * @Description : 协催申请参数
 * @Date : 10:55 2017/7/20
 */

@Data
public class AssistApplyParams {
    private String caseId; //案件ID
    private String applyReason; //申请原因
    private Integer assistWay; //协催方式
}