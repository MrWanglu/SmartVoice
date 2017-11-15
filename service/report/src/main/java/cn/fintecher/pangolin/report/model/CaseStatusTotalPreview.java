package cn.fintecher.pangolin.report.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import java.util.List;

/**
 * @Author : huyanmin
 * @Description :
 * @Date : 2017/11/7.
 */
@Entity
@Data
public class CaseStatusTotalPreview {

    //第四部分 案件状态总览
    @ApiModelProperty(notes = "未催收案件数")
    private Integer toFollowCaseCount;
    @ApiModelProperty(notes = "催收中案件数")
    private Integer followingCaseCount;
    @ApiModelProperty(notes = "承诺还款案件数")
    private Integer commitmentBackCaseCount;
    @ApiModelProperty(notes = "今日流入案件")
    private Integer flowInCaseToday;
    @ApiModelProperty(notes = "今日结清案件")
    private Integer finishCaseToday;
    @ApiModelProperty(notes = "今日流出案件")
    private Integer flowOutCaseToday;
}
