package cn.fintecher.pangolin.business.model;

import lombok.Data;

/**
 * @author : xiaqun
 * @Description : 小流转案件审批参数
 * @Date : 11:30 2017/8/16
 */

@Data
public class CirculationApprovalParams {
    private String caseId; //案件ID
    private Integer type; //案件类型 0-电催 1-外访
    private Integer result; //审批结果 0-通过 1-拒绝
}