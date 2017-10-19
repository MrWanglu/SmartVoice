package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.OutDistributeInfo;
import cn.fintecher.pangolin.business.model.OutsourceInfo;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.OutsourcePoolRepository;
import cn.fintecher.pangolin.business.repository.OutsourceRecordRepository;
import cn.fintecher.pangolin.business.repository.OutsourceRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.strategy.CaseStrategy;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.LabelValue;
import cn.fintecher.pangolin.util.ZWDateUtil;
import freemarker.template.Configuration;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by huyanmin on 2017/10/11.
 * Description: 委外池service
 */
@Service("outsourcePoolService")
public class OutsourcePoolService {
    final Logger log = LoggerFactory.getLogger(OutsourcePoolService.class);

    //案件批次号最大99999（5位）
    public final static String CASE_SEQ = "caseSeq";
    @Inject
    CaseInfoRepository caseInfoRepository;
    @Inject
    OutsourceRepository outsourceRepository;
    @Inject
    OutsourcePoolRepository outsourcePoolRepository;
    @Autowired
    private BatchSeqService batchSeqService;

    @Autowired
    OutsourceRecordRepository outsourceRecordRepository;
    @Inject
    private Configuration freemarkerConfiguration;
    @Autowired
    RestTemplate restTemplate;

    @Inject
    RunCaseStrategyService runCaseStrategyService;


    /**
     * @Description 获取委外案件信息
     */
    public List<OutDistributeInfo> distributePreview(OutsourceInfo outsourceInfo) {

        //选择的案件ID列表
        List<String> caseInfoList = outsourceInfo.getOutCaseIds();
        List<CaseInfo> caseInfoYes = new ArrayList<>(); //可分配案件
        for (String caseId : caseInfoList) {
            OutsourcePool outsourcePool = outsourcePoolRepository.findOne(QOutsourcePool.outsourcePool.caseInfo.id.eq(caseId));
            CaseInfo caseInfo = caseInfoRepository.findOne(caseId);
            if (Objects.isNull(outsourcePool) || Objects.isNull(caseInfo)) {
                throw new RuntimeException("有案件未找到!");
            }
            caseInfoYes.add(caseInfo);
        }
        //案件列表
        List<CaseInfo> caseInfoObjList = new ArrayList<>();
        //每个委外方分配的数量
        List<Integer> disNumList = outsourceInfo.getDistributionCount();
        List<OutDistributeInfo> list = new ArrayList<>();
        //已经分配的案件数量
        int alreadyCaseNum = 0;
        //接收委外方列表信息
        List<String> outsourceList = outsourceInfo.getOutId();

        //按数量分配
        Integer rule = outsourceInfo.getIsNumAvg();

        //平均分配案件数，如果无法平均，则依次分配
        if (Objects.equals(rule, 1)) {
            int caseNum = caseInfoYes.size();
            int outsourceNum = outsourceList.size();
            List<Integer> caseNumList = new ArrayList<>(outsourceNum);
            for (int i = 0; i < outsourceNum; i++) {
                caseNumList.add(caseNum / outsourceNum);
            }
            if (caseNum % outsourceNum != 0) {
                for (int i = 0; i < caseNum % outsourceNum; i++) {
                    caseNumList.set(i, caseNumList.get(i) + 1);
                }
            }
            disNumList = caseNumList;
        }
        for (int i = 0; i < (outsourceList != null ? outsourceList.size() : 0); i++) {
            if (alreadyCaseNum == caseInfoYes.size()) {
                return list;
            }
            String outsourceId = outsourceList.get(i);
            OutDistributeInfo outDistributeInfo = new OutDistributeInfo();
            if (Objects.equals(rule, 1)) {
                outDistributeInfo.setCaseDistributeCount(disNumList.get(i));
            }
            Outsource outsource = outsourceRepository.findOne(outsourceId);
            outDistributeInfo.setOutCode(outsource.getOutsCode());
            outDistributeInfo.setOutName(outsource.getOutsName());
            outDistributeInfo.setCollectionCount(outsourcePoolRepository.getOutsourceCaseCount(outsourceId));
            outDistributeInfo.setCollectionAmt(outsourcePoolRepository.getOutsourceAmtCount(outsourceId));
            if (Objects.equals(rule, 0)) {
                alreadyCaseNum = alreadyCaseNum + 1;
            } else {
                if (Objects.nonNull(disNumList)) {
                    //需要分配的案件数据
                    Integer disNumber = disNumList.get(i);
                    for (int j = 0; j < disNumber; j++) {
                        //检查输入的案件数量是否和选择的案件数量一致
                        String caseId = caseInfoYes.get(alreadyCaseNum).getId();
                        OutsourcePool outsourcePool = outsourcePoolRepository.findOne(QOutsourcePool.outsourcePool.caseInfo.id.eq(caseId));
                        CaseInfo caseInfo = caseInfoRepository.findOne(caseId);
                        if (Objects.nonNull(outsourcePool) && Objects.nonNull(caseInfo)) {
                            if(Objects.nonNull(caseInfo.getRealPayAmount())){
                                caseInfo.setRealPayAmount(BigDecimal.ZERO);
                            }
                            BigDecimal amount = caseInfo.getOverdueAmount().subtract(caseInfo.getRealPayAmount()); //案件需要委外的金额
                            outDistributeInfo.setCaseDistributeMoneyCount(outDistributeInfo.getCaseDistributeMoneyCount().add(amount));
                        }
                        alreadyCaseNum = alreadyCaseNum + 1;
                    }
                }

            }
            outDistributeInfo.setCaseTotalCount(outDistributeInfo.getCollectionCount() + outDistributeInfo.getCaseDistributeCount());
            outDistributeInfo.setCaseMoneyTotalCount(outDistributeInfo.getCollectionAmt().add(outDistributeInfo.getCaseDistributeMoneyCount()));
            list.add(outDistributeInfo);
        }
        return list;
    }

