package cn.fintecher.pangolin.report.model;

import cn.fintecher.pangolin.entity.util.ExcelAnno;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by sunyanping on 2017/9/19.
 */
@Data
public class FollowupExportModel {

    @ExcelAnno(cellName = "产品系列")
    private String series;
    @ExcelAnno(cellName = "合同编号")
    private String contractNumber;
    @ExcelAnno(cellName = "贷款日期")
    private Date loanDate;
    @ExcelAnno(cellName = "合同金额")
    private String cityName;
    @ExcelAnno(cellName = "城市")
    private String provinceName;
    @ExcelAnno(cellName = "省份")
    private BigDecimal contractAmount;
    @ExcelAnno(cellName = "剩余本金(元)")
    private BigDecimal leftCapital;
    @ExcelAnno(cellName = "剩余利息(元)")
    private BigDecimal leftInterest;
    @ExcelAnno(cellName = "逾期总金额(元)")
    private BigDecimal overdueAmount;
    @ExcelAnno(cellName = "逾期本金(元)")
    private BigDecimal overdueCapital;
    @ExcelAnno(cellName = "逾期利息(元)")
    private BigDecimal overdueInterest;
    @ExcelAnno(cellName = "逾期罚息(元)")
    private BigDecimal overdueFine;
    @ExcelAnno(cellName = "还款期数")
    private Integer periods;
    @ExcelAnno(cellName = "每期还款金额(元)")
    private BigDecimal perPayAmount;
    @ExcelAnno(cellName = "其他费用(元)")
    private BigDecimal otherAmt;
    @ExcelAnno(cellName = "逾期日期")
    private Date overDueDate;
    @ExcelAnno(cellName = "逾期期数")
    private Integer overduePeriods;
    @ExcelAnno(cellName = "逾期天数")
    private Integer overdueDays;
    @ExcelAnno(cellName = "已还款金额(元)")
    private BigDecimal hasPayAmount = new BigDecimal(0);
    @ExcelAnno(cellName = "已还款期数")
    private Integer hasPayPeriods;
    @ExcelAnno(cellName = "最近还款日期")
    private Date latelyPayDate;
    @ExcelAnno(cellName = "最近还款金额(元)")
    private BigDecimal latelyPayAmount;
    @ExcelAnno(cellName = "佣金比例(%)")
    private BigDecimal commissionRate;

    @ExcelAnno(cellName = "委外方")
    private String outsName;
    @ExcelAnno(cellName = "委外案件总金额")
    private BigDecimal outsourceTotalAmount;
    @ExcelAnno(cellName = "剩余金额(元)")
    private BigDecimal leftAmount;
    @ExcelAnno(cellName = "剩余委托时间(天)")
    private Integer leftDays;
    @ExcelAnno(cellName = "委案日期")
    private Date outTime;
    @ExcelAnno(cellName = "结案日期")
    private Date endOutTime;
    @ExcelAnno(cellName = "委案到期日期")
    private Date overOutTime;
    @ExcelAnno(cellName = "案件状态")
    private String outsourceCaseStatus;

    @ExcelAnno(cellName = "客户姓名")
    private String personalName;
    @ExcelAnno(cellName = "身份证号")
    private String idCard;
    @ExcelAnno(cellName = "手机号码")
    private String mobileNo;
    @ExcelAnno(cellName = "身份证户籍地址")
    private String idCardAddress;
    @ExcelAnno(cellName = "家庭住址")
    private String localHomeAddress;
    @ExcelAnno(cellName = "固定电话")
    private String localPhoneNo;

    @ExcelAnno(cellName = "客户还款卡银行")
    private String depositBank;
    @ExcelAnno(cellName = "客户还款卡号")
    private String cardNumber;

    @ExcelAnno(cellName = "工作单位名称")
    private String companyName;
    @ExcelAnno(cellName = "工作单位地址")
    private String companyAddress;
    @ExcelAnno(cellName = "工作单位电话")
    private String companyPhone;

    @ExcelAnno(cellName = "跟进时间")
    private String follTime;
    @ExcelAnno(cellName = "跟进方式")
    private String follType;
    @ExcelAnno(cellName = "催收对象")
    private String follTarget;
    @ExcelAnno(cellName = "姓名")
    private String follTargetName;
    @ExcelAnno(cellName = "电话状态")
    private String follPhoneNum;
    @ExcelAnno(cellName = "催收反馈")
    private String follFeedback;
    @ExcelAnno(cellName = "跟进记录")
    private String follContent;
    @ExcelAnno(cellName = "跟进人员")
    private String follOperator;

