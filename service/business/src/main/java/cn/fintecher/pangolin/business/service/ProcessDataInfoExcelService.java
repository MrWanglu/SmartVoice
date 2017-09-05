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

import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: PeiShouWen
 * @Description: 开始处理发过来的案件数据
 * @Date 16:26 2017/7/24
 */
@Service("processDataInfoExcelService")
public class ProcessDataInfoExcelService {

    private final Logger logger= LoggerFactory.getLogger(ProcessDataInfoExcelService.class);

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
    @Transactional
    public void doTask(DataInfoExcelModel dataInfoExcelModel,ConcurrentHashMap<String,DataInfoExcelModel> dataInfoExcelModelMap,
                       User user,int index){
            logger.info("{}  处理案件信息开始 {}......",Thread.currentThread(),index);

            //案件数据信息
            String key=dataInfoExcelModel.getPersonalName().concat("_").concat(dataInfoExcelModel.getIdCard()).concat("_")
                    .concat(dataInfoExcelModel.getPrinCode()).concat("_").concat(dataInfoExcelModel.getProductName())
                    .concat("_").concat(dataInfoExcelModel.getCompanyCode());
            //案件附件信息
            List<CaseInfoFile> caseInfoFileList=dataInfoExcelModel.getCaseInfoFileList();
            //产品信息
            Product product=null;
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
                caseInfoExceptionRepository.save(addCaseInfoException(dataInfoExcelModel, user, caseInfoDistributedSets, caseInfoSets));
            } else {
                dataInfoExcelModelMap.put(key,dataInfoExcelModel);
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
        logger.info("{}  处理案件信息结束 {}.",Thread.currentThread(),index);
    }