    /**
     * 委外待分配案件分配
     *
     * @param outsourceInfo
     * @param user
     * @throws Exception
     */
    @Transactional
    public void distributeCeaseInfo(OutsourceInfo outsourceInfo, User user) {
        //选择的案件ID列表
        List<String> caseInfoList = outsourceInfo.getOutCaseIds();
        //检查案件状态（待分配 催收中 已结案）
        List<OutsourcePool> caseInfoYes = new ArrayList<>(); //可分配案件
        for (String caseId : caseInfoList) {
            OutsourcePool outsourcePool = outsourcePoolRepository.findOne(QOutsourcePool.outsourcePool.caseInfo.id.eq(caseId));
            if (Objects.isNull(outsourcePool)) {
                throw new RuntimeException("有案件未找到!");
            }
            caseInfoYes.add(outsourcePool);
        }
        List<OutsourcePool> outsourcePoolList = new ArrayList<>();//用于批量保存已分配出去案件的空盒子
        List<OutsourceRecord> outsourceRecords = new ArrayList<>();//待保存的案件委外记录集合
        //每个委外方分配的数量
        List<Integer> disNumList = outsourceInfo.getDistributionCount();
        //已经分配的案件数量
        int alreadyCaseNum = 0;
        //接收委外方列表信息
        List<String> outsourceList = outsourceInfo.getOutId();
        Integer isDebt = outsourceInfo.getIsDebt();
        LabelValue seqResult = batchSeqService.nextSeq(CASE_SEQ, 5);
        String ouorBatch = seqResult.getValue();
        if (Objects.equals(isDebt, 1)) {
            for (int i = 0; i < caseInfoYes.size(); i++) {
                OutsourcePool outsourcePool = caseInfoYes.get(i);
                CaseInfo caseInfo = caseInfoRepository.findOne(outsourcePool.getCaseInfo().getId());
                String personalName = caseInfo.getPersonalInfo().getName();
                String idCard = caseInfo.getPersonalInfo().getIdCard();
                String companyCode=null;
                if(Objects.nonNull(user.getCompanyCode())){
                    companyCode=user.getCompanyCode();
                }
                Object[] nums = (Object[])outsourcePoolRepository.getGzNum(personalName, idCard, companyCode);
                if (Objects.nonNull(nums)) {
                    Outsource outsource = outsourceRepository.findOne(Objects.isNull(nums[0].toString())?null:nums[0].toString());
                    if (Objects.nonNull(outsource)) {
                        //优先将案件委外给有共债案件的委外方
                        setOutsourcePool(outsourcePool, outsource, ouorBatch, user, outsourcePoolList);
                        //添加委外记录
                        saveOutsourceRecord(outsourcePool, outsource, user, ouorBatch, outsourceRecords);
                        caseInfoYes.remove(outsourcePool);
                    }
                    i--;
                    continue;
                }
            }
            //保存共债分配的案件
            outsourcePoolRepository.save(outsourcePoolList);//批量保存分配的案子
            outsourceRecordRepository.save(outsourceRecords);
        }
        //按数量分配
        Integer rule = outsourceInfo.getIsNumAvg();
        //平均分配案件数，如果无法平均，则依次分配
        if (Objects.equals(rule, 1)) {
            int caseNum = caseInfoYes.size();
            int outsourceNum = outsourceList.size();
            List<Integer> caseNumList = new ArrayList<>(outsourceNum);
            for (int i = 0; i < outsourceNum; i++) {
                caseNumList.add(caseNum / outsourceNum);
            }
            if (caseNum % outsourceNum != 0) {
                for (int i = 0; i < caseNum % outsourceNum; i++) {
                    caseNumList.set(i, caseNumList.get(i) + 1);
                }
            }
            disNumList = caseNumList;
        }

        for (int i = 0; i < (outsourceList != null ? outsourceList.size() : 0); i++) {
            String outsourceId = outsourceList.get(i);
            Outsource outsource = outsourceRepository.findOne(outsourceId);
            //需要分配的案件数据
            Integer disNum = disNumList.get(i);
            for (int j = 0; j < disNum; j++) {
                //检查输入的案件数量是否和选择的案件数量一致
                if (alreadyCaseNum == caseInfoYes.size()) {
                    return;
                }
                String outId = caseInfoYes.get(alreadyCaseNum).getId();
                OutsourcePool outsourcePool = outsourcePoolRepository.findOne(outId);
                if(Objects.nonNull(outsourcePool)){
                    setOutsourcePool(outsourcePool, outsource, ouorBatch, user, outsourcePoolList);
                    //添加委外记录
                    saveOutsourceRecord(outsourcePool, outsource, user, ouorBatch, outsourceRecords);
                }
                alreadyCaseNum = alreadyCaseNum + 1;
            }
        }
        outsourcePoolRepository.save(outsourcePoolList);//批量保存分配的案子
        outsourceRecordRepository.save(outsourceRecords);
    }

