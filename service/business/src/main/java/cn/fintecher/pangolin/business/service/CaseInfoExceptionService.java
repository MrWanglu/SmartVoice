package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.ItemsModel;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.business.web.CaseInfoController;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.IdcardUtils;
import cn.fintecher.pangolin.util.ZWDateUtil;
import com.querydsl.core.BooleanBuilder;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author: PeiShouWen
 * @Description: 案件异常池服务类
 * @Date 16:28 2017/8/7
 */
@Service("caseInfoExceptionService")
public class CaseInfoExceptionService {

    private final Logger log = LoggerFactory.getLogger(CaseInfoController.class);
    @Autowired
    CaseInfoExceptionRepository caseInfoExceptionRepository;

    @Autowired
    CaseInfoRepository caseInfoRepository;

    @Autowired
    CaseInfoFileRepository caseInfoFileRepository;

    @Autowired
    PersonalContactRepository personalContactRepository;

    @Autowired
    PersosnalAddressRepository persosnalAddressRepository;

    @Autowired
    PersonalBankRepository personalBankRepository;

    @Autowired
    ProductSeriesRepository productSeriesRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    PersonalJobRepository personalJobRepository;

    @Autowired
    AreaCodeService areaCodeService;

    @Autowired
    PrincipalRepository principalRepository;

    @Autowired
    PersonalRepository personalRepository;

    @Autowired
    CaseInfoDistributedRepository caseInfoDistributedRepository;

    /**
     * 检查时候有异常案件未处理
     *
     * @return 查询所有未处理的异常案件
     */
    public boolean checkCaseExceptionExist(User user) {
        QCaseInfoException qCaseInfoException = QCaseInfoException.caseInfoException;
        return caseInfoExceptionRepository.exists(qCaseInfoException.companyCode.eq(user.getCompanyCode())
                .and(qCaseInfoException.repeatStatus.eq(CaseInfoException.RepeatStatusEnum.PENDING.getValue())));
    }

    /**
     * 获取所有异常案件
     *
     * @return caseInfoExceptionList
     */
    public List<CaseInfoException> getAllCaseInfoException() {
        List<CaseInfoException> caseInfoExceptionList = caseInfoExceptionRepository.findAll();
        return caseInfoExceptionList;
    }

    /**
     * 添加异常案件至待分配池
     *
     * @param caseInfoExceptionId
     * @param user
     * @return
     */
    public CaseInfoDistributed addCaseInfoDistributed(String caseInfoExceptionId, User user) {
        CaseInfoException caseInfoException = caseInfoExceptionRepository.getOne(caseInfoExceptionId);
        List<CaseInfoFile> caseInfoFileList = findCaseInfoFileById(caseInfoExceptionId);
        Personal personal = createPersonal(caseInfoException, user);
        personal = personalRepository.save(personal);
        //更新或添加联系人信息
        addContract(caseInfoException, user, personal);
        //更新或添加地址信息
        addAddr(caseInfoException, user, personal);
        //开户信息
        addBankInfo(caseInfoException, user, personal);
        //单位信息
        addPersonalJob(caseInfoException, user, personal);
        //产品系列
        Product product = addProducts(caseInfoException, user);
        CaseInfoDistributed caseInfoDistributed = addCaseInfoDistributed(caseInfoException, product, user, personal);
        caseInfoDistributedRepository.save(caseInfoDistributed);
        //附件信息
        saveCaseFile(caseInfoFileList, caseInfoDistributed.getId(), caseInfoDistributed.getCaseNumber());
        caseInfoExceptionRepository.delete(caseInfoException);
        return caseInfoDistributed;
    }

    /**
     * 更新异常案件
     */
    public List<CaseInfo> updateCaseInfoException(String caseInfoExceptionId, List<String> caseInfoIds, User user) {
        List<CaseInfoFile> caseInfoFileList = findCaseInfoFileById(caseInfoExceptionId);
        CaseInfoException caseInfoException = caseInfoExceptionRepository.findOne(caseInfoExceptionId);
        List<CaseInfo> caseInfoList = new ArrayList<>();
        for(String caseInfoId : caseInfoIds){
            CaseInfo caseInfo = caseInfoRepository.findOne(caseInfoId);
            addCaseInfo(caseInfo, caseInfoException, user);
            caseInfoRepository.save(caseInfo);
            //附件信息
            saveCaseFile(caseInfoFileList, caseInfo.getId(), caseInfo.getCaseNumber());
            caseInfoExceptionRepository.delete(caseInfoException);
            caseInfoList.add(caseInfo);
        }
        return caseInfoList;
    }

    /**
     * 更新已分配异常案件
     */
    public CaseInfo updateExceptionCase(String caseInfoExceptionId, String caseId, User user, ItemsModel itemsModel) {
        CaseInfoException caseInfoException = caseInfoExceptionRepository.findOne(caseInfoExceptionId);
        CaseInfo caseInfo = caseInfoRepository.findOne(caseId);
        Personal personal = caseInfo.getPersonalInfo();
        if (!itemsModel.getPersonalItems().isEmpty()) {
            personal = updateAssignPersonal(itemsModel.getPersonalItems(), personal, caseInfoException, user);
        }
        if (!itemsModel.getJobItems().isEmpty()) {
            updateJob(itemsModel.getJobItems(), personal, caseInfoException, user);
        }
        if (!itemsModel.getConnectItems().isEmpty()) {
            addContract(caseInfoException, user, personal);
        }
        if (!itemsModel.getCaseItems().isEmpty()) {
            caseInfo = updateCase(itemsModel.getCaseItems(), caseInfo, caseInfoException, user);
        }
        if (!itemsModel.getBankItems().isEmpty()) {
            updateBank(itemsModel.getBankItems(), personal, caseInfoException, user);
        }
        caseInfo.setPersonalInfo(personal);
        caseInfoRepository.save(caseInfo);
        caseInfoExceptionRepository.delete(caseInfoException);
        return caseInfo;
    }

