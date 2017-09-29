package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.CaseInfoVerficationModel;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.message.SendReminderMessage;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author yuanyanting
 * @version Id:CaseInfoJudicialService.java,v 0.1 2017/9/27 14:46 yuanyanting Exp $$
 */
@Service("caseInfoJudicialService")
public class CaseInfoJudicialService {

    @Inject
    private CaseInfoJudicialApplyRepository caseInfoJudicialApplyRepository;

    @Inject
    private CaseInfoRepository caseInfoRepository;

    @Inject
    private CaseAssistRepository caseAssistRepository;

    @Inject
    private CaseTurnRecordRepository caseTurnRecordRepository;

    @Inject
    private CaseInfoJudicialRepository caseInfoJudicialRepository;

    @Inject
    private ReminderService reminderService;

    /**
     * set司法申请属性值
     * @param apply 申请
     * @param caseInfo 案件
     * @param user 申请人
     * @param applyReason
     */
    public void setJudicialApply(CaseInfoJudicialApply apply, CaseInfo caseInfo, User user, String applyReason) {
        BeanUtils.copyProperties(caseInfo, apply);
        apply.setApplicant(user.getRealName()); // 申请人
        apply.setApplicationDate(ZWDateUtil.getNowDateTime()); // 申请日期
        apply.setApplicationReason(applyReason); // 申请理由
        apply.setApprovalStatus(CaseInfoVerificationApply.ApprovalStatus.approval_pending.getValue()); // 申请状态：审批待通过
        apply.setCaseId(caseInfo.getId()); // 案件Id
        if (Objects.nonNull(caseInfo.getArea())) {
            apply.setCity(caseInfo.getArea().getId()); // 城市
            if (Objects.nonNull(caseInfo.getArea().getParent())) {
                apply.setProvince(caseInfo.getArea().getParent().getId()); // 省份
            }
        }
        if (Objects.nonNull(caseInfo.getPrincipalId())) {
            apply.setPrincipalName(caseInfo.getPrincipalId().getName()); // 委托方名称
        }
        if (Objects.nonNull(caseInfo.getPersonalInfo())) {
            apply.setPersonalName(caseInfo.getPersonalInfo().getName()); // 客户名称
            apply.setMobileNo(caseInfo.getPersonalInfo().getMobileNo()); // 电话号
            apply.setIdCard(caseInfo.getPersonalInfo().getIdCard()); // 身份证号
        }
    }

    /**
     * set核销申请通过属性值
     *
     * @param caseInfoVerficationModel 核销申请
     * @param user 审批人
     */
    public void caseInfoJudicialApply(CaseInfoVerficationModel caseInfoVerficationModel,User user) {
        CaseInfoJudicialApply caseInfoJudicialApply = caseInfoJudicialApplyRepository.findOne(caseInfoVerficationModel.getId());
        CaseInfoJudicial caseInfoJudicial = new CaseInfoJudicial();
        // 超级管理员
        if (Objects.isNull(user.getCompanyCode())) {
            if (Objects.nonNull(caseInfoVerficationModel.getCompanyCode())) {
                caseInfoJudicialApply.setCompanyCode(caseInfoVerficationModel.getCompanyCode());
                caseInfoJudicial.setCompanyCode(caseInfoVerficationModel.getCompanyCode());
            }
        } else {
            caseInfoJudicialApply.setCompanyCode(user.getCompanyCode());
            caseInfoJudicial.setCompanyCode(user.getCompanyCode());
        }
        if (Objects.equals(caseInfoVerficationModel.getApprovalResult(), 0)) { // 审批拒绝
            caseInfoJudicialApply.setApprovalResult(CaseInfoVerificationApply.ApprovalResult.disapprove.getValue()); // 审批结果：拒绝
            caseInfoJudicialApply.setApprovalStatus(CaseInfoVerificationApply.ApprovalStatus.approval_disapprove.getValue()); // 审批状态：审批拒绝
            caseInfoJudicialApplyRepository.save(caseInfoJudicialApply);
        } else { // 核销审批通过
            caseInfoJudicialApply.setApprovalResult(CaseInfoVerificationApply.ApprovalResult.approve.getValue()); // 审批结果：通过
            caseInfoJudicialApply.setApprovalStatus(CaseInfoVerificationApply.ApprovalStatus.approval_approve.getValue()); // 审批状态：审批通过
            caseInfoJudicialApply.setOperator(user.getUserName()); // 审批人
            caseInfoJudicialApply.setOperatorTime(ZWDateUtil.getNowDateTime()); // 审批时间
            CaseInfo caseInfo = caseInfoRepository.findOne(caseInfoJudicialApply.getCaseId());
            List<CaseAssist> caseAssistList = new ArrayList<>();
            //处理协催案件
            if (Objects.equals(caseInfo.getAssistFlag(), 1)) { //协催标识
                //结束协催案件
                CaseAssist one = caseAssistRepository.findOne(QCaseAssist.caseAssist.caseId.eq(caseInfo).and(QCaseAssist.caseAssist.assistStatus.notIn(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())));
                if (Objects.nonNull(one)) {
                    one.setAssistCloseFlag(0); //手动结束
                    one.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //协催结束
                    one.setOperator(user);
                    one.setOperatorTime(new Date());
                    one.setCaseFlowinTime(new Date()); //流入时间
                    caseAssistList.add(one);
                }
                caseInfo.setAssistFlag(0); //协催标识置0
                caseInfo.setAssistStatus(null);//协催状态置空
                caseInfo.setAssistWay(null);
                caseInfo.setAssistCollector(null);
                caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //29-协催完成
                //协催结束新增一条流转记录
                CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
                BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
                caseTurnRecord.setId(null); //主键置空
                caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
                caseTurnRecord.setDepartId(caseInfo.getDepartment().getId()); //部门ID
                caseTurnRecord.setReceiveUserRealName(caseInfo.getCurrentCollector().getRealName()); //接受人名称
                caseTurnRecord.setReceiveDeptName(caseInfo.getCurrentCollector().getDepartment().getName()); //接收部门名称
                caseTurnRecord.setOperatorUserName(user.getUserName()); //操作员用户名
                caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                caseTurnRecordRepository.saveAndFlush(caseTurnRecord);
            }
            caseInfo.setEndType(CaseInfo.EndType.JUDGMENT_CLOSED.getValue()); // 结案类型：司法结案
            caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.CASE_OVER.getValue()); // 催收状态：已结案
            caseInfoRepository.save(caseInfo);
            caseInfoJudicial.setCaseInfo(caseInfo);
            caseInfoJudicialRepository.save(caseInfoJudicial);
            caseInfoJudicial.setOperatorUserName(user.getUserName()); // 操作用户名
            caseInfoJudicial.setOperatorRealName(user.getRealName()); // 操作姓名
            caseInfoJudicial.setOperatorTime(ZWDateUtil.getNowDateTime()); // 操作时间
            caseInfoJudicialApplyRepository.save(caseInfoJudicialApply);
            //消息提醒
            SendReminderMessage sendReminderMessage = new SendReminderMessage();
            sendReminderMessage.setUserId(user.getId());
            sendReminderMessage.setTitle("客户 [" + caseInfo.getPersonalInfo().getName() + "] 的司法申请审批" + (Objects.equals(caseInfoVerficationModel.getApprovalResult(), 0) ? "拒绝" : "通过"));
            sendReminderMessage.setContent(caseInfoVerficationModel.getApprovalOpinion());
            sendReminderMessage.setType(ReminderType.judicial);
            reminderService.sendReminder(sendReminderMessage);
        }
    }
}
