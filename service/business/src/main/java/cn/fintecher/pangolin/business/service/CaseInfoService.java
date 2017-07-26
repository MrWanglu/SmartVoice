package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.*;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author : xiaqun
 * @Description : 催收业务
 * @Date : 16:45 2017/7/17
 */

@Service("caseInfoService")
public class CaseInfoService {
    final Logger log = LoggerFactory.getLogger(CaseInfoService.class);

    private static final String TYPE_TEL = "0010";

    private static final String ASSIST_APPLY_CODE = "SysParam.assistApplyOverday";

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
        if (Objects.equals(reDistributionParams.getIsAssist(), false)) { //不是协催案件
            if (Objects.equals(user.getType(), CaseInfo.CollectionType.TEL.getValue())) { //分配给15-电催
                setAttribute(caseInfo, user, tokenUser);
            } else if (Objects.equals(user.getType(), CaseInfo.CollectionType.VISIT.getValue())) { //分配给16-外访
                if (Objects.equals(caseInfo.getAssistFlag(), 1)) { //有协催标识
                    Iterator<CaseAssist> it = getCaseAssist(reDistributionParams.getCaseId());
                    if (Objects.equals(caseInfo.getAssistStatus(), CaseInfo.AssistStatus.ASSIST_APPROVEING.getValue())) { //有协催申请
                        CaseAssistApply caseAssistApply = getCaseAssistApply(reDistributionParams.getCaseId(), tokenUser, "");
                        caseAssistApplyRepository.saveAndFlush(caseAssistApply);
                    } else { //有协催案件
                        CaseAssist caseAssist = null;
                        CaseAssist temp;
                        while (it.hasNext()) {
                            temp = it.next();
                            if (Objects.isNull(temp.getAssistCloseFlag())) { //协催结束标识为空
                                caseAssist = temp;
                            }
                        }
                        if (Objects.isNull(caseAssist)) {
                            throw new RuntimeException("协催案件未找到");
                        }
                        caseAssist.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //协催状态 29-协催完成
                        caseAssist.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                        caseAssist.setOperator(tokenUser); //操作员
                        caseAssistRepository.saveAndFlush(caseAssist);

                        //同步更新原案件协催状态
                        caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //29-协催完成

                        //协催结束新增一条流转记录
                        CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
                        BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
                        caseTurnRecord.setId(null); //主键置空
                        caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
                        caseTurnRecord.setDepartId(caseInfo.getDepartment().getId()); //部门ID
                        caseTurnRecord.setReceiveUserid(caseInfo.getCurrentCollector()); //接受人
                        caseTurnRecord.setReceiveDeptid(user.getDepartment()); //接收部门
                        caseTurnRecord.setOperator(tokenUser); //操作员
                        caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                        caseTurnRecordRepository.saveAndFlush(caseTurnRecord);
                    }
                }
                setAttribute(caseInfo, user, tokenUser);

                //同步更新原案件协催员，协催方式，协催标识，协催状态
                caseInfo.setAssistCollector(null); //协催员置空
                caseInfo.setAssistWay(null); //协催方式置空
                caseInfo.setAssistFlag(0); //协催标识 0-否
                caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //协催状态 29-协催完成
            }
        } else { //是协催案件
            if (!Objects.equals(tokenUser.getType(), CaseInfo.CollectionType.VISIT.getValue())) {
                throw new RuntimeException("协催案件不能分配给外访以外人员");
            }
            Iterator<CaseAssist> it = getCaseAssist(reDistributionParams.getCaseId());
            CaseAssist caseAssist = null;
            CaseAssist temp;
            while (it.hasNext()) {
                temp = it.next();
                if (Objects.isNull(temp.getAssistCloseFlag())) { //协催结束标识为空
                    caseAssist = temp;
                }
            }
            if (Objects.isNull(caseAssist)) {
                throw new RuntimeException("协催案件未找到");
            }
            caseAssist.setAssistCollector(user); //协催员
            caseAssist.setOperator(tokenUser); //操作员
            caseAssist.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
            caseAssist.setHoldDays(0); //持案天数归0
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
        caseTurnRecord.setReceiveUserid(user); //接受人
        caseTurnRecord.setReceiveDeptid(user.getDepartment()); //接收部门
        caseTurnRecord.setOperator(tokenUser); //操作员
        caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
        caseTurnRecordRepository.saveAndFlush(caseTurnRecord);
    }

