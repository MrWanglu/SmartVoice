package cn.fintecher.pangolin.dataimp.entity;

import cn.fintecher.pangolin.dataimp.annotation.ClassFeature;
import cn.fintecher.pangolin.dataimp.annotation.ExcelAnno;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: PeiShouWen
 * @Description: Excel数据导入实体接收类
 * @Date 17:57 2017/3/20
 */
@ApiModel(value = "DataInfoExcelModel",
        description = "Excel数据导入实体类")
@Data
@Document
@ClassFeature(name = "Excel数据导入实体类")
public class DataInfoExcel implements Serializable {
    @Id
    @ApiModelProperty(notes = "数据主键")
    private String id;

    @ApiModelProperty(notes = "客户姓名")
    @ExcelAnno(cellName = "客户姓名")
    private String personalName;

    @ApiModelProperty(notes = "身份证号码")
    @ExcelAnno(cellName = "身份证号")
    private String idCard;

    @ApiModelProperty(notes = "手机号码")
    @ExcelAnno(cellName = "手机号码")
    private String mobileNo;

    @ApiModelProperty(notes = "产品系列")
    @ExcelAnno(cellName = "产品系列")
    private String productSeriesName;

    @ApiModelProperty(notes = "产品名称")
    @ExcelAnno(cellName = "产品名称")
    private String productName;

    @ApiModelProperty(notes = "合同编号")
    @ExcelAnno(cellName = "合同编号")
    private String contractNumber;

    @ApiModelProperty(notes = "贷款日期 就是合同签订日期")
    @ExcelAnno(cellName = "贷款日期")
    private Date loanDate;

    @ApiModelProperty("总期数")
    @ExcelAnno(cellName = "还款期数")
    private Integer periods = new Integer(0);

    @ApiModelProperty("每期还款日")
    @ExcelAnno(cellName = "每期还款日")
    private Date perDueDate;

    @ApiModelProperty("每期还款金额(元)")
    @ExcelAnno(cellName = "每期还款金额(元)")
    private Double perPayAmount = new Double(0);

    @ApiModelProperty("合同金额")
    @ExcelAnno(cellName = "合同金额(元)")
    private Double contractAmount = new Double(0);

    @ApiModelProperty("剩余本金")
    @ExcelAnno(cellName = "剩余本金(元)")
    private Double leftCapital = new Double(0);

    @ApiModelProperty("剩余利息")
    @ExcelAnno(cellName = "剩余利息(元)")
    private Double leftInterest = new Double(0);

    @ApiModelProperty("案件金额")
    @ExcelAnno(cellName = "逾期总金额(元)")
    private Double overdueAmount = new Double(0);

    @ApiModelProperty("逾期本金(元)")
    @ExcelAnno(cellName = "逾期本金(元)")
    private Double overdueCapital = new Double(0);

    @ApiModelProperty("逾期利息(元)")
    @ExcelAnno(cellName = "逾期利息(元)")
    private Double overDueInterest = new Double(0);

    @ApiModelProperty("逾期罚息(元)")
    @ExcelAnno(cellName = "逾期罚息(元)")
    private Double overdueFine = new Double(0);

    @ApiModelProperty("逾期滞纳金(元)")
    @ExcelAnno(cellName = "逾期滞纳金(元)")
    private Double overdueDelayFine = new Double(0);

    @ApiModelProperty("其他费用")
    @ExcelAnno(cellName = "其他费用(元)")
    private Double otherAmt = new Double(0);

    @ApiModelProperty("逾期日期")
    @ExcelAnno(cellName = "逾期日期")
    private Date overDueDate;

    @ApiModelProperty("逾期期数")
    @ExcelAnno(cellName = "逾期期数")
    private Integer overDuePeriods = new Integer(0);

    @ApiModelProperty("逾期天数")
    @ExcelAnno(cellName = "逾期天数")
    private Integer overDueDays = new Integer(0);

    @ApiModelProperty("已还款金额(元)")
    @ExcelAnno(cellName = "已还款金额(元)")
    private Double hasPayAmount = new Double(0);

    @ApiModelProperty("已还款期数")
    @ExcelAnno(cellName = "已还款期数")
    private Integer hasPayPeriods = new Integer(0);

    @ApiModelProperty("最近还款日期")
    @ExcelAnno(cellName = "最近还款日期")
    private Date latelyPayDate;

    @ApiModelProperty("最近还款金额(元)")
    @ExcelAnno(cellName = "最近还款金额(元)")
    private Double latelyPayAmount = new Double(0);

    @ApiModelProperty("客户还款卡银行")
    @ExcelAnno(cellName = "客户还款卡银行")
    private String depositBank;

    @ApiModelProperty("客户还款卡号")
    @ExcelAnno(cellName = "客户还款卡号")
    private String cardNumber;

    @ApiModelProperty("省份")
    @ExcelAnno(cellName = "省份")
    private String province;

    @ApiModelProperty("城市")
    @ExcelAnno(cellName = "城市")
    private String city;

    @ApiModelProperty("家庭住址")
    @ExcelAnno(cellName = "家庭住址")
    private String homeAddress;

    @ApiModelProperty("家庭固话")
    @ExcelAnno(cellName = "家庭固话")
    private String homePhone;

    @ApiModelProperty("身份证户籍地址")
    @ExcelAnno(cellName = "身份证户籍地址")
    private String idCardAddress;

    @ApiModelProperty("工作单位名称")
    @ExcelAnno(cellName = "工作单位名称")
    private String companyName;

    @ApiModelProperty("工作单位地址")
    @ExcelAnno(cellName = "工作单位地址")
    private String companyAddr;

