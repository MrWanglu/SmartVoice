package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.CaseInfoVerificationApply;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.springframework.beans.BeanUtils;

import java.util.Objects;

/**
 * Created by sunyanping on 2017/9/27.
 */
public class VerificationApplyService {

    /**
     * set核销申请属性值
     * @param apply 核销申请
     * @param caseInfo 案件
     * @param user 申请人
     * @param applyReason
     */
    public void setVerificationApply(CaseInfoVerificationApply apply, CaseInfo caseInfo, User user, String applyReason) {
        BeanUtils.copyProperties(caseInfo, apply);
        apply.setId(null);
        apply.setOperator(user.getRealName()); // 操作人
        apply.setOperatorTime(ZWDateUtil.getNowDateTime()); // 操作时间
        apply.setApplicant(user.getRealName()); // 申请人
        apply.setApplicationDate(ZWDateUtil.getNowDateTime()); // 申请日期
        apply.setApplicationReason(applyReason); // 申请理由
        apply.setApprovalStatus(CaseInfoVerificationApply.ApprovalStatus.approval_pending.getValue()); // 申请状态：审批待通过
        apply.setCaseId(caseInfo.getId()); // 案件Id
        if (Objects.nonNull(caseInfo.getArea())) {
            apply.setCity(caseInfo.getArea().getAreaName()); // 城市
            if (Objects.nonNull(caseInfo.getArea().getParent())) {
                apply.setProvince(caseInfo.getArea().getParent().getAreaName()); // 省份
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
    
}