    /**
     * @Description 客户信息
     */
    public Personal getCustInfo(String caseId) {
        CaseInfo caseInfo = caseInfoRepository.findOne(caseId); //获得案件
        if (Objects.isNull(caseInfo)) {
            throw new RuntimeException("该案件未找到");
        }
        return caseInfo.getPersonalInfo();
    }

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
                || Objects.equals(caseInfo.getCollectionStatus(), CaseInfo.CollectionStatus.EARLY_PAYING.getValue())) {
            throw new RuntimeException("该案件正在还款中或已还款，不允许再次还款");
        }
        if (BigDecimal.ZERO.compareTo(payApplyParams.getPayAmt()) == 1) {
            throw new RuntimeException("还款金额不能小于0");
        }
        if (BigDecimal.ZERO.compareTo(payApplyParams.getDerateFee()) == 1) {
            throw new RuntimeException("减免金额不能小于0");
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
        casePayApply.setApplyDerateAmt(payApplyParams.getDerateFee()); //申请减免金额
        casePayApply.setPayType(payApplyParams.getPayaType()); //还款类型
        casePayApply.setPayWay(payApplyParams.getPayWay()); //还款方式
        casePayApply.setDerateFlag(payApplyParams.getDerateFlag()); //减免标识
        casePayApply.setApproveDerateRemark(payApplyParams.getDerateDescripton()); //减免费用备注
        if (Objects.equals(payApplyParams.getDerateFlag(), 1)) { //减免标识 1-有减免
            casePayApply.setApproveStatus(CasePayApply.ApproveStatus.PAY_TO_AUDIT.getValue()); //审批状态 57-还款待审核
        } else { //减免标识 0-没有减免
            casePayApply.setApproveStatus(CasePayApply.ApproveStatus.DERATE_TO_AUDIT.getValue()); //审批状态 55-减免待审核
        }
        casePayApply.setPayMemo(payApplyParams.getPayDescripton()); //还款说明
        casePayApply.setApplayUserName(tokenUser.getUserName()); //申请人
        casePayApply.setApplayRealName(tokenUser.getRealName()); //申请人姓名
        casePayApply.setApplayDeptName(tokenUser.getDepartment().getName()); //申请人部门名称
        casePayApply.setApplayDate(ZWDateUtil.getNowDateTime()); //申请时间
        casePayApply.setCompanyCode(caseInfo.getCompanyCode()); //公司code码
        casePayApply.setPersonalPhone(caseInfo.getPersonalInfo().getMobileNo()); //客户手机号
        casePayApply.setPrincipalId(caseInfo.getPrincipalId().getId()); //委托方ID
        casePayApply.setPrincipalName(caseInfo.getPrincipalId().getName()); //委托方名称
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
        casePayApply.setApproveStatus(CasePayApply.ApproveStatus.REVOKE.getValue()); //还款审批状态 54-撤回
        casePayApply.setOperatorUserName(tokenUser.getUserName()); //操作人用户名
        casePayApply.setOperatorRealName(tokenUser.getRealName()); //操作人名称
        casePayApply.setOperatorDate(ZWDateUtil.getNowDateTime()); //操作时间
        casePayApplyRepository.saveAndFlush(casePayApply);

        //修改原案件催收状态
        CaseInfo caseInfo = caseInfoRepository.findOne(casePayApply.getCaseId());
        if (Objects.isNull(caseInfo)) {
            throw new RuntimeException("该案件未找到");
        }
        caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.COLLECTIONING.getValue()); //案件催收状态 21-催收中
        caseInfoRepository.saveAndFlush(caseInfo);
    }

    /**
     * @Description 添加跟进记录
     */
    public CaseFollowupRecord saveFollowupRecord(CaseFollowupRecord caseFollowupRecord, User tokenUser) {
        caseFollowupRecord.setOperator(tokenUser); //操作员
        caseFollowupRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
        caseFollowupRecordRepository.saveAndFlush(caseFollowupRecord);

        //同步更新案件
        CaseInfo caseInfo = caseInfoRepository.findOne(caseFollowupRecord.getCaseId().getId()); //获取案件信息
        if (Objects.isNull(caseInfo)) {
            throw new RuntimeException("该案件未找到");
        }
        caseInfo.setFollowupTime(caseFollowupRecord.getOperatorTime()); //最新跟进时间
        caseInfo.setFollowupBack(caseFollowupRecord.getCollectionFeedback()); //催收反馈
        caseInfo.setPromiseAmt(caseFollowupRecord.getPromiseAmt()); //承诺还款金额
        caseInfo.setPromiseTime(caseFollowupRecord.getPromiseDate()); //承诺还款日期
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
        if (Objects.equals(endCaseParams.getIsAssist(), false)) { //不是协催案件
            if (Objects.equals(caseInfo.getAssistFlag(), 1)) { //有协催标识
                Iterator<CaseAssist> it = getCaseAssist(endCaseParams.getCaseId());
                if (Objects.equals(caseInfo.getAssistStatus(), CaseInfo.AssistStatus.ASSIST_APPROVEING.getValue())) { //有协催申请
                    CaseAssistApply caseAssistApply = getCaseAssistApply(endCaseParams.getCaseId(), tokenUser, endCaseParams.getEndRemark());
                    caseAssistApplyRepository.saveAndFlush(caseAssistApply);
                } else { //有协催案件
                    CaseAssist caseAssist = null;
                    CaseAssist temp;
                    while (it.hasNext()) {
                        temp = it.next();
                        if (Objects.isNull(temp.getAssistCloseFlag())) { //协催结束标识为空
                            caseAssist = temp;
                        }
                    }
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
            caseInfoRepository.saveAndFlush(caseInfo);
        } else { //是协催案件
            Iterator<CaseAssist> it = getCaseAssist(endCaseParams.getCaseId());
            CaseAssist caseAssist = null;
            CaseAssist temp;
            while (it.hasNext()) {
                temp = it.next();
                if (Objects.isNull(temp.getAssistCloseFlag())) { //协催结束标识为空
                    caseAssist = temp;
                }
            }
            if (Objects.isNull(caseAssist)) {
                throw new RuntimeException("协催案件未找到");
            }
            caseAssist.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //协催状态 29-协催完成
            caseAssist.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
            caseAssist.setOperator(tokenUser); //操作员
            caseAssistRepository.saveAndFlush(caseAssist);
            if (Objects.equals(caseInfo.getAssistWay(), CaseAssist.AssistWay.ONCE_ASSIST.getValue())) { //单次协催，原案件催收状态为催收中
                //同步更新原案件状态
                caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.COLLECTIONING.getValue()); //催收状态 21-催收中
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
                caseTurnRecord.setDepartId(caseInfo.getDepartment().getId()); //部门ID
                caseTurnRecord.setReceiveUserid(caseInfo.getCurrentCollector()); //接受人
                caseTurnRecord.setReceiveDeptid(caseInfo.getCurrentCollector().getDepartment()); //接收部门
                caseTurnRecord.setOperator(tokenUser); //操作员
                caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                caseTurnRecordRepository.saveAndFlush(caseTurnRecord);
            } else { //全程协催，原案件催收状态为已结案
                //同步更新原案件状态
                caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.CASE_OVER.getValue()); //催收状态 24-已结案
                caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //协催状态 29-协催完成
                caseInfo.setOperator(tokenUser); //操作人
                caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                caseInfo.setEndRemark(endCaseParams.getEndRemark()); //结案说明
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
        if (Objects.equals(caseInfo.getAssistFlag(), 1)) { //有协催标识
            Iterator<CaseAssist> it = getCaseAssist(assistApplyParams.getCaseId());
            if (!it.hasNext()) { //有协催申请
                throw new RuntimeException("该案件已经提交了协催申请，不允许重复提交");
            } else { //有协催案件
                throw new RuntimeException("该案件正在协催，不允许重复申请");
            }
        }
        //获得失效日数
        QSysParam qSysParam = QSysParam.sysParam;
        Iterable<SysParam> sysParams = sysParamRepository.findAll(qSysParam.code.eq(ASSIST_APPLY_CODE).and(qSysParam.type.eq(TYPE_TEL)).and(qSysParam.companyCode.eq(tokenUser.getCompanyCode())));
        if (Objects.isNull(sysParams)) {
            throw new RuntimeException("协催失效配置天数未找到");
        }
        Integer days = Integer.parseInt(sysParams.iterator().next().getValue());
        Long nowtime = ZWDateUtil.getNowDateTime().getTime();
        Long invalidTime = nowtime + days * 86400000;
        Date applyInvalidTime = new Date(invalidTime); //失效日期

        //新增协催申请记录
        CaseAssistApply caseAssistApply = new CaseAssistApply();
        caseAssistApply.setCaseId(assistApplyParams.getCaseId()); //案件ID
        caseAssistApply.setCaseNumber(caseInfo.getCaseNumber()); //案件编号
        caseAssistApply.setPersonalName(caseInfo.getPersonalInfo().getName()); //客户姓名
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
        caseAssistApply.setProductSeries(caseInfo.getProduct().getProductSeries().getId()); //产品系列ID
        caseAssistApply.setProductId(caseInfo.getProduct().getId()); //产品ID
        caseAssistApply.setProductSeriesName(caseInfo.getProduct().getProductSeries().getSeriesName()); //产品系列名称
        caseAssistApply.setProductName(caseInfo.getProduct().getProdcutName()); //产品名称
        caseAssistApply.setCompanyCode(caseInfo.getCompanyCode()); //公司code码
        caseAssistApplyRepository.saveAndFlush(caseAssistApply);

        //更新原案件
        caseInfo.setAssistFlag(1); //协催标识
        caseInfo.setAssistWay(assistApplyParams.getAssistWay()); //协催方式
        caseInfo.setOperator(tokenUser); //操作人
        caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
        caseInfoRepository.saveAndFlush(caseInfo);
    }

    /**
     * @Description 判断用户下有没有正在催收的案件
     */
    public CollectionCaseModel haveCollectionCase(User user) {
        QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
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
        QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
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
        for (BatchInfoModel batchInfoModel : batchInfoModels) {
            Integer caseCount = batchInfoModel.getDistributionCount(); //分配案件数
            if (!Objects.equals(tokenUser.getDepartment().getType(), Department.Type.OUTBOUND_COLLECTION.getValue())) { //分配给外访以外
                for (int i = 0; i < caseCount; i++) {
                    CaseInfo caseInfo = caseInfoRepository.findOne(caseIds.get(i)); //获得案件信息
                    if (Objects.equals(caseInfo.getAssistFlag(), 1)) { //有协催标识
                        Iterator<CaseAssist> it = getCaseAssist(caseIds.get(i));
                        if (Objects.equals(caseInfo.getAssistStatus(), CaseInfo.AssistStatus.ASSIST_APPROVEING.getValue())) { //有协催申请
                            CaseAssistApply caseAssistApply = getCaseAssistApply(caseIds.get(i), tokenUser, "");
                            caseAssistApplies.add(caseAssistApply);
                        } else { //有协催案件
                            CaseAssist caseAssist = null;
                            CaseAssist temp;
                            while (it.hasNext()) {
                                temp = it.next();
                                if (Objects.isNull(temp.getAssistCloseFlag())) { //协催结束标识为空
                                    caseAssist = temp;
                                }
                            }
                            if (Objects.isNull(caseAssist)) {
                                throw new RuntimeException("协催案件未找到");
                            }
                            caseAssist.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //协催状态 29-协催完成
                            caseAssist.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                            caseAssist.setOperator(tokenUser); //操作员
                            caseAssists.add(caseAssist);

                            //同步更新原案件协催状态
                            caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //29-协催完成

                            //协催结束新增一条流转记录
                            CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
                            BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
                            caseTurnRecord.setId(null); //主键置空
                            caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
                            caseTurnRecord.setDepartId(caseInfo.getDepartment().getId()); //部门ID
                            caseTurnRecord.setReceiveUserid(caseInfo.getCurrentCollector()); //接受人
                            caseTurnRecord.setReceiveDeptid(batchInfoModel.getCollectionUser().getDepartment()); //接收部门
                            caseTurnRecord.setOperator(tokenUser); //操作员
                            caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                            caseTurnRecords.add(caseTurnRecord);

                            setAttribute(caseInfo, batchInfoModel.getCollectionUser(), tokenUser);
                        }
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
                    caseTurnRecord.setReceiveUserid(batchInfoModel.getCollectionUser()); //接受人
                    caseTurnRecord.setReceiveDeptid(batchInfoModel.getCollectionUser().getDepartment()); //接收部门
                    caseTurnRecord.setOperator(tokenUser); //操作员
                    caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                    caseTurnRecords.add(caseTurnRecord);
                    caseIds.remove(0);
                }
            } else { //分配给外访
                for (int i = 0; i < caseCount; i++) {
                    CaseInfo caseInfo = caseInfoRepository.findOne(caseIds.get(i)); //获得案件信息
                    if (Objects.equals(caseInfo.getAssistFlag(), 1)) { //协催案件
                        if (!Objects.equals(batchInfoModel.getCollectionUser().getType(), CaseInfo.CollectionType.VISIT.getValue())) {
                            throw new RuntimeException("协催案件不能分配给外访以外人员");
                        }
                        Iterator<CaseAssist> it = getCaseAssist(caseIds.get(i));
                        CaseAssist caseAssist = null;
                        CaseAssist temp;
                        while (it.hasNext()) {
                            temp = it.next();
                            if (Objects.isNull(temp.getAssistCloseFlag())) { //协催结束标识为空
                                caseAssist = temp;
                            }
                        }
                        if (Objects.isNull(caseAssist)) {
                            throw new RuntimeException("协催案件未找到");
                        }
                        caseAssist.setAssistCollector(batchInfoModel.getCollectionUser()); //协催员
                        caseAssist.setOperator(tokenUser); //操作员
                        caseAssist.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                        caseAssist.setHoldDays(0); //持案天数归0
                        caseAssists.add(caseAssist);

                        //同步更新原案件协催员
                        caseInfo.setAssistCollector((batchInfoModel.getCollectionUser())); //协催员
                    } else {
                        setAttribute(caseInfo, batchInfoModel.getCollectionUser(), tokenUser);
                    }
                    caseInfos.add(caseInfo);
                    //分配完成新增流转记录
                    CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
                    BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
                    caseTurnRecord.setId(null); //主键置空
                    caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
                    caseTurnRecord.setDepartId(batchInfoModel.getCollectionUser().getDepartment().getId()); //部门ID
                    caseTurnRecord.setReceiveUserid(batchInfoModel.getCollectionUser()); //接受人
                    caseTurnRecord.setReceiveDeptid(batchInfoModel.getCollectionUser().getDepartment()); //接收部门
                    caseTurnRecord.setOperator(tokenUser); //操作员
                    caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                    caseTurnRecords.add(caseTurnRecord);
                    caseIds.remove(0);
                }
            }
        }
        caseInfoRepository.save(caseInfos);
        caseAssistRepository.save(caseAssists);
        caseAssistApplyRepository.save(caseAssistApplies);
        caseTurnRecordRepository.save(caseTurnRecords);
    }

    /**
     * @Description 案件颜色打标
     */
    public CaseInfo caseMarkColor(CaseMarkParams caseMarkParams, User tokenUser) {
        CaseInfo caseInfo = caseInfoRepository.findOne(caseMarkParams.getCaseId());
        if (Objects.isNull(caseInfo)) {
            throw new RuntimeException("该案件未找到");
        }
        caseInfo.setCaseMark(caseMarkParams.getColorNum()); //打标
        caseInfo.setOperator(tokenUser); //操作人
        caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
        return caseInfo;
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
        return personalContact;
    }

    /**
     * @Description 重新分配案件字段复制
     */
    private CaseInfo setAttribute(CaseInfo caseInfo, User user, User tokenUser) {
        caseInfo.setLatelyCollector(caseInfo.getCurrentCollector()); //上一个催收员
        caseInfo.setCurrentCollector(user); //当前催收员
        caseInfo.setHoldDays(0); //持案天数归0
        caseInfo.setFollowUpNum(caseInfo.getFollowUpNum() + 1); //流转次数加一
        caseInfo.setCaseFollowInTime(ZWDateUtil.getNowDateTime()); //流入时间
        caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.WAITCOLLECTION.getValue()); //催收状态 20-待催收
        caseInfo.setOperator(tokenUser); //操作员
        caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
        return caseInfo;
    }

    /**
     * @Description 获得协催申请并set值
     */
    private CaseAssistApply getCaseAssistApply(String caseId, User tokenUser, String memo) {
        List<Integer> list = new ArrayList<>(); //协催审批状态列表
        list.add(CaseAssistApply.ApproveStatus.TEL_APPROVAL.getValue()); // 32-电催待审批
        list.add(CaseAssistApply.ApproveStatus.VISIT_APPROVAL.getValue()); // 34-外访待审批
        QCaseAssistApply qCaseAssistApply = QCaseAssistApply.caseAssistApply;
        Iterable<CaseAssistApply> caseAssistApplies = caseAssistApplyRepository.findAll(qCaseAssistApply.caseId.eq(caseId)
                .and(qCaseAssistApply.approvePhoneResult.in(list)));
        if (!caseAssistApplies.iterator().hasNext()) {
            throw new RuntimeException("协催申请记录未找到");
        }
        CaseAssistApply caseAssistApply = caseAssistApplies.iterator().next(); //获得协催申请
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
     * @Description 查询协催案件
     */
    private Iterator<CaseAssist> getCaseAssist(String caseId) {
        QCaseAssist qCaseAssist = QCaseAssist.caseAssist;
        Iterable<CaseAssist> caseAssists = caseAssistRepository.findAll(qCaseAssist.caseId.id.eq(caseId)); //查询协催案件
        return caseAssists.iterator();
    }

    /**
     * @Description 获取联系人信息
     */
    public List<PersonalContact> getPersonalContact(String personalId) {
        QPersonalContact qPersonalContact = QPersonalContact.personalContact;
        Iterable<PersonalContact> personalContacts = personalContactRepository.findAll(qPersonalContact.personalInfo.id.eq(personalId));
        Iterator<PersonalContact> it = personalContacts.iterator();
        List<PersonalContact> list = new ArrayList<>();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }
}