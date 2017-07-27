package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/7/27.
 */
@Data
public class AssignAssistParam {
    @ApiModelProperty("协催案件ID")
    private String caseAssistId;
    @ApiModelProperty("要分配的协催员ID")
    private String assistorId;
}
