package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.business.utils.ZWMathUtil;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.IdcardUtils;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.util.ZWStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: PeiShouWen
 * @Description: 开始处理发过来的案件数据
 * @Date 16:26 2017/7/24
 */
@Service("processDataInfoExcelService")
public class ProcessDataInfoExcelService {

    private final Logger logger = LoggerFactory.getLogger(ProcessDataInfoExcelService.class);

    @Autowired
    PersonalRepository personalRepository;

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
    CaseInfoDistributedRepository caseInfoDistributedRepository;

    @Autowired
    CaseInfoExceptionRepository caseInfoExceptionRepository;

    @Autowired
    CaseInfoRepository caseInfoRepository;

    @Autowired
    AreaCodeService areaCodeService;

    @Autowired
    PrincipalRepository principalRepository;

    @Autowired
    CaseInfoFileRepository caseInfoFileRepository;

    @Async
    public void doTask(DataInfoExcelModel dataInfoExcelModel, ConcurrentHashMap<String, DataInfoExcelModel> dataInfoExcelModelMap,
                       User user, int index) {
        logger.info("{}  处理案件信息开始 {}......", Thread.currentThread(), index);

        //案件数据信息
        String key = dataInfoExcelModel.getPersonalName().concat("_").concat(dataInfoExcelModel.getIdCard()).concat("_")
                .concat(dataInfoExcelModel.getPrinCode()).concat("_").concat(dataInfoExcelModel.getProductName())
                .concat("_").concat(dataInfoExcelModel.getCompanyCode());

        //案件附件信息
        List<CaseInfoFile> caseInfoFileList = dataInfoExcelModel.getCaseInfoFileList();
        //产品信息
        Product product = null;
        /**
         * 首先检查待分配案件是否有该案件，有的话直接进入数据异常表
         */
        QCaseInfoDistributed qCaseInfoDistributed = QCaseInfoDistributed.caseInfoDistributed;
        Iterable<CaseInfoDistributed> caseInfoDistributedIterable = caseInfoDistributedRepository.findAll(qCaseInfoDistributed.personalInfo.name.eq(dataInfoExcelModel.getPersonalName())
                .and(qCaseInfoDistributed.personalInfo.idCard.eq(dataInfoExcelModel.getIdCard()))
                .and(qCaseInfoDistributed.principalId.code.eq(dataInfoExcelModel.getPrinCode()))
                .and(qCaseInfoDistributed.product.prodcutName.eq(dataInfoExcelModel.getProductName()))
                .and(qCaseInfoDistributed.companyCode.eq(dataInfoExcelModel.getCompanyCode())));
        if (dataInfoExcelModelMap.containsKey(key) || caseInfoDistributedIterable.iterator().hasNext()) {
            //数据进入案件异常池
            Set<String> caseInfoDistributedSets = new HashSet<>();
            for (Iterator<CaseInfoDistributed> it = caseInfoDistributedIterable.iterator(); it.hasNext(); ) {
                caseInfoDistributedSets.add(it.next().getId());
            }
            Set<String> caseInfoSets = checkCaseInfoExist(dataInfoExcelModel);
            while (caseInfoDistributedSets.isEmpty() && caseInfoSets.isEmpty()) {
                Iterable<CaseInfoDistributed> cd = caseInfoDistributedRepository.findAll(qCaseInfoDistributed.personalInfo.name.eq(dataInfoExcelModel.getPersonalName())
                        .and(qCaseInfoDistributed.personalInfo.idCard.eq(dataInfoExcelModel.getIdCard()))
                        .and(qCaseInfoDistributed.principalId.code.eq(dataInfoExcelModel.getPrinCode()))
                        .and(qCaseInfoDistributed.product.prodcutName.eq(dataInfoExcelModel.getProductName()))
                        .and(qCaseInfoDistributed.companyCode.eq(dataInfoExcelModel.getCompanyCode())));
                for (Iterator<CaseInfoDistributed> it1 = cd.iterator(); it1.hasNext(); ) {
                    caseInfoDistributedSets.add(it1.next().getId());
                }
            }
            caseInfoExceptionRepository.save(addCaseInfoException(dataInfoExcelModel, user, caseInfoDistributedSets, caseInfoSets));
        } else {
            dataInfoExcelModelMap.put(key, dataInfoExcelModel);
            Set<String> caseInfoSets = checkCaseInfoExist(dataInfoExcelModel);
            if (caseInfoSets.isEmpty()) {
                //进入案件正常池
                Personal personal = createPersonal(dataInfoExcelModel, user);
                personal = personalRepository.save(personal);
                //更新或添加联系人信息
                addContract(dataInfoExcelModel, user, personal);
                //更新或添加地址信息
                addAddr(dataInfoExcelModel, user, personal);
                //开户信息
                addBankInfo(dataInfoExcelModel, user, personal);
                //单位信息
                addPersonalJob(dataInfoExcelModel, user, personal);
                //产品系列
                product = addProducts(dataInfoExcelModel, user);
                CaseInfoDistributed caseInfoDistributed = addCaseInfoDistributed(dataInfoExcelModel, product, user, personal);
                caseInfoDistributed = caseInfoDistributedRepository.save(caseInfoDistributed);
                //附件信息
                for (CaseInfoFile obj : caseInfoFileList) {
                    obj.setCaseId(caseInfoDistributed.getId());
                    obj.setCaseNumber(caseInfoDistributed.getCaseNumber());
                }
                caseInfoFileRepository.save(caseInfoFileList);
            } else {
                //异常池
                caseInfoExceptionRepository.save(addCaseInfoException(dataInfoExcelModel, user, new HashSet<>(), caseInfoSets));
                caseInfoFileRepository.save(caseInfoFileList);
            }
        }
        logger.info("{}  处理案件信息结束 {}.", Thread.currentThread(), index);
    }