    /**
     * 更新待分配异常案件
     */
    public CaseInfoDistributed updateCaseDistributeException(String caseInfoExceptionId, String caseId, User user, ItemsModel itemsModel) {
        CaseInfoException caseInfoException = caseInfoExceptionRepository.findOne(caseInfoExceptionId);
        CaseInfoDistributed caseInfoDistributed = caseInfoDistributedRepository.findOne(caseId);
        Personal personal = caseInfoDistributed.getPersonalInfo();
        if (!itemsModel.getPersonalItems().isEmpty()) {
            personal = updateAssignPersonal(itemsModel.getPersonalItems(), personal, caseInfoException, user);
        }
        if (!itemsModel.getJobItems().isEmpty()) {
            updateJob(itemsModel.getJobItems(), personal, caseInfoException, user);
        }
        if (!itemsModel.getConnectItems().isEmpty()) {
            addContract(caseInfoException, user, personal);
        }
        if (!itemsModel.getCaseItems().isEmpty()) {
            caseInfoDistributed = updateCaseDistributed(itemsModel.getCaseItems(), caseInfoDistributed, caseInfoException, user);
        }
        if (!itemsModel.getBankItems().isEmpty()) {
            updateBank(itemsModel.getBankItems(), personal, caseInfoException, user);
        }
        caseInfoDistributed.setPersonalInfo(personal);
        caseInfoDistributedRepository.save(caseInfoDistributed);
        caseInfoExceptionRepository.delete(caseInfoException);
        return caseInfoDistributed;
    }

