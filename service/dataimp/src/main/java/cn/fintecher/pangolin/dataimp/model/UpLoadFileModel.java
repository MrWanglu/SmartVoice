package cn.fintecher.pangolin.dataimp.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 14:04 2017/4/12
 */
@Data
public class UpLoadFileModel implements Serializable {
    @ApiModelProperty(notes = "批次号")
    private String batchNumber;

    @ApiModelProperty(notes = "案件编号")
    private String caseNum;

    @ApiModelProperty(notes = "案件ID")
    private String caseId;

    @ApiModelProperty(notes = "文件ID")
    private List<String> fileIdList;

    @ApiModelProperty("公司Code")
    private String companyCode;


}
