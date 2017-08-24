package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.*;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.entity.message.SendReminderMessage;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWDateUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;

import static cn.fintecher.pangolin.entity.QCaseInfo.caseInfo;

/**
 * @author : xiaqun
 * @Description : 催收业务
 * @Date : 16:45 2017/7/17
 */

@Service("caseInfoService")
public class CaseInfoService {
    final Logger log = LoggerFactory.getLogger(CaseInfoService.class);


    @Inject
    CaseInfoRepository caseInfoRepository;

    @Inject
    CaseAssistRepository caseAssistRepository;

    @Inject
    CaseAssistApplyRepository caseAssistApplyRepository;

    @Inject
    CaseTurnRecordRepository caseTurnRecordRepository;

    @Inject
    CasePayApplyRepository casePayApplyRepository;

    @Inject
    CaseFollowupRecordRepository caseFollowupRecordRepository;

    @Inject
    SysParamRepository sysParamRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    UserService userService;

    @Inject
    CasePayFileRepository casePayFileRepository;

    @Inject
    PersonalContactRepository personalContactRepository;

    @Inject
    RestTemplate restTemplate;

    @Autowired
    CaseInfoExceptionService caseInfoExceptionService;

    @Autowired
    CaseInfoDistributedRepository caseInfoDistributedRepository;

    @Autowired
    CaseRepairRepository caseRepairRepository;

    @Inject
    DepartmentRepository departmentRepository;

    @Inject
    PersonalAddressRepository personalAddressRepository;

    @Inject
    ReminderService reminderService;

    @Inject
    CompanyRepository companyRepository;

    /**
     * @Description 重新分配
     */
    public void reDistribution(ReDistributionParams reDistributionParams, User tokenUser) {
        if (StringUtils.equals(reDistributionParams.getUserName(), "administrator")) {
            throw new RuntimeException("不能分配给超级管理员");
        }
        User user = userRepository.findByUserName(reDistributionParams.getUserName());
        if (Objects.isNull(user)) {
            throw new RuntimeException("查不到该用户");
        }
        if (Objects.equals(user.getStatus(), 1)) {
            throw new RuntimeException("该用户已停用");
        }
        CaseInfo caseInfo = caseInfoRepository.findOne(reDistributionParams.getCaseId());
        QCaseAssist qCaseAssist = QCaseAssist.caseAssist;
        if (Objects.equals(reDistributionParams.getIsAssist(), false)) { //不是协催案件
            if (Objects.equals(user.getType(), User.Type.TEL.getValue())) { //分配给15-电催
                setAttribute(caseInfo, user, tokenUser);
            } else if (Objects.equals(user.getType(), User.Type.VISIT.getValue())) { //分配给16-外访
                if (Objects.equals(caseInfo.getAssistFlag(), 1)) { //有协催标识
                    if (Objects.equals(caseInfo.getAssistStatus(), CaseInfo.AssistStatus.ASSIST_APPROVEING.getValue())) { //有协催申请
                        CaseAssistApply caseAssistApply = getCaseAssistApply(reDistributionParams.getCaseId(), tokenUser, "流转强制拒绝");
                        caseAssistApplyRepository.saveAndFlush(caseAssistApply);
                    } else { //有协催案件
                        CaseAssist caseAssist = caseAssistRepository.findOne(qCaseAssist.caseId.id.eq(reDistributionParams.getCaseId()).
                                and(qCaseAssist.assistStatus.ne(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())));
                        if (Objects.isNull(caseAssist)) {
                            throw new RuntimeException("协催案件未找到");
                        }
                        caseAssist.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //协催状态 29-协催完成
                        caseAssist.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                        caseAssist.setOperator(tokenUser); //操作员
                        caseAssistRepository.saveAndFlush(caseAssist);

                        //协催结束新增一条流转记录
                        CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
                        BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
                        caseTurnRecord.setId(null); //主键置空
                        caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
                        caseTurnRecord.setDepartId(caseInfo.getCurrentCollector().getDepartment().getId()); //部门ID
                        caseTurnRecord.setReceiveUserRealName(caseInfo.getCurrentCollector().getRealName()); //接受人名称
                        caseTurnRecord.setReceiveUserId(caseInfo.getCurrentCollector().getId()); //接受人ID
                        caseTurnRecord.setCurrentCollector(caseInfo.getLatelyCollector().getId()); //当前催收员ID
                        caseTurnRecord.setReceiveDeptName(user.getDepartment().getName()); //接收部门名称
                        caseTurnRecord.setCirculationType(2); //流转类型 2-正常流转
                        caseTurnRecord.setOperatorUserName(tokenUser.getUserName()); //操作员
                        caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                        caseTurnRecordRepository.saveAndFlush(caseTurnRecord);
                    }
                }
                setAttribute(caseInfo, user, tokenUser);

                //同步更新原案件协催员，协催方式，协催标识，协催状态
                caseInfo.setAssistCollector(null); //协催员置空
                caseInfo.setAssistWay(null); //协催方式置空
                caseInfo.setAssistFlag(0); //协催标识 0-否
                caseInfo.setAssistStatus(null); //协催状态置空
            }
        } else { //是协催案件
            if (!Objects.equals(user.getType(), User.Type.VISIT.getValue())) {
                throw new RuntimeException("协催案件不能分配给外访以外人员");
            }
            CaseAssist caseAssist = caseAssistRepository.findOne(qCaseAssist.caseId.id.eq(reDistributionParams.getCaseId()).
                    and(qCaseAssist.assistStatus.ne(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())));
            if (Objects.isNull(caseAssist)) {
                throw new RuntimeException("协催案件未找到");
            }
            caseAssist.setLatelyCollector(caseAssist.getAssistCollector()); //上一个协催员
            caseAssist.setAssistCollector(user); //协催员
            caseAssist.setDepartId(user.getDepartment().getId()); //部门
            caseAssist.setOperator(tokenUser); //操作员
            caseAssist.setCaseFlowinTime(ZWDateUtil.getNowDateTime()); //流入时间
            caseAssist.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
            caseAssist.setHoldDays(0); //持案天数归0
            caseAssist.setMarkId(CaseInfo.Color.NO_COLOR.getValue());//打标
            caseAssist.setAssistStatus(CaseInfo.AssistStatus.ASSIST_WAIT_ACC.getValue());//协催待催收
            caseAssist.setCaseFlowinTime(new Date());
            caseAssistRepository.saveAndFlush(caseAssist);

            //同步更新原案件协催员
            caseInfo.setAssistCollector(user); //协催员
        }
        caseInfoRepository.saveAndFlush(caseInfo);

        //分配完成新增流转记录
        CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
        BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
        caseTurnRecord.setId(null); //主键置空
        caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
        caseTurnRecord.setDepartId(user.getDepartment().getId()); //部门ID
        caseTurnRecord.setReceiveUserRealName(user.getRealName()); //接受人名称
        caseTurnRecord.setReceiveDeptName(user.getDepartment().getName()); //接收部门名称
        caseTurnRecord.setReceiveUserId(user.getId()); //接受人ID
        caseTurnRecord.setCurrentCollector(caseInfo.getCurrentCollector().getId()); //当前催收员ID
        caseTurnRecord.setCirculationType(2); //流转类型 2-正常流转
        caseTurnRecord.setOperatorUserName(tokenUser.getUserName()); //操作员用户名
        caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
        caseTurnRecordRepository.saveAndFlush(caseTurnRecord);
    }

//    /**
//     * @Description 客户信息
//     */
//    public Personal getCustInfo(String caseId) {
//        CaseInfo caseInfo = caseInfoRepository.findOne(caseId); //获得案件
//        if (Objects.isNull(caseInfo)) {
//            throw new RuntimeException("该案件未找到");
//        }
//        //获取客户基本信息
//        return caseInfo.getPersonalInfo();
//    }

