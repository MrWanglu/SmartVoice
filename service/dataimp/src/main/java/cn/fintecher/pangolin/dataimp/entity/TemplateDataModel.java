package cn.fintecher.pangolin.dataimp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 16:12 2017/7/18
 */
@Data
@Document
@ApiModel(value = "TemplateDataModel",
        description = "Excel数据导入模板类")
public class TemplateDataModel implements Serializable {
    @ApiModelProperty("模板id")
    @Id
    public String id;
    @ApiModelProperty("模板名称")
    public String templateName;
    @ApiModelProperty("委托方ID")
    public String principalId;
    @ApiModelProperty("委托方")
    public String principalName;
    @ApiModelProperty("Excel标题开始行编号")
    public String titleRowNum;
    @ApiModelProperty("Excel数据开始行编号")
    public String dataRowNum;
    @ApiModelProperty("Excel数据开始列编号")
    public String dataColNum;
    @ApiModelProperty("创建时间")
    private Date operatorTime;
    @ApiModelProperty("操作人员")
    private String operator;
    @ApiModelProperty("操作人姓名")
    private String operatorName;
    @ApiModelProperty("公司编号")
    private String  companyCode;
    private List<TemplateExcelInfo> templateExcelInfoList;
}