    /**
     * 根据案件ID查询案件附件
     *
     * @param caseInfoExceptionId
     * @return
     */
    private List<CaseInfoFile> findCaseInfoFileById(String caseInfoExceptionId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QCaseInfoFile.caseInfoFile.caseId.eq(caseInfoExceptionId));
        return IterableUtils.toList(caseInfoFileRepository.findAll(builder));
    }

    /**
     * 保存案件附件
     *
     * @param caseInfoFileList
     * @param caseId
     * @param caseNum
     */
    private void saveCaseFile(List<CaseInfoFile> caseInfoFileList, String caseId, String caseNum) {
        if (caseInfoFileList.size() > 0) {
            for (CaseInfoFile obj : caseInfoFileList) {
                obj.setCaseId(caseId);
                obj.setCaseNumber(caseNum);
            }
            caseInfoFileRepository.save(caseInfoFileList);
        }
    }

    /**
     * 删除异常案件
     */
    public void deleteCaseInfoException(String caseInfoExceptionId) {
        log.debug("delete caseInfoException...");
        caseInfoExceptionRepository.delete(caseInfoExceptionId);
    }

    /**
     * 案件计入待分配池中
     *
     * @param caseInfoException
     * @param product
     * @param user
     * @param personal
     * @return
     */
    private CaseInfoDistributed addCaseInfoDistributed(CaseInfoException caseInfoException, Product product, User user, Personal personal) {
        CaseInfoDistributed caseInfoDistributed = new CaseInfoDistributed();
        caseInfoDistributed.setDepartment(user.getDepartment());
        caseInfoDistributed.setPersonalInfo(personal);
        caseInfoDistributed.setArea(areaHandler(caseInfoException));
        caseInfoDistributed.setBatchNumber(caseInfoException.getBatchNumber());
        caseInfoDistributed.setCaseNumber(caseInfoException.getCaseNumber());
        caseInfoDistributed.setProduct(product);
        caseInfoDistributed.setContractNumber(caseInfoException.getContractNumber());
        caseInfoDistributed.setContractAmount(caseInfoException.getContractAmount());
        caseInfoDistributed.setOverdueAmount(caseInfoException.getOverdueAmount());
        caseInfoDistributed.setLeftCapital(caseInfoException.getLeftCapital());
        caseInfoDistributed.setLeftInterest(caseInfoException.getLeftInterest());
        caseInfoDistributed.setOverdueCapital(caseInfoException.getOverdueCapital());
        caseInfoDistributed.setOverdueInterest(caseInfoException.getOverDueInterest());
        caseInfoDistributed.setOverdueFine(caseInfoException.getOverdueFine());
        caseInfoDistributed.setOverdueDelayFine(caseInfoException.getOverdueDelayFine());
        caseInfoDistributed.setPeriods(caseInfoException.getPeriods());
        caseInfoDistributed.setPerDueDate(caseInfoException.getPerDueDate());
        caseInfoDistributed.setPerPayAmount(caseInfoException.getPerPayAmount());
        caseInfoDistributed.setOverduePeriods(caseInfoException.getOverDuePeriods());
        caseInfoDistributed.setOverdueDays(caseInfoException.getOverDueDays());
        caseInfoDistributed.setHasPayAmount(caseInfoException.getHasPayAmount());
        caseInfoDistributed.setHasPayPeriods(caseInfoException.getHasPayPeriods());
        caseInfoDistributed.setLatelyPayDate(caseInfoException.getLatelyPayDate());
        caseInfoDistributed.setLatelyPayAmount(caseInfoException.getLatelyPayAmount());
        caseInfoDistributed.setCaseType(CaseInfo.CaseType.DISTRIBUTE.getValue());
        caseInfoDistributed.setPayStatus(caseInfoException.getPaymentStatus());
        caseInfoDistributed.setPrincipalId(principalRepository.findByCode(caseInfoException.getPrinCode()));
        caseInfoDistributed.setCollectionStatus(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue());
        caseInfoDistributed.setDelegationDate(caseInfoException.getDelegationDate());
        caseInfoDistributed.setCloseDate(caseInfoException.getCloseDate());
        caseInfoDistributed.setCommissionRate(caseInfoException.getCommissionRate());
        caseInfoDistributed.setHandNumber(caseInfoException.getCaseHandNum());
        caseInfoDistributed.setLoanDate(caseInfoException.getLoanDate());
        caseInfoDistributed.setOverdueManageFee(caseInfoException.getOverdueManageFee());
        caseInfoDistributed.setHandUpFlag(CaseInfo.HandUpFlag.NO_HANG.getValue());
        caseInfoDistributed.setOtherAmt(caseInfoException.getOtherAmt());
        caseInfoDistributed.setOperator(user);
        caseInfoDistributed.setOperatorTime(ZWDateUtil.getNowDateTime());
        caseInfoDistributed.setCompanyCode(caseInfoException.getCompanyCode());
        caseInfoDistributed.setCaseMark(CaseInfo.Color.NO_COLOR.getValue()); //案件颜色标记
        return caseInfoDistributed;
    }

    /**
     * 案件更新到正常池
     *
     * @param caseInfoException
     * @param user
     * @return
     */
    private CaseInfo addCaseInfo(CaseInfo caseInfo, CaseInfoException caseInfoException, User user) {
        caseInfo.setArea(areaHandler(caseInfoException));
        caseInfo.setOverdueAmount(caseInfoException.getOverdueAmount());
        caseInfo.setLeftCapital(caseInfoException.getLeftCapital());
        caseInfo.setLeftInterest(caseInfoException.getLeftInterest());
        caseInfo.setOverdueCapital(caseInfoException.getOverdueCapital());
        caseInfo.setOverdueInterest(caseInfoException.getOverDueInterest());
        caseInfo.setOverdueFine(caseInfoException.getOverdueFine());
        caseInfo.setOverdueDelayFine(caseInfoException.getOverdueDelayFine());
        caseInfo.setPeriods(caseInfoException.getPeriods());
        caseInfo.setPerDueDate(caseInfoException.getPerDueDate());
        caseInfo.setPerPayAmount(caseInfoException.getPerPayAmount());
        caseInfo.setOverduePeriods(caseInfoException.getOverDuePeriods());
        caseInfo.setOverdueDays(caseInfoException.getOverDueDays());
        caseInfo.setHasPayAmount(caseInfoException.getHasPayAmount());
        caseInfo.setHasPayPeriods(caseInfoException.getHasPayPeriods());
        caseInfo.setPayStatus(caseInfoException.getPaymentStatus());
        caseInfo.setCommissionRate(caseInfoException.getCommissionRate());
        caseInfo.setOtherAmt(caseInfoException.getOtherAmt());
        caseInfo.setOperator(user);
        caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime());
        return caseInfo;
    }

    public Personal updateAssignPersonal(List<String> items, Personal personal, CaseInfoException caseInfoException, User user) {
        if (Objects.isNull(personal)) {
            personal = new Personal();
        }
        if (items.contains("客户姓名")) {
            personal.setName(caseInfoException.getPersonalName());
        }
        if (items.contains("身份证号")) {
            personal.setIdCard(caseInfoException.getIdCard());
        }
        if (items.contains("手机号码")) {
            personal.setMobileNo(caseInfoException.getMobileNo());
        }
        if (items.contains("身份证户籍地址")) {
            personal.setIdCardAddress(caseInfoException.getIdCardAddress());
        }
        if (items.contains("家庭住址")) {
            personal.setLocalHomeAddress(caseInfoException.getHomeAddress());
        }
        if (items.contains("固定电话")) {
            personal.setLocalPhoneNo(caseInfoException.getHomePhone());
        }
        personal.setOperatorTime(ZWDateUtil.getNowDateTime());
        personal.setOperator(user.getId());
        return personal;
    }

    public void updateJob(List<String> items, Personal personal, CaseInfoException caseInfoException, User user) {
        PersonalJob job = personalJobRepository.findByPersonalId(personal.getId());
        if (Objects.isNull(job)) {
            job = new PersonalJob();
        }
        if (items.contains("工作单位名称")) {
            job.setCompanyName(caseInfoException.getCompanyName());
        }
        if (items.contains("工作单位地址")) {
            job.setAddress(caseInfoException.getCompanyAddr());
        }
        if (items.contains("工作单位电话")) {
            job.setPhone(caseInfoException.getCompanyPhone());
        }
        job.setOperatorTime(ZWDateUtil.getNowDateTime());
        job.setOperator(user.getId());
        personalJobRepository.save(job);
    }

    public CaseInfo updateCase(List<String> items, CaseInfo caseInfo, CaseInfoException caseInfoException, User user) {
        if (items.contains("产品系列")) {
            Product product = caseInfo.getProduct();
            ProductSeries series = product.getProductSeries();
            series.setSeriesName(caseInfoException.getProductSeriesName());
            product.setProductSeries(series);
            caseInfo.setProduct(product);
        }
        if (items.contains("合同编号")) {
            caseInfo.setContractNumber(caseInfoException.getContractNumber());
        }
        if (items.contains("城市")) {
            caseInfo.setArea(areaHandler(caseInfoException));
        }
        if (items.contains("省份")) {
            caseInfo.setArea(areaHandler(caseInfoException));
        }
        if (items.contains("贷款日期")) {
            caseInfo.setLoanDate(caseInfoException.getLoanDate());
        }
        if (items.contains("合同金额")) {
            caseInfo.setContractAmount(caseInfoException.getContractAmount());
        }
        if (items.contains("剩余本金(元)")) {
            caseInfo.setLeftCapital(caseInfoException.getLeftCapital());
        }
        if (items.contains("剩余利息(元)")) {
            caseInfo.setLeftInterest(caseInfoException.getLeftInterest());
        }
        if (items.contains("逾期总金额(元)")) {
            caseInfo.setOverdueAmount(caseInfoException.getOverdueAmount());
        }
        if (items.contains("逾期本金(元)")) {
            caseInfo.setOverdueCapital(caseInfoException.getOverdueCapital());
        }
        if (items.contains("逾期利息(元)")) {
            caseInfo.setOverdueInterest(caseInfoException.getOverDueInterest());
        }
        if (items.contains("逾期罚息(元)")) {
            caseInfo.setOverdueFine(caseInfoException.getOverdueFine());
        }
        if (items.contains("还款期数")) {
            caseInfo.setPeriods(caseInfoException.getPeriods());
        }
        if (items.contains("每期还款金额(元)")) {
            caseInfo.setPerPayAmount(caseInfoException.getPerPayAmount());
        }
        if (items.contains("其他费用(元)")) {
            caseInfo.setOtherAmt(caseInfoException.getOtherAmt());
        }
        if (items.contains("逾期日期")) {
            caseInfo.setOverDueDate(caseInfoException.getOverDueDate());
        }
        if (items.contains("逾期期数")) {
            caseInfo.setOverduePeriods(caseInfoException.getOverDuePeriods());
        }
        if (items.contains("逾期天数")) {
            caseInfo.setOverdueDays(caseInfoException.getOverDueDays());
        }
        if (items.contains("已还款金额(元)")) {
            caseInfo.setHasPayAmount(caseInfoException.getHasPayAmount());
        }
        if (items.contains("已还款期数")) {
            caseInfo.setHasPayPeriods(caseInfoException.getHasPayPeriods());
        }
        if (items.contains("最近还款日期")) {
            caseInfo.setLatelyPayDate(caseInfoException.getLatelyPayDate());
        }
        if (items.contains("最近还款金额(元)")) {
            caseInfo.setLatelyPayAmount(caseInfoException.getLatelyPayAmount());
        }
        if (items.contains("佣金比例(%)")) {
            caseInfo.setCommissionRate(caseInfoException.getCommissionRate());
        }
        caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime());
        caseInfo.setOperator(user);
        return caseInfo;
    }

    public CaseInfoDistributed updateCaseDistributed(List<String> items, CaseInfoDistributed caseInfoDistributed, CaseInfoException caseInfoException, User user) {
        if (items.contains("产品系列")) {
            Product product = caseInfoDistributed.getProduct();
            ProductSeries series = product.getProductSeries();
            series.setSeriesName(caseInfoException.getProductSeriesName());
            product.setProductSeries(series);
            caseInfoDistributed.setProduct(product);
        }
        if (items.contains("合同编号")) {
            caseInfoDistributed.setContractNumber(caseInfoException.getContractNumber());
        }
        if (items.contains("城市")) {
            caseInfoDistributed.setArea(areaHandler(caseInfoException));
        }
        if (items.contains("省份")) {
            caseInfoDistributed.setArea(areaHandler(caseInfoException));
        }
        if (items.contains("贷款日期")) {
            caseInfoDistributed.setLoanDate(caseInfoException.getLoanDate());
        }
        if (items.contains("合同金额")) {
            caseInfoDistributed.setContractAmount(caseInfoException.getContractAmount());
        }
        if (items.contains("剩余本金(元)")) {
            caseInfoDistributed.setLeftCapital(caseInfoException.getLeftCapital());
        }
        if (items.contains("剩余利息(元)")) {
            caseInfoDistributed.setLeftInterest(caseInfoException.getLeftInterest());
        }
        if (items.contains("逾期总金额(元)")) {
            caseInfoDistributed.setOverdueAmount(caseInfoException.getOverdueAmount());
        }
        if (items.contains("逾期本金(元)")) {
            caseInfoDistributed.setOverdueCapital(caseInfoException.getOverdueCapital());
        }
        if (items.contains("逾期利息(元)")) {
            caseInfoDistributed.setOverdueInterest(caseInfoException.getOverDueInterest());
        }
        if (items.contains("逾期罚息(元)")) {
            caseInfoDistributed.setOverdueFine(caseInfoException.getOverdueFine());
        }
        if (items.contains("还款期数")) {
            caseInfoDistributed.setPeriods(caseInfoException.getPeriods());
        }
        if (items.contains("每期还款金额(元)")) {
            caseInfoDistributed.setPerPayAmount(caseInfoException.getPerPayAmount());
        }
        if (items.contains("其他费用(元)")) {
            caseInfoDistributed.setOtherAmt(caseInfoException.getOtherAmt());
        }
        if (items.contains("逾期日期")) {
            caseInfoDistributed.setOverDueDate(caseInfoException.getOverDueDate());
        }
        if (items.contains("逾期期数")) {
            caseInfoDistributed.setOverduePeriods(caseInfoException.getOverDuePeriods());
        }
        if (items.contains("逾期天数")) {
            caseInfoDistributed.setOverdueDays(caseInfoException.getOverDueDays());
        }
        if (items.contains("已还款金额(元)")) {
            caseInfoDistributed.setHasPayAmount(caseInfoException.getHasPayAmount());
        }
        if (items.contains("已还款期数")) {
            caseInfoDistributed.setHasPayPeriods(caseInfoException.getHasPayPeriods());
        }
        if (items.contains("最近还款日期")) {
            caseInfoDistributed.setLatelyPayDate(caseInfoException.getLatelyPayDate());
        }
        if (items.contains("最近还款金额(元)")) {
            caseInfoDistributed.setLatelyPayAmount(caseInfoException.getLatelyPayAmount());
        }
        if (items.contains("佣金比例(%)")) {
            caseInfoDistributed.setCommissionRate(caseInfoException.getCommissionRate());
        }
        caseInfoDistributed.setOperatorTime(ZWDateUtil.getNowDateTime());
        caseInfoDistributed.setOperator(user);
        return caseInfoDistributed;
    }

    public void updateBank(List<String> items, Personal personal, CaseInfoException caseInfoException, User user) {
        PersonalBank bank = personalBankRepository.findOne(QPersonalBank.personalBank.personalId.eq(personal.getId()));
        if (Objects.isNull(bank)) {
            bank = new PersonalBank();
        }
        if (items.contains("客户还款卡银行")) {
            bank.setDepositBank(caseInfoException.getDepositBank());
        }
        if (items.contains("客户还款卡号")) {
            bank.setCardNumber(caseInfoException.getCardNumber());
        }
        bank.setOperatorTime(ZWDateUtil.getNowDateTime());
        bank.setOperator(user.getId());
        personalBankRepository.save(bank);
    }

    /**
     * 工作信息
     *
     * @param caseInfoException
     * @param user
     * @param personal
     */
    private void addPersonalJob(CaseInfoException caseInfoException, User user, Personal personal) {
        if (StringUtils.isNotBlank(caseInfoException.getCompanyAddr()) || StringUtils.isNotBlank(caseInfoException.getCompanyName())
                || StringUtils.isNotBlank(caseInfoException.getCompanyPhone())) {
            PersonalJob personalJob = new PersonalJob();
            personalJob.setAddress(caseInfoException.getCompanyAddr());
            personalJob.setCompanyName(caseInfoException.getCompanyName());
            personalJob.setPhone(caseInfoException.getCompanyPhone());
            personalJob.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalJob.setOperator(user.getId());
            personalJob.setPersonalId(personal.getId());
            personalJobRepository.save(personalJob);
        }
    }


    /**
     * 创建客户信息
     *
     * @param caseInfoException
     * @param user
     * @return
     */
    private Personal createPersonal(CaseInfoException caseInfoException, User user) {
        //创建客户信息
        Personal personal = new Personal();
        personal.setName(caseInfoException.getPersonalName());
        String sex = IdcardUtils.getGenderByIdCard(caseInfoException.getIdCard());
        if ("M".equals(sex)) {
            personal.setSex(Personal.SexEnum.MAN.getValue());
        } else if ("F".equals(sex)) {
            personal.setSex(Personal.SexEnum.WOMEN.getValue());
        } else {
            personal.setSex(Personal.SexEnum.UNKNOWN.getValue());
        }
        personal.setAge(IdcardUtils.getAgeByIdCard(caseInfoException.getIdCard()));
        personal.setMobileNo(caseInfoException.getMobileNo());
        personal.setMobileStatus(Personal.PhoneStatus.UNKNOWN.getValue());
        personal.setIdCard(caseInfoException.getIdCard());
        personal.setIdCardAddress(caseInfoException.getIdCardAddress());
        personal.setLocalPhoneNo(caseInfoException.getHomePhone());
        //现居住地址
        personal.setLocalHomeAddress(caseInfoException.getHomeAddress());
        personal.setOperator(user.getId());
        personal.setOperatorTime(ZWDateUtil.getNowDateTime());
        personal.setCompanyCode(caseInfoException.getCompanyCode());
        personal.setDataSource(Constants.DataSource.IMPORT.getValue());
        personal.setMarital(Personal.MARITAL.UNKNOW.getValue());
        personal.setNumber(caseInfoException.getPersonalNumber()); //客户号
        return personal;
    }

    /**
     * 添加或更新联系人信息
     *
     * @param caseInfoException
     * @param user
     * @param personal
     */
    private void addContract(CaseInfoException caseInfoException, User user, Personal personal) {
        List<PersonalContact> personalContactList = new ArrayList<>();
        PersonalContact personalContact = new PersonalContact();
        personalContact.setPersonalId(personal.getId());
        personalContact.setRelation(Personal.RelationEnum.SELF.getValue());
        personalContact.setName(caseInfoException.getPersonalName());
        personalContact.setPhone(caseInfoException.getMobileNo());
        personalContact.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
        personalContact.setMobile(caseInfoException.getHomePhone());
        personalContact.setIdCard(caseInfoException.getIdCard());
        personalContact.setEmployer(caseInfoException.getCompanyName());
        personalContact.setWorkPhone(caseInfoException.getCompanyPhone());
        personalContact.setSource(caseInfoException.getDataSources());
        personalContact.setAddress(caseInfoException.getHomeAddress());
        personalContact.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
        personalContact.setOperator(user.getId());
        personalContact.setOperatorTime(ZWDateUtil.getNowDateTime());
        personalContactList.add(personalContact);
        // 联系人1信息
        if (Objects.nonNull(caseInfoException.getContactName1())
                || Objects.nonNull(caseInfoException.getContactPhone1())
                || Objects.nonNull(caseInfoException.getContactHomePhone1())) {
            PersonalContact obj = new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(caseInfoException.getContactRelation1()));
            obj.setName(caseInfoException.getContactName1());
            obj.setPhone(caseInfoException.getContactPhone1());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(caseInfoException.getContactHomePhone1());
            obj.setEmployer(caseInfoException.getContactWorkUnit1());
            obj.setWorkPhone(caseInfoException.getContactUnitPhone1());
            obj.setSource(caseInfoException.getDataSources());
            obj.setAddress(caseInfoException.getContactCurrAddress1());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人2信息
        if (Objects.nonNull(caseInfoException.getContactName2())
                || Objects.nonNull(caseInfoException.getContactPhone2())
                || Objects.nonNull(caseInfoException.getContactHomePhone2())) {
            PersonalContact obj = new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(caseInfoException.getContactRelation2()));
            obj.setName(caseInfoException.getContactName2());
            obj.setPhone(caseInfoException.getContactPhone2());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(caseInfoException.getContactHomePhone2());
            obj.setEmployer(caseInfoException.getContactWorkUnit2());
            obj.setWorkPhone(caseInfoException.getContactUnitPhone2());
            obj.setSource(caseInfoException.getDataSources());
            obj.setAddress(caseInfoException.getContactCurrAddress2());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人3信息
        if (Objects.nonNull(caseInfoException.getContactName3())
                || Objects.nonNull(caseInfoException.getContactPhone3())
                || Objects.nonNull(caseInfoException.getContactHomePhone3())) {
            PersonalContact obj = new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(caseInfoException.getContactRelation3()));
            obj.setName(caseInfoException.getContactName3());
            obj.setPhone(caseInfoException.getContactPhone3());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(caseInfoException.getContactHomePhone3());
            obj.setEmployer(caseInfoException.getContactWorkUnit3());
            obj.setWorkPhone(caseInfoException.getContactUnitPhone3());
            obj.setSource(caseInfoException.getDataSources());
            obj.setAddress(caseInfoException.getContactCurrAddress3());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人4信息
        if (Objects.nonNull(caseInfoException.getContactName4())
                || Objects.nonNull(caseInfoException.getContactPhone4())
                || Objects.nonNull(caseInfoException.getContactHomePhone4())) {
            PersonalContact obj = new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(caseInfoException.getContactRelation4()));
            obj.setName(caseInfoException.getContactName4());
            obj.setPhone(caseInfoException.getContactPhone4());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(caseInfoException.getContactHomePhone4());
            obj.setEmployer(caseInfoException.getContactWorkUnit4());
            obj.setWorkPhone(caseInfoException.getContactUnitPhone4());
            obj.setSource(caseInfoException.getDataSources());
            obj.setAddress(caseInfoException.getContactCurrAddress4());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人5信息
        if (Objects.nonNull(caseInfoException.getContactName5())
                || Objects.nonNull(caseInfoException.getContactPhone5())
                || Objects.nonNull(caseInfoException.getContactHomePhone5())) {
            PersonalContact obj = new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(caseInfoException.getContactRelation5()));
            obj.setName(caseInfoException.getContactName5());
            obj.setPhone(caseInfoException.getContactPhone5());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(caseInfoException.getContactHomePhone5());
            obj.setEmployer(caseInfoException.getContactWorkUnit5());
            obj.setWorkPhone(caseInfoException.getContactUnitPhone5());
            obj.setSource(caseInfoException.getDataSources());
            obj.setAddress(caseInfoException.getContactCurrAddress5());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人6信息
        if (Objects.nonNull(caseInfoException.getContactName6())
                || Objects.nonNull(caseInfoException.getContactPhone6())
                || Objects.nonNull(caseInfoException.getContactHomePhone6())) {
            PersonalContact obj = new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(caseInfoException.getContactRelation6()));
            obj.setName(caseInfoException.getContactName6());
            obj.setPhone(caseInfoException.getContactPhone6());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(caseInfoException.getContactHomePhone6());
            obj.setEmployer(caseInfoException.getContactWorkUnit6());
            obj.setWorkPhone(caseInfoException.getContactUnitPhone6());
            obj.setSource(caseInfoException.getDataSources());
            obj.setAddress(caseInfoException.getContactCurrAddress6());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人7信息
        if (Objects.nonNull(caseInfoException.getContactName7())
                || Objects.nonNull(caseInfoException.getContactPhone7())
                || Objects.nonNull(caseInfoException.getContactHomePhone7())) {
            PersonalContact obj = new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(caseInfoException.getContactRelation7()));
            obj.setName(caseInfoException.getContactName7());
            obj.setPhone(caseInfoException.getContactPhone7());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(caseInfoException.getContactHomePhone7());
            obj.setEmployer(caseInfoException.getContactWorkUnit7());
            obj.setWorkPhone(caseInfoException.getContactUnitPhone7());
            obj.setSource(caseInfoException.getDataSources());
            obj.setAddress(caseInfoException.getContactCurrAddress7());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人8信息
        if (Objects.nonNull(caseInfoException.getContactName8())
                || Objects.nonNull(caseInfoException.getContactPhone8())
                || Objects.nonNull(caseInfoException.getContactHomePhone8())) {
            PersonalContact obj = new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(caseInfoException.getContactRelation8()));
            obj.setName(caseInfoException.getContactName8());
            obj.setPhone(caseInfoException.getContactPhone8());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(caseInfoException.getContactHomePhone8());
            obj.setEmployer(caseInfoException.getContactWorkUnit8());
            obj.setWorkPhone(caseInfoException.getContactUnitPhone8());
            obj.setSource(caseInfoException.getDataSources());
            obj.setAddress(caseInfoException.getContactCurrAddress8());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人9信息
        if (Objects.nonNull(caseInfoException.getContactName9())
                || Objects.nonNull(caseInfoException.getContactPhone9())
                || Objects.nonNull(caseInfoException.getContactHomePhone9())) {
            PersonalContact obj = new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(caseInfoException.getContactRelation9()));
            obj.setName(caseInfoException.getContactName9());
            obj.setPhone(caseInfoException.getContactPhone9());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(caseInfoException.getContactHomePhone9());
            obj.setEmployer(caseInfoException.getContactWorkUnit9());
            obj.setWorkPhone(caseInfoException.getContactUnitPhone9());
            obj.setSource(caseInfoException.getDataSources());
            obj.setAddress(caseInfoException.getContactCurrAddress9());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人10信息
        if (Objects.nonNull(caseInfoException.getContactName10())
                || Objects.nonNull(caseInfoException.getContactPhone10())
                || Objects.nonNull(caseInfoException.getContactHomePhone10())) {
            PersonalContact obj = new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(caseInfoException.getContactRelation10()));
            obj.setName(caseInfoException.getContactName10());
            obj.setPhone(caseInfoException.getContactPhone10());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(caseInfoException.getContactHomePhone10());
            obj.setEmployer(caseInfoException.getContactWorkUnit10());
            obj.setWorkPhone(caseInfoException.getContactUnitPhone10());
            obj.setSource(caseInfoException.getDataSources());
            obj.setAddress(caseInfoException.getContactCurrAddress10());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人11信息
        if (Objects.nonNull(caseInfoException.getContactName11())
                || Objects.nonNull(caseInfoException.getContactPhone11())
                || Objects.nonNull(caseInfoException.getContactHomePhone11())) {
            PersonalContact obj = new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(caseInfoException.getContactRelation11()));
            obj.setName(caseInfoException.getContactName11());
            obj.setPhone(caseInfoException.getContactPhone11());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(caseInfoException.getContactHomePhone11());
            obj.setEmployer(caseInfoException.getContactWorkUnit11());
            obj.setWorkPhone(caseInfoException.getContactUnitPhone11());
            obj.setSource(caseInfoException.getDataSources());
            obj.setAddress(caseInfoException.getContactCurrAddress11());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人12信息
        if (Objects.nonNull(caseInfoException.getContactName12())
                || Objects.nonNull(caseInfoException.getContactPhone12())
                || Objects.nonNull(caseInfoException.getContactHomePhone12())) {
            PersonalContact obj = new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(caseInfoException.getContactRelation12()));
            obj.setName(caseInfoException.getContactName12());
            obj.setPhone(caseInfoException.getContactPhone12());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(caseInfoException.getContactHomePhone12());
            obj.setEmployer(caseInfoException.getContactWorkUnit12());
            obj.setWorkPhone(caseInfoException.getContactUnitPhone12());
            obj.setSource(caseInfoException.getDataSources());
            obj.setAddress(caseInfoException.getContactCurrAddress12());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人13信息
        if (Objects.nonNull(caseInfoException.getContactName13())
                || Objects.nonNull(caseInfoException.getContactPhone13())
                || Objects.nonNull(caseInfoException.getContactHomePhone13())) {
            PersonalContact obj = new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(caseInfoException.getContactRelation13()));
            obj.setName(caseInfoException.getContactName13());
            obj.setPhone(caseInfoException.getContactPhone13());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(caseInfoException.getContactHomePhone13());
            obj.setEmployer(caseInfoException.getContactWorkUnit13());
            obj.setWorkPhone(caseInfoException.getContactUnitPhone13());
            obj.setSource(caseInfoException.getDataSources());
            obj.setAddress(caseInfoException.getContactCurrAddress13());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人14信息
        if (Objects.nonNull(caseInfoException.getContactName14())
                || Objects.nonNull(caseInfoException.getContactPhone14())
                || Objects.nonNull(caseInfoException.getContactHomePhone14())) {
            PersonalContact obj = new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(caseInfoException.getContactRelation14()));
            obj.setName(caseInfoException.getContactName14());
            obj.setPhone(caseInfoException.getContactPhone14());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(caseInfoException.getContactHomePhone14());
            obj.setEmployer(caseInfoException.getContactWorkUnit14());
            obj.setWorkPhone(caseInfoException.getContactUnitPhone14());
            obj.setSource(caseInfoException.getDataSources());
            obj.setAddress(caseInfoException.getContactCurrAddress14());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人15信息
        if (Objects.nonNull(caseInfoException.getContactName15())
                || Objects.nonNull(caseInfoException.getContactPhone15())
                || Objects.nonNull(caseInfoException.getContactHomePhone15())) {
            PersonalContact obj = new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(caseInfoException.getContactRelation15()));
            obj.setName(caseInfoException.getContactName15());
            obj.setPhone(caseInfoException.getContactPhone15());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(caseInfoException.getContactHomePhone15());
            obj.setEmployer(caseInfoException.getContactWorkUnit15());
            obj.setWorkPhone(caseInfoException.getContactUnitPhone15());
            obj.setSource(caseInfoException.getDataSources());
            obj.setAddress(caseInfoException.getContactCurrAddress15());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        personalContactRepository.save(personalContactList);
    }

    /**
     * 更新或新增地址信息
     *
     * @param caseInfoException
     * @param user
     * @param personal
     */
    private void addAddr(CaseInfoException caseInfoException, User user, Personal personal) {
        List<PersonalAddress> personalAddressList = new ArrayList<>();
        //居住地址(个人)
        if (StringUtils.isNotBlank(caseInfoException.getHomeAddress())) {
            PersonalAddress personalAddress = new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(Personal.RelationEnum.SELF.getValue());
            personalAddress.setName(caseInfoException.getPersonalName());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(caseInfoException.getHomeAddress());
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }

        //身份证户籍地址（个人）
        if (StringUtils.isNotBlank(caseInfoException.getIdCardAddress())) {
            PersonalAddress personalAddress = new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(Personal.RelationEnum.SELF.getValue());
            personalAddress.setName(caseInfoException.getPersonalName());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setType(Personal.AddrRelationEnum.IDCARDADDR.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(caseInfoException.getIdCardAddress());
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }

        //工作单位地址（个人）
        if (StringUtils.isNotBlank(caseInfoException.getCompanyAddr())) {
            PersonalAddress personalAddress = new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(Personal.RelationEnum.SELF.getValue());
            personalAddress.setName(caseInfoException.getPersonalName());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setType(Personal.AddrRelationEnum.UNITADDR.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(caseInfoException.getCompanyAddr());
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }

        //居住地址(联系人1)
        if (StringUtils.isNotBlank(caseInfoException.getContactCurrAddress1())) {
            PersonalAddress personalAddress = new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(caseInfoException.getContactRelation1()));
            personalAddress.setName(caseInfoException.getContactName1());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(caseInfoException.getContactCurrAddress1());
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }

        //居住地址(联系人2)
        if (StringUtils.isNotBlank(caseInfoException.getContactCurrAddress2())) {
            PersonalAddress personalAddress = new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(caseInfoException.getContactRelation2()));
            personalAddress.setName(caseInfoException.getContactName2());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(caseInfoException.getContactCurrAddress2());
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }

        //居住地址(联系人3)
        if (StringUtils.isNotBlank(caseInfoException.getContactCurrAddress3())) {
            PersonalAddress personalAddress = new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(caseInfoException.getContactRelation3()));
            personalAddress.setName(caseInfoException.getContactName3());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(caseInfoException.getContactCurrAddress3());
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);

        }

        //居住地址(联系人4)
        if (StringUtils.isNotBlank(caseInfoException.getContactCurrAddress4())) {
            PersonalAddress personalAddress = new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(caseInfoException.getContactRelation4()));
            personalAddress.setName(caseInfoException.getContactName4());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(caseInfoException.getContactCurrAddress4());
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人5)
        if (StringUtils.isNotBlank(caseInfoException.getContactCurrAddress5())) {
            PersonalAddress personalAddress = new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(caseInfoException.getContactRelation5()));
            personalAddress.setName(caseInfoException.getContactName5());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(caseInfoException.getContactCurrAddress5());
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人6)
        if (StringUtils.isNotBlank(caseInfoException.getContactCurrAddress6())) {
            PersonalAddress personalAddress = new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(caseInfoException.getContactRelation6()));
            personalAddress.setName(caseInfoException.getContactName6());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(caseInfoException.getContactCurrAddress6());
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人7)
        if (StringUtils.isNotBlank(caseInfoException.getContactCurrAddress7())) {
            PersonalAddress personalAddress = new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(caseInfoException.getContactRelation7()));
            personalAddress.setName(caseInfoException.getContactName7());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(caseInfoException.getContactCurrAddress7());
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人8)
        if (StringUtils.isNotBlank(caseInfoException.getContactCurrAddress8())) {
            PersonalAddress personalAddress = new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(caseInfoException.getContactRelation8()));
            personalAddress.setName(caseInfoException.getContactName8());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(caseInfoException.getContactCurrAddress8());
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人9)
        if (StringUtils.isNotBlank(caseInfoException.getContactCurrAddress9())) {
            PersonalAddress personalAddress = new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(caseInfoException.getContactRelation9()));
            personalAddress.setName(caseInfoException.getContactName9());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(caseInfoException.getContactCurrAddress9());
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人10)
        if (StringUtils.isNotBlank(caseInfoException.getContactCurrAddress10())) {
            PersonalAddress personalAddress = new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(caseInfoException.getContactRelation10()));
            personalAddress.setName(caseInfoException.getContactName10());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(caseInfoException.getContactCurrAddress10());
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人11)
        if (StringUtils.isNotBlank(caseInfoException.getContactCurrAddress11())) {
            PersonalAddress personalAddress = new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(caseInfoException.getContactRelation11()));
            personalAddress.setName(caseInfoException.getContactName11());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(caseInfoException.getContactCurrAddress11());
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人12)
        if (StringUtils.isNotBlank(caseInfoException.getContactCurrAddress12())) {
            PersonalAddress personalAddress = new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(caseInfoException.getContactRelation12()));
            personalAddress.setName(caseInfoException.getContactName12());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(caseInfoException.getContactCurrAddress12());
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人13)
        if (StringUtils.isNotBlank(caseInfoException.getContactCurrAddress13())) {
            PersonalAddress personalAddress = new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(caseInfoException.getContactRelation13()));
            personalAddress.setName(caseInfoException.getContactName13());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(caseInfoException.getContactCurrAddress13());
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人14)
        if (StringUtils.isNotBlank(caseInfoException.getContactCurrAddress14())) {
            PersonalAddress personalAddress = new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(caseInfoException.getContactRelation14()));
            personalAddress.setName(caseInfoException.getContactName14());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(caseInfoException.getContactCurrAddress14());
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人15)
        if (StringUtils.isNotBlank(caseInfoException.getContactCurrAddress15())) {
            PersonalAddress personalAddress = new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(caseInfoException.getContactRelation15()));
            personalAddress.setName(caseInfoException.getContactName4());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(caseInfoException.getContactCurrAddress15());
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        persosnalAddressRepository.save(personalAddressList);
    }

    /**
     * 开户信息
     *
     * @param caseInfoException
     * @param user
     * @param personal
     */
    private void addBankInfo(CaseInfoException caseInfoException, User user, Personal personal) {
        if (Objects.nonNull(caseInfoException.getDepositBank()) ||
                Objects.nonNull(caseInfoException.getCardNumber())) {
            PersonalBank personalBank = new PersonalBank();
            personalBank.setDepositBank(caseInfoException.getDepositBank());
            personalBank.setCardNumber(caseInfoException.getCardNumber());
            personalBank.setPersonalId(personal.getId());
            personal.setOperatorTime(ZWDateUtil.getNowDateTime());
            personal.setOperator(user.getId());
            personalBank.setAccountNumber(caseInfoException.getAccountNumber());//开户号
            personalBankRepository.save(personalBank);
        }
    }

    /**
     * 新增或更新产品及系列名称
     *
     * @param caseInfoException
     * @param user
     */
    private Product addProducts(CaseInfoException caseInfoException, User user) {
        ProductSeries productSeries = null;
        if (StringUtils.isNotBlank(caseInfoException.getProductSeriesName())) {
            productSeries = new ProductSeries();
            productSeries.setSeriesName(caseInfoException.getProductSeriesName());
            productSeries.setOperator(user.getId());
            productSeries.setOperatorTime(ZWDateUtil.getNowDateTime());
            productSeries.setPrincipal_id(caseInfoException.getPrinCode());
            productSeries.setCompanyCode(caseInfoException.getCompanyCode());
            productSeries = productSeriesRepository.save(productSeries);
        }
        //产品名称
        Product product = null;
        if (StringUtils.isNotBlank(caseInfoException.getProductName())) {
            product = new Product();
            product.setProdcutName(caseInfoException.getProductName());
            product.setOperator(user.getId());
            product.setOperatorTime(ZWDateUtil.getNowDateTime());
            product.setCompanyCode(caseInfoException.getCompanyCode());
            product.setProductSeries(productSeries);
            product = productRepository.save(product);
        }
        return product;
    }


    /**
     * 解析居住地址
     *
     * @param caseInfoException
     */
    private String nowLivingAddr(CaseInfoException caseInfoException, String addr) {
        if (Objects.isNull(addr)) {
            return null;
        }
        String province = null;
        if (Objects.isNull(caseInfoException.getProvince())) {
            province = "";
        } else {
            province = caseInfoException.getProvince();
        }
        String city = null;
        if (Objects.isNull(caseInfoException.getCity())) {
            city = "";
        } else {
            city = caseInfoException.getCity();
        }
        //现居住地地址
        if (addr.startsWith(province)) {
            return addr;
        } else if (addr.startsWith(city)) {
            return caseInfoException.getProvince().concat(addr);
        } else {
            return province.concat(city).concat(addr);
        }
    }

    /**
     * 联系人关联关系解析
     *
     * @param
     */
    private Integer getRelationType(String relationName) {
        //关系判断
        if (Objects.nonNull(relationName)) {
            for (Personal.RelationEnum relation : Personal.RelationEnum.values()) {
                if (relation.getRemark().equals(relationName)) {
                    return relation.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 地址设置(城市--->家庭住址--->身份证地址)
     *
     * @param caseInfoException
     * @return
     */
    private AreaCode areaHandler(CaseInfoException caseInfoException) {
        List<String> personalAreaList = new ArrayList<>();
        List<String> emptyList = new ArrayList<>();
        personalAreaList.add(caseInfoException.getCity());
        personalAreaList.add(caseInfoException.getHomeAddress());
        personalAreaList.add(caseInfoException.getIdCardAddress());
        emptyList.add(null);
        personalAreaList.removeAll(emptyList);
        for (String area : personalAreaList) {
            AreaCode areaCode = areaCodeService.queryAreaCodeByName(area);
            if (Objects.nonNull(areaCode)) {
                return areaCode;
            }
        }
        return null;
    }
}
