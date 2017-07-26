package cn.fintecher.pangolin.business.model;

import lombok.Data;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author : xiaqun
 * @Description : 还款页面参数
 * @Date : 9:31 2017/7/19
 */

@Data
public class PayApplyParams {
    @DecimalMax(value = "9999999999.99", message = "减免金额不能大于100亿")
    private BigDecimal derateFee;//减免费用
    private String derateDescripton;//减免备注
    private List<String> fileIds;//上传凭证Id集合
    @NotNull
    private Integer payWay;//还款方式
    @NotNull
    private Integer payaType;//还款类型
    private String caseId;//案件ID
    @NotNull
    @DecimalMax(value = "9999999999.99", message = "还款金额不能大于100亿")
    private BigDecimal payAmt;//还款金额
    private String payDescripton;  //还款说明
    private Integer derateFlag; //减免标识 0-没有减免 1-有减免
}