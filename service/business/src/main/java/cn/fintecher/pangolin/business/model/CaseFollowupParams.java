package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author : xiaqun
 * @Description : 添加跟进记录参数
 * @Date : 10:34 2017/8/8
 */

@Data
public class CaseFollowupParams {
    private String id;
    private String caseId; //案件ID
    private String caseNumber; //案件编号
    private String personalId; //客户信息ID
    private Integer collectionFeedback; //催收反馈
    private Integer collectionType; //催收类型
    private String content; //跟进内容
    private Integer follnextFlag; //下次跟进标识 0-没有 1-有
    private String follnextContent; //下次跟进提醒内容
    private String follnextDate; //下次跟进提醒日期
    private Integer promiseFlag; //承诺还款标识 0-没有承诺 1-有承诺
    private BigDecimal promiseAmt; //承诺还款金额
    private Date promiseDate; //承诺还款日期
    private Integer source; //数据来源
    private Integer target; //跟进对象
    private String targetName; //跟进对象名称
    private Integer type; //跟进方式
    private Integer collectionWay; //催记方式 0-自动 1-手动
    private String companyCode; //公司code码
    private List fileIds; //文件ID
    private Integer addrType;//地址类型
    private String detail;//详细地址
    private String collectionLocation;//定位地址
    private Integer contactState; //联系电话状态
    private String contactPhone; //联系电话
    private Integer addrStatus; //地址状态
    private String taskId;
    private String recoderId;
    private String taskcallerId;
    private Integer callType;//erpv3 163,中通天鸿164,云羿 165,BeauPhone语音卡 229
    private String fileName;//录音文件名称
    private String filePath;//录音文件目录
}