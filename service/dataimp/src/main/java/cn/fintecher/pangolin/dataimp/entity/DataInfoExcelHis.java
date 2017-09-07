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
 * @Description: Excel数据导入实体历史记录类
 * @Date 17:57 2017/3/20
 */
@ApiModel(value = "DataInfoExcelModel",
        description = "Excel数据导入实体类")
@Data
@Document
public class DataInfoExcelHis implements Serializable {
    @Id
    @ApiModelProperty(notes = "数据主键")
    private String id;

    @ApiModelProperty(notes = "客户姓名")
    private String personalName;

    @ApiModelProperty(notes = "身份证号码")
    private String idCard;

    @ApiModelProperty(notes = "手机号码")
    private String mobileNo;

    @ApiModelProperty(notes = "产品系列")
    private String productSeriesName;

    @ApiModelProperty(notes = "产品名称")
    private String productName;

    @ApiModelProperty(notes = "合同编号")
    private String contractNumber;

    @ApiModelProperty(notes = "贷款日期 就是合同签订日期")
    private Date loanDate;

    @ApiModelProperty("总期数")
    private Integer periods = new Integer(0);

    @ApiModelProperty("每期还款日")
    private Date perDueDate;

    @ApiModelProperty("每期还款金额(元)")
    private Double perPayAmount = new Double(0);

    @ApiModelProperty("合同金额")
    private Double contractAmount = new Double(0);

    @ApiModelProperty("剩余本金")
    private Double leftCapital = new Double(0);

    @ApiModelProperty("剩余利息")
    private Double leftInterest = new Double(0);

    @ApiModelProperty("案件金额")
    private Double overdueAmount = new Double(0);

    @ApiModelProperty("逾期本金(元)")
    private Double overdueCapital = new Double(0);

    @ApiModelProperty("逾期利息(元)")
    private Double overDueInterest = new Double(0);

    @ApiModelProperty("逾期罚息(元)")
    private Double overdueFine = new Double(0);

    @ApiModelProperty("逾期滞纳金(元)")
    private Double overdueDelayFine = new Double(0);

    @ApiModelProperty("其他费用")
    private Double otherAmt = new Double(0);

    @ApiModelProperty("逾期日期")
    private Date overDueDate;

    @ApiModelProperty("逾期期数")
    private Integer overDuePeriods = new Integer(0);

    @ApiModelProperty("逾期天数")
    private Integer overDueDays = new Integer(0);

    @ApiModelProperty("已还款金额(元)")
    private Double hasPayAmount = new Double(0);

    @ApiModelProperty("已还款期数")
    private Integer hasPayPeriods = new Integer(0);

    @ApiModelProperty("最近还款日期")
    private Date latelyPayDate;

    @ApiModelProperty("最近还款金额(元)")
    private Double latelyPayAmount = new Double(0);

    @ApiModelProperty("客户还款卡银行")
    private String depositBank;

    @ApiModelProperty("客户还款卡号")
    private String cardNumber;

    @ApiModelProperty("省份")
    private String province;

    @ApiModelProperty("城市")
    private String city;

    @ApiModelProperty("家庭住址")
    private String homeAddress;

    @ApiModelProperty("家庭固话")
    private String homePhone;

    @ApiModelProperty("身份证户籍地址")
    private String idCardAddress;

    @ApiModelProperty("工作单位名称")
    private String companyName;

    @ApiModelProperty("工作单位地址")
    private String companyAddr;

    @ApiModelProperty("工作单位电话")
    private String companyPhone;

    @ApiModelProperty("联系人1姓名")
    private String contactName1;

    @ApiModelProperty("联系人1与客户关系")
    private String contactRelation1;

    @ApiModelProperty("联系人1工作单位")
    private String contactWorkUnit1;

    @ApiModelProperty("联系人1单位电话")
    private String contactUnitPhone1;

    @ApiModelProperty("联系人1手机号码")
    private String contactPhone1;

    @ApiModelProperty("联系人1住宅电话")
    private String contactHomePhone1;

    @ApiModelProperty("联系人1现居地址")
    private String contactCurrAddress1;

    @ApiModelProperty("联系人2姓名")
    private String contactName2;

    @ApiModelProperty("联系人2与客户关系")
    private String contactRelation2;

    @ApiModelProperty("联系人2手机号码")
    private String contactPhone2;

    @ApiModelProperty("联系人2工作单位")
    private String contactWorkUnit2;

