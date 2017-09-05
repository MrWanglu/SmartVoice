package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.AssistingStatisticsModel;
import cn.fintecher.pangolin.business.model.BatchInfoModel;
import cn.fintecher.pangolin.business.repository.CaseAssistRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.DepartmentRepository;
import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.Constants;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static cn.fintecher.pangolin.entity.QCaseInfo.caseInfo;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/7/25.
 */
@Service("caseAssistService")
public class CaseAssistService {

    @Inject
    private CaseAssistRepository caseAssistRepository;
    @Inject
    private DepartmentRepository departmentRepository;
    @Inject
    private SysParamRepository sysParamRepository;
    @Inject
    private CaseInfoRepository caseInfoRepository;

    /**
     * 获取机构下待协催、协催中的协催案件
     *
     * @param department 部门
     * @return
     */
    public AssistingStatisticsModel getDepartmentCollectingAssist(Department department) {
        QCaseAssist qCaseAssist = QCaseAssist.caseAssist;
        //找到所有协催待催收、协催催收中的协催案件
        BooleanExpression exp = qCaseAssist.assistStatus.eq(CaseInfo.AssistStatus.ASSIST_COLLECTING.getValue())
                .or(qCaseAssist.assistStatus.eq(CaseInfo.AssistStatus.ASSIST_WAIT_ACC.getValue()));
        List<CaseAssist> all = IterableUtils.toList(caseAssistRepository.findAll(exp));
        if (all.isEmpty()) {
            return null;
        }
        List<CaseAssist> caseAssistList = new ArrayList<>();
        for (CaseAssist caseAssist :all) {
            Department one = departmentRepository.findOne(caseAssist.getDepartId());
            if (Objects.nonNull(one)) {
                if (one.getCode().startsWith(department.getCode()) && Objects.equals(one.getCompanyCode(), department.getCompanyCode())) {
                    caseAssistList.add(caseAssist);
                }
            } else {
                throw new RuntimeException("待催收/催收中的协催案件应该归属于某一部门");
            }
        }
        List<String> assistId = new ArrayList<>();
        for (CaseAssist c : caseAssistList) {
            assistId.add(c.getId());
        }
        AssistingStatisticsModel model = new AssistingStatisticsModel();
        model.setNum(caseAssistList.size());
        model.setAssistList(assistId);
        return model;
    }

    /**
     * 找到某个用户正在协催的案件
     *
     * @param user 用户
     * @return
     */
    public AssistingStatisticsModel getCollectorAssist(User user) {
        QCaseAssist qCaseAssist = QCaseAssist.caseAssist;
        // 找到User正在协催、待催收的协催案件
        BooleanExpression exp = qCaseAssist.assistCollector.userName.eq(user.getUserName())
                .and(qCaseAssist.assistStatus.in(CaseInfo.AssistStatus.ASSIST_WAIT_ACC.getValue(),
                        CaseInfo.AssistStatus.ASSIST_COLLECTING.getValue()));
        Iterable<CaseAssist> all = caseAssistRepository.findAll(exp);
        if (!all.iterator().hasNext()) {
            return null;
        }
        List<String> assistId = new ArrayList<>();
        while (all.iterator().hasNext()) {
            CaseAssist next = all.iterator().next();
            assistId.add(next.getId());
        }
        AssistingStatisticsModel model = new AssistingStatisticsModel();
        model.setNum(assistId.size());
        model.setAssistList(assistId);
        return model;
    }

    /**
     * 获取强制流转的协催案件
     * @param companyCode
     * @return
     */
    public List<CaseAssist> getForceTurnAssistCase(String companyCode){
        List<CaseAssist> caseAssistList = new ArrayList<>();
        QSysParam qSysParam = QSysParam.sysParam;
        SysParam assistBigDaysRemind = sysParamRepository.findOne(qSysParam.companyCode.eq(companyCode)
                .and(qSysParam.code.eq(Constants.SYS_ASSISTREMIND_BIGDAYSREMIND))
                .and(qSysParam.status.eq(SysParam.StatusEnum.Start.getValue())));
        SysParam assistBigDays = sysParamRepository.findOne(qSysParam.companyCode.eq(companyCode)
                .and(qSysParam.code.eq(Constants.SYS_ASSIST_BIGDAYS))
                .and(qSysParam.status.eq(SysParam.StatusEnum.Start.getValue())));
        if (Objects.nonNull(assistBigDaysRemind) && Objects.nonNull(assistBigDays)) {
            QCaseAssist qCaseAssist = QCaseAssist.caseAssist;
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(qCaseAssist.holdDays.between(Integer.valueOf(assistBigDays.getValue()) - Integer.valueOf(assistBigDaysRemind.getValue()),
                    Integer.valueOf(assistBigDays.getValue())).
                    and(qCaseAssist.assistWay.eq(CaseAssist.AssistWay.WHOLE_ASSIST.getValue())).
                    and(qCaseAssist.assistStatus.in(28,117,118)).
                    and(qCaseAssist.companyCode.eq(companyCode)).
                    and(qCaseAssist.assistCollector.isNotNull()).
                    and(qCaseAssist.leaveCaseFlag.ne(CaseInfo.leaveCaseFlagEnum.YES_LEAVE.getValue())));
            caseAssistList.addAll(IterableUtils.toList(caseAssistRepository.findAll(builder)));
        }
        return caseAssistList;
    }

