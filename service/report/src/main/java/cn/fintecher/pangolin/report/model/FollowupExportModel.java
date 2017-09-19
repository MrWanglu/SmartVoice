package cn.fintecher.pangolin.report.model;

import cn.fintecher.pangolin.entity.util.ExcelAnno;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by sunyanping on 2017/9/19.
 */
@Data
public class FollowupExportModel {
    @ExcelAnno(cellName = "案件编号")
    @ApiModelProperty("案件编号")
    private String caseNumber;
    @ExcelAnno(cellName = "批次号")
    @ApiModelProperty("批次号")
    private String batchNumber;
    @ExcelAnno(cellName = "委托方")
    @ApiModelProperty("委托方")
    private String principalName;
    @ExcelAnno(cellName = "跟进时间")
    @ApiModelProperty("跟进时间")
    private String follTime;
    @ExcelAnno(cellName = "跟进方式")
    @ApiModelProperty("跟进方式")
    private String follType;
    @ExcelAnno(cellName = "客户姓名")
    @ApiModelProperty("客户姓名")
    private String personalName;
    @ExcelAnno(cellName = "客户身份证")
    @ApiModelProperty("客户身份证")
    private String idCard;
    @ExcelAnno(cellName = "催收对象")
    @ApiModelProperty("催收对象")
    private String follTarget;
    @ExcelAnno(cellName = "姓名")
    @ApiModelProperty("姓名")
    private String follTargetName;
    @ExcelAnno(cellName = "电话/地址")
    @ApiModelProperty("电话/地址")
    private String follPhoneNum;
    @ExcelAnno(cellName = "定位地址")
    @ApiModelProperty("定位地址")
    private String location;
    @ExcelAnno(cellName = "催收反馈")
    @ApiModelProperty("催收反馈")
    private String follFeedback;
    @ExcelAnno(cellName = "跟进内容")
    @ApiModelProperty("跟进内容")
    private String follContent;
    @ExcelAnno(cellName = "客户号")
    @ApiModelProperty("客户号")
    private String personalNum;
    @ExcelAnno(cellName = "账户号")
    @ApiModelProperty("账户号")
    private String accountNum;
    @ExcelAnno(cellName = "手数")
    @ApiModelProperty("手数")
    private Integer handNum;
}
