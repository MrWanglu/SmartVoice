package cn.fintecher.pangolin.file.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by ChenChang on 2017/4/7.
 */
@Data
public class UnZipCaseFileRequest {
    @ApiModelProperty("批次号")
    private String batchNum;
    @ApiModelProperty("文件ID")
    private String uploadFile;
    @ApiModelProperty("公司Code")
    private String companyCode;
    @ApiModelProperty("案件编号")
    private String caseNumber;
}
