package cn.fintecher.pangolin.dataimp.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by sunyanping on 2017/9/22.
 */
@Data
@Document
@ApiModel(description = "数据行错误信息")
public class RowError {
    @ApiModelProperty("唯一标识（主键）")
    @Id
    private String Id;
    @ApiModelProperty("sheet名称")
    private String sheetName;
    @ApiModelProperty("行数")
    private Integer rowIndex;
    @ApiModelProperty("客户姓名")
    private String name;
    @ApiModelProperty("身份证号")
    private String idCard;
    @ApiModelProperty("电话号码")
    private String phone;
    @ApiModelProperty("错误信息")
    private String errorMsg;
    @ApiModelProperty("批次号")
    private String batchNumber;
    @ApiModelProperty("案件编号")
    private String caseNumber;
    @ApiModelProperty("公司Code")
    private String companyCode;
}
