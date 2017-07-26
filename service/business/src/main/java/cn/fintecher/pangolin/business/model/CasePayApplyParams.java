package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: LvGuoRong
 * @Description:费用审批的多条件查询
 * @Date: 2017/7/24
 */

@Data
public class CasePayApplyParams {
    private String personalName;//客户姓名
    private String personalPhone;//客户手机
    private String batchNumber;//批次号
    private BigDecimal applyDerateAmt;//费用减免金额
    private BigDecimal payaApplyMinAmt; //最早申请日期
    private BigDecimal payaApplyMaxAmt; //最晚申请日期

    private Integer approveType;//减免类型
    private Integer approveCostresult;//审批结果
    private String principalId;//委托方
    private Integer payType;//还款类型
    private Integer payWay;//还款方式
    private Integer approveStatus;//审批状态
    private String applayUserName;//申请人
    private Date payaApplyMinDate; //最早申请日期
    private Date payaApplyMaxDate; //最晚申请日期
}
