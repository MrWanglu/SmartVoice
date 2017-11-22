package cn.fintecher.pangolin.report.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : xiaqun
 * @Description : 查询案件条件
 * @Date : 9:52 2017/11/1
 */

@Data
public class CaseInfoConditionParams {
    private String collectionStatusList; //催收状态集合
    private String assistStatusList; //协催状态集合
    private Integer collectionStatus; //催收状态
    private String personalName; //客户姓名
    private String mobileNo; //客户手机号
    private String deptCode; //机构code码
    private String collectorName; //催收员姓名
    private BigDecimal overdueMaxAmt; //最大案件金额
    private BigDecimal overdueMinAmt; //最小案件金额
    private String payStatus; //还款状态
    private Integer overMaxDay; //最大逾期天数
    private Integer overMinDay; //最小逾期天数
    private String batchNumber; //批次号
    private String principalId; //委托方ID
    private String idCard; //客户身份证号
    private Integer followupBack; //催收反馈
    private Integer assistWay; //协催方式
    private Integer caseMark; //案件标记
    private String collectionType; //催收类型
    private Integer areaId; //所属城市ID
    private Integer parentAreaId; //省ID
    private String startFollowDate; //根据时间开始
    private String endFollowDate; //跟进时间结束
    private String cardNumber; //银行卡号
    private BigDecimal realPayMaxAmt; //最大还款金额
    private BigDecimal realPayMinAmt; //最小还款金额
    private String userId; //用户ID
    private Integer isManager; //是否是管理员
    private String companyCode; //公司code
    private Integer feedBack; //催收反馈
    private String code; //催收反馈
    private Integer page; //页数
    private Integer size; //每页条数
    private String sort; //排序

}