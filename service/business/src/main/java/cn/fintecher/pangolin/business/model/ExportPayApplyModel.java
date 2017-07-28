package cn.fintecher.pangolin.business.model;

import lombok.Data;

/**
 * @Author: LvGuoRong
 * @Description:
 * @Date: 2017/7/24
 */

@Data
public class ExportPayApplyModel {
    //案件编号
    private String caseNumber;
    //批次号
    private String batchNumber;
    //委托方
    private String principalId;
    //客户姓名
    private String personalName;
    //手机号
    private String personalPhone;
    //案件金额
    private String CaseAmt;
    //还款金额
    private String applyPayAmt;
    //还款类型:部分逾期还款 全额逾期还款 减免逾期还款 部分提前结清 全额提前结清 减免提前结清")
    private String payType;
    //还款方式：0对公 1代扣 2微信 3支付宝 4其他")
    private String payWay;
    //审批结果：0撤回，1待审核，2审核通过(入账)，3审核拒绝(驳回)")
    private String approveStatus;
    //申请人
    private String applayUserName;
    //申请日期"
    private String applayDate;
}
