package cn.fintecher.pangolin.business.model;

import cn.fintecher.pangolin.entity.util.ExcelAnno;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-02-15:20
 */
@Data
@ApiModel(value = "UploadUserBackcashPlanExcelModel", description = "导入月度回款Excel数据")
public class UploadUserBackcashPlanExcelModel {
    @ApiModelProperty(notes = "序号")
    @ExcelAnno(cellName = "序号")
    private String index;
    @ApiModelProperty(notes = "用户名")
    @ExcelAnno(cellName = "用户名")
    private String userName;
    @ApiModelProperty(notes = "姓名")
    @ExcelAnno(cellName = "姓名")
    private String realName;
    @ApiModelProperty(notes = "年度")
    @ExcelAnno(cellName = "年度")
    private Integer year;
    @ApiModelProperty(notes = "月份")
    @ExcelAnno(cellName = "月份")
    private Integer month;
    @ApiModelProperty(name = "回款金额")
    @ExcelAnno(cellName = "回款金额(元)")
    private BigDecimal backCash = new BigDecimal(0);
}
