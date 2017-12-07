package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.exception.GeneralException;
import cn.fintecher.pangolin.business.model.*;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.strategy.CaseStrategy;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.LabelValue;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.mongodb.DBCollection;
import freemarker.template.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.math.BigDecimal;

import java.time.temporal.ChronoUnit;
import java.util.*;

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
    @Autowired
    RestTemplate restTemplate;
    @Inject
    RunCaseStrategyService runCaseStrategyService;
    @Inject
    SysParamRepository sysParamRepository;


    /**
     * @Description 委外待分配按数量平均分配预览
     */
    public PreviewModel distributePreviewByNum(OutsourceInfo outsourceInfo, User user) {

        PreviewModel previewModel = new PreviewModel();
        //包含共债案件的ID列表
        List<String> debtList = new ArrayList<>();
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
        //查询公债案件
        Integer isDebt = outsourceInfo.getIsDebt();
        if (Objects.equals(isDebt, 1)) {
            for (int i = 0; i < caseInfoYes.size(); i++) {
                CaseInfo caseInfo = caseInfoYes.get(i);
                String personalName = caseInfo.getPersonalInfo().getName();
                String idCard = caseInfo.getPersonalInfo().getIdCard();
                Object[] nums = (Object[]) outsourcePoolRepository.getGzNum(personalName, idCard, user.getCompanyCode());
                ;//按机构分
                if (Objects.nonNull(nums)) {
                    debtList.add(caseInfo.getId());
                    caseInfoYes.remove(caseInfo);
                    i--;
                    continue;
                }
            }
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

        //按金额平均分配
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

        //按数量平均分配
        if (Objects.equals(rule, 2)) {
            BeanUtils.copyProperties(distributePreviewByAmt(outsourceInfo, caseInfoYes), previewModel);
            debtList.addAll(previewModel.getCaseIds());
            previewModel.setCaseIds(debtList);
            return previewModel;
        }

        //按综合分配
        if (Objects.equals(rule, 3)) {
            BeanUtils.copyProperties(previewIntegrate(outsourceInfo, caseInfoYes), previewModel);
            debtList.addAll(previewModel.getCaseIds());
            previewModel.setCaseIds(debtList);
            return previewModel;
        }

        for (int i = 0; i < (outsourceList != null ? outsourceList.size() : 0); i++) {
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
        previewModel.setOutsourceIds(outsourceList);
        previewModel.setCaseIds(outsourceInfo.getOutCaseIds());
        previewModel.setNumList(disNumList);
        previewModel.setOutList(list);
        return previewModel;
    }

    /**
     * @Description 委外待分配按金额平均分配预览
     */
    public PreviewModel distributePreviewByAmt(OutsourceInfo outsourceInfo, List<CaseInfo> caseInfoYes) {

        PreviewModel previewModel = new PreviewModel();
        //对案件进行排序
        Collections.sort(caseInfoYes, new Comparator<CaseInfo>() {
            @Override
            public int compare(CaseInfo o1, CaseInfo o2) {
                return o2.getOverdueAmount().compareTo(o1.getOverdueAmount());
            }
        });
        //存储委外方
        List<String> outsourceIds = outsourceInfo.getOutId();
        List<DisModel> outsourceDisModleList = new ArrayList();
        for (String id : outsourceIds) {
            DisModel outsourceDisModle = new DisModel();
            outsourceDisModle.setId(id);
            outsourceDisModleList.add(outsourceDisModle);
        }

        //将最大的放到案件
        for (CaseInfo caseInfo : caseInfoYes) {
            Collections.sort(outsourceDisModleList, new Comparator<DisModel>() {
                @Override
                public int compare(DisModel o1, DisModel o2) {
                    return o1.getAmt().compareTo(o2.getAmt());
                }
            });
            outsourceDisModleList.get(0).setAmt(outsourceDisModleList.get(0).getAmt().add(caseInfo.getOverdueAmount()));
            outsourceDisModleList.get(0).getCaseIds().add(caseInfo.getId());
            outsourceDisModleList.get(0).getCaseInfos().add(caseInfo);
        }
        List<OutDistributeInfo> list = new ArrayList<>();
        for (DisModel model : outsourceDisModleList) {
            String outsourceId = model.getId();
            OutDistributeInfo outDistributeInfo = new OutDistributeInfo();
            Outsource outsource = outsourceRepository.findOne(outsourceId);
            outDistributeInfo.setOutCode(outsource.getOutsCode());
            outDistributeInfo.setOutName(outsource.getOutsName());
            previewModel.getCaseIds().addAll(model.getCaseIds());
            previewModel.getOutsourceIds().add(model.getId());
            previewModel.getNumList().add(model.getCaseIds().size());
            outDistributeInfo.setCollectionCount(outsourcePoolRepository.getOutsourceCaseCount(outsourceId));
            outDistributeInfo.setCollectionAmt(outsourcePoolRepository.getOutsourceAmtCount(outsourceId));
            outDistributeInfo.setCaseDistributeCount(model.getCaseInfos().size());
            outDistributeInfo.setCaseDistributeMoneyCount(model.getAmt());
            outDistributeInfo.setCaseTotalCount(outDistributeInfo.getCollectionCount() + outDistributeInfo.getCaseDistributeCount());
            outDistributeInfo.setCaseMoneyTotalCount(outDistributeInfo.getCollectionAmt().add(outDistributeInfo.getCaseDistributeMoneyCount()));
            list.add(outDistributeInfo);
        }
        previewModel.setOutList(list);
        return previewModel;
    }

    /**
     * @Description 委外待分配综合分配预览
     */
    private PreviewModel previewIntegrate(OutsourceInfo outsourceInfo, List<CaseInfo> caseInfoYes) {

        PreviewModel previewModel = new PreviewModel();
        List<DisModel> disModels = new ArrayList<>();
        //对案件进行排序
        Collections.sort(caseInfoYes, (o1, o2) -> {
            return o2.getOverdueAmount().compareTo(o1.getOverdueAmount());
        });
        int start = (int) Math.round(caseInfoYes.size() * 0.25);
        int end = (int) Math.round(caseInfoYes.size() * 0.75);
        List<CaseInfo> numAvgList = new ArrayList<>();
        //取出100条案件中的25到75个案件，存放到numAvgList
        for (int i = start; i < end; i++) {
            numAvgList.add(caseInfoYes.get(start));
            caseInfoYes.remove(start);
        }
//        List<CaseInfo> amtAvgList = caseInfoYes;
        //先数量平均分配
        int caseNum = numAvgList.size();
        int outsourceNum = outsourceInfo.getOutId().size();
        //存储按数量平均分配的案件ID
        for (String id : outsourceInfo.getOutId()) {
            DisModel model = new DisModel();
            model.setId(id);
            disModels.add(model);
        }

        List<Integer> caseNumList = new ArrayList<>(outsourceNum);
        for (int i = 0; i < outsourceNum; i++) {
            caseNumList.add(caseNum / outsourceNum);
            disModels.get(i).setNum(caseNum / outsourceNum);
        }
        //讲案件与委外方无法平均分配的案件再依次分给委外方
        if (caseNum % outsourceNum != 0) {
            for (int i = 0; i < caseNum % outsourceNum; i++) {
                caseNumList.set(i, caseNumList.get(i) + 1);
                disModels.get(i).setNum(caseNumList.get(i));
            }
        }
        Collections.shuffle(numAvgList);
        //将平均分配的案件ID和金额放入disModels中
        for (DisModel model : disModels) {
            List<CaseInfo> temp = numAvgList.subList(0, model.getNum());
            numAvgList = numAvgList.subList(model.getNum(), numAvgList.size());
            for (CaseInfo caseInfo : temp) {
                model.setAmt(model.getAmt().add(caseInfo.getOverdueAmount()));
                model.getCaseIds().add(caseInfo.getId());
                model.getCaseInfos().add(caseInfo);
            }
        }
        //剩余50条按金额平均分配
        for (CaseInfo caseInfo : caseInfoYes) {
            Collections.sort(disModels, (o1, o2) -> {
                return o1.getAmt().compareTo(o2.getAmt());
            });
            disModels.get(0).setAmt(disModels.get(0).getAmt().add(caseInfo.getOverdueAmount()));
            disModels.get(0).getCaseIds().add(caseInfo.getId());
            disModels.get(0).getCaseInfos().add(caseInfo);
        }
        List<OutDistributeInfo> list = new ArrayList<>();
        for (DisModel model : disModels) {
            String outsourceId = model.getId();
            OutDistributeInfo outDistributeInfo = new OutDistributeInfo();
            Outsource outsource = outsourceRepository.findOne(outsourceId);
            outDistributeInfo.setOutCode(outsource.getOutsCode());
            outDistributeInfo.setOutName(outsource.getOutsName());
            previewModel.getCaseIds().addAll(model.getCaseIds());
            previewModel.getOutsourceIds().add(model.getId());
            previewModel.getNumList().add(model.getCaseIds().size());
            outDistributeInfo.setCollectionCount(outsourcePoolRepository.getOutsourceCaseCount(outsourceId));
            outDistributeInfo.setCollectionAmt(outsourcePoolRepository.getOutsourceAmtCount(outsourceId));
            outDistributeInfo.setCaseDistributeCount(model.getCaseInfos().size());
            outDistributeInfo.setCaseDistributeMoneyCount(model.getAmt());
            outDistributeInfo.setCaseTotalCount(outDistributeInfo.getCollectionCount() + outDistributeInfo.getCaseDistributeCount());
            outDistributeInfo.setCaseMoneyTotalCount(outDistributeInfo.getCollectionAmt().add(outDistributeInfo.getCaseDistributeMoneyCount()));
            list.add(outDistributeInfo);
        }
        previewModel.setOutList(list);
        return previewModel;
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
                String companyCode = null;
                if (Objects.nonNull(user.getCompanyCode())) {
                    companyCode = user.getCompanyCode();
                }
                Object[] nums = (Object[]) outsourcePoolRepository.getGzNum(personalName, idCard, companyCode);
                if (Objects.nonNull(nums)) {
                    Outsource outsource = outsourceRepository.findOne(Objects.isNull(nums[0].toString()) ? null : nums[0].toString());
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
                if (Objects.nonNull(outsourcePool)) {
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
            List<CaseInfo> caseInfoList = caseInfos;
            Iterator<CaseInfo> iterator = caseInfoList.iterator();
            if (StringUtils.isNotBlank(caseStrategy.getStrategyText())) {
                if (caseStrategy.getStrategyText().contains(Constants.STRATEGY_AREA_ID)) {
                    while (iterator.hasNext()) {
                        CaseInfo next = iterator.next();
                        if (Objects.isNull(next.getArea())) {
                            iterator.remove();
                        }
                    }
                }
                if (caseStrategy.getStrategyText().contains(Constants.STRATEGY_PRODUCT_SERIES)) {
                    while (iterator.hasNext()) {
                        CaseInfo next = iterator.next();
                        if (Objects.isNull(next.getProduct())) {
                            iterator.remove();
                        } else if (Objects.isNull(next.getProduct().getProductSeries())) {
                            iterator.remove();
                        }
                    }
                }
            }
            for (CaseInfo caseInfo : caseInfoList) {
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

        if (!list.isEmpty()) {
            result = new ArrayList<>();
            setModelValue(list, result);
        }
        return result;
    }

    /**
     * @Description 委外待分配按数量平均分配预览
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
                            if (Objects.nonNull(caseInfo.getRealPayAmount())) {
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

    /**
     * 撤销案件分配信息
     *
     * @param batchNumber
     * @param user
     */
    public void revertOutCaseInfoDistribute(String batchNumber, User user) throws GeneralException {
        //判断时间 需求 超过系统设置的时间不能撤销
        QSysParam qSysParam = QSysParam.sysParam;
        SysParam sysParam = sysParamRepository.findOne(qSysParam.companyCode.eq(user.getCompanyCode()).
                and(qSysParam.code.eq(Constants.SYS_REVOKE_DISTRIBUTE)).
                and(qSysParam.status.eq(SysParam.StatusEnum.Start.getValue())));
        if (Objects.isNull(sysParam)) {
            throw new GeneralException("请设置系统参数-案件撤销时长");
        }
        Iterable<OutsourcePool> outsourcePoolRepositoryAll = outsourcePoolRepository.findAll(QOutsourcePool.outsourcePool.outBatch.eq(batchNumber).
                and(QOutsourcePool.outsourcePool.caseInfo.companyCode.eq(user.getCompanyCode())).
                and(QOutsourcePool.outsourcePool.caseInfo.casePoolType.eq(CaseInfo.CasePoolType.OUTER.getValue())), new Sort(Sort.Direction.ASC, "operateTime"));

        if (DateTime.now().isAfter(outsourcePoolRepositoryAll.iterator().next().getOperateTime().getTime() + Integer.parseInt(sysParam.getValue()) * 60000)) {
            throw new GeneralException("撤销案件超出了系统设置的撤回时间");
        }
        for (OutsourcePool outsourcePool : outsourcePoolRepositoryAll) {
            outsourcePool.setOutStatus(OutsourcePool.OutStatus.TO_OUTSIDE.getCode());
            outsourcePool.setContractAmt(null);
            outsourcePool.setOverduePeriods(null);
            outsourcePool.setEndOutsourceTime(null);
            outsourcePool.setOverOutsourceTime(null);
            outsourcePool.setOutsource(null);
            outsourcePool.setOperateTime(null);
            outsourcePool.setOperator(null);
            outsourcePool.setOutBatch(null);
            outsourcePool.setOutBackAmt(null);
            outsourcePool.setOutTime(null);
            outsourcePool.setOutoperationStatus(null);
        }
        outsourcePoolRepository.save(outsourcePoolRepositoryAll);
    }
}