    /**
     * @Description 申请还款操作
     */
    public void doPay(PayApplyParams payApplyParams, User tokenUser) {
        CaseInfo caseInfo = caseInfoRepository.findOne(payApplyParams.getCaseId());
        if (Objects.isNull(caseInfo)) {
            throw new RuntimeException("该案件未找到");
        }
        if (Objects.equals(caseInfo.getHandUpFlag(), CaseInfo.HandUpFlag.YES_HANG.getValue())) {
            throw new RuntimeException("挂起案件不允许做还款操作");
        }
        if (Objects.equals(caseInfo.getCollectionStatus(), CaseInfo.CollectionStatus.OVER_PAYING.getValue())
                || Objects.equals(caseInfo.getCollectionStatus(), CaseInfo.CollectionStatus.EARLY_PAYING.getValue())
                || Objects.equals(caseInfo.getCollectionStatus(), CaseInfo.CollectionStatus.REPAID.getValue())) {
            throw new RuntimeException("该案件正在还款中或已还款，不允许再次还款");
        }
        if (BigDecimal.ZERO.compareTo(payApplyParams.getPayAmt()) == 1) {
            throw new RuntimeException("还款金额不能小于0");
        }
        if (Objects.equals(payApplyParams.getDerateFlag(), 1)) {
            if (BigDecimal.ZERO.compareTo(payApplyParams.getDerateFee()) == 1) {
                throw new RuntimeException("减免金额不能小于0");
            }
        }

        //更新案件状态
        if (Objects.equals(payApplyParams.getPayaType(), CasePayApply.PayType.DERATEOVERDUE.getValue())
                || Objects.equals(payApplyParams.getPayaType(), CasePayApply.PayType.ALLOVERDUE.getValue())
                || Objects.equals(payApplyParams.getPayaType(), CasePayApply.PayType.PARTOVERDUE.getValue())) { //还款类型为减免逾期还款、全额逾期还款、部分逾期还款
            if (Objects.isNull(caseInfo.getOverdueAmount())
                    || Objects.equals(BigDecimal.ZERO.compareTo(caseInfo.getOverdueAmount()), 0)) { //如果逾期总金额为空或为0
                throw new RuntimeException("逾期本期应还金额为0，不允许还款");
            }
            caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.OVER_PAYING.getValue()); //催收状态 22-逾期还款中
        } else { //还款类型为减免提前结清、全额提前结清、部分提前结清
            if (Objects.isNull(caseInfo.getEarlySettleAmt())
                    || Objects.equals(BigDecimal.ZERO.compareTo(caseInfo.getEarlySettleAmt()), 0)) { //如果提前结清总金额为空或为0
                throw new RuntimeException("提前结清本期应还金额为0，不允许还款");
            }
            caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.EARLY_PAYING.getValue()); //催收状态 23-提前结清中
        }
        caseInfo.setOperator(tokenUser); //操作员
        caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
        caseInfoRepository.saveAndFlush(caseInfo);

        //新增还款审批记录
        CasePayApply casePayApply = new CasePayApply();
        casePayApply.setCaseId(caseInfo.getId()); //案件ID
        casePayApply.setCaseNumber(caseInfo.getCaseNumber()); //案件编号
        casePayApply.setPersonalName(caseInfo.getPersonalInfo().getName()); //客户姓名
        casePayApply.setPersonalId(caseInfo.getPersonalInfo().getId()); //客户信息ID
        casePayApply.setCollectionType(caseInfo.getCollectionType()); //催收类型
        casePayApply.setDepartId(caseInfo.getDepartment().getId()); //部门ID
        casePayApply.setApplyPayAmt(payApplyParams.getPayAmt()); //申请还款金额
        casePayApply.setApplyDerateAmt(Objects.isNull(payApplyParams.getDerateFee()) ? new BigDecimal(0) : payApplyParams.getDerateFee()); //申请减免金额
        casePayApply.setPayType(payApplyParams.getPayaType()); //还款类型
        casePayApply.setPayWay(payApplyParams.getPayWay()); //还款方式
        casePayApply.setDerateFlag(payApplyParams.getDerateFlag()); //减免标识
        casePayApply.setApproveDerateRemark(payApplyParams.getDerateDescripton()); //减免费用备注
        if (Objects.equals(payApplyParams.getDerateFlag(), 1)) { //减免标识 1-有减免
            casePayApply.setApproveStatus(CasePayApply.ApproveStatus.DERATE_TO_AUDIT.getValue()); //审批状态 55-减免待审核
        } else { //减免标识 0-没有减免
            casePayApply.setApproveStatus(CasePayApply.ApproveStatus.PAY_TO_AUDIT.getValue()); //审批状态 57-还款待审核
        }
        casePayApply.setPayMemo(payApplyParams.getPayDescripton()); //还款说明
        casePayApply.setApplyUserName(tokenUser.getUserName()); //申请人
        casePayApply.setApplyRealName(tokenUser.getRealName()); //申请人姓名
        casePayApply.setApplyDeptName(tokenUser.getDepartment().getName()); //申请人部门名称
        casePayApply.setApplyDate(ZWDateUtil.getNowDateTime()); //申请时间
        casePayApply.setCompanyCode(caseInfo.getCompanyCode()); //公司code码
        casePayApply.setPersonalPhone(caseInfo.getPersonalInfo().getMobileNo()); //客户手机号
        casePayApply.setPrincipalId(caseInfo.getPrincipalId().getId()); //委托方ID
        casePayApply.setPrincipalName(caseInfo.getPrincipalId().getName()); //委托方名称
        casePayApply.setBatchNumber(caseInfo.getBatchNumber()); //批次号
        casePayApply.setCaseAmt(caseInfo.getHasPayAmount()); //案件金额
        casePayApplyRepository.saveAndFlush(casePayApply);
        //保存还款凭证文件id到case_pay_file
        List<String> fileIds = payApplyParams.getFileIds();
        if (Objects.nonNull(fileIds) && fileIds.size() > 0) {
            for (String id : fileIds) {
                String fileId = id.trim();
                CasePayFile casePayFile = new CasePayFile();
                casePayFile.setFileid(fileId);
                casePayFile.setCaseNumber(caseInfo.getCaseNumber());
                casePayFile.setOperatorTime(ZWDateUtil.getNowDateTime());
                casePayFile.setOperator(tokenUser.getUserName());
                casePayFile.setOperatorName(tokenUser.getRealName());
                casePayFile.setPayId(casePayApply.getId());
                casePayFile.setCaseId(caseInfo.getId());
                casePayFileRepository.saveAndFlush(casePayFile);
            }
        }
    }

    /**
     * @Description 还款撤回
     */
    public void payWithdraw(String payApplyId, User tokenUser) {
        CasePayApply casePayApply = casePayApplyRepository.findOne(payApplyId);
        if (Objects.isNull(casePayApply)) {
            throw new RuntimeException("该还款审批未找到");
        }
        if (Objects.equals(casePayApply.getApproveStatus(), CasePayApply.ApproveStatus.REVOKE.getValue())) {
            throw new RuntimeException("还款已撤回，不能再次撤回");
        }

        //修改原案件催收状态
        CaseInfo caseInfo = caseInfoRepository.findOne(casePayApply.getCaseId());
        if (Objects.isNull(caseInfo)) {
            throw new RuntimeException("该案件未找到");
        }
        if (Objects.equals(caseInfo.getHandUpFlag(), 1)) {
            throw new RuntimeException("挂起案件不允许操作");
        }
        if (Objects.equals(caseInfo.getCollectionStatus(), CaseInfo.CollectionStatus.REPAID.getValue()) //已还款
                || Objects.equals(caseInfo.getCollectionStatus(), CaseInfo.CollectionStatus.CASE_OVER.getValue()) //已结案
                || Objects.equals(caseInfo.getCollectionStatus(), CaseInfo.CollectionStatus.PART_REPAID.getValue()) //部分已还款
                || Objects.equals(caseInfo.getCollectionStatus(), CaseInfo.CollectionStatus.CASE_OUT.getValue())) { //已委外
            throw new RuntimeException("已还款或已结案案件不允许操作");
        }
        if (Objects.equals(casePayApply.getDerateFlag(), 1)) { //有减免标识
            if (!Objects.equals(casePayApply.getApproveStatus(), CasePayApply.ApproveStatus.DERATE_TO_AUDIT.getValue())) { //减免待审核
                throw new RuntimeException("非待审核状态的还款申请不能撤回");
            }
        } else { //没有减免标识
            if (!Objects.equals(casePayApply.getApproveStatus(), CasePayApply.ApproveStatus.PAY_TO_AUDIT.getValue())) { //还款待审核
                throw new RuntimeException("非待审核状态的还款申请不能撤回");
            }
        }
        casePayApply.setApproveStatus(CasePayApply.ApproveStatus.REVOKE.getValue()); //还款审批状态 54-撤回
        casePayApply.setOperatorUserName(tokenUser.getUserName()); //操作人用户名
        casePayApply.setOperatorRealName(tokenUser.getRealName()); //操作人名称
        casePayApply.setOperatorDate(ZWDateUtil.getNowDateTime()); //操作时间
        casePayApplyRepository.saveAndFlush(casePayApply);

        caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.COLLECTIONING.getValue()); //案件催收状态 21-催收中
        caseInfoRepository.saveAndFlush(caseInfo);
    }

    /**
     * @Description 添加跟进记录
     */
    public CaseFollowupRecord saveFollowupRecord(CaseFollowupParams caseFollowupParams, User tokenUser) {
        CaseInfo caseInfo = caseInfoRepository.findOne(caseFollowupParams.getCaseId()); //获取案件信息
        if (Objects.isNull(caseInfo)) {
            throw new RuntimeException("该案件未找到");
        }
        CaseFollowupRecord caseFollowupRecord = new CaseFollowupRecord();
        BeanUtils.copyProperties(caseFollowupParams, caseFollowupRecord);
        caseFollowupRecord.setCaseId(caseFollowupParams.getCaseId());
        caseFollowupRecord.setPersonalId(caseFollowupParams.getPersonalId());
        caseFollowupRecord.setOperator(tokenUser.getUserName()); //操作人
        caseFollowupRecord.setOperatorName(tokenUser.getRealName()); //操作人姓名
        caseFollowupRecord.setOperatorDeptName(tokenUser.getDepartment().getName()); // 操作人部门
        caseFollowupRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
        caseFollowupRecordRepository.saveAndFlush(caseFollowupRecord);

        //同步更新案件
        if (Objects.equals(CaseInfo.CollectionStatus.WAITCOLLECTION.getValue(), caseInfo.getCollectionStatus())
                || Objects.equals(CaseInfo.CollectionStatus.PART_REPAID.getValue(), caseInfo.getCollectionStatus())
                || Objects.equals(CaseInfo.CollectionStatus.REPAID.getValue(), caseInfo.getCollectionStatus())) {
            caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.COLLECTIONING.getValue());//首次跟进将催收状态变为催收中
        }
        caseInfo.setFollowupTime(caseFollowupRecord.getOperatorTime()); //最新跟进时间
        caseInfo.setFollowupBack(caseFollowupRecord.getCollectionFeedback()); //催收反馈
        caseInfo.setPromiseAmt(caseFollowupRecord.getPromiseAmt()); //承诺还款金额
        caseInfo.setPromiseTime(caseFollowupRecord.getPromiseDate()); //承诺还款日期
        caseInfo.setOperator(tokenUser); //操作人
        caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
        caseInfoRepository.saveAndFlush(caseInfo);
        return caseFollowupRecord;
    }

    /**
     * @Description 结案
     */
    public void endCase(EndCaseParams endCaseParams, User tokenUser) {
        CaseInfo caseInfo = caseInfoRepository.findOne(endCaseParams.getCaseId());
        if (Objects.isNull(caseInfo)) {
            throw new RuntimeException("该案件未找到");
        }
        if (Objects.equals(caseInfo.getCollectionStatus(), CaseInfo.CollectionStatus.CASE_OVER.getValue())) {
            throw new RuntimeException("该案件已结案");
        }
        QCaseAssist qCaseAssist = QCaseAssist.caseAssist;
        if (Objects.equals(endCaseParams.getIsAssist(), false)) { //不是协催案件
            if (Objects.equals(caseInfo.getAssistFlag(), 1)) { //有协催标识
                if (Objects.equals(caseInfo.getAssistStatus(), CaseInfo.AssistStatus.ASSIST_APPROVEING.getValue())) { //有协催申请
                    CaseAssistApply caseAssistApply = getCaseAssistApply(endCaseParams.getCaseId(), tokenUser, endCaseParams.getEndRemark());
                    caseAssistApplyRepository.saveAndFlush(caseAssistApply);
                } else { //有协催案件
                    CaseAssist caseAssist = caseAssistRepository.findOne(qCaseAssist.caseId.id.eq(endCaseParams.getCaseId()).
                            and(qCaseAssist.assistStatus.ne(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())));
                    if (Objects.isNull(caseAssist)) {
                        throw new RuntimeException("协催案件未找到");
                    }
                    caseAssist.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //协催状态 29-协催完成
                    caseAssist.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                    caseAssist.setOperator(tokenUser); //操作员
                    caseAssistRepository.saveAndFlush(caseAssist);
                }
            }
            caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.CASE_OVER.getValue()); //催收状态 24-已结案
            caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //协催状态 29-协催完成
            caseInfo.setOperator(tokenUser); //操作人
            caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
            caseInfo.setEndRemark(endCaseParams.getEndRemark()); //结案说明
            caseInfo.setEndType(endCaseParams.getEndType()); //结案方式
            caseInfoRepository.saveAndFlush(caseInfo);
        } else { //是协催案件
            CaseAssist caseAssist = caseAssistRepository.findOne(qCaseAssist.caseId.id.eq(endCaseParams.getCaseId()).
                    and(qCaseAssist.assistStatus.ne(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())));
            if (Objects.isNull(caseAssist)) {
                throw new RuntimeException("协催案件未找到");
            }
            caseAssist.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //协催状态 29-协催完成
            caseAssist.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
            caseAssist.setOperator(tokenUser); //操作员
            caseAssistRepository.saveAndFlush(caseAssist);
            if (Objects.equals(caseInfo.getAssistWay(), CaseAssist.AssistWay.ONCE_ASSIST.getValue())) { //单次协催
                //同步更新原案件状态
                caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //协催状态 29-协催完成
                caseInfo.setAssistCollector(null); //协催员置空
                caseInfo.setAssistWay(null); //协催方式置空
                caseInfo.setAssistFlag(0); //协催标识 0-否
                caseInfo.setOperator(tokenUser); //操作人
                caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                caseInfoRepository.saveAndFlush(caseInfo);

                //同时新增一条流转记录
                CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
                BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
                caseTurnRecord.setId(null); //主键置空
                caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
                caseTurnRecord.setDepartId(caseInfo.getCurrentCollector().getDepartment().getId()); //部门ID
                caseTurnRecord.setReceiveUserRealName(caseInfo.getCurrentCollector().getRealName()); //接受人名称
                caseTurnRecord.setReceiveDeptName(caseInfo.getCurrentCollector().getDepartment().getName()); //接收部门名称
                caseTurnRecord.setReceiveUserId(caseInfo.getCurrentCollector().getId()); //接受人ID
                caseTurnRecord.setCurrentCollector(caseInfo.getLatelyCollector().getId()); //当前催收员ID
                caseTurnRecord.setCirculationType(2); //流转类型 2-正常流转
                caseTurnRecord.setOperatorUserName(tokenUser.getUserName()); //操作员用户名
                caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                caseTurnRecordRepository.saveAndFlush(caseTurnRecord);
            } else { //全程协催，原案件催收状态为已结案
                //同步更新原案件状态
                caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.CASE_OVER.getValue()); //催收状态 24-已结案
                caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //协催状态 29-协催完成
                caseInfo.setOperator(tokenUser); //操作人
                caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                caseInfo.setEndRemark(endCaseParams.getEndRemark()); //结案说明
                caseInfo.setEndType(endCaseParams.getEndType()); //结案方式
                caseInfoRepository.saveAndFlush(caseInfo);
            }
        }
    }

    /**
     * @Description 协催申请
     */
    public void saveAssistApply(AssistApplyParams assistApplyParams, User tokenUser) {
        CaseInfo caseInfo = caseInfoRepository.findOne(assistApplyParams.getCaseId());
        if (Objects.isNull(caseInfo)) {
            throw new RuntimeException("该案件未找到");
        }
        if (!Objects.equals(caseInfo.getCollectionType(), CaseInfo.CollectionType.TEL.getValue())) {
            throw new RuntimeException("非电催案件不允许申请协催");
        }
        QCaseAssist qCaseAssist = QCaseAssist.caseAssist;
        if (Objects.equals(caseInfo.getAssistFlag(), 1)) { //有协催标识
            CaseAssist caseAssist = caseAssistRepository.findOne(qCaseAssist.caseId.id.eq(assistApplyParams.getCaseId()).
                    and(qCaseAssist.assistStatus.ne(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())));
            if (Objects.isNull(caseAssist)) { //有协催申请
                throw new RuntimeException("该案件已经提交了协催申请，不允许重复提交");
            } else { //有协催案件
                throw new RuntimeException("该案件正在协催，不允许重复申请");
            }
        }
        //获得失效日数
        QSysParam qSysParam = QSysParam.sysParam;
        SysParam sysParam = sysParamRepository.findOne(qSysParam.code.eq(Constants.ASSIST_APPLY_CODE).and(qSysParam.type.eq(Constants.TYPE_TEL)).and(qSysParam.companyCode.eq(tokenUser.getCompanyCode())));
        if (Objects.isNull(sysParam)) {
            throw new RuntimeException("协催失效配置天数未找到");
        }
        Integer days = Integer.parseInt(sysParam.getValue());
        Long nowtime = ZWDateUtil.getNowDateTime().getTime();
        Long invalidTime = nowtime + days * 86400000;
        Date applyInvalidTime = new Date(invalidTime); //失效日期

        //新增协催申请记录
        CaseAssistApply caseAssistApply = new CaseAssistApply();
        caseAssistApply.setCaseId(assistApplyParams.getCaseId()); //案件ID
        caseAssistApply.setCaseNumber(caseInfo.getCaseNumber()); //案件编号
        caseAssistApply.setPersonalName(caseInfo.getPersonalInfo().getName()); //客户姓名
        caseAssistApply.setPersonalPhone(caseInfo.getPersonalInfo().getMobileNo()); // 客户电话
        caseAssistApply.setPersonalId(caseInfo.getPersonalInfo().getId()); //客户信息ID
        caseAssistApply.setCollectionType(caseInfo.getCollectionType()); //催收类型
        caseAssistApply.setDepartId(caseInfo.getDepartment().getId()); //部门ID
        caseAssistApply.setPrincipalId(caseInfo.getPrincipalId().getId()); //委托方ID
        caseAssistApply.setPrincipalName(caseInfo.getPrincipalId().getName()); //委托方名称
        caseAssistApply.setOverdueAmount(caseInfo.getOverdueAmount()); //逾期总金额
        caseAssistApply.setOverdueDays(caseInfo.getOverdueDays()); //逾期总天数
        caseAssistApply.setOverduePeriods(caseInfo.getOverduePeriods()); //逾期期数
        caseAssistApply.setHoldDays(caseInfo.getHoldDays()); //持案天数
        caseAssistApply.setLeftDays(caseInfo.getLeftDays()); //剩余天数
        caseAssistApply.setAreaId(caseInfo.getArea().getId()); //省份编号
        caseAssistApply.setAreaName(caseInfo.getArea().getAreaName()); //城市名称
        caseAssistApply.setApplyUserName(tokenUser.getUserName()); //申请人
        caseAssistApply.setApplyRealName(tokenUser.getRealName()); //申请人姓名
        caseAssistApply.setApplyDeptName(tokenUser.getDepartment().getName()); //申请人部门名称
        caseAssistApply.setApplyReason(assistApplyParams.getApplyReason()); //申请原因
        caseAssistApply.setApplyDate(ZWDateUtil.getNowDateTime()); //申请时间
        caseAssistApply.setApplyInvalidTime(applyInvalidTime); //申请失效日期
        caseAssistApply.setAssistWay(assistApplyParams.getAssistWay()); //协催方式
        caseAssistApply.setApproveStatus(CaseAssistApply.ApproveStatus.TEL_APPROVAL.getValue()); //审批状态 32-电催待审批
        caseAssistApply.setProductSeries(caseInfo.getProduct().getProductSeries().getId()); //产品系列ID
        caseAssistApply.setProductId(caseInfo.getProduct().getId()); //产品ID
        caseAssistApply.setProductSeriesName(caseInfo.getProduct().getProductSeries().getSeriesName()); //产品系列名称
        caseAssistApply.setProductName(caseInfo.getProduct().getProdcutName()); //产品名称
        caseAssistApply.setCompanyCode(caseInfo.getCompanyCode()); //公司code码
        caseAssistApplyRepository.saveAndFlush(caseAssistApply);

        //更新原案件
        caseInfo.setAssistFlag(1); //协催标识
        caseInfo.setAssistWay(assistApplyParams.getAssistWay()); //协催方式
        caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_APPROVEING.getValue()); //协催状态
        caseInfo.setOperator(tokenUser); //操作人
        caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
        caseInfoRepository.saveAndFlush(caseInfo);
    }

    /**
     * @Description 判断用户下有没有正在催收的案件
     */
    public CollectionCaseModel haveCollectionCase(User user) {
        QCaseInfo qCaseInfo = caseInfo;
        Iterable<CaseInfo> caseInfos = caseInfoRepository.findAll(qCaseInfo.currentCollector.eq(user).
                and(qCaseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue())).
                and(qCaseInfo.companyCode.eq(user.getCompanyCode()))); //获取催收员为该用户并且催收状态不为已结案的所有案件
        Iterator<CaseInfo> it = caseInfos.iterator();
        return todoIt(it);
    }

    /**
     * @Description 判断机构下有没有正在催收的案件
     */
    public CollectionCaseModel haveCollectionCase(Department department) {
        QCaseInfo qCaseInfo = caseInfo;
        Iterable<CaseInfo> caseInfos = caseInfoRepository.findAll(qCaseInfo.department.code.startsWith(department.getCode()).
                and(qCaseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue())).
                and(qCaseInfo.companyCode.eq(department.getCompanyCode()))); //获取部门下状态不为结案的所有案件
        Iterator<CaseInfo> it = caseInfos.iterator();
        return todoIt(it);
    }

    /**
     * @Description 构建collectionCaseModel
     */
    private CollectionCaseModel todoIt(Iterator<CaseInfo> it) {
        Integer num = 0;
        List<String> caseIds = new ArrayList<>();
        CollectionCaseModel collectionCaseModel = new CollectionCaseModel();
        if (it.hasNext()) { //查到的集合不为空
            while (it.hasNext()) {
                CaseInfo caseInfo = it.next();
                caseIds.add(caseInfo.getId());
                num++;
            }
            collectionCaseModel.setNum(num);
            collectionCaseModel.setCaseIds(caseIds);
        } else {
            collectionCaseModel.setNum(num);
            collectionCaseModel.setCaseIds(caseIds);
        }
        return collectionCaseModel;
    }

    /**
     * @Description 获取案件分配信息
     */
    public BatchDistributeModel getBatchDistribution(User tokenUser) {
        Iterable<User> users = userService.getAllUser(tokenUser.getDepartment().getId(), 0);
        Iterator<User> it = users.iterator();
        Integer avgCaseNum = 0; //人均案件数
        Integer userNum = 0; //登录用户部门下的所有启用用户总数
        Integer caseNum = 0; //登录用户部门下的所有启用用户持有未结案案件总数
        List<BatchInfoModel> batchInfoModels = new ArrayList<>();
        while (it.hasNext()) {
            BatchInfoModel batchInfoModel = new BatchInfoModel();
            User user = it.next();
            Integer caseCount = caseInfoRepository.getCaseCount(user.getId());
            batchInfoModel.setCaseCount(caseCount); //持有案件数
            batchInfoModel.setCollectionUser(user); //催收人
            batchInfoModels.add(batchInfoModel);
            userNum++;
            caseNum = caseNum + caseCount;
        }
        if (userNum != 0) {
            avgCaseNum = (caseNum % userNum == 0) ? caseNum / userNum : (caseNum / userNum + 1);
        }
        BatchDistributeModel batchDistributeModel = new BatchDistributeModel();
        batchDistributeModel.setAverageNum(avgCaseNum);
        batchDistributeModel.setBatchInfoModelList(batchInfoModels);
        return batchDistributeModel;
    }

    /**
     * @Description 批量分配
     */
    public void batchCase(BatchDistributeModel batchDistributeModel, User tokenUser) {
        List<BatchInfoModel> batchInfoModels = batchDistributeModel.getBatchInfoModelList();
        List<String> caseIds = batchDistributeModel.getCaseIds();
        List<CaseAssist> caseAssists = new ArrayList<>();
        List<CaseAssistApply> caseAssistApplies = new ArrayList<>();
        List<CaseInfo> caseInfos = new ArrayList<>();
        List<CaseTurnRecord> caseTurnRecords = new ArrayList<>();
        QCaseAssist qCaseAssist = QCaseAssist.caseAssist;
        for (BatchInfoModel batchInfoModel : batchInfoModels) {
            Integer caseCount = batchInfoModel.getDistributionCount(); //分配案件数
            if (0 == caseCount) {
                continue;
            }
            if (!Objects.equals(batchInfoModel.getCollectionUser().getType(), User.Type.VISIT.getValue())) { //分配给外访以外
                for (int i = 0; i < caseCount; i++) {
                    CaseInfo caseInfo = caseInfoRepository.findOne(caseIds.get(i)); //获得案件信息
                    if (Objects.equals(caseInfo.getAssistFlag(), 1)) { //有协催标识
                        if (Objects.equals(caseInfo.getCollectionType(), CaseInfo.CollectionType.VISIT.getValue())) { //是协催案件
                            throw new RuntimeException("协催案件不能分配给外访以外的人员");
                        }
                        setAttribute(caseInfo, batchInfoModel.getCollectionUser(), tokenUser);
                    } else { //没有协催标识
                        setAttribute(caseInfo, batchInfoModel.getCollectionUser(), tokenUser);
                    }
                    caseInfos.add(caseInfo);
                    //分配完成新增流转记录
                    CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
                    BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
                    caseTurnRecord.setId(null); //主键置空
                    caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
                    caseTurnRecord.setDepartId(batchInfoModel.getCollectionUser().getDepartment().getId()); //部门ID
                    caseTurnRecord.setReceiveUserRealName(batchInfoModel.getCollectionUser().getRealName()); //接受人名称
                    caseTurnRecord.setReceiveDeptName(batchInfoModel.getCollectionUser().getDepartment().getName()); //接收部门名称
                    caseTurnRecord.setReceiveUserId(caseInfo.getCurrentCollector().getId()); //接受人ID
                    caseTurnRecord.setCurrentCollector(caseInfo.getLatelyCollector().getId()); //当前催收员ID
                    caseTurnRecord.setCirculationType(2); //流转类型 2-正常流转
                    caseTurnRecord.setOperatorUserName(tokenUser.getUserName()); //操作员用户名
                    caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                    caseTurnRecords.add(caseTurnRecord);
                }
            } else { //分配给外访
                for (int i = 0; i < caseCount; i++) {
                    CaseInfo caseInfo = caseInfoRepository.findOne(caseIds.get(i)); //获得案件信息
                    if (Objects.equals(caseInfo.getAssistFlag(), 1)) { //有协催标识
                        if (Objects.equals(caseInfo.getAssistStatus(), CaseInfo.AssistStatus.ASSIST_APPROVEING.getValue())) { //有协催申请
                            CaseAssistApply caseAssistApply = getCaseAssistApply(caseIds.get(i), tokenUser, "案件流转强制拒绝");
                            caseAssistApplies.add(caseAssistApply);
                        } else { //有协催案件
                            CaseAssist caseAssist = caseAssistRepository.findOne(qCaseAssist.caseId.id.eq(caseIds.get(i)).
                                    and(qCaseAssist.assistStatus.ne(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())));
                            if (Objects.isNull(caseAssist)) {
                                throw new RuntimeException("协催案件未找到");
                            }
                            caseAssist.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //协催状态 29-协催完成
                            caseAssist.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                            caseAssist.setOperator(tokenUser); //操作员
                            caseAssists.add(caseAssist);

                            //协催结束新增一条流转记录
                            CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
                            BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
                            caseTurnRecord.setId(null); //主键置空
                            caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
                            caseTurnRecord.setDepartId(caseInfo.getCurrentCollector().getDepartment().getId()); //部门ID
                            caseTurnRecord.setReceiveUserRealName(caseInfo.getCurrentCollector().getRealName()); //接受人名称
                            caseTurnRecord.setReceiveDeptName(batchInfoModel.getCollectionUser().getDepartment().getName()); //接收部门名称
                            caseTurnRecord.setReceiveUserId(caseInfo.getCurrentCollector().getId()); //接受人ID
                            caseTurnRecord.setCurrentCollector(caseInfo.getLatelyCollector().getId()); //当前催收员ID
                            caseTurnRecord.setCirculationType(2); //流转类型 2-正常流转
                            caseTurnRecord.setOperatorUserName(tokenUser.getUserName()); //操作员用户名
                            caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                            caseTurnRecords.add(caseTurnRecord);
                        }
                        //同步更新原案件协催员，协催方式，协催标识，协催状态
                        caseInfo.setAssistCollector(null); //协催员置空
                        caseInfo.setAssistWay(null); //协催方式置空
                        caseInfo.setAssistFlag(0); //协催标识 0-否
                        caseInfo.setAssistStatus(null); //协催状态置空
                    } else { //没有协催标识
                        setAttribute(caseInfo, batchInfoModel.getCollectionUser(), tokenUser);
                    }
                    caseInfos.add(caseInfo);
                    //分配完成新增流转记录
                    CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
                    BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
                    caseTurnRecord.setId(null); //主键置空
                    caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
                    caseTurnRecord.setDepartId(batchInfoModel.getCollectionUser().getDepartment().getId()); //部门ID
                    caseTurnRecord.setReceiveUserRealName(batchInfoModel.getCollectionUser().getRealName()); //接受人名称
                    caseTurnRecord.setReceiveDeptName(batchInfoModel.getCollectionUser().getDepartment().getName()); //接收部门名称
                    caseTurnRecord.setReceiveUserId(batchInfoModel.getCollectionUser().getId()); //接受人ID
                    caseTurnRecord.setCurrentCollector(caseInfo.getCurrentCollector().getId()); //当前催收员ID
                    caseTurnRecord.setCirculationType(2); //流转类型 2-正常流转
                    caseTurnRecord.setOperatorUserName(tokenUser.getUserName()); //操作员用户名
                    caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                    caseTurnRecords.add(caseTurnRecord);
                    caseIds.remove(0);
                }
            }
            caseInfoRepository.save(caseInfos);
            caseAssistRepository.save(caseAssists);
            caseAssistApplyRepository.save(caseAssistApplies);
            caseTurnRecordRepository.save(caseTurnRecords);
        }

    }


    /**
     * @Description 案件颜色打标
     */

    public void caseMarkColor(CaseMarkParams caseMarkParams, User tokenUser) {
        List<String> caseIds = caseMarkParams.getCaseIds();
        for (String caseId : caseIds) {
            CaseInfo caseInfo = caseInfoRepository.findOne(caseId);
            if (Objects.isNull(caseInfo)) {
                throw new RuntimeException("案件未找到");
            }
            caseInfo.setCaseMark(caseMarkParams.getColorNum()); //打标
            caseInfo.setOperator(tokenUser); //操作人
            caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
            caseInfoRepository.saveAndFlush(caseInfo);
        }
    }

    /**
     * @Description 修改联系人电话状态
     */
    public PersonalContact modifyPhoneStatus(PhoneStatusParams phoneStatusParams, User tokenUser) {
        PersonalContact personalContact = personalContactRepository.findOne(phoneStatusParams.getPersonalContactId());
        if (Objects.isNull(personalContact)) {
            throw new RuntimeException("该联系人信息未找到");
        }
        personalContact.setPhoneStatus(phoneStatusParams.getPhoneStatus()); //电话状态
        personalContact.setOperator(tokenUser.getUserName()); //操作人
        personalContact.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
        personalContactRepository.saveAndFlush(personalContact);
        return personalContact;
    }

    /**
     * @Description 重新分配案件字段复制
     */
    private CaseInfo setAttribute(CaseInfo caseInfo, User user, User tokenUser) {
        caseInfo.setLatelyCollector(caseInfo.getCurrentCollector()); //上一个催收员
        caseInfo.setCurrentCollector(user); //当前催收员
        caseInfo.setHoldDays(0); //持案天数归0
        caseInfo.setCaseMark(CaseInfo.Color.NO_COLOR.getValue()); //案件标记为无色
        caseInfo.setFollowUpNum(caseInfo.getFollowUpNum() + 1); //流转次数加一
        caseInfo.setCaseFollowInTime(ZWDateUtil.getNowDateTime()); //流入时间
        caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.WAITCOLLECTION.getValue()); //催收状态 20-待催收
        caseInfo.setLeaveCaseFlag(0); //留案标识置0
        caseInfo.setFollowupBack(null); //催收反馈置空
        caseInfo.setFollowupTime(null); //跟进时间置空
        caseInfo.setPromiseAmt(new BigDecimal(0)); //承诺还款金额置0
        caseInfo.setPromiseTime(null); //承诺还款时间置空
        caseInfo.setCirculationStatus(null); //流转审批状态置空
        caseInfo.setDepartment(user.getDepartment());
        if (Objects.equals(user.getType(), User.Type.TEL.getValue())) {
            caseInfo.setCollectionType(CaseInfo.CollectionType.TEL.getValue());
        } else if (Objects.equals(user.getType(), User.Type.VISIT.getValue())) {
            caseInfo.setCollectionType(CaseInfo.CollectionType.VISIT.getValue());
        } else if (Objects.equals(user.getType(), User.Type.JUD.getValue())) {
            caseInfo.setCollectionType(CaseInfo.CollectionType.JUDICIAL.getValue());
        } else if (Objects.equals(user.getType(), User.Type.OUT.getValue())) {
            caseInfo.setCollectionType(CaseInfo.CollectionType.outside.getValue());
        } else if (Objects.equals(user.getType(), User.Type.OUT.getValue())) {
            caseInfo.setCollectionType(CaseInfo.CollectionType.remind.getValue());
        }
        caseInfo.setOperator(tokenUser); //操作员
        caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
        caseInfo.setDepartment(user.getDepartment()); //部门
        return caseInfo;
    }

    /**
     * @Description 获得协催申请并set值
     */
    public CaseAssistApply getCaseAssistApply(String caseId, User tokenUser, String memo) {
        List<Integer> list = new ArrayList<>(); //协催审批状态列表
        list.add(CaseAssistApply.ApproveStatus.TEL_APPROVAL.getValue()); // 32-电催待审批
        list.add(CaseAssistApply.ApproveStatus.VISIT_APPROVAL.getValue()); // 34-外访待审批
        QCaseAssistApply qCaseAssistApply = QCaseAssistApply.caseAssistApply;
        CaseAssistApply caseAssistApply = caseAssistApplyRepository.findOne(qCaseAssistApply.caseId.eq(caseId)
                .and(qCaseAssistApply.approveStatus.in(list)));
        if (Objects.isNull(caseAssistApply)) {
            throw new RuntimeException("协催申请记录未找到");
        }
        if (Objects.equals(caseAssistApply.getCollectionType(), CaseInfo.CollectionType.TEL.getValue())) { // 15-电催
            caseAssistApply.setApproveStatus(CaseAssistApply.ApproveStatus.TEL_COMPLETE.getValue()); //审批状态 33-电催完成
            caseAssistApply.setApprovePhoneResult(CaseAssistApply.ApproveResult.FORCED_REJECT.getValue()); //电催审批结果 40-强制拒绝
            caseAssistApply.setApprovePhoneUser(tokenUser.getUserName()); //电催审批人用户名
            caseAssistApply.setApprovePhoneName(tokenUser.getRealName()); //电催审批人姓名
            caseAssistApply.setApprovePhoneDatetime(ZWDateUtil.getNowDateTime()); //电催审批时间
            caseAssistApply.setApprovePhoneMemo(memo); //电催审批意见  需要写成常量
        } else if (Objects.equals(caseAssistApply.getCollectionType(), CaseInfo.CollectionType.VISIT.getValue())) { // 16-外访
            caseAssistApply.setApproveStatus(CaseAssistApply.ApproveStatus.VISIT_COMPLETE.getValue()); //审批状态 35-外访完成
            caseAssistApply.setApproveOutResult(CaseAssistApply.ApproveResult.FORCED_REJECT.getValue()); //外访审批结果 40-强制拒绝
            caseAssistApply.setApproveOutUser(tokenUser.getUserName()); //外访审批人用户名
            caseAssistApply.setApproveOutName(tokenUser.getRealName()); //外访审批人姓名
            caseAssistApply.setApproveOutDatetime(ZWDateUtil.getNowDateTime()); //外访审批时间
            caseAssistApply.setApproveOutMemo(memo); //外访审批意见  需要写成常量
        }
        return caseAssistApply;
    }

    /**
     * @Description 电催添加修复信息
     */
    public PersonalContact saveRepairInfo(RepairInfoModel repairInfoModel, User tokenUser) {
        PersonalContact personalContact = new PersonalContact();
        personalContact.setPersonalId(repairInfoModel.getPersonalId()); //客户信息ID
        personalContact.setRelation(repairInfoModel.getRelation()); //关系
        personalContact.setName(repairInfoModel.getName()); //姓名
        personalContact.setPhone(repairInfoModel.getPhone()); //电话号码
        personalContact.setPhoneStatus(repairInfoModel.getPhoneStatus()); //电话状态
        personalContact.setSocialType(repairInfoModel.getSocialType()); //社交帐号类型
        personalContact.setSocialValue(repairInfoModel.getSocialValue()); //社交帐号内容
        personalContact.setSource(Constants.DataSource.REPAIR.getValue()); //数据来源 147-修复
        personalContact.setOperator(tokenUser.getUserName()); //操作人
        personalContact.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
        personalContactRepository.saveAndFlush(personalContact);
        return personalContact;
    }

    /**
     * @Description 查看凭证
     */
    public List<UploadFile> getRepaymentVoucher(String casePayId) {
        //下载外访资料
        List<UploadFile> uploadFiles = new ArrayList<>();//文件对象集合
        QCasePayFile qCasePayFile = QCasePayFile.casePayFile;
        Iterable<CasePayFile> caseFlowupFiles = casePayFileRepository.findAll(qCasePayFile.payId.eq(casePayId));
        Iterator<CasePayFile> it = caseFlowupFiles.iterator();
        while (it.hasNext()) {
            CasePayFile casePayFile = it.next();
            ResponseEntity<UploadFile> entity = restTemplate.getForEntity("http://file-service/api/uploadFile/" + casePayFile.getFileid(), UploadFile.class);
            if (!entity.hasBody()) {
                throw new RuntimeException("下载失败");
            } else {
                UploadFile uploadFile = entity.getBody();//文件对象
                uploadFiles.add(uploadFile);
            }
        }
        return uploadFiles;
    }

    /**
     * @Description 分配前判断是否有协催案件或协催标识
     */
    public List<String> checkCaseAssist(CheckAssistParams checkAssistParams) {
        List<String> list = new ArrayList<>();
        String information;
        for (String caseId : checkAssistParams.getList()) {
            CaseInfo caseInfo = caseInfoRepository.findOne(caseId); //遍历每一个案件
            if (Objects.isNull(caseInfo)) {
                throw new RuntimeException("所选案件的案件信息未找到");
            }
            if (Objects.equals(caseInfo.getAssistFlag(), 1)) { //有协催标识
                information = "案件编号为" + caseInfo.getCaseNumber() + "的案件已申请协催或存在协催案件";
                list.add(information);
            }
        }
        return list;
    }

    /**
     * @Descripion 留案操作
     */
    public LeaveCaseModel leaveCase(LeaveCaseParams leaveCaseParams, User tokenUser) {
        //获得所持有未结案的案件总数
        Integer caseNum = caseInfoRepository.getCaseCount(tokenUser.getId());

        //查询已留案案件数
        QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
        int flagNum = (int) caseInfoRepository.count(qCaseInfo.currentCollector.id.eq(tokenUser.getId()).and(qCaseInfo.leaveCaseFlag.eq(1)));

        //获得留案比例
        QSysParam qSysParam = QSysParam.sysParam;
        SysParam sysParam;
        String companyCode;
        if (Objects.isNull(tokenUser.getCompanyCode())) {
            if (Objects.isNull(leaveCaseParams.getCompanyCode())) {
                throw new RuntimeException("请选择公司");
            }
            companyCode = leaveCaseParams.getCompanyCode();
        } else {
            companyCode = tokenUser.getCompanyCode();
        }
        if (Objects.equals(leaveCaseParams.getType(), 0)) { //电催

            sysParam = sysParamRepository.findOne(qSysParam.code.eq(Constants.SYS_PHNOEFLOW_LEAVERATE).and(qSysParam.companyCode.eq(companyCode)));
        } else { //外访
            sysParam = sysParamRepository.findOne(qSysParam.code.eq(Constants.SYS_OUTBOUNDFLOW_LEAVERATE).and(qSysParam.companyCode.eq(companyCode)));
        }
        Double rate = Double.parseDouble(sysParam.getValue()) / 100;

        //计算留案案件是否超过比例
        Integer leaveNum = (int) (caseNum * rate); //可留案的案件数
        List<String> caseIds = leaveCaseParams.getCaseIds();
        for (String caseId : caseIds) {
            CaseInfo caseInfo = caseInfoRepository.findOne(caseId);
            if (Objects.isNull(caseInfo)) {
                throw new RuntimeException("所选案件未找到");
            }
            if (!Objects.equals(caseInfo.getCurrentCollector().getId(), tokenUser.getId())) {
                throw new RuntimeException("只能对自己所持有的案件进行留案操作");
            }
            if (Objects.equals(caseInfo.getLeaveCaseFlag(), 1)) {
                throw new RuntimeException("所选案件存在已经留案的案件");
            }
            if (flagNum >= leaveNum) {
                throw new RuntimeException("所选案件数量超过可留案案件数");
            }
            caseInfo.setLeaveCaseFlag(1); //留案标志
            if (Objects.equals(tokenUser.getType(), User.Type.TEL.getValue())) {
                caseInfo.setCaseType(CaseInfo.CaseType.PHNONELEAVETURN.getValue()); //案件类型 177-电催保留流转
            } else if (Objects.equals(tokenUser.getType(), User.Type.VISIT.getValue())) {
                caseInfo.setCaseType(CaseInfo.CaseType.OUTLEAVETURN.getValue()); //案件类型 181-外访保留流转
            }
            caseInfo.setOperator(tokenUser); //操作人
            caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
            caseInfoRepository.saveAndFlush(caseInfo);
            flagNum++;
        }
        LeaveCaseModel leaveCaseModel = new LeaveCaseModel();
        leaveCaseModel.setCaseNum(leaveNum - flagNum);
        return leaveCaseModel;
    }

    /**
     * @Description 申请提前流转
     */
    public void advanceCirculation(AdvanceCirculationParams advanceCirculationParams, User tokenUser) {
        List<String> caseIds = advanceCirculationParams.getCaseIds();
        for (String caseId : caseIds) {
            CaseInfo caseInfo = caseInfoRepository.findOne(caseId);
            if (Objects.isNull(caseInfo)) {
                throw new RuntimeException("所选案件未找到");
            }
            if (Objects.equals(advanceCirculationParams.getType(), 0)) {
                caseInfo.setCaseType(CaseInfo.CaseType.PHNONEFAHEADTURN.getValue()); //案件类型 176-电催提前流转
                caseInfo.setCirculationStatus(CaseInfo.CirculationStatus.PHONE_WAITING.getValue()); //小流转审批状态 197-电催流转待审批
            } else {
                caseInfo.setCaseType(CaseInfo.CaseType.OUTFAHEADTURN.getValue()); //案件类型 17-外访提前流转
                caseInfo.setCirculationStatus(CaseInfo.CirculationStatus.VISIT_WAITING.getValue()); //小流转审批状态 200-外访流转待审批
            }
            caseInfo.setOperator(tokenUser); //操作人
            caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
            caseInfoRepository.saveAndFlush(caseInfo);
        }
        //消息提醒
        List<User> userList = userService.getAllHigherLevelManagerByUser(tokenUser.getId());
        List<String> managerIdList = new ArrayList<>();
        for (User user : userList) {
            managerIdList.add(user.getId());
        }
        SendReminderMessage sendReminderMessage = new SendReminderMessage();
        sendReminderMessage.setTitle("案件提前流转申请");
        sendReminderMessage.setContent("您有 [" + caseIds.size() + "] 条提前流转案件申请需要审批");
        sendReminderMessage.setType(ReminderType.CIRCULATION);
        sendReminderMessage.setCcUserIds(managerIdList.toArray(new String[managerIdList.size()]));
        reminderService.sendReminder(sendReminderMessage);
    }

    /**
     * @Description 审批小流转案件
     */
    public void approvalCirculation(CirculationApprovalParams circulationApprovalParams, User tokenUser) {
        CaseInfo caseInfo = caseInfoRepository.findOne(circulationApprovalParams.getCaseId()); //获取案件信息
        if (Objects.isNull(caseInfo)) {
            throw new RuntimeException("该案件未找到");
        }
        String userIdForReminde = caseInfo.getCurrentCollector().getId();
        if (Objects.equals(circulationApprovalParams.getResult(), 0)) { //审批通过
            if (Objects.equals(circulationApprovalParams.getType(), 0)) { //电催小流转
                caseInfo.setCirculationStatus(CaseInfo.CirculationStatus.PHONE_PASS.getValue()); //198-电催流转通过
                if (Objects.equals(caseInfo.getAssistFlag(), 1)) { //有协催标志
                    if (Objects.equals(caseInfo.getAssistStatus(), CaseInfo.AssistStatus.ASSIST_APPROVEING.getValue())) { //有协催申请
                        CaseAssistApply caseAssistApply = getCaseAssistApply(caseInfo.getId(), tokenUser, "流转强制拒绝");
                        caseAssistApplyRepository.saveAndFlush(caseAssistApply);
                    } else { //有协催案件
                        QCaseAssist qCaseAssist = QCaseAssist.caseAssist;
                        CaseAssist caseAssist = caseAssistRepository.findOne(qCaseAssist.caseId.id.eq(caseInfo.getId()).
                                and(qCaseAssist.assistStatus.ne(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())));
                        if (Objects.isNull(caseAssist)) {
                            throw new RuntimeException("协催案件未找到");
                        }
                        caseAssist.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //协催状态 29-协催完成
                        caseAssist.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                        caseAssist.setOperator(tokenUser); //操作员
                        caseAssistRepository.saveAndFlush(caseAssist);

                        //协催结束新增一条流转记录
                        CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
                        BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
                        caseTurnRecord.setId(null); //主键置空
                        caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
                        caseTurnRecord.setDepartId(caseInfo.getCurrentCollector().getDepartment().getId()); //部门ID
                        caseTurnRecord.setReceiveUserRealName(caseInfo.getCurrentCollector().getRealName()); //接受人名称
                        caseTurnRecord.setReceiveDeptName(caseInfo.getCurrentCollector().getDepartment().getName()); //接收部门名称
                        caseTurnRecord.setReceiveUserId(caseInfo.getCurrentCollector().getId()); //接受人ID
                        caseTurnRecord.setCurrentCollector(caseInfo.getLatelyCollector().getId()); //当前催收员ID
                        caseTurnRecord.setCirculationType(2); //流转类型 2-正常流转
                        caseTurnRecord.setOperatorUserName(tokenUser.getUserName()); //操作员
                        caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                        caseTurnRecordRepository.saveAndFlush(caseTurnRecord);
                    }
                    //更新原案件状态
                    caseInfo.setAssistCollector(null); //协催员置空
                    caseInfo.setAssistWay(null); //协催方式置空
                    caseInfo.setAssistFlag(0); //协催标识 0-否
                    caseInfo.setAssistStatus(null); //协催状态置空
                }
            } else { //外访小流转
                caseInfo.setCirculationStatus(CaseInfo.CirculationStatus.VISIT_PASS.getValue()); //201-外访流转通过
            }
            //更新原案件
            caseInfo.setAssistCollector(null); //协催员置空
            caseInfo.setAssistWay(null); //协催方式置空
            caseInfo.setAssistFlag(0); //协催标识 0-否
            caseInfo.setFollowupBack(null); //催收反馈置空
            caseInfo.setFollowupTime(null); //跟进时间置空
            caseInfo.setPromiseAmt(null); //承诺还款金额置空
            caseInfo.setPromiseTime(null); //承诺还款时间置空
            caseInfo.setLatelyCollector(caseInfo.getCurrentCollector()); //上一个催收员变为当前催收员
            caseInfo.setCurrentCollector(null); //当前催收员变为审批人
            caseInfo.setHoldDays(0); //持案天数归0
            caseInfo.setCaseMark(CaseInfo.Color.NO_COLOR.getValue()); //案件标记为无色
            caseInfo.setFollowUpNum(caseInfo.getFollowUpNum() + 1); //流转次数加一
            caseInfo.setCaseFollowInTime(ZWDateUtil.getNowDateTime()); //流入时间
            caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.WAITCOLLECTION.getValue()); //催收状态 20-待催收
            caseInfo.setLeaveCaseFlag(0); //留案标识置0

            //通过后添加一条流转记录
            CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
            BeanUtils.copyProperties(caseInfo, caseTurnRecord);
            caseTurnRecord.setId(null);
            caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
            caseTurnRecord.setDepartId(tokenUser.getDepartment().getId()); //部门ID
            caseTurnRecord.setReceiveUserRealName(tokenUser.getRealName()); //接受人名称
            caseTurnRecord.setReceiveDeptName(tokenUser.getDepartment().getName()); //接收部门名称
            caseTurnRecord.setReceiveUserId(tokenUser.getId()); //接受人ID
            caseTurnRecord.setCurrentCollector(caseInfo.getLatelyCollector().getId()); //当前催收员ID
            caseTurnRecord.setCirculationType(1); //流转类型 1-手动流转
            caseTurnRecord.setOperatorUserName(tokenUser.getUserName()); //操作员用户名
            caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
            caseTurnRecordRepository.saveAndFlush(caseTurnRecord);
        } else { //审批拒绝
            if (Objects.equals(circulationApprovalParams.getType(), 0)) { //电催小流转
                caseInfo.setCirculationStatus(CaseInfo.CirculationStatus.PHONE_REFUSE.getValue()); //199-电催流转拒绝
            } else { //外访小流转
                caseInfo.setCirculationStatus(CaseInfo.CirculationStatus.VISIT_REFUSE.getValue()); //202-外访流转拒绝
            }
            caseInfo.setCaseType(CaseInfo.CaseType.DISTRIBUTE.getValue()); //案件类型恢复为193-案件分配
        }
        caseInfo.setOperator(tokenUser); //操作人
        caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
        caseInfoRepository.saveAndFlush(caseInfo);

        //消息提醒
        SendReminderMessage sendReminderMessage = new SendReminderMessage();
        sendReminderMessage.setUserId(userIdForReminde);
        sendReminderMessage.setTitle("案件提前流转申请结果");
        sendReminderMessage.setContent("您申请的提前流转案件 [" + caseInfo.getCaseNumber() + "] 申请" + (Objects.equals(circulationApprovalParams.getResult(), 0) ? "已通过" : "被拒绝"));
        sendReminderMessage.setType(ReminderType.CIRCULATION);
        reminderService.sendReminder(sendReminderMessage);
    }

    /**
     * @Description 查询客户联系人
     */
    public List<PersonalContact> getPersonalContact(String personalId) {
        OrderSpecifier<Date> sortOrder = QPersonalContact.personalContact.operatorTime.desc();
        QPersonalContact qPersonalContact = QPersonalContact.personalContact;
        Iterable<PersonalContact> personalContacts1 = personalContactRepository.findAll(qPersonalContact.source.eq(Constants.DataSource.IMPORT.getValue()).
                and(qPersonalContact.personalId.eq(personalId))); //查询导入的联系人信息
        Iterable<PersonalContact> personalContacts2 = personalContactRepository.findAll(qPersonalContact.source.eq(Constants.DataSource.REPAIR.getValue()).
                and(qPersonalContact.personalId.eq(personalId)), sortOrder); //查询修复的联系人信息
        if (!personalContacts1.iterator().hasNext() && !personalContacts2.iterator().hasNext()) {
            return new ArrayList<>();
        }
        List<PersonalContact> personalContactList = IteratorUtils.toList(personalContacts1.iterator());
        List<PersonalContact> personalContactList1 = IteratorUtils.toList(personalContacts2.iterator());
        personalContactList1.addAll(personalContactList);
        return personalContactList1;
    }

    /**
     * @Description 外方添加修复信息
     */
    public PersonalAddress saveVisitRepairInfo(RepairInfoModel repairInfoModel, User tokenUser) {
        PersonalAddress personalAddress = new PersonalAddress();
        personalAddress.setPersonalId(repairInfoModel.getPersonalId()); //客户信息ID
        personalAddress.setRelation(repairInfoModel.getRelation()); //关系
        personalAddress.setName(repairInfoModel.getName()); //姓名
        personalAddress.setDetail(repairInfoModel.getAddress()); //地址
        personalAddress.setStatus(repairInfoModel.getAddressStatus()); //地址状态
        personalAddress.setSource(Constants.DataSource.REPAIR.getValue()); //数据来源 147-修复
        personalAddress.setType(repairInfoModel.getType()); //地址类型
        personalAddress.setOperator(tokenUser.getUserName()); //操作人
        personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
        personalAddressRepository.saveAndFlush(personalAddress);
        return personalAddress;
    }

    /**
     * @Description 查询客户联系人地址
     */
    public List<PersonalAddress> getPersonalAddress(String personalId) {
        OrderSpecifier<Date> sortOrder = QPersonalAddress.personalAddress.operatorTime.desc();
        QPersonalAddress qPersonalAddress = QPersonalAddress.personalAddress;
        Iterable<PersonalAddress> personalAddresses1 = personalAddressRepository.findAll(qPersonalAddress.source.eq(Constants.DataSource.IMPORT.getValue()).
                and(qPersonalAddress.personalId.eq(personalId))); //查询导入的联系人信息
        Iterable<PersonalAddress> personalAddresses2 = personalAddressRepository.findAll(qPersonalAddress.source.eq(Constants.DataSource.REPAIR.getValue()).
                and(qPersonalAddress.personalId.eq(personalId)), sortOrder); //查询修复的联系人信息
        if (!personalAddresses1.iterator().hasNext() && !personalAddresses2.iterator().hasNext()) {
            return new ArrayList<>();
        }
        List<PersonalAddress> personalAddressList = IteratorUtils.toList(personalAddresses1.iterator());
        List<PersonalAddress> personalAddressList1 = IteratorUtils.toList(personalAddresses2.iterator());
        personalAddressList1.addAll(personalAddressList);
        return personalAddressList1;
    }

    /**
     * @Descriprion 修改地址状态
     */
    public PersonalAddress modifyAddressStatus(PhoneStatusParams phoneStatusParams, User tokenUser) {
        PersonalAddress personalAddress = personalAddressRepository.findOne(phoneStatusParams.getPersonalAddressId());
        if (Objects.isNull(personalAddress)) {
            throw new RuntimeException("该联系人信息未找到");
        }
        personalAddress.setStatus(phoneStatusParams.getAddressStatus()); //地址状态
        personalAddress.setOperator(tokenUser.getUserName()); //操作人
        personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
        personalAddressRepository.saveAndFlush(personalAddress);
        return personalAddress;
    }

    @Transactional
    public void distributeRepairCase(AccCaseInfoDisModel accCaseInfoDisModel, User user) throws Exception {
        //案件列表
        List<CaseInfo> caseInfoObjList = new ArrayList<>();
        //流转记录列表
        List<CaseTurnRecord> caseTurnRecordList = new ArrayList<>();
        List<CaseRepair> caseRepairList = new ArrayList<>();
        //选择的案件ID列表
        List<String> caseInfoList = accCaseInfoDisModel.getCaseIdList();
        //每个机构或人分配的数量
        List<Integer> disNumList = accCaseInfoDisModel.getCaseNumList();
        //已经分配的案件数量
        int alreadyCaseNum = 0;
        //接收案件列表信息
        List<String> deptOrUserList = null;
        //机构分配
        if (accCaseInfoDisModel.getDisType().equals(AccCaseInfoDisModel.DisType.DEPART_WAY.getValue())) {
            //所要分配 机构id
            deptOrUserList = accCaseInfoDisModel.getDepIdList();
        } else if (accCaseInfoDisModel.getDisType().equals(AccCaseInfoDisModel.DisType.USER_WAY.getValue())) {
            //得到所有用户ID
            deptOrUserList = accCaseInfoDisModel.getUserIdList();
        }
        for (int i = 0; i < (deptOrUserList != null ? deptOrUserList.size() : 0); i++) {
            //如果按机构分配则是机构的ID，如果是按用户分配则是用户ID
            String deptOrUserid = deptOrUserList.get(i);
            Department department = null;
            User targetUser = null;
            if (accCaseInfoDisModel.getDisType().equals(AccCaseInfoDisModel.DisType.DEPART_WAY.getValue())) {
                department = departmentRepository.findOne(deptOrUserid);
            } else if (accCaseInfoDisModel.getDisType().equals(AccCaseInfoDisModel.DisType.USER_WAY.getValue())) {
                targetUser = userRepository.findOne(deptOrUserid);
            }
            //需要分配的案件数据
            Integer disNum = disNumList.get(i);
            for (int j = 0; j < disNum; j++) {
                //检查输入的案件数量是否和选择的案件数量一致
                if (alreadyCaseNum > caseInfoList.size()) {
                    throw new Exception("选择的案件总量与实际输入的案件数量不匹配");
                }
                String caseId = caseInfoList.get(alreadyCaseNum);
                CaseRepair caseRepair = caseRepairRepository.findOne(caseId);
                if (Objects.equals(caseRepair.getCaseId().getCollectionType(), CaseInfo.CollectionType.TEL.getValue())
                        && !Objects.equals(user.getType(), User.Type.SYNTHESIZE.getValue())
                        && Objects.nonNull(user.getType())) {
                    if (!Objects.equals(user.getType(), User.Type.TEL.getValue())) {
                        throw new Exception("当前用户不可以分配电催案件");
                    }
                    if (Objects.nonNull(department) && !Objects.equals(department.getType(), Department.Type.TELEPHONE_COLLECTION.getValue())) {
                        throw new Exception("电催案件不能分配给电催以外机构");
                    }
                    if (Objects.nonNull(targetUser) && !Objects.equals(targetUser.getType(), User.Type.TEL.getValue())) {
                        throw new Exception("电催案件不能分配给电催以外人员");
                    }
                }
                if (Objects.equals(caseRepair.getCaseId().getCollectionType(), CaseInfo.CollectionType.VISIT.getValue())
                        && !Objects.equals(user.getType(), User.Type.SYNTHESIZE.getValue())
                        && Objects.nonNull(user.getType())) {
                    if (!Objects.equals(user.getType(), User.Type.VISIT.getValue())) {
                        throw new Exception("当前用户不可以分配外访案件");
                    }
                    if (Objects.nonNull(department) && !Objects.equals(department.getType(), Department.Type.OUTBOUND_COLLECTION.getValue())) {
                        throw new Exception("外访案件不能分配给外访以外机构");
                    }
                    if (Objects.nonNull(targetUser) && !Objects.equals(targetUser.getType(), User.Type.VISIT.getValue())) {
                        throw new Exception("外访案件不能分配给外访以外人员");
                    }
                }

                if (Objects.nonNull(caseRepair)) {
                    CaseInfo caseInfo = new CaseInfo();
                    BeanUtils.copyProperties(caseRepair.getCaseId(), caseInfo);
                    if (Objects.nonNull(department)) {
                        caseInfo.setDepartment(department);
                        caseInfo.setCaseFollowInTime(null);
                        caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue()); //催收状态-待分配
                    }
                    if (Objects.nonNull(targetUser)) {
                        caseInfo.setDepartment(targetUser.getDepartment());
                        caseInfo.setCurrentCollector(targetUser);
                        caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.WAITCOLLECTION.getValue()); //催收状态-待催收
                        caseInfo.setCaseFollowInTime(ZWDateUtil.getNowDateTime());
                    }
                    caseInfo.setOperator(user);
                    caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime());
                    caseInfo.setLatelyCollector(caseInfo.getCurrentCollector()); //上一个催收员
                    caseInfo.setHoldDays(0); //持案天数归0
                    caseInfo.setCaseType(CaseInfo.CaseType.DISTRIBUTE.getValue()); //流转类型-案件分配
                    caseInfo.setFollowUpNum(caseInfo.getFollowUpNum() + 1); //流转次数加一
                    caseInfo.setCaseFollowInTime(ZWDateUtil.getNowDateTime()); //流入时间
                    caseInfo.setLeaveCaseFlag(CaseInfo.leaveCaseFlagEnum.NO_LEAVE.getValue()); //留案标识默认-非留案
                    caseInfo.setCaseMark(CaseInfo.Color.NO_COLOR.getValue()); //案件标记为无色
                    //案件列表
                    caseInfoObjList.add(caseInfo);
                    //案件流转记录
                    CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
                    BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
                    caseTurnRecord.setId(null); //主键置空
                    caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
                    caseTurnRecord.setDepartId(caseInfo.getDepartment().getId()); //部门ID
                    caseTurnRecord.setReceiveUserRealName(caseInfo.getCurrentCollector().getRealName()); //接受人名称
                    if (Objects.nonNull(caseInfo.getCurrentCollector())) {
                        caseTurnRecord.setReceiveDeptName(caseInfo.getCurrentCollector().getDepartment().getName()); //接收部门名称
                    } else {
                        caseTurnRecord.setReceiveDeptName(caseInfo.getDepartment().getName());
                    }
                    caseTurnRecord.setOperatorUserName(user.getUserName()); //操作员用户名
                    caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                    caseTurnRecord.setCirculationType(2);
                    caseTurnRecordList.add(caseTurnRecord);
                    //更新修复池
                    caseRepair.setRepairStatus(CaseRepair.CaseRepairStatus.DISTRIBUTE.getValue());
                    caseRepair.setOperatorTime(ZWDateUtil.getNowDateTime());
                    caseRepair.setOperator(user);
                    caseRepairList.add(caseRepair);
                }
                alreadyCaseNum = alreadyCaseNum + 1;
            }
        }
        //保存流转记录
        caseTurnRecordRepository.save(caseTurnRecordList);
        //保存修复信息
        caseRepairRepository.save(caseRepairList);
        //保存案件信息
        caseInfoRepository.save(caseInfoObjList);
    }

    /**
     * @Description 获取特定用户案件分配信息
     */
    public BatchDistributeModel getAttachBatchDistribution(List<String> userIds) {
        Iterator<String> it = userIds.iterator();
        Integer avgCaseNum = 0; //人均案件数
        Integer userNum = 0; //登录用户部门下的所有启用用户总数
        Integer caseNum = 0; //登录用户部门下的所有启用用户持有未结案案件总数
        List<BatchInfoModel> batchInfoModels = new ArrayList<>();
        while (it.hasNext()) {
            BatchInfoModel batchInfoModel = new BatchInfoModel();
            String userId = it.next();
            Integer caseCount = caseInfoRepository.getCaseCount(userId);
            batchInfoModel.setCaseCount(caseCount); //持有案件数
            batchInfoModel.setCollectionUser(userRepository.findOne(userId)); //催收人
            batchInfoModels.add(batchInfoModel);
            userNum++;
            caseNum = caseNum + caseCount;
        }
        if (userNum != 0) {
            avgCaseNum = (caseNum % userNum == 0) ? caseNum / userNum : (caseNum / userNum + 1);
        }
        BatchDistributeModel batchDistributeModel = new BatchDistributeModel();
        batchDistributeModel.setAverageNum(avgCaseNum);
        batchDistributeModel.setBatchInfoModelList(batchInfoModels);
        return batchDistributeModel;
    }

    /**
     * @Description 获取特定机构案件分配信息
     */
    public Long getDeptBatchDistribution(String deptId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QCaseInfo.caseInfo.department.id.eq(deptId));
        builder.and(QCaseInfo.caseInfo.collectionStatus.in(20, 21, 22, 23, 25));
        return caseInfoRepository.count(builder);
    }


    /**
     * 案件重新分配
     *
     * @param accCaseInfoDisModel
     * @param user
     * @throws Exception
     */
    @Transactional
    public void distributeCeaseInfoAgain(AccCaseInfoDisModel accCaseInfoDisModel, User user) throws Exception {
        //选择的案件ID列表
        List<String> caseInfoList = accCaseInfoDisModel.getCaseIdList();
        //检查案件状态（待分配 待催收 催收中 承诺还款 可以分配）
        List<CaseInfo> caseInfoNo = new ArrayList<>(); //不可分配案件
        List<CaseInfo> caseInfoYes = new ArrayList<>(); //可分配案件
        for (String caseId : caseInfoList) {
            CaseInfo caseInfo = caseInfoRepository.findOne(caseId);
            if (Objects.isNull(caseInfo)) {
                throw new RuntimeException("有案件未找到!");
            }
            Integer collectionStatus = caseInfo.getCollectionStatus();
            if (Objects.equals(collectionStatus, CaseInfo.CollectionStatus.CASE_OVER.getValue()) //已结案
                    || Objects.equals(collectionStatus, CaseInfo.CollectionStatus.CASE_OUT.getValue()) //已委外
                    || Objects.equals(collectionStatus, CaseInfo.CollectionStatus.EARLY_PAYING.getValue())) {  //提前借清还款中
                caseInfoNo.add(caseInfo);
            } else {
                caseInfoYes.add(caseInfo);
            }
        }
        if (!caseInfoNo.isEmpty()) {
            throw new RuntimeException("已结案/已委外/提前借清还款中的案件不可重新分配!");
        }
        //案件列表
        List<CaseInfo> caseInfoObjList = new ArrayList<>();
        //流转记录列表
        List<CaseTurnRecord> caseTurnRecordList = new ArrayList<>();
        // 协催案件表
        List<CaseAssist> caseAssistList = new ArrayList<>();
        //每个机构或人分配的数量
        List<Integer> disNumList = accCaseInfoDisModel.getCaseNumList();
        //已经分配的案件数量
        int alreadyCaseNum = 0;
        //接收案件列表信息
        List<String> deptOrUserList = null;
        //机构分配
        if (accCaseInfoDisModel.getDisType().equals(AccCaseInfoDisModel.DisType.DEPART_WAY.getValue())) {
            //所要分配 机构id
            deptOrUserList = accCaseInfoDisModel.getDepIdList();
        } else if (accCaseInfoDisModel.getDisType().equals(AccCaseInfoDisModel.DisType.USER_WAY.getValue())) {
            //得到所有用户ID
            deptOrUserList = accCaseInfoDisModel.getUserIdList();
        }
        for (int i = 0; i < (deptOrUserList != null ? deptOrUserList.size() : 0); i++) {
            //如果按机构分配则是机构的ID，如果是按用户分配则是用户ID
            String deptOrUserid = deptOrUserList.get(i);
            Department department = null;
            User targetUser = null;
            if (accCaseInfoDisModel.getDisType().equals(AccCaseInfoDisModel.DisType.DEPART_WAY.getValue())) {
                department = departmentRepository.findOne(deptOrUserid);
            } else if (accCaseInfoDisModel.getDisType().equals(AccCaseInfoDisModel.DisType.USER_WAY.getValue())) {
                targetUser = userRepository.findOne(deptOrUserid);
            }
            //需要分配的案件数据
            Integer disNum = disNumList.get(i);
            for (int j = 0; j < disNum; j++) {
                //检查输入的案件数量是否和选择的案件数量一致
                if (alreadyCaseNum > caseInfoYes.size()) {
                    throw new Exception("选择的案件总量与实际输入的案件数量不匹配");
                }
                String caseId = caseInfoYes.get(alreadyCaseNum).getId();
                CaseInfo caseInfo = caseInfoRepository.findOne(caseId);
                caseInfo.setCaseType(CaseInfo.CaseType.DISTRIBUTE.getValue()); //案件类型-案件分配
                //按照部门分
                if (Objects.nonNull(department)) {
                    caseInfo.setDepartment(department); //部门
                    caseInfo.setLatelyCollector(caseInfo.getCurrentCollector()); //上个催收员
                    caseInfo.setCurrentCollector(null); //当前催收员置空
                    try {
                        setCollectionType(caseInfo, department, null);
                    } catch (final Exception e) {
                        throw new RuntimeException(e.getMessage());
                    }
                    caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue()); //催收状态-待分配
                }
                //按照用户分
                if (Objects.nonNull(targetUser)) {
                    caseInfo.setDepartment(targetUser.getDepartment());
                    caseInfo.setCurrentCollector(targetUser);
                    try {
                        setCollectionType(caseInfo, null, targetUser);
                    } catch (Exception e) {
                        throw new RuntimeException(e.getMessage());
                    }
                    caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.WAITCOLLECTION.getValue()); //催收状态-待催收
                }
                //处理协催案件
                if (Objects.equals(caseInfo.getAssistFlag(), 1)) { //协催标识
                    //结束协催案件
                    CaseAssist one = caseAssistRepository.findOne(QCaseAssist.caseAssist.caseId.eq(caseInfo)
                            .and(QCaseAssist.caseAssist.assistStatus.notIn(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())));
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
                }
                //案件类型
                caseInfo.setCaseType(CaseInfo.CaseType.DISTRIBUTE.getValue()); //流转类型-案件分配
                caseInfo.setCaseFollowInTime(new Date()); //案件流入时间
                caseInfo.setFollowUpNum(caseInfo.getFollowUpNum() + 1);//流转次数
                caseInfo.setCaseMark(CaseInfo.Color.NO_COLOR.getValue()); //案件打标-无色
                caseInfo.setFollowupBack(null); //催收反馈置空
                caseInfo.setFollowupTime(null);//跟进时间置空
                caseInfo.setPromiseAmt(new BigDecimal(0));//承诺还款置0
                caseInfo.setPromiseTime(null);//承诺还款日期置空
                caseInfo.setCirculationStatus(null);//小流转状态
                caseInfo.setLeaveCaseFlag(CaseInfo.leaveCaseFlagEnum.NO_LEAVE.getValue());//留案标识
                caseInfo.setLeaveDate(null);//留案操作日期
                caseInfo.setHasLeaveDays(0);//留案天数
                caseInfo.setHandUpFlag(CaseInfo.HandUpFlag.NO_HANG.getValue());//是否挂起
                caseInfo.setOperator(user);
                caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime());
                //案件流转记录
                CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
                BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
                caseTurnRecord.setId(null); //主键置空
                caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
                caseTurnRecord.setDepartId(caseInfo.getDepartment().getId()); //部门ID
                if (Objects.nonNull(caseInfo.getCurrentCollector())) {
                    caseTurnRecord.setReceiveDeptName(caseInfo.getCurrentCollector().getDepartment().getName()); //接收部门名称
                    caseTurnRecord.setReceiveUserId(caseInfo.getCurrentCollector().getId()); //接收人ID
                    caseTurnRecord.setReceiveUserRealName(caseInfo.getCurrentCollector().getRealName()); //接受人名称
                } else {
                    caseTurnRecord.setReceiveDeptName(caseInfo.getDepartment().getName());
                }
                caseTurnRecord.setOperatorUserName(user.getUserName()); //操作员用户名
                caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                caseTurnRecord.setCirculationType(2); //流转类型 2-正常流转
                caseTurnRecordList.add(caseTurnRecord);
                //案件列表
                caseInfoObjList.add(caseInfo);
                alreadyCaseNum = alreadyCaseNum + 1;
            }
        }
        //保存案件信息
        caseInfoRepository.save(caseInfoObjList);
        //保存流转记录
        caseTurnRecordRepository.save(caseTurnRecordList);
        //保存协催案件
        caseAssistRepository.save(caseAssistList);
    }

    /**
     * 根据部门/用户Type设置案件催收类型
     *
     * @param caseInfo
     * @param department
     * @param user
     */
    public void setCollectionType(CaseInfo caseInfo, Department department, User user) throws Exception {
        Integer type = null;
        if (Objects.nonNull(department)) {
            type = department.getType();
        }
        if (Objects.nonNull(user)) {
            type = user.getType();
        }
        switch (type) {
            case 1: //电话催收
                caseInfo.setCollectionType(CaseInfo.CollectionType.TEL.getValue());
                break;
            case 2: //外访催收
                caseInfo.setCollectionType(CaseInfo.CollectionType.VISIT.getValue());
                break;
//            case 3: //司法催收
//                caseInfo.setCollectionType(CaseInfo.CollectionType.JUDICIAL.getValue());
//                break;
//            case 4: //委外催收
//                caseInfo.setCollectionType(CaseInfo.CollectionType.outside.getValue());
//                break;
//            case 6: //提醒催收
//                caseInfo.setCollectionType(CaseInfo.CollectionType.remind.getValue());
//                break;
            default:
                throw new RuntimeException("不允许向该部门下分配案件!");
        }
    }

    public void turnCaseConfirm(List<String> caseIds, User user) {
        List<CaseInfo> caseInfos = caseInfoRepository.findAll(caseIds);
        List<CaseTurnRecord> caseTurnRecords = new ArrayList<>();
        List<CaseInfo> caseInfoResults = new ArrayList<>();
        for (int i = 0; i < caseInfos.size(); i++) {
            CaseInfo caseInfo = caseInfos.get(i);
            setAttribute(caseInfo, user, user);
            caseInfo.setCollectionType(CaseInfo.CollectionType.VISIT.getValue());
            caseInfo.setCaseType(CaseInfo.CaseType.OUTSMALLTURN.getValue());
            caseInfo.setCirculationStatus(CaseInfo.CirculationStatus.VISIT_PASS.getValue());
            caseInfoResults.add(caseInfo);
            CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
            BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
            caseTurnRecord.setId(null); //主键置空
            caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
            caseTurnRecord.setDepartId(user.getDepartment().getId()); //部门ID
            caseTurnRecord.setReceiveUserRealName(user.getRealName()); //接受人名称
            caseTurnRecord.setReceiveDeptName(user.getDepartment().getName()); //接收部门名称
            caseTurnRecord.setCirculationType(2);
            caseTurnRecord.setOperatorUserName(user.getUserName()); //操作员用户名
            caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
            caseTurnRecords.add(caseTurnRecord);
        }
        caseInfoRepository.save(caseInfoResults);
        caseTurnRecordRepository.save(caseTurnRecords);
    }

    public void turnCaseDistribution(BatchDistributeModel batchDistributeModel, User tokenUser) {
        List<BatchInfoModel> batchInfoModels = batchDistributeModel.getBatchInfoModelList();
        List<String> caseIds = batchDistributeModel.getCaseIds();
        List<CaseInfo> caseInfos = new ArrayList<>();
        List<CaseTurnRecord> caseTurnRecords = new ArrayList<>();
        for (BatchInfoModel batchInfoModel : batchInfoModels) {
            Integer caseCount = batchInfoModel.getDistributionCount(); //分配案件数
            if (0 == caseCount) {
                continue;
            }
            for (int i = 0; i < caseCount; i++) {
                CaseInfo caseInfo = caseInfoRepository.findOne(caseIds.get(i)); //获得案件信息
                User user = batchInfoModel.getCollectionUser();
                if (!Objects.equals(user.getType(), User.Type.VISIT.getValue())) {
                    throw new RuntimeException("外访案件不能分配给非外访人员");
                }
                setAttribute(caseInfo, user, tokenUser);
                caseInfo.setCaseType(CaseInfo.CaseType.DISTRIBUTE.getValue()); //流转类型-案件分配
                caseInfos.add(caseInfo);
                CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
                BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
                caseTurnRecord.setId(null); //主键置空
                caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
                caseTurnRecord.setDepartId(user.getDepartment().getId()); //部门ID
                caseTurnRecord.setReceiveUserRealName(user.getRealName()); //接受人名称
                caseTurnRecord.setReceiveDeptName(user.getDepartment().getName()); //接收部门名称
                caseTurnRecord.setCirculationType(2);
                caseTurnRecord.setOperatorUserName(user.getUserName()); //操作员用户名
                caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                caseTurnRecords.add(caseTurnRecord);
            }
        }
        caseInfoRepository.save(caseInfos);
        caseTurnRecordRepository.save(caseTurnRecords);
    }

    public void receiveCaseAssist(String id, User user) {
        synchronized (this) {
            CaseAssist caseAssist = caseAssistRepository.findOne(id);
            //更改协催案件信息
            caseAssist.setDepartId(user.getDepartment().getId()); //协催部门ID
            caseAssist.setAssistCollector(user); //协催员
            caseAssist.setAssistStatus(CaseInfo.AssistStatus.ASSIST_WAIT_ACC.getValue()); //协催状态 待催收
            caseAssist.setOperator(user); //操作员
            caseAssist.setCaseFlowinTime(ZWDateUtil.getNowDateTime()); //流入时间
            caseAssist.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
            caseAssist.setHoldDays(0); //持案天数归0
            caseAssist.setLeaveCaseFlag(0);
            caseAssist.setMarkId(CaseInfo.Color.NO_COLOR.getValue()); //颜色 无色
            //更改案件信息
            CaseInfo caseInfo = caseAssist.getCaseId();
            caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_WAIT_ACC.getValue()); //协催状态
            caseInfo.setAssistCollector(user); //协催员
            caseAssist.setCaseId(caseInfo);
            caseAssistRepository.save(caseAssist);
        }
    }

    /**
     * 获取所有即将强制流转案件List
     *
     * @return
     */
    public List<CaseInfo> getAllForceTurnCase() {
        List<CaseInfo> caseInfoList = new ArrayList<>();
        //电催案件
        caseInfoList.addAll(getForceTurnCase(Constants.SYS_PHNOEFLOW_BIGDAYSREMIND, Constants.SYS_PHNOEFLOW_BIGDAYS));
        //外访案件
        caseInfoList.addAll(getForceTurnCase(Constants.SYS_OUTBOUNDFLOW_BIGDAYSREMIND, Constants.SYS_PHNOEFLOW_BIGDAYS));
        return caseInfoList;
    }

    /**
     * 获取强制流转案件
     *
     * @param bigDaysRemind
     * @param bigDays
     * @return
     */
    public List<CaseInfo> getForceTurnCase(String bigDaysRemind, String bigDays) {
        List<CaseInfo> caseInfoList = new ArrayList<>();
        //遍历所有公司
        List<Company> companyList = companyRepository.findAll();
        for (Company company : companyList) {
            QSysParam qSysParam = QSysParam.sysParam;
            SysParam sysParam = sysParamRepository.findOne(qSysParam.companyCode.eq(company.getCode())
                    .and(qSysParam.code.eq(bigDaysRemind))
                    .and(qSysParam.status.eq(SysParam.StatusEnum.Start.getValue())));
            SysParam sysParam1 = sysParamRepository.findOne(qSysParam.companyCode.eq(company.getCode())
                    .and(qSysParam.code.eq(bigDays))
                    .and(qSysParam.status.eq(SysParam.StatusEnum.Start.getValue())));
            if (Objects.nonNull(sysParam) && Objects.nonNull(sysParam1)) {
                QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
                BooleanBuilder builder = new BooleanBuilder();
                builder.and(qCaseInfo.holdDays.between(Integer.valueOf(sysParam1.getValue()) - Integer.valueOf(sysParam.getValue()),
                        Integer.valueOf(sysParam1.getValue())).
                        and(qCaseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue())).
                        and(qCaseInfo.leaveCaseFlag.ne(CaseInfo.leaveCaseFlagEnum.YES_LEAVE.getValue())));
                caseInfoList.addAll(IterableUtils.toList(caseInfoRepository.findAll(builder)));
            }
        }
        return caseInfoList;
    }

    /**
     * 获取所有若干天无进展案件List
     *
     * @return
     */
    public List<CaseInfo> getAllNowhereCase() {
        List<CaseInfo> caseInfoList = new ArrayList<>();
        //电催案件
        caseInfoList.addAll(getNowhereCase(Constants.SYS_PHONEREMIND_DAYS));
        //外访案件
        caseInfoList.addAll(getNowhereCase(Constants.SYS_OUTREMIND_DAYS));
        return caseInfoList;
    }

    /**
     * 获取若干天无进展案件
     *
     * @param remindDays
     * @return
     */
    public List<CaseInfo> getNowhereCase(String remindDays) {
        List<CaseInfo> caseInfoList = new ArrayList<>();
        //遍历所有公司
        List<Company> companyList = companyRepository.findAll();
        for (Company company : companyList) {
            QSysParam qSysParam = QSysParam.sysParam;
            SysParam sysParam = sysParamRepository.findOne(qSysParam.companyCode.eq(company.getCode())
                    .and(qSysParam.code.eq(remindDays))
                    .and(qSysParam.status.eq(SysParam.StatusEnum.Start.getValue())));
            if (Objects.nonNull(sysParam)) {
                QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
                BooleanBuilder builder = new BooleanBuilder();
                builder.and(qCaseInfo.followupTime.isNull().
                        and((qCaseInfo.caseFollowInTime.lt(new Date(System.currentTimeMillis() - Constants.ONE_DAY_MILLIS * Integer.valueOf(sysParam.getValue()))))).
                        and(qCaseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue())).
                        and(qCaseInfo.leaveCaseFlag.ne(CaseInfo.leaveCaseFlagEnum.YES_LEAVE.getValue())));
            }
        }
        return caseInfoList;
    }
}