    /**
     * 分配案件信息
     */
    private void setOutsourcePool(OutsourcePool outsourcePool, Outsource outsource, String ouorBatch, User user, List<OutsourcePool> outsourcePoolList) {
        outsourcePool.setOutsource(outsource);
        outsourcePool.setOutBatch(ouorBatch);
        outsourcePool.setOperateTime(ZWDateUtil.getNowDateTime());
        outsourcePool.setOperator(user.getUserName());
        outsourcePool.setOutStatus(OutsourcePool.OutStatus.OUTSIDING.getCode());
        outsourcePool.setOutTime(ZWDateUtil.getNowDateTime());
        BigDecimal b2 = outsourcePool.getCaseInfo().getRealPayAmount();//实际还款金额
        if (Objects.isNull(b2)) {
            outsourcePool.getCaseInfo().setRealPayAmount(BigDecimal.ZERO);
        }
        BigDecimal b1 = outsourcePool.getCaseInfo().getOverdueAmount();//原案件金额
        outsourcePool.setContractAmt(b1.subtract(b2));//委外案件金额=原案件金额-实际还款金额
        outsourcePool.setOverduePeriods(outsourcePool.getOverduePeriods());//逾期时段
        /*GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(ZWDateUtil.getNowDateTime());
        gc.add(2, 3);
        outsourcePool.setOverOutsourceTime(gc.getTime());*/
        if (Objects.nonNull(user.getCompanyCode())) {
            outsourcePool.setCompanyCode(user.getCompanyCode());
        }
        outsourcePoolList.add(outsourcePool);
    }

    /**
     * 添加委外记录
     */
    private void saveOutsourceRecord(OutsourcePool outsourcePool, Outsource outsource, User user, String ouorBatch, List<OutsourceRecord> outsourceRecords) {
        //委外记录
        OutsourceRecord outsourceRecord = new OutsourceRecord();
        outsourceRecord.setCaseInfo(outsourcePool.getCaseInfo());
        outsourceRecord.setOutsource(outsource);
        outsourceRecord.setCreateTime(ZWDateUtil.getNowDateTime());
        outsourceRecord.setCreator(user.getUserName());
        outsourceRecord.setFlag(0);//默认正常
        outsourceRecord.setOuorBatch(ouorBatch);//批次号
        outsourceRecords.add(outsourceRecord);
    }