    @ExcelAnno(cellName = "联系人1姓名")
    private String concat1Name;
    @ExcelAnno(cellName = "联系人1手机号码")
    private String concat1Phone;
    @ExcelAnno(cellName = "联系人1住宅电话")
    private String concat1Mobile;
    @ExcelAnno(cellName = "联系人1现居地址")
    private String concat1Address;
    @ExcelAnno(cellName = "联系人1与客户关系")
    private String concat1Relation;
    @ExcelAnno(cellName = "联系人1工作单位")
    private String concat1Employer;
    @ExcelAnno(cellName = "联系人1单位电话")
    private String concat1WorkPhone;

    @ExcelAnno(cellName = "联系人2姓名")
    private String concat2Name;
    @ExcelAnno(cellName = "联系人2手机号码")
    private String concat2Phone;
    @ExcelAnno(cellName = "联系人2住宅电话")
    private String concat2Mobile;
    @ExcelAnno(cellName = "联系人2现居地址")
    private String concat2Address;
    @ExcelAnno(cellName = "联系人2与客户关系")
    private String concat2Relation;
    @ExcelAnno(cellName = "联系人2工作单位")
    private String concat2Employer;
    @ExcelAnno(cellName = "联系人2单位电话")
    private String concat2WorkPhone;

    @ExcelAnno(cellName = "联系人3姓名")
    private String concat3Name;
    @ExcelAnno(cellName = "联系人3手机号码")
    private String concat3Phone;
    @ExcelAnno(cellName = "联系人3住宅电话")
    private String concat3Mobile;
    @ExcelAnno(cellName = "联系人3现居地址")
    private String concat3Address;
    @ExcelAnno(cellName = "联系人3与客户关系")
    private String concat3Relation;
    @ExcelAnno(cellName = "联系人3工作单位")
    private String concat3Employer;
    @ExcelAnno(cellName = "联系人3单位电话")
    private String concat3WorkPhone;

    @ExcelAnno(cellName = "联系人4姓名")
    private String concat4Name;
    @ExcelAnno(cellName = "联系人4手机号码")
    private String concat4Phone;
    @ExcelAnno(cellName = "联系人4住宅电话")
    private String concat4Mobile;
    @ExcelAnno(cellName = "联系人4现居地址")
    private String concat4Address;
    @ExcelAnno(cellName = "联系人4与客户关系")
    private String concat4Relation;
    @ExcelAnno(cellName = "联系人4工作单位")
    private String concat4Employer;
    @ExcelAnno(cellName = "联系人4单位电话")
    private String concat4WorkPhone;

    @ExcelAnno(cellName = "联系人5姓名")
    private String concat5Name;
    @ExcelAnno(cellName = "联系人5手机号码")
    private String concat5Phone;
    @ExcelAnno(cellName = "联系人5住宅电话")
    private String concat5Mobile;
    @ExcelAnno(cellName = "联系人5现居地址")
    private String concat5Address;
    @ExcelAnno(cellName = "联系人5与客户关系")
    private String concat5Relation;
    @ExcelAnno(cellName = "联系人5工作单位")
    private String concat5Employer;
    @ExcelAnno(cellName = "联系人5单位电话")
    private String concat5WorkPhone;

    @ExcelAnno(cellName = "联系人6姓名")
    private String concat6Name;
    @ExcelAnno(cellName = "联系人6手机号码")
    private String concat6Phone;
    @ExcelAnno(cellName = "联系人6住宅电话")
    private String concat6Mobile;
    @ExcelAnno(cellName = "联系人6现居地址")
    private String concat6Address;
    @ExcelAnno(cellName = "联系人6与客户关系")
    private String concat6Relation;
    @ExcelAnno(cellName = "联系人6工作单位")
    private String concat6Employer;
    @ExcelAnno(cellName = "联系人6单位电话")
    private String concat6WorkPhone;

    @ExcelAnno(cellName = "联系人7姓名")
    private String concat7Name;
    @ExcelAnno(cellName = "联系人7手机号码")
    private String concat7Phone;
    @ExcelAnno(cellName = "联系人7住宅电话")
    private String concat7Mobile;
    @ExcelAnno(cellName = "联系人7现居地址")
    private String concat7Address;
    @ExcelAnno(cellName = "联系人7与客户关系")
    private String concat7Relation;
    @ExcelAnno(cellName = "联系人7工作单位")
    private String concat7Employer;
    @ExcelAnno(cellName = "联系人7单位电话")
    private String concat7WorkPhone;

    @ExcelAnno(cellName = "联系人8姓名")
    private String concat8Name;
    @ExcelAnno(cellName = "联系人8手机号码")
    private String concat8Phone;
    @ExcelAnno(cellName = "联系人8住宅电话")
    private String concat8Mobile;
    @ExcelAnno(cellName = "联系人8现居地址")
    private String concat8Address;
    @ExcelAnno(cellName = "联系人8与客户关系")
    private String concat8Relation;
    @ExcelAnno(cellName = "联系人8工作单位")
    private String concat8Employer;
    @ExcelAnno(cellName = "联系人8单位电话")
    private String concat8WorkPhone;