    /**
     * 获取机构下协催结束的协催案件
     *
     * @param department 部门
     * @return
     */
    public AssistingStatisticsModel getDepartmentEndAssist(Department department) {
        QCaseAssist qCaseAssist = QCaseAssist.caseAssist;
        //找到所有协催结束的协催案件
        BooleanExpression exp = qCaseAssist.assistStatus.eq(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue());
        List<CaseAssist> all = IterableUtils.toList(caseAssistRepository.findAll(exp));
        if (all.isEmpty()) {
            return null;
        }
        List<CaseAssist> caseAssistList = new ArrayList<>();
        for (CaseAssist caseAssist :all) {
            Department one = departmentRepository.findOne(caseAssist.getDepartId());
            if (Objects.nonNull(one)) {
                if (one.getCode().startsWith(department.getCode()) && Objects.equals(one.getCompanyCode(), department.getCompanyCode())) {
                    caseAssistList.add(caseAssist);
                }
            } else {
                throw new RuntimeException("协催结束的协催案件应该归属于某一部门");
            }
        }
        List<String> assistId = new ArrayList<>();
        for (CaseAssist c : caseAssistList) {
            assistId.add(c.getId());
        }
        AssistingStatisticsModel model = new AssistingStatisticsModel();
        model.setNum(caseAssistList.size());
        model.setAssistList(assistId);
        return model;
    }

    /**
     * 找到某个用户协催结束的案件
     *
     * @param user 用户
     * @return
     */
    public AssistingStatisticsModel getCollectorEndAssist(User user) {
        QCaseAssist qCaseAssist = QCaseAssist.caseAssist;
        // 找到User结束协催的协催案件
        BooleanBuilder exp = new BooleanBuilder();
        exp.and(qCaseAssist.assistCollector.userName.eq(user.getUserName()));
        exp.and(qCaseAssist.assistStatus.eq(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()));
        Iterable<CaseAssist> all = caseAssistRepository.findAll(exp);
        if (!all.iterator().hasNext()) {
            return null;
        }
        List<String> assistId = new ArrayList<>();
        while (all.iterator().hasNext()) {
            CaseAssist next = all.iterator().next();
            assistId.add(next.getId());
        }
        AssistingStatisticsModel model = new AssistingStatisticsModel();
        model.setNum(assistId.size());
        model.setAssistList(assistId);
        return model;
    }

    /**
     * 获取用户当前可留案案件数
     * @param user
     * @return
     */
    public Integer leaveCaseAssistNum(User user) {
        //获得所持有未结案的案件总数
        QCaseInfo qCaseInfo = caseInfo;
        List<Integer> status = new ArrayList<>();
        status.add(CaseInfo.CollectionStatus.WAITCOLLECTION.getValue());
        status.add(CaseInfo.CollectionStatus.COLLECTIONING.getValue());
        status.add(CaseInfo.CollectionStatus.OVER_PAYING.getValue());
        status.add(CaseInfo.CollectionStatus.EARLY_PAYING.getValue());
        status.add(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue());
        status.add(CaseInfo.CollectionStatus.REPAID.getValue());
        status.add(CaseInfo.CollectionStatus.PART_REPAID.getValue());
        long count = caseInfoRepository.count((qCaseInfo.currentCollector.id.eq(user.getId()).or(qCaseInfo.assistCollector.id.eq(user.getId()))
                .and(qCaseInfo.collectionStatus.in(status))));
        //获得留案比例
        QSysParam qSysParam = QSysParam.sysParam;
        SysParam sysParam = sysParamRepository.findOne(qSysParam.code.eq(Constants.SYS_ASSIST_LEAVERATE).and(qSysParam.companyCode.eq(user.getCompanyCode())));
        Double rate = Double.parseDouble(sysParam.getValue()) / 100;
        //根据总案件数和留案比例获取总共可留案的案件数
        Integer leaveNum = (int) (count * rate);
        //查询已留案案件数
        long count1 = caseAssistRepository.leaveCaseAssistCount(user.getId());
        return (int)(leaveNum - count1);
    }

