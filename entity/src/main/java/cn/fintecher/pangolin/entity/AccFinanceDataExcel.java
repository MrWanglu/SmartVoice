package cn.fintecher.pangolin.entity;

import cn.fintecher.pangolin.entity.util.ExcelAnno;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * Created by LQ on 2017/6/5.
 * @Description : 委外方/委托方财务数据Excel导入实体类
 */
@Data
@ApiModel("Excel数据导入实体类")
public class AccFinanceDataExcel {
    @ApiModelProperty(notes = "序号")
    @ExcelAnno(cellName = "序号")
    private String index;

    @ApiModelProperty(notes = "案件编号")
    @ExcelAnno(cellName = "案件编号")
    private String caseNum;

    @ApiModelProperty(notes = "客户姓名")
    @ExcelAnno(cellName = "客户姓名")
    private String custName;

    @ApiModelProperty(notes = "身份证号")
    @ExcelAnno(cellName = "身份证号")
    private String idCardNumber;

    @ApiModelProperty(notes = "案件金额")
    @ExcelAnno(cellName = "案件金额")
    private Double caseAmount = new Double(0);

    @ApiModelProperty(notes = "已还款金额")
    @ExcelAnno(cellName = "已还款金额")
    private Double payAmount = new Double(0);
}
