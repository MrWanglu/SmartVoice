package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : xiaqun
 * @Description : 案件分案撤销模型
 * @Date : 8:58 2017/10/18
 */

@Data
public class CaseRevokeDistributeModel {
    private String caseId; //案件ID
    private String caseNumber; //案件编号
    private String batchNumber; //批次号
    private String personalName; //客户姓名
    private BigDecimal overdueAmt; //案件金额
    private String principalName; //委托方名称
    private String currentCollectorName; //当前催收员姓名
    private String reason; //原因
}