    @ApiModelProperty("联系人2单位电话")
    private String contactUnitPhone2;

    @ApiModelProperty("联系人2住宅电话")
    private String contactHomePhone2;

    @ApiModelProperty("联系人2现居地址")
    private String contactCurrAddress2;

    @ApiModelProperty("联系人3姓名")
    private String contactName3;

    @ApiModelProperty("联系人3与客户关系")
    private String contactRelation3;

    @ApiModelProperty("联系人3手机号码")
    private String contactPhone3;

    @ApiModelProperty("联系人3工作单位")
    private String contactWorkUnit3;

    @ApiModelProperty("联系人3单位电话")
    private String contactUnitPhone3;

    @ApiModelProperty("联系人3住宅电话")
    private String contactHomePhone3;

    @ApiModelProperty("联系人3现居地址")
    private String contactCurrAddress3;

    @ApiModelProperty("联系人4姓名")
    private String contactName4;

    @ApiModelProperty("联系人4与客户关系")
    private String contactRelation4;

    @ApiModelProperty("联系人4手机号码")
    private String contactPhone4;

    @ApiModelProperty("联系人4工作单位")
    private String contactWorkUnit4;

    @ApiModelProperty("联系人4单位电话")
    private String contactUnitPhone4;

    @ApiModelProperty("联系人4住宅电话")
    private String contactHomePhone4;

    @ApiModelProperty("联系人4现居地址")
    private String contactCurrAddress4;

    @ApiModelProperty("联系人5姓名")
    private String contactName5;

    @ApiModelProperty("联系人5与客户关系")
    private String contactRelation5;

    @ApiModelProperty("联系人5工作单位")
    private String contactWorkUnit5;

    @ApiModelProperty("联系人5单位电话")
    private String contactUnitPhone5;

    @ApiModelProperty("联系人5手机号码")
    private String contactPhone5;

    @ApiModelProperty("联系人5住宅电话")
    private String contactHomePhone5;

    @ApiModelProperty("联系人5现居地址")
    private String contactCurrAddress5;

    @ApiModelProperty("联系人6姓名")
    private String contactName6;

    @ApiModelProperty("联系人6与客户关系")
    private String contactRelation6;

    @ApiModelProperty("联系人6工作单位")
    private String contactWorkUnit6;

    @ApiModelProperty("联系人6单位电话")
    private String contactUnitPhone6;

    @ApiModelProperty("联系人6手机号码")
    private String contactPhone6;

    @ApiModelProperty("联系人6住宅电话")
    private String contactHomePhone6;

    @ApiModelProperty("联系人6现居地址")
    private String contactCurrAddress6;

    @ApiModelProperty("联系人7姓名")
    private String contactName7;

    @ApiModelProperty("联系人7与客户关系")
    private String contactRelation7;

    @ApiModelProperty("联系人7工作单位")
    private String contactWorkUnit7;

    @ApiModelProperty("联系人7单位电话")
    private String contactUnitPhone7;

    @ApiModelProperty("联系人7手机号码")
    private String contactPhone7;

    @ApiModelProperty("联系人7住宅电话")
    private String contactHomePhone7;

    @ApiModelProperty("联系人7现居地址")
    private String contactCurrAddress7;

    @ApiModelProperty("联系人8姓名")
    private String contactName8;

    @ApiModelProperty("联系人8与客户关系")
    private String contactRelation8;

    @ApiModelProperty("联系人8工作单位")
    private String contactWorkUnit8;

    @ApiModelProperty("联系人8单位电话")
    private String contactUnitPhone8;

    @ApiModelProperty("联系人8手机号码")
    private String contactPhone8;

    @ApiModelProperty("联系人8住宅电话")
    private String contactHomePhone8;

    @ApiModelProperty("联系人8现居地址")
    private String contactCurrAddress8;

    @ApiModelProperty("联系人9姓名")
    private String contactName9;

    @ApiModelProperty("联系人9与客户关系")
    private String contactRelation9;

    @ApiModelProperty("联系人9工作单位")
    private String contactWorkUnit9;

    @ApiModelProperty("联系人9单位电话")
    private String contactUnitPhone9;

    @ApiModelProperty("联系人9手机号码")
    private String contactPhone9;

    @ApiModelProperty("联系人9住宅电话")
    private String contactHomePhone9;

    @ApiModelProperty("联系人9现居地址")
    private String contactCurrAddress9;

