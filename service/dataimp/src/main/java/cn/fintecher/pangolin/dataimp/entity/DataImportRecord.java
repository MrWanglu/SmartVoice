package cn.fintecher.pangolin.dataimp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: PeiShouWen
 * @Description: 案件导入记录实体
 * @Date 10:56 2017/3/9
 */
@Data
@Document
@ApiModel(value = "DataImportRecord", description = "案件导入记录信息")
public class DataImportRecord implements Serializable {
    @ApiModelProperty("唯一标识（主键）")
    @Id
    private String Id;

    @ApiModelProperty("批次号")
    private String batchNumber;

    @ApiModelProperty("外键:文件ID")
    private String fileId;

    @ApiModelProperty("创建时间")
    private Date operatorTime;

    @ApiModelProperty("操作人员")
    private String operator;

    @ApiModelProperty("操作人姓名")
    private String operatorName;

    @ApiModelProperty("委托方编号")
    private String principalId;

    @ApiModelProperty("委托方名称")
    private String principalName;

    @ApiModelProperty("结案日期")
    private Date closeDate;

    @ApiModelProperty("委案日期")
    private Date delegationDate;

    @ApiModelProperty("案件手数")
    private Integer handNumber;

    @ApiModelProperty("备注")
    private String memo;

    @ApiModelProperty("Excel 模板ID")
    private String templateId;

    @ApiModelProperty("公司码")
    private String companyCode;
}
