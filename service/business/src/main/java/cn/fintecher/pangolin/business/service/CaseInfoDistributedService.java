package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.*;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.strategy.CaseStrategy;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @Author: PeiShouWen
 * @Description: 案件分配服务类
 * @Date 16:38 2017/8/7
 */
@Service("caseInfoDistributedService")
public class CaseInfoDistributedService {

    Logger logger = LoggerFactory.getLogger(CaseInfoDistributedService.class);

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
    OutsourcePoolRepository outsourcePoolRepository;

    @Inject
    CaseInfoService caseInfoService;

    @Inject
    CaseInfoRemarkRepository caseInfoRemarkRepository;

    @Inject
    RunCaseStrategyService runCaseStrategyService;

    /**
     * 案件分配
     *
     * @param accCaseInfoDisModel
     * @param user
     * @throws Exception
     */
    @Transactional
    public void distributeCeaseInfo(AccCaseInfoDisModel accCaseInfoDisModel, User user) throws Exception {
        //案件列表
        List<CaseInfo> caseInfoObjList = new ArrayList<>();
        //流转记录列表
        List<CaseTurnRecord> caseTurnRecordList = new ArrayList<>();
        List<CaseRepair> caseRepairList = new ArrayList<>();
        //选择的案件ID列表
        List<String> caseInfoList = accCaseInfoDisModel.getCaseIdList();
        //被分配的案件ID列表
        List<String> caseInfoAlready = new ArrayList<>();
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
                CaseInfoDistributed caseInfoDistributed = caseInfoDistributedRepository.findOne(caseId);
                if (Objects.nonNull(caseInfoDistributed)) {
                    CaseInfo caseInfo = new CaseInfo();
                    BeanUtils.copyProperties(caseInfoDistributed, caseInfo);
                    caseInfo.setCaseType(CaseInfo.CaseType.DISTRIBUTE.getValue()); //案件类型-案件分配
                    if (Objects.nonNull(department)) {
                        caseInfo.setDepartment(department); //部门
                        try {
                            caseInfoService.setCollectionType(caseInfo, department, null);
                        } catch (Exception e) {
                            throw new RuntimeException(e.getMessage());
                        }
                        caseInfo.setCaseFollowInTime(ZWDateUtil.getNowDateTime()); //案件流入时间
                        caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue()); //催收状态-待分配
                    }
                    if (Objects.nonNull(targetUser)) {
                        caseInfo.setDepartment(targetUser.getDepartment());
                        caseInfo.setCurrentCollector(targetUser);
                        try {
                            caseInfoService.setCollectionType(caseInfo, null, targetUser);
                        } catch (Exception e) {
                            throw new RuntimeException(e.getMessage());
                        }
                        caseInfo.setCaseFollowInTime(ZWDateUtil.getNowDateTime());
                        caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.WAITCOLLECTION.getValue()); //催收状态-待催收
                    }
                    caseInfo.setLeaveCaseFlag(CaseInfo.leaveCaseFlagEnum.NO_LEAVE.getValue()); //留案标识默认-非留案
                    caseInfo.setAssistFlag(CaseInfo.AssistFlag.NO_ASSIST.getValue());
                    //案件剩余天数(结案日期-当前日期)
                    caseInfo.setLeftDays(ZWDateUtil.getBetween(ZWDateUtil.getNowDate(), caseInfo.getCloseDate(), ChronoUnit.DAYS));
                    //案件类型
                    caseInfo.setCaseType(CaseInfo.CaseType.DISTRIBUTE.getValue());
                    caseInfo.setCaseMark(CaseInfo.Color.NO_COLOR.getValue());//打标标记
                    caseInfo.setFollowUpNum(0);//流转次数
                    caseInfo.setOperator(user);
                    caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime());
                    //案件流转记录
                    CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
                    BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
                    caseTurnRecord.setId(null); //主键置空
                    caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
                    caseTurnRecord.setDepartId(caseInfo.getDepartment().getId()); //部门ID
                    if (Objects.nonNull(caseInfo.getCurrentCollector())) { //催收员不为空则是分给催收员
                        caseTurnRecord.setReceiveDeptName(caseInfo.getCurrentCollector().getDepartment().getName()); //接收部门名称
                        caseTurnRecord.setReceiveUserId(caseInfo.getCurrentCollector().getId()); //接收人ID
                        caseTurnRecord.setReceiveUserRealName(caseInfo.getCurrentCollector().getRealName()); //接受人名称
                    } else {
                        caseTurnRecord.setReceiveDeptName(caseInfo.getDepartment().getName());
                    }
                    caseTurnRecord.setCirculationType(3); //流转类型 3-正常流转
                    caseTurnRecord.setOperatorUserName(user.getUserName()); //操作员用户名
                    caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                    caseTurnRecordList.add(caseTurnRecord);

                    //进入案件修复池
                    CaseRepair caseRepair = new CaseRepair();
                    caseRepair.setCaseId(caseInfo);
                    caseRepair.setRepairStatus(CaseRepair.CaseRepairStatus.REPAIRING.getValue());
                    caseRepair.setOperatorTime(ZWDateUtil.getNowDateTime());
                    caseRepair.setCompanyCode(user.getCompanyCode());
                    caseRepairList.add(caseRepair);
                    caseInfo.setId(null);
                    //案件列表
                    caseInfoObjList.add(caseInfo);
                }
                caseInfoAlready.add(caseInfoDistributed.getId());
                alreadyCaseNum = alreadyCaseNum + 1;
            }
        }
        //保存案件信息
        caseInfoRepository.save(caseInfoObjList);
        //保存流转记录
        caseTurnRecordRepository.save(caseTurnRecordList);
        //保存修复信息
        caseRepairRepository.save(caseRepairList);
        //删除待分配案件
        for (String id : caseInfoAlready) {
            caseInfoDistributedRepository.delete(id);
        }
    }

    public void manualAllocation(ManualParams manualParams, User user) {
        try {
            Iterable<CaseInfoDistributed> all = caseInfoDistributedRepository
                    .findAll(QCaseInfoDistributed.caseInfoDistributed.caseNumber.in(manualParams.getCaseNumberList()));
            Iterator<CaseInfoDistributed> iterator = all.iterator();
            List<CaseInfo> caseInfoList = new ArrayList<>();
            List<CaseRepair> caseRepairList = new ArrayList<>();
            List<OutsourcePool> outsourcePoolList = new ArrayList<>();
            List<CaseInfoRemark> caseInfoRemarkList = new ArrayList<>();
            Integer type = manualParams.getType();
            //内催
            if (Objects.equals(0, type)) {
                while (iterator.hasNext()) {
                    CaseInfoDistributed next = iterator.next();
                    CaseInfo caseInfo = new CaseInfo();
                    setCaseInfo(next, caseInfo, user);
                    caseInfo.setCasePoolType(CaseInfo.CasePoolType.INNER.getValue());
                    caseInfoList.add(caseInfo);
                    addCaseRepair(caseRepairList, caseInfo, user);//修复池增加案件
                }
            }
            //委外
            if (Objects.equals(1, type)) {
                while (iterator.hasNext()) {
                    CaseInfoDistributed next = iterator.next();
                    CaseInfo caseInfo = new CaseInfo();
                    setCaseInfo(next, caseInfo, user);
                    caseInfo.setCasePoolType(CaseInfo.CasePoolType.OUTER.getValue());
                    caseInfoList.add(caseInfo);
                    OutsourcePool outsourcePool = new OutsourcePool();
                    outsourcePool.setCaseInfo(caseInfo);
                    outsourcePool.setOutStatus(OutsourcePool.OutStatus.TO_OUTSIDE.getCode());
                    outsourcePoolList.add(outsourcePool);
                }
            }
            List<CaseInfo> save = caseInfoRepository.save(caseInfoList);
            caseRepairRepository.save(caseRepairList);
            outsourcePoolRepository.save(outsourcePoolList);
            caseInfoRemarkRepository.save(caseInfoRemarkList);
            if (!save.isEmpty()) {
                for (CaseInfo caseInfo : save) {
                    addCaseInfoRemark(caseInfoRemarkList, caseInfo, user);
                }
                caseInfoRemarkRepository.save(caseInfoRemarkList);
            }
            caseInfoDistributedRepository.delete(all);
        } catch (BeansException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("分配失败!");
        }
    }

    private void addCaseInfoRemark(List<CaseInfoRemark> caseInfoRemarkList, CaseInfo caseInfo, User user) {
        CaseInfoRemark caseInfoRemark = new CaseInfoRemark();
        caseInfoRemark.setCaseId(caseInfo.getId());
        caseInfoRemark.setRemark(caseInfo.getMemo());
        caseInfoRemark.setCompanyCode(caseInfo.getCompanyCode());
        caseInfoRemark.setOperatorRealName(user.getRealName());
        caseInfoRemark.setOperatorUserName(user.getUserName());
        caseInfoRemark.setOperatorTime(new Date());
        caseInfoRemarkList.add(caseInfoRemark);
    }

    private void setCaseInfo(CaseInfoDistributed caseInfoDistributed, CaseInfo caseInfo, User user) {
        BeanUtils.copyProperties(caseInfoDistributed, caseInfo);
        caseInfo.setId(null);
        caseInfo.setCaseType(CaseInfo.CaseType.DISTRIBUTE.getValue()); //案件类型-案件分配
        caseInfo.setCaseFollowInTime(ZWDateUtil.getNowDateTime()); //案件流入时间
        caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue()); //催收状态-待分配
        caseInfo.setLeaveCaseFlag(CaseInfo.leaveCaseFlagEnum.NO_LEAVE.getValue()); //留案标识默认-非留案
        caseInfo.setAssistFlag(CaseInfo.AssistFlag.NO_ASSIST.getValue());
        caseInfo.setLeftDays(ZWDateUtil.getBetween(ZWDateUtil.getNowDate(), caseInfo.getCloseDate(), ChronoUnit.DAYS));//案件剩余天数(结案日期-当前日期)
        caseInfo.setCaseMark(CaseInfo.Color.NO_COLOR.getValue());//打标标记
        caseInfo.setFollowUpNum(0);//流转次数
        caseInfo.setOperator(user);
        caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime());
    }

    private void addCaseTurnRecord(List<CaseTurnRecord> caseTurnRecordList, CaseInfo caseInfo, User user) {
        CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
        BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
        caseTurnRecord.setId(null); //主键置空
        caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
        caseTurnRecord.setCirculationType(3); //流转类型 3-正常流转
        caseTurnRecord.setOperatorUserName(user.getUserName()); //操作员用户名
        caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
        caseTurnRecordList.add(caseTurnRecord);
    }

    private void addCaseRepair(List<CaseRepair> caseRepairList, CaseInfo caseInfo, User user) {
        CaseRepair caseRepair = new CaseRepair();
        caseRepair.setCaseId(caseInfo);
        caseRepair.setRepairStatus(CaseRepair.CaseRepairStatus.REPAIRING.getValue());
        caseRepair.setOperatorTime(ZWDateUtil.getNowDateTime());
        caseRepair.setOperator(user);
        caseRepair.setCompanyCode(user.getCompanyCode());
        caseRepairList.add(caseRepair);
    }

    public AllocationCountModel allocationCount(ManualParams manualParams) {
        if (Objects.isNull(manualParams.getCaseNumberList()) || manualParams.getCaseNumberList().isEmpty()) {
            throw new RuntimeException("请先选择要分配的案件");
        }
        if (Objects.isNull(manualParams.getType())) {
            throw new RuntimeException("请选择要分给委外/内催");
        }
        try {
            List<Object[]> obj = caseInfoDistributedRepository.allocationCount(manualParams.getCaseNumberList());
            Object[] objects = obj.get(0);
            BigInteger caseTotal = (BigInteger) objects[0];
            BigDecimal caseAmount = (objects[1] == null) ? new BigDecimal(0) : (BigDecimal) objects[1];
            AllocationCountModel model = new AllocationCountModel();
            model.setCaseTotal(caseTotal);
            model.setCaseAmount(caseAmount);
            return model;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("统计案件信息错误!");
        }
    }

    /**
     * 案件导入策略分案
     *
     * @param model 选择的案件
     * @param user  用户
     * @return
     */
    public void strategyAllocation(CountStrategyAllocationModel model, User user) {
        List<CountAllocationModel> modelList = model.getModelList();

        // 策略分配
        List<CaseInfoDistributed> caseInfoDistributedList = new ArrayList<>();
        List<CaseInfo> caseInfoList = new ArrayList<>(); // 分配到CaseInfo的案件
        List<CaseRepair> caseRepairList = new ArrayList<>(); // 分配到内催时添加修复池也新增
        List<OutsourcePool> outsourcePoolList = new ArrayList<>(); // 分到委外时需要OutsourcePool新增
        List<CaseInfoRemark> caseInfoRemarkList = new ArrayList<>(); // 分配时备注表新增
        for (CountAllocationModel aModel : modelList) {
            List<CaseInfoDistributed> all = caseInfoDistributedRepository.findAll(aModel.getIds());
            if (Objects.equals(aModel.getType(), 0)) { // 内催
                for (CaseInfoDistributed caseInfoDistributed : all) {
                    CaseInfo caseInfo = new CaseInfo();
                    setCaseInfo(caseInfoDistributed, caseInfo, user);
                    caseInfo.setCasePoolType(CaseInfo.CasePoolType.INNER.getValue());
                    caseInfoList.add(caseInfo);
                    addCaseRepair(caseRepairList, caseInfo, user);//修复池增加案件
                    caseInfoDistributedList.add(caseInfoDistributed);
                }
            }
            if (Objects.equals(aModel.getType(), 1)) { // 委外
                for (CaseInfoDistributed caseInfoDistributed : all) {
                    CaseInfo caseInfo = new CaseInfo();
                    setCaseInfo(caseInfoDistributed, caseInfo, user);
                    caseInfo.setCasePoolType(CaseInfo.CasePoolType.OUTER.getValue());
                    caseInfoList.add(caseInfo);
                    OutsourcePool outsourcePool = new OutsourcePool();
                    outsourcePool.setCaseInfo(caseInfo);
                    outsourcePool.setOutStatus(OutsourcePool.OutStatus.TO_OUTSIDE.getCode());
                    outsourcePoolList.add(outsourcePool);
                    caseInfoDistributedList.add(caseInfoDistributed);
                }
            }
        }
        List<CaseInfo> save = caseInfoRepository.save(caseInfoList);
        caseRepairRepository.save(caseRepairList);
        outsourcePoolRepository.save(outsourcePoolList);
        if (!save.isEmpty()) {
            for (CaseInfo caseInfo : save) {
                addCaseInfoRemark(caseInfoRemarkList, caseInfo, user);
            }
            caseInfoRemarkRepository.save(caseInfoRemarkList);
        }
        caseInfoDistributedRepository.delete(caseInfoDistributedList);
    }

    /**
     * 统计策略分配情况
     *
     * @param caseInfoIdList 选择的案件
     * @param user           用户
     * @return
     */
    public CountStrategyAllocationModel countStrategyAllocation(CaseInfoIdList caseInfoIdList, User user) {
        List<CaseInfoDistributed> all = new ArrayList<>();
        if (Objects.isNull(caseInfoIdList.getIds()) || caseInfoIdList.getIds().isEmpty()) {
            all = caseInfoDistributedRepository.findAll();
        } else {
            all = caseInfoDistributedRepository.findAll(caseInfoIdList.getIds());
        }
        if (all.isEmpty()) {
            throw new RuntimeException("待分配案件为空!");
        }
        ResponseEntity<List<CaseStrategy>> forEntity = null;
        try {
            ParameterizedTypeReference<List<CaseStrategy>> responseType = new ParameterizedTypeReference<List<CaseStrategy>>() {
            };
            forEntity = restTemplate.exchange(Constants.CASE_STRATEGY_URL
                    .concat("companyCode=").concat(user.getCompanyCode())
                    .concat("&strategyType=").concat(CaseStrategy.StrategyType.IMPORT.getValue().toString()), HttpMethod.GET, null, responseType);
        } catch (RestClientException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("获取策略错误");
        }
        List<CaseStrategy> caseStrategies = forEntity.getBody();
        if (caseStrategies.isEmpty()) {
            throw new RuntimeException("未找到需要执行的策略");
        }
        // 策略分配
        CountStrategyAllocationModel model = new CountStrategyAllocationModel();
        CountAllocationModel modelInner = new CountAllocationModel();
        modelInner.setType(0);
        CountAllocationModel modelOuter = new CountAllocationModel();
        modelOuter.setType(1);
        for (CaseStrategy caseStrategy : caseStrategies) {
            List<CaseInfoDistributed> checkedList = new ArrayList<>(); // 策略匹配到的案件
            KieSession kieSession = null;
            try {
                kieSession = runCaseStrategyService.runCaseRule(checkedList, caseStrategy,Constants.CASE_INFO_DISTRIBUTE_RULE);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e.getMessage());
            }
            for (CaseInfoDistributed caseInfoDistributed : all) {
                kieSession.insert(caseInfoDistributed);//插入
                kieSession.fireAllRules();//执行规则
            }
            kieSession.dispose();
            if (checkedList.isEmpty()) {
                continue;
            }
            if (Objects.equals(caseStrategy.getAssignType(), 2)) { // 内催
                for (CaseInfoDistributed caseInfoDistributed : checkedList) {
                    countAllocation(caseInfoDistributed, modelInner);
                }
            }
            if (Objects.equals(caseStrategy.getAssignType(), 3)) { // 委外
                for (CaseInfoDistributed caseInfoDistributed : checkedList) {
                    countAllocation(caseInfoDistributed, modelOuter);
                }
            }
            all.removeAll(checkedList);
        }
        List<CountAllocationModel> modelList = model.getModelList();
        modelList.add(modelInner);
        modelList.add(modelOuter);
        model.setModelList(modelList);
        return model;
    }

    private void countAllocation(CaseInfoDistributed caseInfoDistributed, CountAllocationModel model) {
        Integer total = model.getTotal();
        model.setTotal(++total);
        BigDecimal amount = model.getAmount();
        model.setAmount(amount.add(caseInfoDistributed.getOverdueAmount()));
        List<String> ids = model.getIds();
        ids.add(caseInfoDistributed.getId());
        model.setIds(ids);
    }

    public CaseStrategy previewResult(String jsonString) {
        StringBuilder sb = new StringBuilder();
        try {
            String jsonText = runCaseStrategyService.analysisRule(jsonString, sb);
            CaseStrategy caseStrategy = new CaseStrategy();
            caseStrategy.setId(UUID.randomUUID().toString());
            caseStrategy.setStrategyText(jsonText);
            return caseStrategy;
        } catch (Exception e) {
            throw new RuntimeException("策略解析失败!");
        }
    }
}
