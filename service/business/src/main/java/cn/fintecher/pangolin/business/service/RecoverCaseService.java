package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.ReDisRecoverCaseParams;
import cn.fintecher.pangolin.business.model.RecoverCaseParams;
import cn.fintecher.pangolin.business.repository.CaseAssistRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoReturnRepository;
import cn.fintecher.pangolin.business.repository.OutsourcePoolRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by sunyanping on 2017/9/27.
 */
@Service
public class RecoverCaseService {

    private final Logger logger = LoggerFactory.getLogger(RecoverCaseService.class);

    @Inject
    private CaseInfoRepository caseInfoRepository;
    @Inject
    private CaseInfoReturnRepository caseInfoReturnRepository;
    @Inject
    private CaseAssistRepository caseAssistRepository;
    @Inject
    private OutsourcePoolRepository outsourcePoolRepository;

    public void recoverCase(RecoverCaseParams recoverCaseParams, User user) {
        if (recoverCaseParams.getIds().isEmpty()) {
            throw new RuntimeException("请选择要回收的案件!");
        }
//        if (StringUtils.isBlank(recoverCaseParams.getReason())) {
//            throw new RuntimeException("回收说明不能为空!");
//        }
        try {
            Iterable<CaseInfo> all = caseInfoRepository.findAll(QCaseInfo.caseInfo.id.in(recoverCaseParams.getIds()));
            Iterator<CaseInfo> iterator = all.iterator();
            while (iterator.hasNext()) {
                CaseInfo caseInfo = iterator.next();
                caseInfo.setOperator(user);
                caseInfo.setOperatorTime(ZWDateUtil.getNowDate());
                caseInfo.setRecoverRemark(CaseInfo.RecoverRemark.RECOVERED.getValue());
                caseInfoRepository.save(caseInfo);
                CaseInfoReturn caseInfoReturn = new CaseInfoReturn();
                caseInfoReturn.setCaseId(caseInfo);
                caseInfoReturn.setSource(CaseInfoReturn.Source.INTERNALCOLLECTION.getValue()); //内催
                caseInfoReturn.setReason(recoverCaseParams.getReason());
                caseInfoReturn.setOperator(user.getId());
                caseInfoReturn.setOperatorTime(new Date());
                caseInfoReturnRepository.save(caseInfoReturn);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("操作失败!");
        }
    }

    public void reDisRecoverCase(ReDisRecoverCaseParams params, User user) {
        if (Objects.isNull(params) || params.getIds().isEmpty()) {
            throw new RuntimeException("请选择重新分配的案件");
        }
        Iterable<CaseInfoReturn> all = caseInfoReturnRepository.findAll(QCaseInfoReturn.caseInfoReturn.id.in(params.getIds()));
        Iterator<CaseInfoReturn> iterator = all.iterator();
        if (!iterator.hasNext()) {
            throw new RuntimeException("选择的案件不存在");
        }
        List<CaseInfo> caseInfoList = new ArrayList<>();
        List<CaseAssist> caseAssistList = new ArrayList<>();
        List<CaseInfoReturn> caseInfoReturnList = new ArrayList<>();
        List<OutsourcePool> outsourcePoolList = new ArrayList<>();
        List<OutsourcePool> outsourcePools = new ArrayList<>();
        while (iterator.hasNext()) {
            CaseInfoReturn caseInfoReturn = iterator.next();
            Integer source = caseInfoReturn.getSource();
            if (Objects.equals(source, CaseInfoReturn.Source.INTERNALCOLLECTION.getValue())) { // 内催回收的案件
                CaseInfo caseInfo = caseInfoReturn.getCaseId();
                caseInfo.setCloseDate(params.getCloseDate());
                setAttr(caseInfo,caseAssistList, caseInfoList,outsourcePoolList, user, params.getType());
                caseInfoReturnList.add(caseInfoReturn);
            }
            if (Objects.equals(source, CaseInfoReturn.Source.OUTSOURCE.getValue())) { // 委外回收的案件
                CaseInfo caseInfo = caseInfoReturn.getOutsourcePool().getCaseInfo();
                caseInfo.setCloseDate(params.getCloseDate());
                outsourcePools.add(caseInfoReturn.getOutsourcePool());
                setAttr(caseInfo,caseAssistList, caseInfoList,outsourcePoolList, user, params.getType());
                caseInfoReturnList.add(caseInfoReturn);
            }
        }
        caseAssistRepository.save(caseAssistList);
        caseInfoRepository.save(caseInfoList);
        outsourcePoolRepository.save(outsourcePoolList);
        caseInfoReturnRepository.delete(caseInfoReturnList);
        outsourcePoolRepository.delete(outsourcePools);
    }

    private void setAttr(CaseInfo caseInfo, List<CaseAssist> caseAssistList, List<CaseInfo> caseInfoList,List<OutsourcePool> outsourcePoolList, User user, Integer type) {
        caseInfo.setRecoverRemark(CaseInfo.RecoverRemark.NOT_RECOVERED.getValue());//未回收
        caseInfo.setCollectionType(null);//催收类型
        caseInfo.setCaseType(null);//案件类型
        caseInfo.setDepartment(null);//部门
        caseInfo.setLatelyCollector(null);//上个催收员
        caseInfo.setCurrentCollector(null);
        caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue());//待分配
        caseInfo.setFollowUpNum(0);
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
        if (Objects.equals(type, 0)) {
            caseInfo.setCasePoolType(CaseInfo.CasePoolType.INNER.getValue()); // 内催
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
        } else if (Objects.equals(type, 1)){
            caseInfo.setCasePoolType(CaseInfo.CasePoolType.OUTER.getValue()); // 委外
            OutsourcePool outsourcePool = new OutsourcePool();
            outsourcePool.setCaseInfo(caseInfo);
            outsourcePool.setOutStatus(OutsourcePool.OutStatus.TO_OUTSIDE.getCode()); //待委外
            outsourcePool.setOperateTime(new Date());
            outsourcePool.setOperator(user.getId());
            outsourcePoolList.add(outsourcePool);
        } else {
            throw new RuntimeException("选择的要分配的目标池未知");
        }
        caseInfoList.add(caseInfo);
    }
}