    /**
     * 检查案件是否已存在
     *
     * @param dataInfoExcelModel
     * @return
     */
    private Set<String> checkCaseInfoExist(DataInfoExcelModel dataInfoExcelModel) {
        /**
         * 检查已有案件是否存在，存在的话直接进入案件异常表，异常表的数据结构与接收数据的DataInfoExcel相同,关联信息信息不做处理
         * (客户姓名、身份证号、委托方ID、产品名称、公司码)
         */
        QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
        Iterable<CaseInfo> caseInfoIterable = caseInfoRepository.findAll(qCaseInfo.personalInfo.name.eq(dataInfoExcelModel.getPersonalName())
                .and(qCaseInfo.personalInfo.idCard.eq(dataInfoExcelModel.getIdCard()))
                .and(qCaseInfo.principalId.code.eq(dataInfoExcelModel.getPrinCode()))
                .and(qCaseInfo.product.prodcutName.eq(dataInfoExcelModel.getProductName()))
                .and(qCaseInfo.companyCode.eq(dataInfoExcelModel.getCompanyCode()))
                .and(qCaseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue())));
        //已有的案件池
        Set<String> caseInfoSets = new HashSet<>();
        for (Iterator<CaseInfo> it = caseInfoIterable.iterator(); it.hasNext(); ) {
            caseInfoSets.add(it.next().getId());
        }
        return caseInfoSets;
    }

    /**
     * 案件进入异常池
     */
    private CaseInfoException addCaseInfoException(DataInfoExcelModel dataInfoExcelModel, User user, Set<String> caseInfoDistributedSets, Set<String> caseInfoSets) {
        CaseInfoException caseInfoException = new CaseInfoException();
        BeanUtils.copyProperties(dataInfoExcelModel, caseInfoException);
        caseInfoException.setCommissionRate(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getCommissionRate(), null, null));
        caseInfoException.setContractAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getContractAmount(), null, null));
        caseInfoException.setHasPayAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getHasPayAmount(), null, null));
        caseInfoException.setOtherAmt(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOtherAmt(), null, null));
        caseInfoException.setLatelyPayAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getLatelyPayAmount(), null, null));
        caseInfoException.setLeftCapital(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getLeftCapital(), null, null));
        caseInfoException.setOverdueAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueAmount(), null, null));
        caseInfoException.setLeftInterest(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getLeftInterest(), null, null));
        caseInfoException.setOverdueCapital(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueCapital(), null, null));
        caseInfoException.setOverdueDelayFine(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueDelayFine(), null, null));
        caseInfoException.setPerPayAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getPerPayAmount(), null, null));
        caseInfoException.setOverdueManageFee(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueManageFee(), null, null));
        caseInfoException.setOverDueInterest(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverDueInterest(), null, null));
        caseInfoException.setOverdueFine(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueFine(), null, null));
        //待分配案件重复
        caseInfoException.setDistributeRepeat(ZWStringUtils.collectionToString(caseInfoDistributedSets, ","));
        //已分配案件重复
        caseInfoException.setAssignedRepeat(ZWStringUtils.collectionToString(caseInfoSets, ","));
        caseInfoException.setRepeatStatus(CaseInfoException.RepeatStatusEnum.PENDING.getValue());
        //操作者
        caseInfoException.setOperatorTime(ZWDateUtil.getNowDateTime());
        caseInfoException.setOperator(user.getId());
        caseInfoException.setOperatorName(user.getRealName());
        caseInfoException.setCaseHandNum(dataInfoExcelModel.getCaseHandNum()); //手数
        caseInfoException.setRecoverRemark(CaseInfo.RecoverRemark.NOT_RECOVERED.getValue());//未回收
        return caseInfoException;
    }


    /**
     * 案件计入待分配池中
     *
     * @param dataInfoExcelModel
     * @param product
     * @param user
     * @param personal
     * @return
     */
    private CaseInfoDistributed addCaseInfoDistributed(DataInfoExcelModel dataInfoExcelModel, Product product, User user, Personal personal) {
        CaseInfoDistributed caseInfoDistributed = new CaseInfoDistributed();
        caseInfoDistributed.setArea(areaHandler(dataInfoExcelModel));
        caseInfoDistributed.setPersonalInfo(personal);
        caseInfoDistributed.setBatchNumber(dataInfoExcelModel.getBatchNumber());
        caseInfoDistributed.setCaseNumber(dataInfoExcelModel.getCaseNumber());
        caseInfoDistributed.setProduct(product);
        caseInfoDistributed.setContractNumber(dataInfoExcelModel.getContractNumber());
        caseInfoDistributed.setContractAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getContractAmount(), null, null));
        caseInfoDistributed.setOverdueAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueAmount(), null, null));
        caseInfoDistributed.setLeftCapital(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getLeftCapital(), null, null));
        caseInfoDistributed.setLeftInterest(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getLeftInterest(), null, null));
        caseInfoDistributed.setOverdueCapital(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueCapital(), null, null));
        caseInfoDistributed.setOverdueInterest(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverDueInterest(), null, null));
        caseInfoDistributed.setOverdueFine(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueFine(), null, null));
        caseInfoDistributed.setOverdueDelayFine(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueDelayFine(), null, null));
        caseInfoDistributed.setPeriods(dataInfoExcelModel.getPeriods());
        caseInfoDistributed.setPerDueDate(dataInfoExcelModel.getPerDueDate());
        caseInfoDistributed.setPerPayAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getPerPayAmount(), null, null));
        caseInfoDistributed.setOverduePeriods(dataInfoExcelModel.getOverDuePeriods());
        caseInfoDistributed.setOverdueDays(dataInfoExcelModel.getOverDueDays());
        caseInfoDistributed.setHasPayAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getHasPayAmount(), null, null));
        caseInfoDistributed.setHasPayPeriods(dataInfoExcelModel.getHasPayPeriods());
        caseInfoDistributed.setLatelyPayDate(dataInfoExcelModel.getLatelyPayDate());
        caseInfoDistributed.setLatelyPayAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getLatelyPayAmount(), null, null));
        caseInfoDistributed.setCaseType(CaseInfo.CaseType.DISTRIBUTE.getValue());
        caseInfoDistributed.setPayStatus(dataInfoExcelModel.getPaymentStatus());
        caseInfoDistributed.setPrincipalId(principalRepository.findByCode(dataInfoExcelModel.getPrinCode()));
        caseInfoDistributed.setCollectionStatus(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue());
        caseInfoDistributed.setDelegationDate(dataInfoExcelModel.getDelegationDate());
        caseInfoDistributed.setCloseDate(dataInfoExcelModel.getCloseDate());
        caseInfoDistributed.setCommissionRate(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getCommissionRate(), 4, null));
        caseInfoDistributed.setHandNumber(dataInfoExcelModel.getCaseHandNum());
        caseInfoDistributed.setLoanDate(dataInfoExcelModel.getLoanDate());
        caseInfoDistributed.setOverdueManageFee(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueManageFee(), null, null));
        caseInfoDistributed.setHandUpFlag(CaseInfo.HandUpFlag.NO_HANG.getValue());
        caseInfoDistributed.setOtherAmt(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOtherAmt(), null, null));
        caseInfoDistributed.setOperator(user);
        caseInfoDistributed.setOperatorTime(ZWDateUtil.getNowDateTime());
        caseInfoDistributed.setCompanyCode(dataInfoExcelModel.getCompanyCode());
        caseInfoDistributed.setCaseMark(CaseInfo.Color.NO_COLOR.getValue()); //案件颜色标记
        caseInfoDistributed.setMemo(dataInfoExcelModel.getMemo()); //备注
        caseInfoDistributed.setFirstPayDate(dataInfoExcelModel.getFirstPayDate()); //首次还款日期
        caseInfoDistributed.setAccountAge(dataInfoExcelModel.getAccountAge()); //账龄
        caseInfoDistributed.setRecoverRemark(CaseInfo.RecoverRemark.NOT_RECOVERED.getValue()); //未回收
        caseInfoDistributed.setRecoverWay(dataInfoExcelModel.getRecoverWay()); //回收方式
        return caseInfoDistributed;
    }

    /**
     * 工作信息
     *
     * @param dataInfoExcelModel
     * @param user
     * @param personal
     */
    private void addPersonalJob(DataInfoExcelModel dataInfoExcelModel, User user, Personal personal) {
        if (StringUtils.isNotBlank(dataInfoExcelModel.getCompanyAddr()) || StringUtils.isNotBlank(dataInfoExcelModel.getCompanyName())
                || StringUtils.isNotBlank(dataInfoExcelModel.getCompanyPhone())) {
            PersonalJob personalJob = new PersonalJob();
            personalJob.setAddress(dataInfoExcelModel.getCompanyAddr());
            personalJob.setCompanyName(dataInfoExcelModel.getCompanyName());
            personalJob.setPhone(dataInfoExcelModel.getCompanyPhone());
            personalJob.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalJob.setOperator(user.getId());
            personalJob.setPersonalId(personal.getId());
            personalJobRepository.save(personalJob);
        }
    }


    /**
     * 创建客户信息
     *
     * @param dataInfoExcelModel
     * @param user
     * @return
     */
    private Personal createPersonal(DataInfoExcelModel dataInfoExcelModel, User user) {
        //创建客户信息
        Personal personal = new Personal();
        personal.setName(dataInfoExcelModel.getPersonalName());
        String sex = IdcardUtils.getGenderByIdCard(dataInfoExcelModel.getIdCard());
        setPersonalSex(personal, sex);
        personal.setAge(IdcardUtils.getAgeByIdCard(dataInfoExcelModel.getIdCard()));
        personal.setMobileNo(dataInfoExcelModel.getMobileNo());
        personal.setMobileStatus(Personal.PhoneStatus.UNKNOWN.getValue());
        personal.setIdCard(dataInfoExcelModel.getIdCard());
        personal.setIdCardAddress(dataInfoExcelModel.getIdCardAddress());
        personal.setLocalPhoneNo(dataInfoExcelModel.getHomePhone());
        //现居住地址
        personal.setLocalHomeAddress(dataInfoExcelModel.getHomeAddress());
        personal.setOperator(user.getId());
        personal.setOperatorTime(ZWDateUtil.getNowDateTime());
        personal.setCompanyCode(dataInfoExcelModel.getCompanyCode());
        personal.setDataSource(Constants.DataSource.IMPORT.getValue());
        personal.setNumber(dataInfoExcelModel.getPersonalNumber()); //客户号
        //婚姻状况
        if (Objects.equals(StringUtils.trim(dataInfoExcelModel.getMarital()), "已婚")) {
            personal.setMarital(Personal.MARITAL.MARRIED.getValue());
        } else if (Objects.equals(StringUtils.trim(dataInfoExcelModel.getMarital()), "未婚")) {
            personal.setMarital(Personal.MARITAL.UNMARRIED.getValue());
        } else
            personal.setMarital(Personal.MARITAL.UNKNOW.getValue());
        return personal;
    }

    private void setPersonalSex(Personal personal, String sex) {
        if ("M".equals(sex)) {
            personal.setSex(Personal.SexEnum.MAN.getValue());
        } else if ("F".equals(sex)) {
            personal.setSex(Personal.SexEnum.WOMEN.getValue());
        } else {
            personal.setSex(Personal.SexEnum.UNKNOWN.getValue());
        }
    }

    /**
     * 添加或更新联系人信息
     *
     * @param dataInfoExcelModel
     * @param user
     * @param personal
     */
    private void addContract(DataInfoExcelModel dataInfoExcelModel, User user, Personal personal) {
        List<PersonalContact> personalContactList = new ArrayList<>();
        // 先添加客户本人的信息
        PersonalContact personalContact = new PersonalContact();
        personalContact.setPersonalId(personal.getId());
        personalContact.setRelation(Personal.RelationEnum.SELF.getValue());
        personalContact.setName(dataInfoExcelModel.getPersonalName());
        personalContact.setPhone(dataInfoExcelModel.getMobileNo());
        personalContact.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
        personalContact.setMobile(dataInfoExcelModel.getHomePhone());
        personalContact.setIdCard(dataInfoExcelModel.getIdCard());
        personalContact.setEmployer(dataInfoExcelModel.getCompanyName());
        personalContact.setWorkPhone(dataInfoExcelModel.getCompanyPhone());
        personalContact.setSource(dataInfoExcelModel.getDataSources());
        personalContact.setAddress(dataInfoExcelModel.getHomeAddress());
        personalContact.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
        personalContact.setOperator(user.getId());
        personalContact.setOperatorTime(ZWDateUtil.getNowDateTime());
        personalContactList.add(personalContact);
        // 联系人1信息
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactName1())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactPhone1())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactHomePhone1())) {
            setContactValue(dataInfoExcelModel, user, personal, personalContactList, getRelationType(dataInfoExcelModel.getContactRelation1()), dataInfoExcelModel.getContactName1(), dataInfoExcelModel.getContactPhone1(), dataInfoExcelModel.getContactHomePhone1(), dataInfoExcelModel.getContactWorkUnit1(), dataInfoExcelModel.getContactUnitPhone1(), dataInfoExcelModel.getContactCurrAddress1());
        }
        //联系人2信息
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactName2())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactPhone2())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactHomePhone2())) {
            setContactValue(dataInfoExcelModel, user, personal, personalContactList, getRelationType(dataInfoExcelModel.getContactRelation2()), dataInfoExcelModel.getContactName2(), dataInfoExcelModel.getContactPhone2(), dataInfoExcelModel.getContactHomePhone2(), dataInfoExcelModel.getContactWorkUnit2(), dataInfoExcelModel.getContactUnitPhone2(), dataInfoExcelModel.getContactCurrAddress2());
        }
        //联系人3信息
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactName3())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactPhone3())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactHomePhone3())) {
            setContactValue(dataInfoExcelModel, user, personal, personalContactList, getRelationType(dataInfoExcelModel.getContactRelation3()), dataInfoExcelModel.getContactName3(), dataInfoExcelModel.getContactPhone3(), dataInfoExcelModel.getContactHomePhone3(), dataInfoExcelModel.getContactWorkUnit3(), dataInfoExcelModel.getContactUnitPhone3(), dataInfoExcelModel.getContactCurrAddress3());
        }
        //联系人4信息
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactName4())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactPhone4())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactHomePhone4())) {
            setContactValue(dataInfoExcelModel, user, personal, personalContactList, getRelationType(dataInfoExcelModel.getContactRelation4()), dataInfoExcelModel.getContactName4(), dataInfoExcelModel.getContactPhone4(), dataInfoExcelModel.getContactHomePhone4(), dataInfoExcelModel.getContactWorkUnit4(), dataInfoExcelModel.getContactUnitPhone4(), dataInfoExcelModel.getContactCurrAddress4());
        }
        //联系人5信息
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactName5())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactPhone5())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactHomePhone5())) {
            setContactValue(dataInfoExcelModel, user, personal, personalContactList, getRelationType(dataInfoExcelModel.getContactRelation5()), dataInfoExcelModel.getContactName5(), dataInfoExcelModel.getContactPhone5(), dataInfoExcelModel.getContactHomePhone5(), dataInfoExcelModel.getContactWorkUnit5(), dataInfoExcelModel.getContactUnitPhone5(), dataInfoExcelModel.getContactCurrAddress5());
        }
        //联系人6信息
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactName6())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactPhone6())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactHomePhone6())) {
            setContactValue(dataInfoExcelModel, user, personal, personalContactList, getRelationType(dataInfoExcelModel.getContactRelation6()), dataInfoExcelModel.getContactName6(), dataInfoExcelModel.getContactPhone6(), dataInfoExcelModel.getContactHomePhone6(), dataInfoExcelModel.getContactWorkUnit6(), dataInfoExcelModel.getContactUnitPhone6(), dataInfoExcelModel.getContactCurrAddress6());
        }
        //联系人7信息
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactName7())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactPhone7())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactHomePhone7())) {
            setContactValue(dataInfoExcelModel, user, personal, personalContactList, getRelationType(dataInfoExcelModel.getContactRelation7()), dataInfoExcelModel.getContactName7(), dataInfoExcelModel.getContactPhone7(), dataInfoExcelModel.getContactHomePhone7(), dataInfoExcelModel.getContactWorkUnit7(), dataInfoExcelModel.getContactUnitPhone7(), dataInfoExcelModel.getContactCurrAddress7());
        }
        //联系人8信息
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactName8())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactPhone8())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactHomePhone8())) {
            setContactValue(dataInfoExcelModel, user, personal, personalContactList, getRelationType(dataInfoExcelModel.getContactRelation8()), dataInfoExcelModel.getContactName8(), dataInfoExcelModel.getContactPhone8(), dataInfoExcelModel.getContactHomePhone8(), dataInfoExcelModel.getContactWorkUnit8(), dataInfoExcelModel.getContactUnitPhone8(), dataInfoExcelModel.getContactCurrAddress8());
        }
        //联系人9信息
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactName9())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactPhone9())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactHomePhone9())) {
            setContactValue(dataInfoExcelModel, user, personal, personalContactList, getRelationType(dataInfoExcelModel.getContactRelation9()), dataInfoExcelModel.getContactName9(), dataInfoExcelModel.getContactPhone9(), dataInfoExcelModel.getContactHomePhone9(), dataInfoExcelModel.getContactWorkUnit9(), dataInfoExcelModel.getContactUnitPhone9(), dataInfoExcelModel.getContactCurrAddress9());
        }
        //联系人10信息
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactName10())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactPhone10())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactHomePhone10())) {
            setContactValue(dataInfoExcelModel, user, personal, personalContactList, getRelationType(dataInfoExcelModel.getContactRelation10()), dataInfoExcelModel.getContactName10(), dataInfoExcelModel.getContactPhone10(), dataInfoExcelModel.getContactHomePhone10(), dataInfoExcelModel.getContactWorkUnit10(), dataInfoExcelModel.getContactUnitPhone10(), dataInfoExcelModel.getContactCurrAddress10());
        }
        //联系人11信息
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactName11())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactPhone11())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactHomePhone11())) {
            setContactValue(dataInfoExcelModel, user, personal, personalContactList, getRelationType(dataInfoExcelModel.getContactRelation11()), dataInfoExcelModel.getContactName11(), dataInfoExcelModel.getContactPhone11(), dataInfoExcelModel.getContactHomePhone11(), dataInfoExcelModel.getContactWorkUnit11(), dataInfoExcelModel.getContactUnitPhone11(), dataInfoExcelModel.getContactCurrAddress11());
        }
        //联系人12信息
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactName12())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactPhone12())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactHomePhone12())) {
            setContactValue(dataInfoExcelModel, user, personal, personalContactList, getRelationType(dataInfoExcelModel.getContactRelation12()), dataInfoExcelModel.getContactName12(), dataInfoExcelModel.getContactPhone12(), dataInfoExcelModel.getContactHomePhone12(), dataInfoExcelModel.getContactWorkUnit12(), dataInfoExcelModel.getContactUnitPhone12(), dataInfoExcelModel.getContactCurrAddress12());
        }
        //联系人13信息
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactName13())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactPhone13())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactHomePhone13())) {
            setContactValue(dataInfoExcelModel, user, personal, personalContactList, getRelationType(dataInfoExcelModel.getContactRelation13()), dataInfoExcelModel.getContactName13(), dataInfoExcelModel.getContactPhone13(), dataInfoExcelModel.getContactHomePhone13(), dataInfoExcelModel.getContactWorkUnit13(), dataInfoExcelModel.getContactUnitPhone13(), dataInfoExcelModel.getContactCurrAddress13());
        }
        //联系人14信息
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactName14())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactPhone14())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactHomePhone14())) {
            setContactValue(dataInfoExcelModel, user, personal, personalContactList, getRelationType(dataInfoExcelModel.getContactRelation14()), dataInfoExcelModel.getContactName14(), dataInfoExcelModel.getContactPhone14(), dataInfoExcelModel.getContactHomePhone14(), dataInfoExcelModel.getContactWorkUnit14(), dataInfoExcelModel.getContactUnitPhone14(), dataInfoExcelModel.getContactCurrAddress14());
        }
        //联系人15信息
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactName15())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactPhone15())
                || StringUtils.isNotBlank(dataInfoExcelModel.getContactHomePhone15())) {
            setContactValue(dataInfoExcelModel, user, personal, personalContactList, getRelationType(dataInfoExcelModel.getContactRelation15()), dataInfoExcelModel.getContactName15(), dataInfoExcelModel.getContactPhone15(), dataInfoExcelModel.getContactHomePhone15(), dataInfoExcelModel.getContactWorkUnit15(), dataInfoExcelModel.getContactUnitPhone15(), dataInfoExcelModel.getContactCurrAddress15());
        }
        personalContactRepository.save(personalContactList);
    }

    private void setContactValue(DataInfoExcelModel dataInfoExcelModel, User user, Personal personal, List<PersonalContact> personalContactList, Integer relationType, String contactName, String contactPhone, String contactHomePhone, String contactWorkUnit, String contactUnitPhone, String contactCurrAddress) {
        PersonalContact obj = new PersonalContact();
        obj.setPersonalId(personal.getId());
        obj.setRelation(relationType);
        obj.setName(contactName);
        obj.setPhone(contactPhone);
        obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
        obj.setMobile(contactHomePhone);
        obj.setEmployer(contactWorkUnit);
        obj.setWorkPhone(contactUnitPhone);
        obj.setSource(dataInfoExcelModel.getDataSources());
        obj.setAddress(contactCurrAddress);
        obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
        obj.setOperator(user.getId());
        obj.setOperatorTime(ZWDateUtil.getNowDateTime());
        personalContactList.add(obj);
    }

    /**
     * 更新或新增地址信息
     *
     * @param dataInfoExcelModel
     * @param user
     * @param personal
     */
    private void addAddr(DataInfoExcelModel dataInfoExcelModel, User user, Personal personal) {
        List<PersonalAddress> personalAddressList = new ArrayList<>();
        //居住地址(个人)
        if (StringUtils.isNotBlank(dataInfoExcelModel.getHomeAddress())) {
            setPersonalAddressValue(user, personal, personalAddressList, Personal.RelationEnum.SELF.getValue(), dataInfoExcelModel.getPersonalName(), Personal.AddrRelationEnum.CURRENTADDR.getValue(), dataInfoExcelModel.getHomeAddress());
        }

        //身份证户籍地址（个人）
        if (StringUtils.isNotBlank(dataInfoExcelModel.getIdCardAddress())) {
            setPersonalAddressValue(user, personal, personalAddressList, Personal.RelationEnum.SELF.getValue(), dataInfoExcelModel.getPersonalName(), Personal.AddrRelationEnum.IDCARDADDR.getValue(), dataInfoExcelModel.getIdCardAddress());
        }

        //工作单位地址（个人）
        if (StringUtils.isNotBlank(dataInfoExcelModel.getCompanyAddr())) {
            setPersonalAddressValue(user, personal, personalAddressList, Personal.RelationEnum.SELF.getValue(), dataInfoExcelModel.getPersonalName(), Personal.AddrRelationEnum.UNITADDR.getValue(), dataInfoExcelModel.getCompanyAddr());
        }

        //居住地址(联系人1)
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress1())) {
            setPersonalAddressValue(user, personal, personalAddressList, getRelationType(dataInfoExcelModel.getContactRelation1()), dataInfoExcelModel.getContactName1(), Personal.AddrRelationEnum.CURRENTADDR.getValue(), dataInfoExcelModel.getContactCurrAddress1());
        }

        //居住地址(联系人2)
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress2())) {
            setPersonalAddressValue(user, personal, personalAddressList, getRelationType(dataInfoExcelModel.getContactRelation2()), dataInfoExcelModel.getContactName2(), Personal.AddrRelationEnum.CURRENTADDR.getValue(), dataInfoExcelModel.getContactCurrAddress2());
        }

        //居住地址(联系人3)
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress3())) {
            setContactAddressValue(user, personal, personalAddressList, getRelationType(dataInfoExcelModel.getContactRelation3()), dataInfoExcelModel.getContactName3(), dataInfoExcelModel.getContactCurrAddress3());

        }

        //居住地址(联系人4)
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress4())) {
            setContactAddressValue(user, personal, personalAddressList, getRelationType(dataInfoExcelModel.getContactRelation4()), dataInfoExcelModel.getContactName4(), dataInfoExcelModel.getContactCurrAddress4());
        }
        //居住地址(联系人5)
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress5())) {
            setContactAddressValue(user, personal, personalAddressList, getRelationType(dataInfoExcelModel.getContactRelation5()), dataInfoExcelModel.getContactName5(), dataInfoExcelModel.getContactCurrAddress5());
        }
        //居住地址(联系人6)
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress6())) {
            setContactAddressValue(user, personal, personalAddressList, getRelationType(dataInfoExcelModel.getContactRelation6()), dataInfoExcelModel.getContactName6(), dataInfoExcelModel.getContactCurrAddress6());
        }
        //居住地址(联系人7)
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress7())) {
            setContactAddressValue(user, personal, personalAddressList, getRelationType(dataInfoExcelModel.getContactRelation7()), dataInfoExcelModel.getContactName7(), dataInfoExcelModel.getContactCurrAddress7());
        }
        //居住地址(联系人8)
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress8())) {
            setContactAddressValue(user, personal, personalAddressList, getRelationType(dataInfoExcelModel.getContactRelation8()), dataInfoExcelModel.getContactName8(), dataInfoExcelModel.getContactCurrAddress8());
        }
        //居住地址(联系人9)
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress9())) {
            setContactAddressValue(user, personal, personalAddressList, getRelationType(dataInfoExcelModel.getContactRelation9()), dataInfoExcelModel.getContactName9(), dataInfoExcelModel.getContactCurrAddress9());
        }
        //居住地址(联系人10)
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress10())) {
            setContactAddressValue(user, personal, personalAddressList, getRelationType(dataInfoExcelModel.getContactRelation10()), dataInfoExcelModel.getContactName10(), dataInfoExcelModel.getContactCurrAddress10());
        }
        //居住地址(联系人11)
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress11())) {
            setContactAddressValue(user, personal, personalAddressList, getRelationType(dataInfoExcelModel.getContactRelation11()), dataInfoExcelModel.getContactName11(), dataInfoExcelModel.getContactCurrAddress11());
        }
        //居住地址(联系人12)
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress12())) {
            setContactAddressValue(user, personal, personalAddressList, getRelationType(dataInfoExcelModel.getContactRelation12()), dataInfoExcelModel.getContactName12(), dataInfoExcelModel.getContactCurrAddress12());
        }
        //居住地址(联系人13)
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress13())) {
            setContactAddressValue(user, personal, personalAddressList, getRelationType(dataInfoExcelModel.getContactRelation13()), dataInfoExcelModel.getContactName13(), dataInfoExcelModel.getContactCurrAddress13());
        }
        //居住地址(联系人14)
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress14())) {
            setContactAddressValue(user, personal, personalAddressList, getRelationType(dataInfoExcelModel.getContactRelation14()), dataInfoExcelModel.getContactName14(), dataInfoExcelModel.getContactCurrAddress14());
        }
        //居住地址(联系人15)
        if (StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress15())) {
            setContactAddressValue(user, personal, personalAddressList, getRelationType(dataInfoExcelModel.getContactRelation15()), dataInfoExcelModel.getContactName4(), dataInfoExcelModel.getContactCurrAddress15());
        }
        persosnalAddressRepository.save(personalAddressList);
    }

    private void setPersonalAddressValue(User user, Personal personal, List<PersonalAddress> personalAddressList, Integer value, String personalName, Integer value3, String homeAddress) {
        PersonalAddress personalAddress = new PersonalAddress();
        personalAddress.setPersonalId(personal.getId());
        personalAddress.setRelation(value);
        personalAddress.setName(personalName);
        personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
        personalAddress.setType(value3);
        personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
        personalAddress.setDetail(homeAddress);
        personalAddress.setOperator(user.getId());
        personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
        personalAddressList.add(personalAddress);
    }

    private void setContactAddressValue(User user, Personal personal, List<PersonalAddress> personalAddressList, Integer relationType, String contactName4, String contactCurrAddress4) {
        PersonalAddress personalAddress = new PersonalAddress();
        personalAddress.setPersonalId(personal.getId());
        personalAddress.setRelation(relationType);
        personalAddress.setName(contactName4);
        personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
        personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
        personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
        personalAddress.setDetail(contactCurrAddress4);
        personalAddress.setOperator(user.getId());
        personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
        personalAddressList.add(personalAddress);
    }

    /**
     * 开户信息
     *
     * @param dataInfoExcelModel
     * @param user
     * @param personal
     */
    private void addBankInfo(DataInfoExcelModel dataInfoExcelModel, User user, Personal personal) {
        if (StringUtils.isNotBlank(dataInfoExcelModel.getDepositBank()) ||
                StringUtils.isNotBlank(dataInfoExcelModel.getCardNumber())) {
            PersonalBank personalBank = new PersonalBank();
            personalBank.setDepositBank(dataInfoExcelModel.getDepositBank());
            personalBank.setCardNumber(dataInfoExcelModel.getCardNumber());
            personalBank.setPersonalId(personal.getId());
            personalBank.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalBank.setOperator(user.getId());
            personalBank.setAccountNumber(dataInfoExcelModel.getAccountNumber());//开户号
            personalBankRepository.save(personalBank);
        }
    }

    /**
     * 新增或更新产品及系列名称
     *
     * @param dataInfoExcelModel
     * @param user
     */
    private Product addProducts(DataInfoExcelModel dataInfoExcelModel, User user) {
        ProductSeries productSeries = null;
        if (StringUtils.isNotBlank(dataInfoExcelModel.getProductSeriesName())) {
            productSeries = new ProductSeries();
            productSeries.setSeriesName(dataInfoExcelModel.getProductSeriesName());
            productSeries.setOperator(user.getId());
            productSeries.setOperatorTime(ZWDateUtil.getNowDateTime());
            productSeries.setPrincipal_id(dataInfoExcelModel.getPrinCode());
            productSeries.setCompanyCode(dataInfoExcelModel.getCompanyCode());
            productSeries = productSeriesRepository.save(productSeries);
        }
        //产品名称
        Product product = null;
        if (StringUtils.isNotBlank(dataInfoExcelModel.getProductName())) {
            product = new Product();
            product.setProdcutName(dataInfoExcelModel.getProductName());
            product.setOperator(user.getId());
            product.setOperatorTime(ZWDateUtil.getNowDateTime());
            product.setCompanyCode(dataInfoExcelModel.getCompanyCode());
            product.setProductSeries(productSeries);
            product = productRepository.save(product);
        }
        return product;
    }


    /**
     * 解析居住地址
     *
     * @param dataInfoExcelModel
     */
    private String nowLivingAddr(DataInfoExcelModel dataInfoExcelModel, String addr) {
        if (StringUtils.isBlank(addr)) {
            return null;
        }
        String province = null;
        if (Objects.isNull(dataInfoExcelModel.getProvince())) {
            province = "";
        } else {
            province = dataInfoExcelModel.getProvince();
        }
        String city = null;
        if (Objects.isNull(dataInfoExcelModel.getCity())) {
            city = "";
        } else {
            city = dataInfoExcelModel.getCity();
        }
        //现居住地地址
        if (addr.startsWith(province)) {
            return addr;
        } else if (addr.startsWith(city)) {
            return dataInfoExcelModel.getProvince().concat(addr);
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
        if (StringUtils.isNotBlank(relationName)) {
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
     * @param dataInfoExcelModel
     * @return
     */
    private AreaCode areaHandler(DataInfoExcelModel dataInfoExcelModel) {
        List<String> personalAreaList = new ArrayList<>();
        List<String> emptyList = new ArrayList<>();
        personalAreaList.add(dataInfoExcelModel.getCity());
        personalAreaList.add(dataInfoExcelModel.getHomeAddress());
        personalAreaList.add(dataInfoExcelModel.getIdCardAddress());
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
