package cn.fintecher.pangolin.business.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by Administrator on 2017/7/27.
 */
@Data
public class OutCaseIdList {
    @ApiModelProperty("回收案件ID")
    private List<String> outCaseIds;
    @ApiModelProperty("回收说明")
    private String returnReason;
    @ApiModelProperty("公司Code")
    private String companyCode;
}
