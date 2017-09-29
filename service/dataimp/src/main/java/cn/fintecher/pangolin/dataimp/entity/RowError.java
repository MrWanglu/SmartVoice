package cn.fintecher.pangolin.dataimp.entity;

import cn.fintecher.pangolin.entity.util.ExcelAnno;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * Created by sunyanping on 2017/9/22.
 */
@Data
@Document
@ApiModel(description = "数据行错误信息")
public class RowError implements Serializable {
    @ApiModelProperty("唯一标识（主键）")
    @Id
    private String id;
    @ApiModelProperty("sheet名称")
    private String sheetName;
    @ApiModelProperty("行数")
    @ExcelAnno(cellName = "Excel行号")
    private Integer rowIndex;
    @ApiModelProperty("客户姓名")
    @ExcelAnno(cellName = "客户姓名")
    private String name;
    @ApiModelProperty("身份证号")
    @ExcelAnno(cellName = "身份证号")
    private String idCard;
    @ApiModelProperty("电话号码")
    @ExcelAnno(cellName = "手机号")
    private String phone;
    @ApiModelProperty("错误信息")
    @ExcelAnno(cellName = "错误内容")
    private String errorMsg;
    @ApiModelProperty("批次号")
    private String batchNumber;
    @ApiModelProperty("案件编号")
    @ExcelAnno(cellName = "案件编号")
    private String caseNumber;
    @ApiModelProperty("案件金额")
    @ExcelAnno(cellName = "案件金额(元)")
    private Double caseAmount;
    @ApiModelProperty("公司Code")
    private String companyCode;
}
