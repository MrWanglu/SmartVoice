package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.Date;

/**
 * @Author: LvGuoRong
 * @Description:
 * @Date: 2017/7/24
 */
@Data
public class ExportPayApply {
    private String personalName;//客户姓名
    private String batchNumber;//批次号
    private Integer payType;//还款类型
    private Integer payWay;//还款方式
    private String applayUserName;//申请人
    private Date payaApplyMinDate;//最早申请时间
    private Date payaApplyMaxDate;//最晚申请时间
    private String principalId;//委托方
    private Integer approveStatus;//审核结果
}
