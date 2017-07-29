package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: LvGuoRong
 * @Description:费用审批的多条件查询
 * @Date: 2017/7/24
 */

@Data
public class CasePayApplyParams {
    @ApiModelProperty(notes = "审批案件ID")
    private String casePayId;
    @ApiModelProperty(notes = "审核意见")
    private String approvePayMemo;
    @ApiModelProperty(notes = "审批结果 入账  驳回")
    private Integer approveResult;
}
