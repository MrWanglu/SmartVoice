package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.AssistingStatisticsModel;
import cn.fintecher.pangolin.business.repository.CaseAssistRepository;
import cn.fintecher.pangolin.business.repository.DepartmentRepository;
import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.Constants;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        Iterable<CaseAssist> all = caseAssistRepository.findAll(exp);
        if (!all.iterator().hasNext()) {
            return null;
        }
        List<CaseAssist> caseAssistList = new ArrayList<>();
        for (CaseAssist caseAssist : all) {
            Department one = departmentRepository.findOne(caseAssist.getDepartId());
            if (one.getCode().startsWith(department.getCode()) && Objects.equals(one.getCompanyCode(), department.getCompanyCode())) {
                caseAssistList.add(caseAssist);
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
                .and(qSysParam.code.eq(Constants.SYS_ASSISTREMIND_BIGDAYS))
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
}
