package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.*;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.strategy.CaseStrategy;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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

    @Inject
    CaseDistributedTemporaryRepository caseDistributedTemporaryRepository;

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

    /**
     * 案件导入手工分案/待分配回收案件批量分配
     * @param manualParams
     * @param user
     */
    public void manualAllocation(ManualParams manualParams, User user) {
        try {
            Iterable<CaseInfoDistributed> all = caseInfoDistributedRepository
                    .findAll(QCaseInfoDistributed.caseInfoDistributed.caseNumber.in(manualParams.getCaseNumberList()));
            Iterator<CaseInfoDistributed> iterator = all.iterator();
            List<CaseInfo> caseInfoList = new ArrayList<>();
            List<CaseRepair> caseRepairList = new ArrayList<>();
            List<OutsourcePool> outsourcePoolList = new ArrayList<>();
            List<CaseInfoRemark> caseInfoRemarkList = new ArrayList<>();
            List<CaseDistributedTemporary> caseDistributedTemporaryList = new ArrayList<>();
            Integer type = manualParams.getType();
            //内催
            if (Objects.equals(0, type)) {
                while (iterator.hasNext()) {
                    CaseInfoDistributed next = iterator.next();
                    next.setRecoverRemark(CaseInfo.RecoverRemark.NOT_RECOVERED.getValue());
                    CaseInfo caseInfo = new CaseInfo();
                    setCaseInfo(next, caseInfo, user, manualParams.getCloseDate());
                    caseInfo.setCasePoolType(CaseInfo.CasePoolType.INNER.getValue());
                    caseInfoList.add(caseInfo);
                    addCaseRepair(caseRepairList, caseInfo, user);//修复池增加案件
                }
            }
            //委外
            if (Objects.equals(1, type)) {
                while (iterator.hasNext()) {
                    CaseInfoDistributed next = iterator.next();
                    next.setRecoverRemark(CaseInfo.RecoverRemark.NOT_RECOVERED.getValue());
                    CaseInfo caseInfo = new CaseInfo();
                    setCaseInfo(next, caseInfo, user, manualParams.getCloseDate());
                    caseInfo.setCasePoolType(CaseInfo.CasePoolType.OUTER.getValue());
                    caseInfoList.add(caseInfo);
                    OutsourcePool outsourcePool = new OutsourcePool();
                    outsourcePool.setCaseInfo(caseInfo);
                    outsourcePool.setCompanyCode(caseInfo.getCompanyCode());
                    outsourcePool.setOutStatus(OutsourcePool.OutStatus.TO_OUTSIDE.getCode());
                    outsourcePool.setOverOutsourceTime(caseInfo.getCloseDate());
                    outsourcePool.setOverduePeriods(caseInfo.getPayStatus());
                    outsourcePoolList.add(outsourcePool);
                }
            }
            List<CaseInfo> save = caseInfoRepository.save(caseInfoList);
            List<CaseRepair> save2 = caseRepairRepository.save(caseRepairList);
            List<OutsourcePool> save1 = outsourcePoolRepository.save(outsourcePoolList);
            caseDistributedTemporary(save, save1, save2, caseInfoRemarkList,user,caseDistributedTemporaryList);
            caseDistributedTemporaryRepository.save(caseDistributedTemporaryList);
            caseInfoDistributedRepository.delete(all);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("分配失败!");
        }
    }

    private CaseDistributedTemporary addCaseInfoDistributeTemp(CaseInfo caseInfo, String caseRepairId, String remarkId, OutsourcePool outsourcePool, User user, Integer type) {
        //新增一条分配临时记录
        CaseDistributedTemporary caseDistributedTemporary = new CaseDistributedTemporary();
        if (Objects.equals(type, CaseInfo.CasePoolType.INNER.getValue())) {
            caseDistributedTemporary.setCaseId(caseInfo.getId()); //案件ID
        }
        if (Objects.equals(type, CaseInfo.CasePoolType.OUTER.getValue())) {
            caseDistributedTemporary.setCaseId(outsourcePool.getId()); //案件ID
        }
        caseDistributedTemporary.setCaseNumber(caseInfo.getCaseNumber()); //案件编号
        caseDistributedTemporary.setBatchNumber(caseInfo.getBatchNumber()); //批次号
        caseDistributedTemporary.setPersonalName(caseInfo.getPersonalInfo().getName()); //客户姓名
        caseDistributedTemporary.setCaseRepairId(caseRepairId);
        caseDistributedTemporary.setCaseRemark(remarkId);//案件备注
        caseDistributedTemporary.setOverdueAmt(caseInfo.getOverdueAmount()); //案件金额
        caseDistributedTemporary.setPrincipalName(caseInfo.getPrincipalId().getName()); //委托方名称
        caseDistributedTemporary.setType(CaseDistributedTemporary.Type.BIG_IN.getValue()); //分案类型
        caseDistributedTemporary.setCompanyCode(caseInfo.getCompanyCode()); //公司code码
        caseDistributedTemporary.setOperatorUserName(user.getUserName()); //操作人用户名
        caseDistributedTemporary.setOperatorRealName(user.getRealName()); //操作人姓名
        caseDistributedTemporary.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
        return caseDistributedTemporary;
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

    private void setCaseInfo(CaseInfoDistributed caseInfoDistributed, CaseInfo caseInfo, User user, Date closeDate) {
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
        if (Objects.nonNull(closeDate)) {
            caseInfo.setCloseDate(closeDate);
        }
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
        caseRepair.setCompanyCode(caseInfo.getCompanyCode());
        caseRepairList.add(caseRepair);
    }

    /**
     * 案件导入手工分案统计
     * @param manualParams
     * @return
     */
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
    public void strategyAllocation(CaseInfoStrategyModel model, User user) {
        try {
            List<CaseInfoStrategyResultModel> modelList = model.getModelList();

            // 策略分配
            List<CaseInfoDistributed> caseInfoDistributedList = new ArrayList<>();
            // 分配到CaseInfo的案件
            List<CaseInfo> caseInfoList = new ArrayList<>();
            // 分配到内催时添加修复池也新增
            List<CaseRepair> caseRepairList = new ArrayList<>();
            // 分配时备注表新增
            List<CaseInfoRemark> caseInfoRemarkList = new ArrayList<>();
            // 流转记录
            List<CaseTurnRecord> caseTurnRecordList = new ArrayList<>();
            for (CaseInfoStrategyResultModel aModel : modelList) {
                List<CaseInfoDistributed> all = caseInfoDistributedRepository.findAll(aModel.getDistributeIds());
                String username = aModel.getUsername();
                if (StringUtils.isNotBlank(username)) {
                    for (CaseInfoDistributed caseInfoDistributed : all) {
                        CaseInfo caseInfo = new CaseInfo();
                        BeanUtils.copyProperties(caseInfoDistributed, caseInfo);
                        caseInfo.setId(null);
                        caseInfo.setCaseType(CaseInfo.CaseType.DISTRIBUTE.getValue()); //案件类型-案件分配
                        caseInfo.setCaseFollowInTime(ZWDateUtil.getNowDateTime()); //案件流入时间
                        caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.WAITCOLLECTION.getValue()); //催收状态-待催收 //催收状态-待分配
                        caseInfo.setLeaveCaseFlag(CaseInfo.leaveCaseFlagEnum.NO_LEAVE.getValue()); //留案标识默认-非留案
                        caseInfo.setAssistFlag(CaseInfo.AssistFlag.NO_ASSIST.getValue());
                        caseInfo.setLeftDays(ZWDateUtil.getBetween(ZWDateUtil.getNowDate(), caseInfo.getCloseDate(), ChronoUnit.DAYS));//案件剩余天数(结案日期-当前日期)
                        caseInfo.setCaseMark(CaseInfo.Color.NO_COLOR.getValue());//打标标记
                        caseInfo.setFollowUpNum(0);//流转次数
                        caseInfo.setOperator(user);
                        caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime());
                        User user1 = userRepository.findByUserName(username);
                        caseInfo.setCurrentCollector(user1);
                        caseInfo.setCasePoolType(CaseInfo.CasePoolType.INNER.getValue());
                        caseInfo.setDepartment(user1.getDepartment());
                        caseInfo.setLeaveCaseFlag(CaseInfo.leaveCaseFlagEnum.NO_LEAVE.getValue()); //留案标识默认-非留案
                        caseInfo.setAssistFlag(CaseInfo.AssistFlag.NO_ASSIST.getValue());
                        caseInfo.setPromiseAmt(new BigDecimal(0));
                        caseInfoService.setCollectionType(caseInfo, null, user1);
                        caseInfoList.add(caseInfo);
                        addCaseRepair(caseRepairList, caseInfo, user);//修复池增加案件
                        addCaseTurnRecord(caseTurnRecordList, caseInfo, user);
                        caseInfoDistributedList.add(caseInfoDistributed);
                    }
                } else {
                    for (CaseInfoDistributed caseInfoDistributed : all) {
                        CaseInfo caseInfo = new CaseInfo();
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
                        caseInfo.setCasePoolType(CaseInfo.CasePoolType.INNER.getValue());
                        Department department = departmentRepository.getOne(aModel.getDepartId());
                        caseInfo.setDepartment(department);
                        caseInfoService.setCollectionType(caseInfo, department, null);
                        caseInfo.setAssistFlag(CaseInfo.AssistFlag.NO_ASSIST.getValue());
                        caseInfo.setPromiseAmt(new BigDecimal(0));
                        caseInfoList.add(caseInfo);
                        addCaseRepair(caseRepairList, caseInfo, user);//修复池增加案件
                        addCaseTurnRecord(caseTurnRecordList, caseInfo, user);
                        caseInfoDistributedList.add(caseInfoDistributed);
                    }
                }
            }
            List<CaseInfo> save = caseInfoRepository.save(caseInfoList);
            caseRepairRepository.save(caseRepairList);
            if (!save.isEmpty()) {
                for (CaseInfo caseInfo : save) {
                    addCaseInfoRemark(caseInfoRemarkList, caseInfo, user);
                }
                caseInfoRemarkRepository.save(caseInfoRemarkList);
            }
            caseInfoDistributedRepository.delete(caseInfoDistributedList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("分配失败");
        }
    }

    private void caseDistributedTemporary(List<CaseInfo> save, List<OutsourcePool> save1, List<CaseRepair> save2, List<CaseInfoRemark> caseInfoRemarkList,
                                          User user, List<CaseDistributedTemporary> caseDistributedTemporaryList) {
        if (!save.isEmpty()) {
            for (CaseInfo caseInfo : save) {
                addCaseInfoRemark(caseInfoRemarkList, caseInfo, user);
            }
            List<CaseInfoRemark> save3 = caseInfoRemarkRepository.save(caseInfoRemarkList);

            for (CaseInfo caseInfo : save) {
                String caseRepairId = null;
                for (CaseRepair caseRepair : save2) {
                    if (Objects.equals(caseRepair.getCaseId().getId(), caseInfo.getId())) {
                        caseRepairId = caseRepair.getId();
                        break;
                    }
                }
                String remarkId = null;
                for (CaseInfoRemark remark : save3) {
                    if (Objects.equals(remark.getCaseId(), caseInfo.getId())) {
                        remarkId = remark.getId();
                        break;
                    }
                }
                if (Objects.equals(caseInfo.getCasePoolType(), CaseInfo.CasePoolType.INNER.getValue())) {
                    CaseDistributedTemporary temp = addCaseInfoDistributeTemp(caseInfo, caseRepairId, remarkId, null, user, CaseInfo.CasePoolType.INNER.getValue());
                    caseDistributedTemporaryList.add(temp);
                }
                if (Objects.equals(caseInfo.getCasePoolType(), CaseInfo.CasePoolType.OUTER.getValue())) {
                    for (OutsourcePool pool : save1) {
                        if (Objects.equals(pool.getCaseInfo().getId(), caseInfo.getId())) {
                            CaseDistributedTemporary temp = addCaseInfoDistributeTemp(caseInfo, caseRepairId, remarkId, pool, user, CaseInfo.CasePoolType.OUTER.getValue());
                            caseDistributedTemporaryList.add(temp);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 统计策略分配情况
     *
     * @param caseInfoIdList 选择的案件
     * @param user           用户
     * @return
     */
    public List<CaseInfoStrategyResultModel> countStrategyAllocation(CaseInfoIdList caseInfoIdList, User user) {
        List<CaseInfoDistributed> all = new ArrayList<>();
        if (Objects.isNull(caseInfoIdList.getIds()) || caseInfoIdList.getIds().isEmpty()) {
            Iterable<CaseInfoDistributed> all1 = caseInfoDistributedRepository.findAll(QCaseInfoDistributed.caseInfoDistributed.recoverRemark.eq(CaseInfo.RecoverRemark.NOT_RECOVERED.getValue())
                    .and(QCaseInfoDistributed.caseInfoDistributed.companyCode.eq(user.getCompanyCode())));
            all = IterableUtils.toList(all1);
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
                    .concat("&strategyType=").concat(CaseStrategy.StrategyType.INNER.getValue().toString()), HttpMethod.GET, null, responseType);
        } catch (RestClientException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("获取策略错误");
        }
        List<CaseStrategy> caseStrategies = forEntity.getBody();
        if (caseStrategies.isEmpty()) {
            throw new RuntimeException("未找到需要执行的策略");
        }
        // 策略分配
        HashMap<String, CaseInfoStrategyResultModel> modelMap = new LinkedHashMap<>();
        for (CaseStrategy caseStrategy : caseStrategies) {
            List<CaseInfoDistributed> checkedList = new ArrayList<>(); // 策略匹配到的案件
            KieSession kieSession = null;
            try {
                kieSession = runCaseStrategyService.runCaseRule(checkedList, caseStrategy, Constants.CASE_INFO_DISTRIBUTE_RULE);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(e.getMessage());
            }
            List<CaseInfoDistributed> distributedList = all;
            Iterator<CaseInfoDistributed> iterator = distributedList.iterator();
            if (StringUtils.isNotBlank(caseStrategy.getStrategyText())) {
                if (caseStrategy.getStrategyText().contains(Constants.STRATEGY_AREA_ID)) {
                    while (iterator.hasNext()) {
                        CaseInfoDistributed next = iterator.next();
                        if (Objects.isNull(next.getArea())) {
                            iterator.remove();
                        }
                    }
                }
                if (caseStrategy.getStrategyText().contains(Constants.STRATEGY_PRODUCT_SERIES)) {
                    while (iterator.hasNext()) {
                        CaseInfoDistributed next = iterator.next();
                        if (Objects.isNull(next.getProduct())) {
                            iterator.remove();
                        } else if (Objects.isNull(next.getProduct().getProductSeries())) {
                            iterator.remove();
                        }
                    }
                }
            }
            for (CaseInfoDistributed caseInfoDistributed : distributedList) {
                kieSession.insert(caseInfoDistributed);
                kieSession.fireAllRules();
            }
            kieSession.dispose();
            if (checkedList.isEmpty()) {
                continue;
            }
            // 策略指定的是部门
            if (Objects.equals(caseStrategy.getAssignType(), CaseStrategy.AssignType.DEPART.getValue())) {
                // 获取到所有部门的ID
                List<String> departments = caseStrategy.getDepartments();
                // 按照部门平均分配
                Integer numAvg = checkedList.size() / departments.size();
                Integer num = checkedList.size() % departments.size();
                int m = 0;
                int n = 0;
                for (int i = 0; i < departments.size(); i++) {
                    String departId = departments.get(i);
                    // 部门所持有案件总数
                    Integer deptCaseCount = caseInfoRepository.getDeptCaseCount(departId);
                    // 部门案件总金额
                    BigDecimal deptCaseAmt = caseInfoRepository.getDeptCaseAmt(departId);
                    if (modelMap.containsKey(departId)) {
                        CaseInfoStrategyResultModel model = modelMap.get(departId);
                        Department one = departmentRepository.getOne(departId);
                        model.setDepartId(departId);
                        model.setDepartName(one.getName());
                        model.setHandNum(deptCaseCount);
                        model.setHandAmount(deptCaseAmt);
                        List<String> distributeIds = model.getDistributeIds();
                        if (num > 0) {
                            model.setDistributeNum(model.getDistributeNum() + numAvg + 1);
                            BigDecimal amount = new BigDecimal(0);
                            for (int k = m; k < m + 1 + numAvg; k++, m++) {
                                amount = amount.add(checkedList.get(k).getOverdueAmount());
                                distributeIds.add(checkedList.get(k).getId());
                            }
                            model.setDistributeAmount(model.getDistributeAmount().add(amount));
                            model.setDistributeIds(distributeIds);
                        } else {
                            model.setDistributeNum(model.getDistributeNum() + numAvg);
                            BigDecimal amount = new BigDecimal(0);
                            for (int k = m; k < m + numAvg; k++, m++) {
                                amount = amount.add(checkedList.get(k).getOverdueAmount());
                                distributeIds.add(checkedList.get(k).getId());
                            }
                            model.setDistributeAmount(model.getDistributeAmount().add(amount));
                            model.setDistributeIds(distributeIds);
                        }
                        modelMap.put(departId, model);
                    } else {
                        CaseInfoStrategyResultModel model = new CaseInfoStrategyResultModel();
                        Department one = departmentRepository.getOne(departId);
                        model.setDepartId(departId);
                        model.setDepartName(one.getName());
                        model.setHandNum(deptCaseCount);
                        model.setHandAmount(deptCaseAmt);
                        List<String> distributeIds = model.getDistributeIds();
                        if (num > 0) {
                            model.setDistributeNum(model.getDistributeNum() + numAvg + 1);
                            BigDecimal amount = new BigDecimal(0);
                            for (int k = m; k < n + 1 + numAvg; k++, m++) {
                                amount = amount.add(checkedList.get(k).getOverdueAmount());
                                distributeIds.add(checkedList.get(k).getId());
                            }
                            n = m;
                            model.setDistributeAmount(model.getDistributeAmount().add(amount));
                            model.setDistributeIds(distributeIds);
                        } else {
                            model.setDistributeNum(model.getDistributeNum() + numAvg);
                            BigDecimal amount = new BigDecimal(0);
                            for (int k = m; k < n + numAvg; k++, m++) {
                                amount = amount.add(checkedList.get(k).getOverdueAmount());
                                distributeIds.add(checkedList.get(k).getId());
                            }
                            n = m;
                            model.setDistributeAmount(model.getDistributeAmount().add(amount));
                            model.setDistributeIds(distributeIds);
                        }
                        modelMap.put(departId, model);
                    }
                }
            }
            // 策略指定的是催收员
            if (Objects.equals(caseStrategy.getAssignType(), CaseStrategy.AssignType.COLLECTOR.getValue())) {
                List<String> users = caseStrategy.getUsers();
                // 按照用户平均分配
                Integer numAvg = checkedList.size() / users.size();
                Integer num = checkedList.size() % users.size();
                int m = 0;
                int n = 0;
                for (int i = 0; i < users.size(); i++) {
                    String userId = users.get(i);
                    // 部门所持有案件总数
                    Integer caseCount = caseInfoRepository.getCaseCount(userId);
                    // 部门案件总金额
                    BigDecimal userCaseAmt = caseInfoRepository.getUserCaseAmt(userId);
                    if (modelMap.containsKey(userId)) {
                        CaseInfoStrategyResultModel model = modelMap.get(userId);
                        User one = userRepository.getOne(userId);
                        Department department = one.getDepartment();
                        model.setDepartId(department.getId());
                        model.setDepartName(department.getName());
                        model.setHandNum(caseCount);
                        model.setHandAmount(userCaseAmt);
                        List<String> distributeIds = model.getDistributeIds();

                        // 将余数分给每个催收员
                        if (num > 0) {
                            model.setDistributeNum(model.getDistributeNum() + numAvg + 1);
                            BigDecimal amount = new BigDecimal(0);
                            for (int k = m; k < n + 1 + numAvg; k++, m++) {
                                amount = amount.add(checkedList.get(k).getOverdueAmount());
                                distributeIds.add(checkedList.get(k).getId());
                            }
                            n = m;
                            model.setDistributeAmount(model.getDistributeAmount().add(amount));
                            model.setDistributeIds(distributeIds);
                        } else {
                            model.setDistributeNum(model.getDistributeNum() + numAvg);
                            BigDecimal amount = new BigDecimal(0);
                            for (int k = m; k < n + numAvg; k++, m++) {
                                amount = amount.add(checkedList.get(k).getOverdueAmount());
                                distributeIds.add(checkedList.get(k).getId());
                            }
                            n = m;
                            model.setDistributeAmount(model.getDistributeAmount().add(amount));
                            model.setDistributeIds(distributeIds);
                        }
                        modelMap.put(userId, model);
                    } else {
                        CaseInfoStrategyResultModel model = new CaseInfoStrategyResultModel();
                        User one = userRepository.getOne(userId);
                        Department department = one.getDepartment();
                        model.setDepartId(department.getId());
                        model.setDepartName(department.getName());
                        model.setHandNum(caseCount);
                        model.setHandAmount(userCaseAmt);
                        model.setUsername(one.getUserName());
                        model.setRealName(one.getRealName());
                        List<String> distributeIds = model.getDistributeIds();
                        if (num > 0) {
                            model.setDistributeNum(model.getDistributeNum() + numAvg + 1);
                            BigDecimal amount = new BigDecimal(0);
                            for (int k = m; k < n + 1 + numAvg; k++, m++) {
                                amount = amount.add(checkedList.get(k).getOverdueAmount());
                                distributeIds.add(checkedList.get(k).getId());
                            }
                            n = m;
                            model.setDistributeAmount(model.getDistributeAmount().add(amount));
                            model.setDistributeIds(distributeIds);
                        } else {
                            model.setDistributeNum(model.getDistributeNum() + numAvg);
                            BigDecimal amount = new BigDecimal(0);
                            for (int k = m; k < n + numAvg; k++, m++) {
                                amount = amount.add(checkedList.get(k).getOverdueAmount());
                                distributeIds.add(checkedList.get(k).getId());
                            }
                            n = m;
                            model.setDistributeAmount(model.getDistributeAmount().add(amount));
                            model.setDistributeIds(distributeIds);
                        }
                        modelMap.put(userId, model);
                    }
                    num--;
                }
            }
            all.removeAll(checkedList);
        }
        List<CaseInfoStrategyResultModel> modelList = new ArrayList<>();
        Iterator<Map.Entry<String, CaseInfoStrategyResultModel>> iterator = modelMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, CaseInfoStrategyResultModel> next = iterator.next();
            modelList.add(next.getValue());
        }
        return modelList;
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
