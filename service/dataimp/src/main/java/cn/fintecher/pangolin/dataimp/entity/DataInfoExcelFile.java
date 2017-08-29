package cn.fintecher.pangolin.dataimp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

/**
 * @Author: PeiShouWen
 * @Description: 案件导入附件实体
 * @Date 9:12 2017/3/25
 */
@Data
@Document
@ApiModel(value = "DataInfoExcelFile",
        description = "数据导入附件")
public class DataInfoExcelFile implements Serializable {
    @Id
    private String id;

    @ApiModelProperty(notes = "批次号")
    private String batchNumber;

    @ApiModelProperty(notes = "案件ID")
    private String caseId;

    @ApiModelProperty(notes = "案件编号")
    private String caseNumber;

    @ApiModelProperty(notes = "文件ID")
    private String fileId;

    @ApiModelProperty(notes = "文件名称")
    private String fileName;

    @ApiModelProperty(notes = "文件路径")
    private String fileUrl;

    @ApiModelProperty(notes = "文件类型")
    private String fileType;

    @ApiModelProperty("创建时间")
    private Date operatorTime;

    @ApiModelProperty("操作人员")
    private String operator;

    @ApiModelProperty("操作人姓名")
    private String operatorName;

    @ApiModelProperty("公司码")
    private String companyCode;


}
