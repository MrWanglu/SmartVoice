package cn.fintecher.pangolin.business.model;

import cn.fintecher.pangolin.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: LvGuoRong
 * @Description:处理审核信息参数临时对象
 * @Date: 2017/7/21
 */

@Data
@ApiModel(value = "CasePayApplys", description = "处理审核信息对象")
public class CasePayApplys extends BaseEntity {
    @ApiModelProperty(notes = "案件ID")
    private String caseId;
    @ApiModelProperty(notes = "审批状态 0已撤回 1待审批 2 审批同意 3审批拒绝")
    private Integer ApproveCostresult;
    @ApiModelProperty(notes = "审核意见")
    private String approvePayMemo;
    @ApiModelProperty(notes = "案件编号")
    private String caseNumber;
    @ApiModelProperty(notes = "还款类型")
    private Integer payType;
    @ApiModelProperty(notes = "审批结果 入账  驳回")
    private Integer approveResult;
}