    /**
     * 委外 策略分配
     *
     * @param caseStrategies 全部的策略
     * @param caseInfos      全部的案件
     */
    public List<OutDistributeInfo> outerStrategyDistribute(List<CaseStrategy> caseStrategies, List<CaseInfo> caseInfos, User user) throws Exception {
        List<OutDistributeInfo> list = new ArrayList<>();
        List<OutDistributeInfo> result = new ArrayList<>();
        for (CaseStrategy caseStrategy : caseStrategies) {
            List<CaseInfo> checkedList = new ArrayList<>(); // 策略匹配到的案件
            KieSession kieSession = null;
            try {
                kieSession = runCaseStrategyService.runCaseRule(checkedList, caseStrategy, Constants.CASE_INFO_RULE);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e.getMessage());
            }
            for (CaseInfo caseInfo : caseInfos) {
                kieSession.insert(caseInfo);//插入
                kieSession.fireAllRules();//执行规则
            }
            kieSession.dispose();
            if (checkedList.isEmpty()) {
                continue;
            }
            List<String> caseIds = new ArrayList<>();
            List<Integer> caseNumList = new ArrayList<>();
            checkedList.forEach(e -> caseIds.add(e.getId()));
            OutsourceInfo outsourceInfo = new OutsourceInfo();
            outsourceInfo.setOutCaseIds(caseIds);
            outsourceInfo.setOutId(caseStrategy.getOutsource());
            outsourceInfo.setDistributionCount(caseNumList);
            outsourceInfo.setIsDebt(0);
            outsourceInfo.setIsNumAvg(1);
            list.addAll(distributePreview(outsourceInfo));
            distributeCeaseInfo(outsourceInfo, user);
            caseInfos.removeAll(checkedList);
        }

        if(!list.isEmpty()){
            result = new ArrayList<>();
            setModelValue(list, result);
        }
        return result;
    }

    private void setModelValue(List<OutDistributeInfo> infoInnerDistributeDepartModels, List<OutDistributeInfo> newDistributeModel) {
        for (OutDistributeInfo oldDistributeModel : infoInnerDistributeDepartModels) {
            boolean state = false;
            for (OutDistributeInfo newDistributeModelTemp : newDistributeModel) {
                if (newDistributeModelTemp.getOutName().equals(oldDistributeModel.getOutName())) {
                    //当前
                    Integer collectionCount = newDistributeModelTemp.getCollectionCount();
                    collectionCount += oldDistributeModel.getCollectionCount();
                    newDistributeModelTemp.setCollectionCount(collectionCount);
                    BigDecimal collectionAmt = newDistributeModelTemp.getCollectionAmt();
                    collectionAmt = collectionAmt.add(oldDistributeModel.getCollectionAmt());
                    newDistributeModelTemp.setCollectionAmt(collectionAmt);
                    //刚才分配的
                    Integer caseDistributeCount = newDistributeModelTemp.getCaseDistributeCount();
                    caseDistributeCount += oldDistributeModel.getCaseDistributeCount();
                    newDistributeModelTemp.setCaseDistributeCount(caseDistributeCount);
                    BigDecimal caseDistributeMoneyCount = newDistributeModelTemp.getCaseDistributeMoneyCount();
                    caseDistributeMoneyCount = caseDistributeMoneyCount.add(oldDistributeModel.getCaseDistributeMoneyCount());
                    newDistributeModelTemp.setCaseDistributeMoneyCount(caseDistributeMoneyCount);
                    //最后的
                    Integer caseTotalCount = newDistributeModelTemp.getCaseTotalCount();
                    caseTotalCount += oldDistributeModel.getCaseTotalCount();
                    newDistributeModelTemp.setCaseTotalCount(caseTotalCount);
                    BigDecimal caseMoneyTotalCount = newDistributeModelTemp.getCaseMoneyTotalCount();
                    caseMoneyTotalCount = caseMoneyTotalCount.add(oldDistributeModel.getCaseMoneyTotalCount());
                    newDistributeModelTemp.setCaseMoneyTotalCount(caseMoneyTotalCount);
                    state = true;
                }
            }
            if (!state) {
                newDistributeModel.add(oldDistributeModel);
            }
        }
    }
}