    /**
     * 检查案件是否已存在
     * @param dataInfoExcelModel
     * @return
     */
    private  Set<String> checkCaseInfoExist(DataInfoExcelModel dataInfoExcelModel) {
        /**
         * 检查已有案件是否存在，存在的话直接进入案件异常表，异常表的数据结构与接收数据的DataInfoExcel相同,关联信息信息不做处理
         * (客户姓名、身份证号、委托方ID、产品名称、公司码)
         */
        QCaseInfo qCaseInfo=QCaseInfo.caseInfo;
        Iterable<CaseInfo> caseInfoIterable = caseInfoRepository.findAll(qCaseInfo.personalInfo.name.eq(dataInfoExcelModel.getPersonalName())
                .and(qCaseInfo.personalInfo.idCard.eq(dataInfoExcelModel.getIdCard()))
                .and(qCaseInfo.principalId.code.eq(dataInfoExcelModel.getPrinCode()))
                .and(qCaseInfo.product.prodcutName.eq(dataInfoExcelModel.getProductName()))
                .and(qCaseInfo.companyCode.eq(dataInfoExcelModel.getCompanyCode()))
                .and(qCaseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue())));
        //已有的案件池
        Set<String> caseInfoSets=new HashSet<>();
        for(Iterator<CaseInfo> it=caseInfoIterable.iterator();it.hasNext();){
            caseInfoSets.add(it.next().getId());
        }
        return caseInfoSets;
    }

    /**
     * 案件进入异常池
     *
     */
    private CaseInfoException addCaseInfoException(DataInfoExcelModel dataInfoExcelModel,User user,Set<String> caseInfoDistributedSets,Set<String> caseInfoSets) {
        CaseInfoException caseInfoException=new CaseInfoException();
        BeanUtils.copyProperties(dataInfoExcelModel,caseInfoException);
        caseInfoException.setCommissionRate(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getCommissionRate(),null,null));
        caseInfoException.setContractAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getContractAmount(),null,null));
        caseInfoException.setHasPayAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getHasPayAmount(),null,null));
        caseInfoException.setOtherAmt(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOtherAmt(),null,null));
        caseInfoException.setLatelyPayAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getLatelyPayAmount(),null,null));
        caseInfoException.setLeftCapital(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getLeftCapital(),null,null));
        caseInfoException.setOverdueAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueAmount(),null,null));
        caseInfoException.setLeftInterest(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getLeftInterest(),null,null));
        caseInfoException.setOverdueCapital(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueCapital(),null,null));
        caseInfoException.setOverdueDelayFine(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueDelayFine(),null,null));
        caseInfoException.setPerPayAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getPerPayAmount(),null,null));
        caseInfoException.setOverdueManageFee(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueManageFee(),null,null));
        caseInfoException.setOverDueInterest(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverDueInterest(),null,null));
        caseInfoException.setOverdueFine(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueFine(),null,null));
        //待分配案件重复
        caseInfoException.setDistributeRepeat(ZWStringUtils.collectionToString(caseInfoDistributedSets,","));
        //已分配案件重复
        caseInfoException.setAssignedRepeat(ZWStringUtils.collectionToString(caseInfoSets,","));
        caseInfoException.setRepeatStatus(CaseInfoException.RepeatStatusEnum.PENDING.getValue());
        //操作者
        caseInfoException.setOperatorTime(ZWDateUtil.getNowDateTime());
        caseInfoException.setOperator(user.getId());
        caseInfoException.setOperatorName(user.getRealName());
        caseInfoException.setCaseHandNum(dataInfoExcelModel.getHandNumber()); //手数
        return caseInfoException;
    }


    /**
     * 案件计入待分配池中
     * @param dataInfoExcelModel
     * @param product
     * @param user
     * @param personal
     * @return
     */
    private CaseInfoDistributed addCaseInfoDistributed(DataInfoExcelModel dataInfoExcelModel, Product product, User user, Personal personal) {
        CaseInfoDistributed caseInfoDistributed=new CaseInfoDistributed();
        caseInfoDistributed.setArea(areaHandler(dataInfoExcelModel));
        caseInfoDistributed.setDepartment(user.getDepartment());
        caseInfoDistributed.setPersonalInfo(personal);
        caseInfoDistributed.setBatchNumber(dataInfoExcelModel.getBatchNumber());
        caseInfoDistributed.setCaseNumber(dataInfoExcelModel.getCaseNumber());
        caseInfoDistributed.setProduct(product);
        caseInfoDistributed.setContractNumber(dataInfoExcelModel.getContractNumber());
        caseInfoDistributed.setContractAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getContractAmount(),null,null));
        caseInfoDistributed.setOverdueAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueAmount(),null,null));
        caseInfoDistributed.setLeftCapital(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getLeftCapital(),null,null) );
        caseInfoDistributed.setLeftInterest(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getLeftInterest(),null,null));
        caseInfoDistributed.setOverdueCapital(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueCapital(),null,null));
        caseInfoDistributed.setOverdueInterest(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverDueInterest(),null,null));
        caseInfoDistributed.setOverdueFine(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueFine(),null,null));
        caseInfoDistributed.setOverdueDelayFine(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueDelayFine(),null,null));
        caseInfoDistributed.setPeriods(dataInfoExcelModel.getPeriods());
        caseInfoDistributed.setPerDueDate(dataInfoExcelModel.getPerDueDate());
        caseInfoDistributed.setPerPayAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getPerPayAmount(),null,null));
        caseInfoDistributed.setOverduePeriods(dataInfoExcelModel.getOverDuePeriods());
        caseInfoDistributed.setOverdueDays(dataInfoExcelModel.getOverDueDays());
        caseInfoDistributed.setHasPayAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getHasPayAmount(),null,null));
        caseInfoDistributed.setHasPayPeriods(dataInfoExcelModel.getHasPayPeriods());
        caseInfoDistributed.setLatelyPayDate(dataInfoExcelModel.getLatelyPayDate());
        caseInfoDistributed.setLatelyPayAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getLatelyPayAmount(),null,null));
        caseInfoDistributed.setCaseType(CaseInfo.CaseType.DISTRIBUTE.getValue());
        caseInfoDistributed.setPayStatus(dataInfoExcelModel.getPaymentStatus());
        caseInfoDistributed.setPrincipalId(principalRepository.findByCode(dataInfoExcelModel.getPrinCode()));
        caseInfoDistributed.setCollectionStatus(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue());
        caseInfoDistributed.setDelegationDate(dataInfoExcelModel.getDelegationDate());
        caseInfoDistributed.setCloseDate(dataInfoExcelModel.getCloseDate());
        caseInfoDistributed.setCommissionRate(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getCommissionRate(),4,null));
