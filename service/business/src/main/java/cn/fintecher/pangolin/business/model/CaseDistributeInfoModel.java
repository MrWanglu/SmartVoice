package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/8/9.
 */
@Data
public class CaseDistributeInfoModel {
    @ApiModelProperty("待分配案件ID List")
    private List<String> caseIdList;
}
