package cn.fintecher.pangolin.dataimp.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: PeiShouWen
 * @Description: 检查附件是否存在
 * @Date 18:56 2017/4/8
 */
@Data
public class DataInfoExcelFileExist implements Serializable {

    @ApiModelProperty("案件ID")
    private String caseId;

    @ApiModelProperty("批次号")
    private String batchNumber;

    @ApiModelProperty("案件编号")
    private String caseNumber;

    @ApiModelProperty("备注")
    private String msg;


}