//        caseInfoDistributed.setHandNumber(dataInfoExcelModel.getCaseHandNum());
        caseInfoDistributed.setHandNumber(dataInfoExcelModel.getHandNumber());
        caseInfoDistributed.setLoanDate(dataInfoExcelModel.getLoanDate());
        caseInfoDistributed.setOverdueManageFee(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueManageFee(),null,null));
        caseInfoDistributed.setHandUpFlag(CaseInfo.HandUpFlag.NO_HANG.getValue());
        caseInfoDistributed.setOtherAmt(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOtherAmt(),null,null));
        caseInfoDistributed.setOperator(user);
        caseInfoDistributed.setOperatorTime(ZWDateUtil.getNowDateTime());
        caseInfoDistributed.setCompanyCode(dataInfoExcelModel.getCompanyCode());
        caseInfoDistributed.setCaseMark(CaseInfo.Color.NO_COLOR.getValue()); //案件颜色标记
        caseInfoDistributed.setMemo(dataInfoExcelModel.getMemo()); //备注
        caseInfoDistributed.setFirstPayDate(dataInfoExcelModel.getFirstPayDate()); //首次还款日期
        caseInfoDistributed.setAccountAge(dataInfoExcelModel.getAccountAge()); //账龄
        return caseInfoDistributed;
    }

    /**
     * 工作信息
     * @param dataInfoExcelModel
     * @param user
     * @param personal
     */
    private void addPersonalJob(DataInfoExcelModel dataInfoExcelModel, User user, Personal personal) {
        if(StringUtils.isNotBlank(dataInfoExcelModel.getCompanyAddr()) || StringUtils.isNotBlank(dataInfoExcelModel.getCompanyName())
                || StringUtils.isNotBlank(dataInfoExcelModel.getCompanyPhone())){
            PersonalJob personalJob=new PersonalJob();
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
     * @param dataInfoExcelModel
     * @param user
     * @return
     */
    private Personal createPersonal(DataInfoExcelModel dataInfoExcelModel, User user) {
        //创建客户信息
        Personal personal=new Personal();
        personal.setName(dataInfoExcelModel.getPersonalName());
        String sex= IdcardUtils.getGenderByIdCard(dataInfoExcelModel.getIdCard());
        if("M".equals(sex)){
            personal.setSex(Personal.SexEnum.MAN.getValue());
        }else if("F".equals(sex)){
            personal.setSex(Personal.SexEnum.WOMEN.getValue());
        }else{
            personal.setSex(Personal.SexEnum.UNKNOWN.getValue());
        }
        personal.setAge(IdcardUtils.getAgeByIdCard(dataInfoExcelModel.getIdCard()));
        personal.setMobileNo(dataInfoExcelModel.getMobileNo());
        personal.setMobileStatus(Personal.PhoneStatus.UNKNOWN.getValue());
        personal.setIdCard(dataInfoExcelModel.getIdCard());
        personal.setIdCardAddress(dataInfoExcelModel.getIdCardAddress());
        personal.setLocalPhoneNo(dataInfoExcelModel.getHomePhone());
        //现居住地址
        personal.setLocalHomeAddress( nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getHomeAddress()));
        personal.setOperator(user.getId());
        personal.setOperatorTime(ZWDateUtil.getNowDateTime());
        personal.setCompanyCode(dataInfoExcelModel.getCompanyCode());
        personal.setDataSource(Constants.DataSource.IMPORT.getValue());
        personal.setMarital(Personal.MARITAL.UNKNOW.getValue());
        personal.setNumber(dataInfoExcelModel.getPersonalNumber()); //客户号
        return personal;
    }

    /**
     * 添加或更新联系人信息
     * @param dataInfoExcelModel
     * @param user
     * @param personal
     */
    private void addContract(DataInfoExcelModel dataInfoExcelModel, User user, Personal personal) {
        List<PersonalContact> personalContactList=new ArrayList<>();
        // 先添加客户本人的信息
        PersonalContact personalContact=new PersonalContact();
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
        if(Objects.nonNull(dataInfoExcelModel.getContactName1())
                || Objects.nonNull(dataInfoExcelModel.getContactPhone1())
                || Objects.nonNull(dataInfoExcelModel.getContactHomePhone1())){
            PersonalContact obj=new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(dataInfoExcelModel.getContactRelation1()));
            obj.setName(dataInfoExcelModel.getContactName1());
            obj.setPhone(dataInfoExcelModel.getContactPhone1());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(dataInfoExcelModel.getContactHomePhone1());
            obj.setEmployer(dataInfoExcelModel.getContactWorkUnit1());
            obj.setWorkPhone(dataInfoExcelModel.getContactUnitPhone1());
            obj.setSource(dataInfoExcelModel.getDataSources());
            obj.setAddress(dataInfoExcelModel.getContactCurrAddress1());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人2信息
        if(Objects.nonNull(dataInfoExcelModel.getContactName2())
                || Objects.nonNull(dataInfoExcelModel.getContactPhone2())
                || Objects.nonNull(dataInfoExcelModel.getContactHomePhone2())){
            PersonalContact obj=new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(dataInfoExcelModel.getContactRelation2()));
            obj.setName(dataInfoExcelModel.getContactName2());
            obj.setPhone(dataInfoExcelModel.getContactPhone2());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(dataInfoExcelModel.getContactHomePhone2());
            obj.setEmployer(dataInfoExcelModel.getContactWorkUnit2());
            obj.setWorkPhone(dataInfoExcelModel.getContactUnitPhone2());
            obj.setSource(dataInfoExcelModel.getDataSources());
            obj.setAddress(dataInfoExcelModel.getContactCurrAddress2());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人3信息
        if(Objects.nonNull(dataInfoExcelModel.getContactName3())
                || Objects.nonNull(dataInfoExcelModel.getContactPhone3())
                || Objects.nonNull(dataInfoExcelModel.getContactHomePhone3())){
            PersonalContact obj=new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(dataInfoExcelModel.getContactRelation3()));
            obj.setName(dataInfoExcelModel.getContactName3());
            obj.setPhone(dataInfoExcelModel.getContactPhone3());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(dataInfoExcelModel.getContactHomePhone3());
            obj.setEmployer(dataInfoExcelModel.getContactWorkUnit3());
            obj.setWorkPhone(dataInfoExcelModel.getContactUnitPhone3());
            obj.setSource(dataInfoExcelModel.getDataSources());
            obj.setAddress(dataInfoExcelModel.getContactCurrAddress3());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人4信息
        if(Objects.nonNull(dataInfoExcelModel.getContactName4())
                || Objects.nonNull(dataInfoExcelModel.getContactPhone4())
                || Objects.nonNull(dataInfoExcelModel.getContactHomePhone4())){
            PersonalContact obj=new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(dataInfoExcelModel.getContactRelation4()));
            obj.setName(dataInfoExcelModel.getContactName4());
            obj.setPhone(dataInfoExcelModel.getContactPhone4());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(dataInfoExcelModel.getContactHomePhone4());
            obj.setEmployer(dataInfoExcelModel.getContactWorkUnit4());
            obj.setWorkPhone(dataInfoExcelModel.getContactUnitPhone4());
            obj.setSource(dataInfoExcelModel.getDataSources());
            obj.setAddress(dataInfoExcelModel.getContactCurrAddress4());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人5信息
        if(Objects.nonNull(dataInfoExcelModel.getContactName5())
                || Objects.nonNull(dataInfoExcelModel.getContactPhone5())
                || Objects.nonNull(dataInfoExcelModel.getContactHomePhone5())){
            PersonalContact obj=new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(dataInfoExcelModel.getContactRelation5()));
            obj.setName(dataInfoExcelModel.getContactName5());
            obj.setPhone(dataInfoExcelModel.getContactPhone5());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(dataInfoExcelModel.getContactHomePhone5());
            obj.setEmployer(dataInfoExcelModel.getContactWorkUnit5());
            obj.setWorkPhone(dataInfoExcelModel.getContactUnitPhone5());
            obj.setSource(dataInfoExcelModel.getDataSources());
            obj.setAddress(dataInfoExcelModel.getContactCurrAddress5());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人6信息
        if(Objects.nonNull(dataInfoExcelModel.getContactName6())
                || Objects.nonNull(dataInfoExcelModel.getContactPhone6())
                || Objects.nonNull(dataInfoExcelModel.getContactHomePhone6())){
            PersonalContact obj=new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(dataInfoExcelModel.getContactRelation6()));
            obj.setName(dataInfoExcelModel.getContactName6());
            obj.setPhone(dataInfoExcelModel.getContactPhone6());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(dataInfoExcelModel.getContactHomePhone6());
            obj.setEmployer(dataInfoExcelModel.getContactWorkUnit6());
            obj.setWorkPhone(dataInfoExcelModel.getContactUnitPhone6());
            obj.setSource(dataInfoExcelModel.getDataSources());
            obj.setAddress(dataInfoExcelModel.getContactCurrAddress6());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人7信息
        if(Objects.nonNull(dataInfoExcelModel.getContactName7())
                || Objects.nonNull(dataInfoExcelModel.getContactPhone7())
                || Objects.nonNull(dataInfoExcelModel.getContactHomePhone7())){
            PersonalContact obj=new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(dataInfoExcelModel.getContactRelation7()));
            obj.setName(dataInfoExcelModel.getContactName7());
            obj.setPhone(dataInfoExcelModel.getContactPhone7());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(dataInfoExcelModel.getContactHomePhone7());
            obj.setEmployer(dataInfoExcelModel.getContactWorkUnit7());
            obj.setWorkPhone(dataInfoExcelModel.getContactUnitPhone7());
            obj.setSource(dataInfoExcelModel.getDataSources());
            obj.setAddress(dataInfoExcelModel.getContactCurrAddress7());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人8信息
        if(Objects.nonNull(dataInfoExcelModel.getContactName8())
                || Objects.nonNull(dataInfoExcelModel.getContactPhone8())
                || Objects.nonNull(dataInfoExcelModel.getContactHomePhone8())){
            PersonalContact obj=new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(dataInfoExcelModel.getContactRelation8()));
            obj.setName(dataInfoExcelModel.getContactName8());
            obj.setPhone(dataInfoExcelModel.getContactPhone8());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(dataInfoExcelModel.getContactHomePhone8());
            obj.setEmployer(dataInfoExcelModel.getContactWorkUnit8());
            obj.setWorkPhone(dataInfoExcelModel.getContactUnitPhone8());
            obj.setSource(dataInfoExcelModel.getDataSources());
            obj.setAddress(dataInfoExcelModel.getContactCurrAddress8());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人9信息
        if(Objects.nonNull(dataInfoExcelModel.getContactName9())
                || Objects.nonNull(dataInfoExcelModel.getContactPhone9())
                || Objects.nonNull(dataInfoExcelModel.getContactHomePhone9())){
            PersonalContact obj=new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(dataInfoExcelModel.getContactRelation9()));
            obj.setName(dataInfoExcelModel.getContactName9());
            obj.setPhone(dataInfoExcelModel.getContactPhone9());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(dataInfoExcelModel.getContactHomePhone9());
            obj.setEmployer(dataInfoExcelModel.getContactWorkUnit9());
            obj.setWorkPhone(dataInfoExcelModel.getContactUnitPhone9());
            obj.setSource(dataInfoExcelModel.getDataSources());
            obj.setAddress(dataInfoExcelModel.getContactCurrAddress9());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人10信息
        if(Objects.nonNull(dataInfoExcelModel.getContactName10())
                || Objects.nonNull(dataInfoExcelModel.getContactPhone10())
                || Objects.nonNull(dataInfoExcelModel.getContactHomePhone10())){
            PersonalContact obj=new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(dataInfoExcelModel.getContactRelation10()));
            obj.setName(dataInfoExcelModel.getContactName10());
            obj.setPhone(dataInfoExcelModel.getContactPhone10());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(dataInfoExcelModel.getContactHomePhone10());
            obj.setEmployer(dataInfoExcelModel.getContactWorkUnit10());
            obj.setWorkPhone(dataInfoExcelModel.getContactUnitPhone10());
            obj.setSource(dataInfoExcelModel.getDataSources());
            obj.setAddress(dataInfoExcelModel.getContactCurrAddress10());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人11信息
        if(Objects.nonNull(dataInfoExcelModel.getContactName11())
                || Objects.nonNull(dataInfoExcelModel.getContactPhone11())
                || Objects.nonNull(dataInfoExcelModel.getContactHomePhone11())){
            PersonalContact obj=new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(dataInfoExcelModel.getContactRelation11()));
            obj.setName(dataInfoExcelModel.getContactName11());
            obj.setPhone(dataInfoExcelModel.getContactPhone11());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(dataInfoExcelModel.getContactHomePhone11());
            obj.setEmployer(dataInfoExcelModel.getContactWorkUnit11());
            obj.setWorkPhone(dataInfoExcelModel.getContactUnitPhone11());
            obj.setSource(dataInfoExcelModel.getDataSources());
            obj.setAddress(dataInfoExcelModel.getContactCurrAddress11());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人12信息
        if(Objects.nonNull(dataInfoExcelModel.getContactName12())
                || Objects.nonNull(dataInfoExcelModel.getContactPhone12())
                || Objects.nonNull(dataInfoExcelModel.getContactHomePhone12())){
            PersonalContact obj=new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(dataInfoExcelModel.getContactRelation12()));
            obj.setName(dataInfoExcelModel.getContactName12());
            obj.setPhone(dataInfoExcelModel.getContactPhone12());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(dataInfoExcelModel.getContactHomePhone12());
            obj.setEmployer(dataInfoExcelModel.getContactWorkUnit12());
            obj.setWorkPhone(dataInfoExcelModel.getContactUnitPhone12());
            obj.setSource(dataInfoExcelModel.getDataSources());
            obj.setAddress(dataInfoExcelModel.getContactCurrAddress12());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人13信息
        if(Objects.nonNull(dataInfoExcelModel.getContactName13())
                || Objects.nonNull(dataInfoExcelModel.getContactPhone13())
                || Objects.nonNull(dataInfoExcelModel.getContactHomePhone13())){
            PersonalContact obj=new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(dataInfoExcelModel.getContactRelation13()));
            obj.setName(dataInfoExcelModel.getContactName13());
            obj.setPhone(dataInfoExcelModel.getContactPhone13());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(dataInfoExcelModel.getContactHomePhone13());
            obj.setEmployer(dataInfoExcelModel.getContactWorkUnit13());
            obj.setWorkPhone(dataInfoExcelModel.getContactUnitPhone13());
            obj.setSource(dataInfoExcelModel.getDataSources());
            obj.setAddress(dataInfoExcelModel.getContactCurrAddress13());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人14信息
        if(Objects.nonNull(dataInfoExcelModel.getContactName14())
                || Objects.nonNull(dataInfoExcelModel.getContactPhone14())
                || Objects.nonNull(dataInfoExcelModel.getContactHomePhone14())){
            PersonalContact obj=new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(dataInfoExcelModel.getContactRelation14()));
            obj.setName(dataInfoExcelModel.getContactName14());
            obj.setPhone(dataInfoExcelModel.getContactPhone14());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(dataInfoExcelModel.getContactHomePhone14());
            obj.setEmployer(dataInfoExcelModel.getContactWorkUnit14());
            obj.setWorkPhone(dataInfoExcelModel.getContactUnitPhone14());
            obj.setSource(dataInfoExcelModel.getDataSources());
            obj.setAddress(dataInfoExcelModel.getContactCurrAddress14());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        //联系人15信息
        if(Objects.nonNull(dataInfoExcelModel.getContactName15())
                || Objects.nonNull(dataInfoExcelModel.getContactPhone15())
                || Objects.nonNull(dataInfoExcelModel.getContactHomePhone15())){
            PersonalContact obj=new PersonalContact();
            obj.setPersonalId(personal.getId());
            obj.setRelation(getRelationType(dataInfoExcelModel.getContactRelation15()));
            obj.setName(dataInfoExcelModel.getContactName15());
            obj.setPhone(dataInfoExcelModel.getContactPhone15());
            obj.setPhoneStatus(Personal.PhoneStatus.UNKNOWN.getValue());
            obj.setMobile(dataInfoExcelModel.getContactHomePhone15());
            obj.setEmployer(dataInfoExcelModel.getContactWorkUnit15());
            obj.setWorkPhone(dataInfoExcelModel.getContactUnitPhone15());
            obj.setSource(dataInfoExcelModel.getDataSources());
            obj.setAddress(dataInfoExcelModel.getContactCurrAddress15());
            obj.setAddressStatus(Personal.AddrStatus.UNKNOWN.getValue());
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalContactList.add(obj);
        }
        personalContactRepository.save(personalContactList);
    }

    /**
     * 更新或新增地址信息
     * @param dataInfoExcelModel
     * @param user
     * @param personal
     */
    private void addAddr(DataInfoExcelModel dataInfoExcelModel, User user, Personal personal) {
        List<PersonalAddress> personalAddressList=new ArrayList<>();
        //居住地址(个人)
        if(StringUtils.isNotBlank(dataInfoExcelModel.getHomeAddress())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(Personal.RelationEnum.SELF.getValue());
            personalAddress.setName(dataInfoExcelModel.getPersonalName());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getHomeAddress()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }

        //身份证户籍地址（个人）
        if(StringUtils.isNotBlank(dataInfoExcelModel.getIdCardAddress()) ){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(Personal.RelationEnum.SELF.getValue());
            personalAddress.setName(dataInfoExcelModel.getPersonalName());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setType(Personal.AddrRelationEnum.IDCARDADDR.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getIdCardAddress()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }

        //工作单位地址（个人）
        if(StringUtils.isNotBlank(dataInfoExcelModel.getCompanyAddr())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(Personal.RelationEnum.SELF.getValue());
            personalAddress.setName(dataInfoExcelModel.getPersonalName());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setType(Personal.AddrRelationEnum.UNITADDR.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getCompanyAddr()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }

        //居住地址(联系人1)
        if(StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress1())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(dataInfoExcelModel.getContactRelation1()));
            personalAddress.setName(dataInfoExcelModel.getContactName1());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getContactCurrAddress1()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }

        //居住地址(联系人2)
        if(StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress2())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(dataInfoExcelModel.getContactRelation2()));
            personalAddress.setName(dataInfoExcelModel.getContactName2());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getContactCurrAddress2()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }

        //居住地址(联系人3)
        if(StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress3())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(dataInfoExcelModel.getContactRelation3()));
            personalAddress.setName(dataInfoExcelModel.getContactName3());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getContactCurrAddress3()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);

        }

        //居住地址(联系人4)
        if(StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress4())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(dataInfoExcelModel.getContactRelation4()));
            personalAddress.setName(dataInfoExcelModel.getContactName4());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getContactCurrAddress4()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人5)
        if(StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress5())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(dataInfoExcelModel.getContactRelation5()));
            personalAddress.setName(dataInfoExcelModel.getContactName5());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getContactCurrAddress5()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人6)
        if(StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress6())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(dataInfoExcelModel.getContactRelation6()));
            personalAddress.setName(dataInfoExcelModel.getContactName6());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getContactCurrAddress6()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人7)
        if(StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress7())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(dataInfoExcelModel.getContactRelation7()));
            personalAddress.setName(dataInfoExcelModel.getContactName7());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getContactCurrAddress7()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人8)
        if(StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress8())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(dataInfoExcelModel.getContactRelation8()));
            personalAddress.setName(dataInfoExcelModel.getContactName8());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getContactCurrAddress8()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人9)
        if(StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress9())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(dataInfoExcelModel.getContactRelation9()));
            personalAddress.setName(dataInfoExcelModel.getContactName9());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getContactCurrAddress9()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人10)
        if(StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress10())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(dataInfoExcelModel.getContactRelation10()));
            personalAddress.setName(dataInfoExcelModel.getContactName10());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getContactCurrAddress10()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人11)
        if(StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress11())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(dataInfoExcelModel.getContactRelation11()));
            personalAddress.setName(dataInfoExcelModel.getContactName11());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getContactCurrAddress11()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人12)
        if(StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress12())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(dataInfoExcelModel.getContactRelation12()));
            personalAddress.setName(dataInfoExcelModel.getContactName12());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getContactCurrAddress12()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人13)
        if(StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress13())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(dataInfoExcelModel.getContactRelation13()));
            personalAddress.setName(dataInfoExcelModel.getContactName13());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getContactCurrAddress13()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人14)
        if(StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress14())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(dataInfoExcelModel.getContactRelation14()));
            personalAddress.setName(dataInfoExcelModel.getContactName14());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getContactCurrAddress14()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        //居住地址(联系人15)
        if(StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress15())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(dataInfoExcelModel.getContactRelation15()));
            personalAddress.setName(dataInfoExcelModel.getContactName4());
            personalAddress.setType(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getContactCurrAddress15()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalAddressList.add(personalAddress);
        }
        persosnalAddressRepository.save(personalAddressList);
    }

    /**
     * 开户信息
     * @param dataInfoExcelModel
     * @param user
     * @param personal
     */
    private void addBankInfo(DataInfoExcelModel dataInfoExcelModel, User user, Personal personal) {
        if (Objects.nonNull(dataInfoExcelModel.getDepositBank()) ||
                Objects.nonNull(dataInfoExcelModel.getCardNumber())) {
            PersonalBank personalBank=new PersonalBank();
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
     * @param dataInfoExcelModel
     * @param user
     */
    private Product  addProducts(DataInfoExcelModel dataInfoExcelModel, User user) {
        ProductSeries productSeries=null;
        if(StringUtils.isNotBlank(dataInfoExcelModel.getProductSeriesName())){
            productSeries=new ProductSeries();
            productSeries.setSeriesName(dataInfoExcelModel.getProductSeriesName());
            productSeries.setOperator(user.getId());
            productSeries.setOperatorTime(ZWDateUtil.getNowDateTime());
            productSeries.setPrincipal_id(dataInfoExcelModel.getPrinCode());
            productSeries.setCompanyCode(dataInfoExcelModel.getCompanyCode());
            productSeries=productSeriesRepository.save(productSeries);
        }
        //产品名称
        Product product=null;
        if(StringUtils.isNotBlank(dataInfoExcelModel.getProductName())){
            product=new Product();
            product.setProdcutName(dataInfoExcelModel.getProductName());
            product.setOperator(user.getId());
            product.setOperatorTime(ZWDateUtil.getNowDateTime());
            product.setCompanyCode(dataInfoExcelModel.getCompanyCode());
            product.setProductSeries(productSeries);
            product=productRepository.save(product);
        }
        return product;
    }



    /**
     * 解析居住地址
     * @param dataInfoExcelModel
     */
    private String nowLivingAddr(DataInfoExcelModel dataInfoExcelModel,String addr) {
        if(Objects.isNull(addr)){
            return null;
        }
        String province=null;
        if(Objects.isNull(dataInfoExcelModel.getProvince())){
            province="";
        } else {
            province = dataInfoExcelModel.getProvince();
        }
        String city=null;
        if(Objects.isNull(dataInfoExcelModel.getCity())){
            city="";
        } else {
            city =dataInfoExcelModel.getCity();
        }
        //现居住地地址
        if(addr.startsWith(province)){
           return addr;
        }else if(addr.startsWith(city)){
            return dataInfoExcelModel.getProvince().concat(addr);
        }else {
           return province.concat(city).concat(addr);
        }
    }

    /**
     * 联系人关联关系解析
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
     * @param dataInfoExcelModel
     * @return
     */
    private AreaCode areaHandler(DataInfoExcelModel dataInfoExcelModel){
        List<String> personalAreaList = new ArrayList<>();
        List<String> emptyList = new ArrayList<>();
        personalAreaList.add(dataInfoExcelModel.getCity());
        personalAreaList.add(dataInfoExcelModel.getHomeAddress());
        personalAreaList.add(dataInfoExcelModel.getIdCardAddress());
        emptyList.add(null);
        personalAreaList.removeAll(emptyList);
        for(String area : personalAreaList){
            AreaCode areaCode = areaCodeService.queryAreaCodeByName(area);
            if(Objects.nonNull(areaCode)){
                return areaCode;
            }
        }
        return null;
    }
}
