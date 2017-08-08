package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 添加跟进记录参数
 * @Date : 10:34 2017/8/8
 */

@Data
public class CaseFollowupParams {
    private String caseId; //案件ID
    private String personalId; //客户信息ID
    private Integer collectionFeedback; //催收反馈
    private Integer collectionType; //催收类型
    private String content; //跟进内容
    private Integer follnextFlag; //下次跟进标识 0-没有 1-有
    private String follnextContent; //下次跟进提醒内容
    private Date follnextDate; //下次跟进提醒日期
    private Integer promiseFlag; //承诺还款标识 0-没有承诺 1-有承诺
    private BigDecimal promiseAmt; //承诺还款金额
    private Date promiseDate; //承诺还款日期
    private Integer source; //数据来源
    private Integer target; //跟进对象
    private String targetName; //跟进对象名称
    private Integer type; //跟进方式
    private String companyCode; //公司code码
}