    @ApiModelProperty("工作单位电话")
    @ExcelAnno(cellName = "工作单位电话")
    private String companyPhone;

    @ApiModelProperty("联系人1姓名")
    @ExcelAnno(cellName = "联系人1姓名")
    private String contactName1;

    @ApiModelProperty("联系人1与客户关系")
    @ExcelAnno(cellName = "联系人1与客户关系")
    private String contactRelation1;

    @ApiModelProperty("联系人1工作单位")
    @ExcelAnno(cellName = "联系人1工作单位")
    private String contactWorkUnit1;

    @ApiModelProperty("联系人1单位电话")
    @ExcelAnno(cellName = "联系人1单位电话")
    private String contactUnitPhone1;

    @ApiModelProperty("联系人1手机号码")
    @ExcelAnno(cellName = "联系人1手机号码")
    private String contactPhone1;

    @ApiModelProperty("联系人1住宅电话")
    @ExcelAnno(cellName = "联系人1住宅电话")
    private String contactHomePhone1;

    @ApiModelProperty("联系人1现居地址")
    @ExcelAnno(cellName = "联系人1现居地址")
    private String contactCurrAddress1;

    @ApiModelProperty("联系人2姓名")
    @ExcelAnno(cellName = "联系人2姓名")
    private String contactName2;

    @ApiModelProperty("联系人2与客户关系")
    @ExcelAnno(cellName = "联系人2与客户关系")
    private String contactRelation2;

    @ApiModelProperty("联系人2手机号码")
    @ExcelAnno(cellName = "联系人2手机号码")
    private String contactPhone2;

    @ApiModelProperty("联系人2工作单位")
    @ExcelAnno(cellName = "联系人2工作单位")
    private String contactWorkUnit2;

    @ApiModelProperty("联系人2单位电话")
    @ExcelAnno(cellName = "联系人2单位电话")
    private String contactUnitPhone2;

    @ApiModelProperty("联系人2住宅电话")
    @ExcelAnno(cellName = "联系人2住宅电话")
    private String contactHomePhone2;

    @ApiModelProperty("联系人2现居地址")
    @ExcelAnno(cellName = "联系人2现居地址")
    private String contactCurrAddress2;

    @ApiModelProperty("联系人3姓名")
    @ExcelAnno(cellName = "联系人3姓名")
    private String contactName3;

    @ApiModelProperty("联系人3与客户关系")
    @ExcelAnno(cellName = "联系人3与客户关系")
    private String contactRelation3;

    @ApiModelProperty("联系人3手机号码")
    @ExcelAnno(cellName = "联系人3手机号码")
    private String contactPhone3;

    @ApiModelProperty("联系人3工作单位")
    @ExcelAnno(cellName = "联系人3工作单位")
    private String contactWorkUnit3;

    @ApiModelProperty("联系人3单位电话")
    @ExcelAnno(cellName = "联系人3单位电话")
    private String contactUnitPhone3;

    @ApiModelProperty("联系人3住宅电话")
    @ExcelAnno(cellName = "联系人3住宅电话")
    private String contactHomePhone3;

    @ApiModelProperty("联系人3现居地址")
    @ExcelAnno(cellName = "联系人3现居地址")
    private String contactCurrAddress3;

    @ApiModelProperty("联系人4姓名")
    @ExcelAnno(cellName = "联系人4姓名")
    private String contactName4;

    @ApiModelProperty("联系人4与客户关系")
    @ExcelAnno(cellName = "联系人4与客户关系")
    private String contactRelation4;

    @ApiModelProperty("联系人4手机号码")
    @ExcelAnno(cellName = "联系人4手机号码")
    private String contactPhone4;

    @ApiModelProperty("联系人4工作单位")
    @ExcelAnno(cellName = "联系人4工作单位")
    private String contactWorkUnit4;

    @ApiModelProperty("联系人4单位电话")
    @ExcelAnno(cellName = "联系人4单位电话")
    private String contactUnitPhone4;

    @ApiModelProperty("联系人4住宅电话")
    @ExcelAnno(cellName = "联系人4住宅电话")
    private String contactHomePhone4;

    @ApiModelProperty("联系人4现居地址")
    @ExcelAnno(cellName = "联系人4现居地址")
    private String contactCurrAddress4;

    @ApiModelProperty("备注")
    @ExcelAnno(cellName = "备注")
    private String memo;

    @ApiModelProperty("佣金比例(%)")
    @ExcelAnno(cellName = "佣金比例(%)")
    private Double commissionRate = new Double(0);

    @ApiModelProperty("逾期管理费")
    @ExcelAnno(cellName = "逾期管理费")
    private Double overdueManageFee=new Double(0);

    @ApiModelProperty("还款状态")
    private String paymentStatus;

    @ExcelAnno(cellName = "批次")
    @ApiModelProperty(notes = "批次号")
    private String batchNumber;

    @ApiModelProperty("委托方编号")
    private String prinCode;

    @ApiModelProperty("委托方名称")
    private String prinName;

    @ApiModelProperty("委案日期")
    private Date delegationDate;

    @ApiModelProperty("结案日期")
    private Date closeDate;

    @ApiModelProperty("创建时间")
    private Date operatorTime;

    @ApiModelProperty("操作人员")
    private String operator;

    @ApiModelProperty("操作人姓名")
    private String operatorName;

    @ApiModelProperty(notes = "数据来源 0-Excel导入")
    private Integer dataSources;

    @ApiModelProperty("案件手数")
    private Integer caseHandNum;

    @ApiModelProperty("公司码")
    private String companyCode;

    @ApiModelProperty("案件编号")
    private String caseNumber;

}