    /**
     * 结束协催
     * @param caseAssist
     * @param user
     */
    public void endCaseAssist(CaseAssist caseAssist, User user) {
        CaseInfo caseInfo = caseAssist.getCaseId();
        // 协催案件
        caseAssist.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //协催状态
        caseAssist.setAssistCloseFlag(0); //手动结束
        caseAssist.setOperatorTime(new Date()); //操作时间
        caseAssist.setOperator(user); //操作员
        //原案件
        caseInfo.setAssistStatus(null); //协催状态
        caseInfo.setAssistCollector(null); //协催员
        caseInfo.setAssistWay(null); //协催方式
        caseInfo.setAssistFlag(0); //协催标识
        caseInfo.setLatelyCollector(caseAssist.getAssistCollector()); //上一个协催员
        caseAssist.setCaseId(caseInfo);
        caseAssistRepository.save(caseAssist);
    }

    /**
     * 协催分配
     * @param caseAssist 要分配的协催案件
     * @param user 操作人
     * @param batchInfoModel 要分配的数据
     * @param caseAssistList 要分配的所有协催案件
     * @param caseTurnRecordList 增加的所有流转记录
     */
    public void distributeCaseAssist(CaseAssist caseAssist, User user,BatchInfoModel batchInfoModel,
                                     List<CaseAssist> caseAssistList, List<CaseTurnRecord> caseTurnRecordList) {
        CaseInfo caseId = caseAssist.getCaseId();
        caseAssist.setLatelyCollector(caseAssist.getAssistCollector());
        caseId.setAssistCollector(batchInfoModel.getCollectionUser());
        caseAssist.setCaseId(caseId);
        caseAssist.setAssistStatus(CaseInfo.AssistStatus.ASSIST_WAIT_ACC.getValue());
        caseAssist.setDepartId(batchInfoModel.getCollectionUser().getDepartment().getId());
        caseAssist.setAssistCollector(batchInfoModel.getCollectionUser());
        caseAssist.setCaseFlowinTime(new Date());
        caseAssist.setOperatorTime(new Date());
        caseAssist.setOperator(user);
        caseAssist.setHasLeaveDays(0);
        caseAssist.setLeaveCaseFlag(CaseInfo.leaveCaseFlagEnum.NO_LEAVE.getValue());
        caseAssist.setHoldDays(0);
        caseAssist.setMarkId(CaseInfo.Color.NO_COLOR.getValue());
        caseAssistList.add(caseAssist);

        //增加流转记录
        CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
        BeanUtils.copyProperties(caseAssist.getCaseId(), caseTurnRecord);
        caseTurnRecord.setCaseId(caseAssist.getCaseId().getId());//案件ID
        caseTurnRecord.setCaseNumber(caseAssist.getCaseId().getCaseNumber());//案件编号
        caseTurnRecord.setOperatorTime(new Date());
        caseTurnRecord.setHoldDays(caseAssist.getHoldDays()); //持案天数
        caseTurnRecord.setCaseType(CaseInfo.CaseType.DISTRIBUTE.getValue());//案件流转类型
        caseTurnRecord.setCompanyCode(user.getCompanyCode());
        caseTurnRecord.setDepartId(user.getDepartment().getId());
        caseTurnRecord.setOperatorUserName(user.getUserName());
        caseTurnRecord.setReceiveDeptName(batchInfoModel.getCollectionUser().getDepartment().getName());//接收部门
        caseTurnRecord.setReceiveUserId(batchInfoModel.getCollectionUser().getId());//接收人ID
        caseTurnRecord.setReceiveUserRealName(batchInfoModel.getCollectionUser().getRealName());
        caseTurnRecord.setCurrentCollector(batchInfoModel.getCollectionUser().getId());
        caseTurnRecordList.add(caseTurnRecord);
    }

    /**
     * 协催案件取消留案修改状态
     * @param assist 协催案件
     * @param user 操作人
     */
    public void cancelLeaveCaseAssist(CaseAssist assist, User user) {
        assist.setLeaveCaseFlag(CaseInfo.leaveCaseFlagEnum.NO_LEAVE.getValue()); //协催标识
        assist.setHasLeaveDays(0); //留案天数
        assist.setLeaveDate(null); //留案日期
        assist.setOperator(user); //操作人
        assist.setOperatorTime(new Date());//修改时间
    }
}
