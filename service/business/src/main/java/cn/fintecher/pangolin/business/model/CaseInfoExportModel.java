package cn.fintecher.pangolin.business.model;

import lombok.Data;

/**
 * @Author: LvGuoRong
 * @Description:
 * @Date: 2017/7/24
 */
@Data
public class CaseInfoExportModel {
    private String cupoDeptname;  //机构名称
    private String custName; //客户姓名
    private String cupoIdcard; //身份证号
    private String cupoPhone; //联系电话
    private String cupoCity; //归属城市
    private String cupoSumper; //总期数
    private String cupoOverday; //逾期天数
    private String cupoPaystatus; //还款状态
    private String cupoAmt; //逾期金额
    private String cupoLoandate; //贷款日期
}