    @ExcelAnno(cellName = "联系人9姓名")
    private String concat9Name;
    @ExcelAnno(cellName = "联系人9手机号码")
    private String concat9Phone;
    @ExcelAnno(cellName = "联系人9住宅电话")
    private String concat9Mobile;
    @ExcelAnno(cellName = "联系人9现居地址")
    private String concat9Address;
    @ExcelAnno(cellName = "联系人9与客户关系")
    private String concat9Relation;
    @ExcelAnno(cellName = "联系人9工作单位")
    private String concat9Employer;
    @ExcelAnno(cellName = "联系人9单位电话")
    private String concat9WorkPhone;

    @ExcelAnno(cellName = "联系人10姓名")
    private String concat10Name;
    @ExcelAnno(cellName = "联系人10手机号码")
    private String concat10Phone;
    @ExcelAnno(cellName = "联系人10住宅电话")
    private String concat10Mobile;
    @ExcelAnno(cellName = "联系人10现居地址")
    private String concat10Address;
    @ExcelAnno(cellName = "联系人10与客户关系")
    private String concat10Relation;
    @ExcelAnno(cellName = "联系人10工作单位")
    private String concat10Employer;
    @ExcelAnno(cellName = "联系人10单位电话")
    private String concat10WorkPhone;

    @ExcelAnno(cellName = "联系人11姓名")
    private String concat11Name;
    @ExcelAnno(cellName = "联系人11手机号码")
    private String concat11Phone;
    @ExcelAnno(cellName = "联系人11住宅电话")
    private String concat11Mobile;
    @ExcelAnno(cellName = "联系人11现居地址")
    private String concat11Address;
    @ExcelAnno(cellName = "联系人11与客户关系")
    private String concat11Relation;
    @ExcelAnno(cellName = "联系人11工作单位")
    private String concat11Employer;
    @ExcelAnno(cellName = "联系人11单位电话")
    private String concat11WorkPhone;

    @ExcelAnno(cellName = "联系人12姓名")
    private String concat12Name;
    @ExcelAnno(cellName = "联系人12手机号码")
    private String concat12Phone;
    @ExcelAnno(cellName = "联系人12住宅电话")
    private String concat12Mobile;
    @ExcelAnno(cellName = "联系人12现居地址")
    private String concat12Address;
    @ExcelAnno(cellName = "联系人12与客户关系")
    private String concat12Relation;
    @ExcelAnno(cellName = "联系人12工作单位")
    private String concat12Employer;
    @ExcelAnno(cellName = "联系人12单位电话")
    private String concat12WorkPhone;

    @ExcelAnno(cellName = "联系人13姓名")
    private String concat13Name;
    @ExcelAnno(cellName = "联系人13手机号码")
    private String concat13Phone;
    @ExcelAnno(cellName = "联系人13住宅电话")
    private String concat13Mobile;
    @ExcelAnno(cellName = "联系人13现居地址")
    private String concat13Address;
    @ExcelAnno(cellName = "联系人13与客户关系")
    private String concat13Relation;
    @ExcelAnno(cellName = "联系人13工作单位")
    private String concat13Employer;
    @ExcelAnno(cellName = "联系人13单位电话")
    private String concat13WorkPhone;

    @ExcelAnno(cellName = "联系人14姓名")
    private String concat14Name;
    @ExcelAnno(cellName = "联系人14手机号码")
    private String concat14Phone;
    @ExcelAnno(cellName = "联系人14住宅电话")
    private String concat14Mobile;
    @ExcelAnno(cellName = "联系人14现居地址")
    private String concat14Address;
    @ExcelAnno(cellName = "联系人14与客户关系")
    private String concat14Relation;
    @ExcelAnno(cellName = "联系人14工作单位")
    private String concat14Employer;
    @ExcelAnno(cellName = "联系人14单位电话")
    private String concat14WorkPhone;

    @ExcelAnno(cellName = "联系人15姓名")
    private String concat15Name;
    @ExcelAnno(cellName = "联系人15手机号码")
    private String concat15Phone;
    @ExcelAnno(cellName = "联系人15住宅电话")
    private String concat15Mobile;
    @ExcelAnno(cellName = "联系人15现居地址")
    private String concat15Address;
    @ExcelAnno(cellName = "联系人15与客户关系")
    private String concat15Relation;
    @ExcelAnno(cellName = "联系人15工作单位")
    private String concat15Employer;
    @ExcelAnno(cellName = "联系人15单位电话")
    private String concat15WorkPhone;
}