    @ApiModelProperty("联系人10姓名")
    private String contactName10;

    @ApiModelProperty("联系人10与客户关系")
    private String contactRelation10;

    @ApiModelProperty("联系人10工作单位")
    private String contactWorkUnit10;

    @ApiModelProperty("联系人10单位电话")
    private String contactUnitPhone10;

    @ApiModelProperty("联系人10手机号码")
    private String contactPhone10;

    @ApiModelProperty("联系人10住宅电话")
    private String contactHomePhone10;

    @ApiModelProperty("联系人10现居地址")
    private String contactCurrAddress10;

    @ApiModelProperty("联系人11姓名")
    private String contactName11;

    @ApiModelProperty("联系人11与客户关系")
    private String contactRelation11;

    @ApiModelProperty("联系人11工作单位")
    private String contactWorkUnit11;

    @ApiModelProperty("联系人11单位电话")
    private String contactUnitPhone11;

    @ApiModelProperty("联系人11手机号码")
    private String contactPhone11;

    @ApiModelProperty("联系人11住宅电话")
    private String contactHomePhone11;

    @ApiModelProperty("联系人11现居地址")
    private String contactCurrAddress11;

    @ApiModelProperty("联系人12姓名")
    private String contactName12;

    @ApiModelProperty("联系人12与客户关系")
    private String contactRelation12;

    @ApiModelProperty("联系人12工作单位")
    private String contactWorkUnit12;

    @ApiModelProperty("联系人12单位电话")
    private String contactUnitPhone12;

    @ApiModelProperty("联系人12手机号码")
    private String contactPhone12;

    @ApiModelProperty("联系人12住宅电话")
    private String contactHomePhone12;

    @ApiModelProperty("联系人12现居地址")
    private String contactCurrAddress12;

    @ApiModelProperty("联系人13姓名")
    private String contactName13;

    @ApiModelProperty("联系人13与客户关系")
    private String contactRelation13;

    @ApiModelProperty("联系人13工作单位")
    private String contactWorkUnit13;

    @ApiModelProperty("联系人13单位电话")
    private String contactUnitPhone13;

    @ApiModelProperty("联系人13手机号码")
    private String contactPhone13;

    @ApiModelProperty("联系人13住宅电话")
    private String contactHomePhone13;

    @ApiModelProperty("联系人13现居地址")
    private String contactCurrAddress13;

    @ApiModelProperty("联系人14姓名")
    private String contactName14;

    @ApiModelProperty("联系人14与客户关系")
    private String contactRelation14;

    @ApiModelProperty("联系人14工作单位")
    private String contactWorkUnit14;

    @ApiModelProperty("联系人14单位电话")
    private String contactUnitPhone14;

    @ApiModelProperty("联系人14手机号码")
    private String contactPhone14;

    @ApiModelProperty("联系人14住宅电话")
    private String contactHomePhone14;

    @ApiModelProperty("联系人14现居地址")
    private String contactCurrAddress14;

    @ApiModelProperty("联系人15姓名")
    private String contactName15;

    @ApiModelProperty("联系人15与客户关系")
    private String contactRelation15;

    @ApiModelProperty("联系人15工作单位")
    private String contactWorkUnit15;

    @ApiModelProperty("联系人15单位电话")
    private String contactUnitPhone15;

    @ApiModelProperty("联系人15手机号码")
    private String contactPhone15;

    @ApiModelProperty("联系人15住宅电话")
    private String contactHomePhone15;

    @ApiModelProperty("联系人15现居地址")
    private String contactCurrAddress15;

    @ApiModelProperty("备注")
    private String memo;

    @ApiModelProperty("逾期管理费")
    private Double overdueManageFee=new Double(0);

    @ApiModelProperty("客户号")
    private String personalNumber;

    @ApiModelProperty("账户号")
    private String accountNumber;

    @ApiModelProperty("首次还款日期")
    private Date firstPayDate;

    @ApiModelProperty("账龄")
    private String accountAge;

    @ApiModelProperty("佣金比例(%)")
    private Double commissionRate = new Double(0);

    @ApiModelProperty("还款状态")
    private String paymentStatus;

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
    private String dataSources;

    @ApiModelProperty("案件手数")
    private Integer caseHandNum;

    @ApiModelProperty("公司码")
    private String companyCode;

    @ApiModelProperty("案件编号")
    private String caseNumber;

}
