package cn.fintecher.pangolin.business.model;

import lombok.Data;

/**
 * @author : xiaqun
 * @Description : 还款审批参数
 * @Date : 10:05 2017/7/29
 */

@Data
public class PaymentParams {
    private String casePayApplyId; //还款审批ID
    private Integer result; //审批结果 0-驳回 1-入账
    private Integer flag; //减免标识 0-是减免审批 1-是还款审批
    private String opinion; //审批意见
}