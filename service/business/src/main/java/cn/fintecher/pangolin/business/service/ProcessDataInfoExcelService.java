package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.business.utils.ZWMathUtil;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.message.ConfirmDataInfoMessage;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.IdcardUtils;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.util.ZWStringUtils;
import com.querydsl.core.BooleanBuilder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

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
    public void doTask(ConfirmDataInfoMessage confirmDataInfoMessage){
        logger.info("{}  处理案件信息开始......",Thread.currentThread());
        //案件数据信息
        DataInfoExcelModel dataInfoExcelModel=confirmDataInfoMessage.getDataInfoExcelModel();
        //案件附件信息
        List<CaseInfoFile> caseInfoFileList=confirmDataInfoMessage.getCaseInfoFileList();
        caseInfoFileRepository.save(caseInfoFileList);
        //产品信息
        Product product=null;
        //用户数据
        User user=confirmDataInfoMessage.getUser();
        Personal personal=createPersonal(dataInfoExcelModel, user);
        //检测客户信息是否已存在（客户姓名、身份证号、公司编码）
        Iterable<Personal> personalIterable=null;
        QPersonal qPersonal=QPersonal.personal;
        BooleanBuilder builder = new BooleanBuilder();
        if(Objects.nonNull(personal.getName()) && Objects.nonNull(personal.getIdCard())){
            builder.and(qPersonal.name.eq(personal.getName()))
                    .and(qPersonal.idCard.eq(personal.getIdCard()))
                    .and(qPersonal.companyCode.eq(personal.getCompanyCode()));
            personalIterable=personalRepository.findAll(builder);
        }
        if(Objects.nonNull(personalIterable) && personalIterable.iterator().hasNext()){
            //更新操作
            for(Iterator<Personal> it = personalIterable.iterator(); it.hasNext();){
                Personal obj=it.next();
                personal.setId(obj.getId());
                personal=obj;
                //更新操作
                personal=personalRepository.save(personal);
                //更新或添加联系人信息
                addOrUpdateContract(dataInfoExcelModel, user, personal);
                //地址信息(个人)
                addOrUpdateAddr(dataInfoExcelModel,user,personal);
                //开户信息
                addOrUpdateBankInfo(dataInfoExcelModel, user, personal);
                //单位信息
                addOrUpdatePersonalJob(dataInfoExcelModel, user, personal);
                //产品系列
                product= addOrUpdateProducts(dataInfoExcelModel, user);
            }
        }else{
            personal=personalRepository.save(personal);
            //更新或添加联系人信息
            addOrUpdateContract(dataInfoExcelModel, user, personal);
            //更新或添加地址信息
            addOrUpdateAddr(dataInfoExcelModel,user,personal);
            //开户信息
            addOrUpdateBankInfo(dataInfoExcelModel, user, personal);
            //单位信息
            addOrUpdatePersonalJob(dataInfoExcelModel, user, personal);
            //产品系列
            product=addOrUpdateProducts(dataInfoExcelModel, user);
        }

        /**
         * 首先检查待分配案件是否有该案件，有的话直接进入数据异常表
         */
        QCaseInfoDistributed qCaseInfoDistributed=QCaseInfoDistributed.caseInfoDistributed;
        Iterable<CaseInfoDistributed> caseInfoDistributedIterable=caseInfoDistributedRepository.findAll(qCaseInfoDistributed.personalInfo.name.eq(dataInfoExcelModel.getPersonalName())
                                              .and(qCaseInfoDistributed.personalInfo.idCard.eq(dataInfoExcelModel.getIdCard()))
                                              .and(qCaseInfoDistributed.principalId.id.eq(dataInfoExcelModel.getPrinCode()))
                                              .and(qCaseInfoDistributed.product.prodcutName.eq(dataInfoExcelModel.getProductName()))
                                              .and(qCaseInfoDistributed.companyCode.eq(dataInfoExcelModel.getCompanyCode())));
        if(caseInfoDistributedIterable.iterator().hasNext()){
            //直接进入数据异常表
        }else{
            /**
             * 检查已有案件是否存在，存在的话直接进入案件异常表，异常表的数据结构与接收数据的DataInfoExcel相同,关联信息信息不做处理
             * (客户姓名、身份证号、委托方ID、产品名称、公司码)
             */
            QCaseInfo qCaseInfo=QCaseInfo.caseInfo;
            Iterable<CaseInfo> caseInfoIterable= caseInfoRepository.findAll(qCaseInfo.personalInfo.name.eq(dataInfoExcelModel.getPersonalName())
                    .and(qCaseInfo.personalInfo.idCard.eq(dataInfoExcelModel.getIdCard()))
                    .and(qCaseInfo.principalId.id.eq(dataInfoExcelModel.getPrinCode()))
                    .and(qCaseInfo.product.prodcutName.eq(dataInfoExcelModel.getProductName()))
                    .and(qCaseInfo.companyCode.eq(dataInfoExcelModel.getCompanyCode()))
                    .and(qCaseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue())));
            if(caseInfoIterable.iterator().hasNext()){
                //直接进入数据异常表
            }else{
                //进入数据待分配表
            }
        }
        Set<String> caseInfoDistributedSets=new HashSet<>();
           for(Iterator<CaseInfoDistributed> it=caseInfoDistributedIterable.iterator();it.hasNext();){
               caseInfoDistributedSets.add(it.next().getId());
           }
        //已有的案件池

        Set<String> caseInfoSets=new HashSet<>();
        for(Iterator<CaseInfo> it=caseInfoIterable.iterator();it.hasNext();){
            caseInfoSets.add(it.next().getId());
        }

        if(!caseInfoDistributedSets.isEmpty() || !caseInfoSets.isEmpty()){
            //进入异常池
            CaseInfoException caseInfoException = addCaseInfoException(dataInfoExcelModel, product, user, personal,caseInfoDistributedSets,caseInfoSets);
            caseInfoExceptionRepository.save(caseInfoException);
        }else{
            //进入案件分配池
            CaseInfoDistributed caseInfoDistributed = addCaseInfoDistributed(dataInfoExcelModel, product, user, personal);
            caseInfoDistributedRepository.save(caseInfoDistributed);
        }
        logger.info("{}  处理案件信息结束.",Thread.currentThread());
    }

    /**
     * 案件进入异常池
     * @param dataInfoExcelModel
     * @param product
     * @param user
     * @param personal
     * @return
     */
    private CaseInfoException addCaseInfoException(DataInfoExcelModel dataInfoExcelModel, Product product, User user, Personal personal,
                                                   Set<String> caseInfoDistributedSets,Set<String> caseInfoSets) {
        CaseInfoException caseInfoException=new CaseInfoException();
        caseInfoException.setDepartment(user.getDepartment());
        caseInfoException.setPersonalInfo(personal);
        caseInfoException.setArea(areaCodeService.queryAreaCodeByName(dataInfoExcelModel.getCity()));
        caseInfoException.setBatchNumber(dataInfoExcelModel.getBatchNumber());
        caseInfoException.setCaseNumber(dataInfoExcelModel.getCaseNumber());
        caseInfoException.setProduct(product);
        caseInfoException.setContractNumber(dataInfoExcelModel.getContractNumber());
        caseInfoException.setContractAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getContractAmount(),null,null));
        caseInfoException.setOverdueAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueAmount(),null,null));
        caseInfoException.setLeftCapital(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getLeftCapital(),null,null) );
        caseInfoException.setLeftInterest(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getLeftInterest(),null,null));
        caseInfoException.setOverdueCapital(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueCapital(),null,null));
        caseInfoException.setOverdueInterest(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverDueInterest(),null,null));
        caseInfoException.setOverdueFine(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueFine(),null,null));
        caseInfoException.setOverdueDelayFine(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueDelayFine(),null,null));
        caseInfoException.setPeriods(dataInfoExcelModel.getPeriods());
        caseInfoException.setPerDueDate(dataInfoExcelModel.getPerDueDate());
        caseInfoException.setPerPayAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getPerPayAmount(),null,null));
        caseInfoException.setOverduePeriods(dataInfoExcelModel.getOverDuePeriods());
        caseInfoException.setOverdueDays(dataInfoExcelModel.getOverDueDays());
        caseInfoException.setHasPayAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getHasPayAmount(),null,null));
        caseInfoException.setHasPayPeriods(dataInfoExcelModel.getHasPayPeriods());
        caseInfoException.setLatelyPayDate(dataInfoExcelModel.getLatelyPayDate());
        caseInfoException.setLatelyPayAmount(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getLatelyPayAmount(),null,null));
        caseInfoException.setCaseType(CaseInfo.CaseType.DISTRIBUTE.getValue());
        caseInfoException.setPayStatus(dataInfoExcelModel.getPaymentStatus());
        caseInfoException.setPrincipalId(principalRepository.findOne(dataInfoExcelModel.getPrinCode()));
        caseInfoException.setCollectionStatus(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue());
        caseInfoException.setDelegationDate(dataInfoExcelModel.getDelegationDate());
        caseInfoException.setCloseDate(dataInfoExcelModel.getCloseDate());
        caseInfoException.setCommissionRate(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getCommissionRate(),4,null));
        caseInfoException.setHandNumber(dataInfoExcelModel.getCaseHandNum());
        caseInfoException.setLoanDate(dataInfoExcelModel.getLoanDate());
        caseInfoException.setOverdueManageFee(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueManageFee(),null,null));
        caseInfoException.setHandUpFlag(CaseInfo.HandUpFlag.NO_HANG.getValue());
        caseInfoException.setOtherAmt(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOtherAmt(),null,null));
        caseInfoException.setOperator(user);
        caseInfoException.setOperatorTime(ZWDateUtil.getNowDateTime());
        caseInfoException.setCompanyCode(dataInfoExcelModel.getCompanyCode());
        caseInfoException.setDistributeRepeat(ZWStringUtils.collectionToString(caseInfoDistributedSets,","));
        caseInfoException.setAssignedRepeat(ZWStringUtils.collectionToString(caseInfoSets,","));
        caseInfoException.setRepeatStatus(CaseInfoException.RepeatStatusEnum.PENDING.getValue());
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
        caseInfoDistributed.setDepartment(user.getDepartment());
        caseInfoDistributed.setPersonalInfo(personal);
        caseInfoDistributed.setArea(areaCodeService.queryAreaCodeByName(dataInfoExcelModel.getCity()));
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
        caseInfoDistributed.setPrincipalId(principalRepository.findOne(dataInfoExcelModel.getPrinCode()));
        caseInfoDistributed.setCollectionStatus(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue());
        caseInfoDistributed.setDelegationDate(dataInfoExcelModel.getDelegationDate());
        caseInfoDistributed.setCloseDate(dataInfoExcelModel.getCloseDate());
        caseInfoDistributed.setCommissionRate(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getCommissionRate(),4,null));
        caseInfoDistributed.setHandNumber(dataInfoExcelModel.getCaseHandNum());
        caseInfoDistributed.setLoanDate(dataInfoExcelModel.getLoanDate());
        caseInfoDistributed.setOverdueManageFee(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOverdueManageFee(),null,null));
        caseInfoDistributed.setHandUpFlag(CaseInfo.HandUpFlag.NO_HANG.getValue());
        caseInfoDistributed.setOtherAmt(ZWMathUtil.DoubleToBigDecimal(dataInfoExcelModel.getOtherAmt(),null,null));
        caseInfoDistributed.setOperator(user);
        caseInfoDistributed.setOperatorTime(ZWDateUtil.getNowDateTime());
        caseInfoDistributed.setCompanyCode(dataInfoExcelModel.getCompanyCode());
        return caseInfoDistributed;
    }

    private void addOrUpdatePersonalJob(DataInfoExcelModel dataInfoExcelModel, User user, Personal personal) {
        if(StringUtils.isNotBlank(dataInfoExcelModel.getCompanyAddr()) || StringUtils.isNotBlank(dataInfoExcelModel.getCompanyName())
                || StringUtils.isNotBlank(dataInfoExcelModel.getCompanyPhone())){
            PersonalJob personalJob=new PersonalJob();
            personalJob.setAddress(dataInfoExcelModel.getCompanyAddr());
            personalJob.setCompanyName(dataInfoExcelModel.getCompanyName());
            personalJob.setPhone(dataInfoExcelModel.getCompanyPhone());
            personalJob.setOperatorTime(ZWDateUtil.getNowDateTime());
            personalJob.setOperator(user.getId());
            personalJob.setPersonalId(personal.getId());
            QPersonalJob qPersonalJob=QPersonalJob.personalJob;
            Iterable<PersonalJob> personalJobIterable=personalJobRepository.findAll(qPersonalJob.personalId.eq(personal.getId())
                                         .and(qPersonalJob.companyName.eq(personalJob.getCompanyName())));
            if(personalJobIterable.iterator().hasNext()){
                for(Iterator<PersonalJob> it = personalJobIterable.iterator(); it.hasNext();){
                    PersonalJob obj=it.next();
                    personalJob.setId(obj.getId());
                    personalJobRepository.save(personalJob);
                }
            }else{
                personalJobRepository.save(personalJob);
            }
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
        return personal;
    }

    /**
     * 添加或更新联系人信息
     * @param dataInfoExcelModel
     * @param user
     * @param personal
     */
    private void addOrUpdateContract(DataInfoExcelModel dataInfoExcelModel, User user, Personal personal) {
        List<PersonalContact> personalContactList=new ArrayList<>();
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
        personalContact.setOperator(user.getId());
        personalContact.setOperatorTime(ZWDateUtil.getNowDateTime());
        //已经有记录的不做任何操作，没有记录的直接做新增操作
        if(!checkPersonalContactExist(personal, personalContact)){
            personalContactList.add(personalContact);
        }

        if(Objects.nonNull(dataInfoExcelModel.getContactName1()) || Objects.nonNull(dataInfoExcelModel.getContactPhone1())
                || Objects.nonNull(dataInfoExcelModel.getContactRelation1()) || Objects.nonNull(dataInfoExcelModel.getContactHomePhone1())){
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
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            //已经有记录的不做任何操作，没有记录的直接做新增操作
            if(!checkPersonalContactExist(personal, obj)){
                personalContactList.add(obj);
            }
        }

        if(Objects.nonNull(dataInfoExcelModel.getContactName2()) || Objects.nonNull(dataInfoExcelModel.getContactPhone2())
                || Objects.nonNull(dataInfoExcelModel.getContactRelation2()) || Objects.nonNull(dataInfoExcelModel.getContactHomePhone2())){
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
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            //已经有记录的不做任何操作，没有记录的直接做新增操作
            if(!checkPersonalContactExist(personal, obj)){
                personalContactList.add(obj);
            }
        }

        if(Objects.nonNull(dataInfoExcelModel.getContactName3()) || Objects.nonNull(dataInfoExcelModel.getContactPhone3())
                || Objects.nonNull(dataInfoExcelModel.getContactRelation3()) || Objects.nonNull(dataInfoExcelModel.getContactHomePhone3())){
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
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            ///已经有记录的不做任何操作，没有记录的直接做新增操作
            if(!checkPersonalContactExist(personal, obj)){
                personalContactList.add(obj);
            }
        }

        if(Objects.nonNull(dataInfoExcelModel.getContactName4()) || Objects.nonNull(dataInfoExcelModel.getContactPhone4())
                || Objects.nonNull(dataInfoExcelModel.getContactRelation3()) || Objects.nonNull(dataInfoExcelModel.getContactHomePhone4())){
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
            obj.setOperator(user.getId());
            obj.setOperatorTime(ZWDateUtil.getNowDateTime());
            //已经有记录的不做任何操作，没有记录的直接做新增操作
            if(!checkPersonalContactExist(personal, obj)){
                personalContactList.add(obj);
            }
        }
        personalContactRepository.save(personalContactList);
    }

    /**
     * 更新或新增地址信息
     * @param dataInfoExcelModel
     * @param user
     * @param personal
     */
    private void addOrUpdateAddr(DataInfoExcelModel dataInfoExcelModel, User user, Personal personal) {
        List<PersonalAddress> personalAddressList=new ArrayList<>();
        //居住地址(个人)
        if(StringUtils.isNotBlank(dataInfoExcelModel.getHomeAddress())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(Personal.RelationEnum.SELF.getValue());
            personalAddress.setName(dataInfoExcelModel.getPersonalName());
            personalAddress.setRelation(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getHomeAddress()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(user.getOperateTime());
            if(!checkPersonalAddr(personal,personalAddress)){
                personalAddressList.add(personalAddress);
            }
        }

        //身份证户籍地址（个人）
        if(StringUtils.isNotBlank(dataInfoExcelModel.getIdCardAddress()) ){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(Personal.RelationEnum.SELF.getValue());
            personalAddress.setName(dataInfoExcelModel.getPersonalName());
            personalAddress.setRelation(Personal.AddrRelationEnum.IDCARDADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getIdCardAddress()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(user.getOperateTime());
            if(!checkPersonalAddr(personal,personalAddress)){
                personalAddressList.add(personalAddress);
            }
        }

        //工作单位地址（个人）
        if(StringUtils.isNotBlank(dataInfoExcelModel.getCompanyAddr())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(Personal.RelationEnum.SELF.getValue());
            personalAddress.setName(dataInfoExcelModel.getPersonalName());
            personalAddress.setRelation(Personal.AddrRelationEnum.UNITADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getCompanyAddr()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(user.getOperateTime());
            if(!checkPersonalAddr(personal,personalAddress)){
                personalAddressList.add(personalAddress);
            }
        }

        //居住地址(联系人1)
        if(StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress1())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(dataInfoExcelModel.getContactRelation1()));
            personalAddress.setName(dataInfoExcelModel.getPersonalName());
            personalAddress.setRelation(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getContactCurrAddress1()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(user.getOperateTime());
            if(!checkPersonalAddr(personal,personalAddress)){
                personalAddressList.add(personalAddress);
            }
        }

        //居住地址(联系人2)
        if(StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress2())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(dataInfoExcelModel.getContactRelation2()));
            personalAddress.setName(dataInfoExcelModel.getPersonalName());
            personalAddress.setRelation(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getContactCurrAddress2()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(user.getOperateTime());
            if(!checkPersonalAddr(personal,personalAddress)){
                personalAddressList.add(personalAddress);
            }
        }

        //居住地址(联系人3)
        if(StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress3())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(dataInfoExcelModel.getContactRelation3()));
            personalAddress.setName(dataInfoExcelModel.getPersonalName());
            personalAddress.setRelation(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getContactCurrAddress3()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(user.getOperateTime());
            if(!checkPersonalAddr(personal,personalAddress)){
                personalAddressList.add(personalAddress);
            }
        }

        //居住地址(联系人4)
        if(StringUtils.isNotBlank(dataInfoExcelModel.getContactCurrAddress4())){
            PersonalAddress personalAddress=new PersonalAddress();
            personalAddress.setPersonalId(personal.getId());
            personalAddress.setRelation(getRelationType(dataInfoExcelModel.getContactRelation4()));
            personalAddress.setName(dataInfoExcelModel.getPersonalName());
            personalAddress.setRelation(Personal.AddrRelationEnum.CURRENTADDR.getValue());
            personalAddress.setStatus(Personal.AddrStatus.UNKNOWN.getValue());
            personalAddress.setSource(Constants.DataSource.IMPORT.getValue());
            personalAddress.setDetail(nowLivingAddr(dataInfoExcelModel,dataInfoExcelModel.getContactCurrAddress4()));
            personalAddress.setOperator(user.getId());
            personalAddress.setOperatorTime(user.getOperateTime());
            if(!checkPersonalAddr(personal,personalAddress)){
                personalAddressList.add(personalAddress);
            }
        }
        persosnalAddressRepository.save(personalAddressList);
    }

    /**
     * 开户信息
     * @param dataInfoExcelModel
     * @param user
     * @param personal
     */
    private void addOrUpdateBankInfo(DataInfoExcelModel dataInfoExcelModel, User user, Personal personal) {
        if (Objects.nonNull(dataInfoExcelModel.getDepositBank()) ||
                Objects.nonNull(dataInfoExcelModel.getCardNumber())) {
            PersonalBank personalBank=new PersonalBank();
            personalBank.setDepositBank(dataInfoExcelModel.getDepositBank());
            personalBank.setCardNumber(dataInfoExcelModel.getCardNumber());
            personalBank.setPersonalId(personal.getId());
            personal.setOperatorTime(ZWDateUtil.getNowDateTime());
            personal.setOperator(user.getId());
            QPersonalBank qPersonalBank=QPersonalBank.personalBank;
            Iterable<PersonalBank> personalBankIterable= personalBankRepository.findAll(qPersonalBank.personalId.eq(personal.getId()));
            if(personalBankIterable.iterator().hasNext()){
                for(Iterator<PersonalBank> it = personalBankIterable.iterator(); it.hasNext();){
                    PersonalBank obj=it.next();
                    personalBank.setId(obj.getId());
                    personalBankRepository.save(personalBank);
                }
            }else{
                personalBankRepository.save(personalBank);
            }
        }
    }

    /**
     * 新增或更新产品及系列名称
     * @param dataInfoExcelModel
     * @param user
     */
    private Product  addOrUpdateProducts(DataInfoExcelModel dataInfoExcelModel, User user) {
        ProductSeries productSeries=null;
        if(StringUtils.isNotBlank(dataInfoExcelModel.getProductSeriesName())){
            productSeries=new ProductSeries();
            productSeries.setSeriesName(dataInfoExcelModel.getProductSeriesName());
            productSeries.setOperator(user.getId());
            productSeries.setOperatorTime(ZWDateUtil.getNowDateTime());
            productSeries.setPrincipal_id(dataInfoExcelModel.getPrinCode());
            productSeries.setCompanyCode(dataInfoExcelModel.getCompanyCode());
            QProductSeries qProductSeries=QProductSeries.productSeries;
            Iterable<ProductSeries> productSeriesIterable= productSeriesRepository.findAll(qProductSeries.seriesName.eq(dataInfoExcelModel.getProductSeriesName())
                    .and(qProductSeries.companyCode.eq(dataInfoExcelModel.getCompanyCode()))
                    .and(qProductSeries.principal_id.eq(dataInfoExcelModel.getPrinCode())));
            if(productSeriesIterable.iterator().hasNext()){
                for(Iterator<ProductSeries> it = productSeriesIterable.iterator(); it.hasNext();){
                    ProductSeries obj=it.next();
                    productSeries.setId(obj.getId());
                    productSeriesRepository.save(productSeries);
                }
            }else{
                productSeries=productSeriesRepository.save(productSeries);
            }
        }
        //产品名称
        if(StringUtils.isNotBlank(dataInfoExcelModel.getProductName())){
            Product product=new Product();
            product.setProdcutName(dataInfoExcelModel.getProductName());
            product.setOperator(user.getId());
            product.setOperatorTime(ZWDateUtil.getNowDateTime());
            product.setCompanyCode(dataInfoExcelModel.getCompanyCode());
            if(Objects.nonNull(productSeries)){
                product.setProductSeries(productSeries);
                QProduct qProduct=QProduct.product;
                Iterable<Product> productIterable= productRepository.findAll(qProduct.prodcutName.eq(dataInfoExcelModel.getProductName())
                        .and(qProduct.companyCode.eq(dataInfoExcelModel.getCompanyCode()))
                        .and(qProduct.productSeries.id.eq(productSeries.getId())));
                return saveProduct(product, productIterable);
            }else{
                QProduct qProduct=QProduct.product;
                Iterable<Product> productIterable= productRepository.findAll(qProduct.prodcutName.eq(dataInfoExcelModel.getProductName())
                        .and(qProduct.companyCode.eq(dataInfoExcelModel.getCompanyCode()))
                        .and(qProduct.productSeries.id.isNull()));
                 return saveProduct(product, productIterable);
            }
        }
        return null;
    }

    private Product saveProduct(Product product, Iterable<Product> productIterable) {
        Product productT=null;
        if(productIterable.iterator().hasNext()){
            for(Iterator<Product> it = productIterable.iterator(); it.hasNext(); ){
                Product obj=it.next();
                product.setId(obj.getId());
                productT=obj;
                productRepository.save(product);
            }
        }else{
            productT=productRepository.save(product);
        }
        return  productT;
    }

    /**
     * 解析居住地址
     * @param dataInfoExcelModel
     */
    private String nowLivingAddr(DataInfoExcelModel dataInfoExcelModel,String addr) {
        //现居住地地址
        if(addr.startsWith(dataInfoExcelModel.getProvince())){
           return addr;
        }else if(addr.startsWith(dataInfoExcelModel.getCity())){
            return dataInfoExcelModel.getProvince().concat(addr);
        }else {
           return dataInfoExcelModel.getProvince().concat(dataInfoExcelModel.getCity()).concat(addr);
        }
    }










    /**
     *检查客户信息是否存在
     * @param personal
     * @param personalContact
     * @return
     */
    public boolean checkPersonalContactExist(Personal personal, PersonalContact personalContact){
        QPersonalContact qPersonalContact=QPersonalContact.personalContact;
        Iterable<PersonalContact> personalContactIterable=null;
        if(Objects.nonNull(personalContact.getName()) && Objects.nonNull(personalContact.getRelation())){
            personalContactIterable=personalContactRepository.findAll(qPersonalContact.personalId.eq(personal.getId())
                    .and(qPersonalContact.name.eq(personalContact.getName()))
                    .and(qPersonalContact.relation.eq(personalContact.getRelation())));
        }else{
            return false;
        }
        //如果库中已有了客户信息则不再做更新操作
        if(personalContactIterable.iterator().hasNext()){
            return true;
        }
        return false;
    }

    /**
     * 检查地址信息是否存在
     * @param personal
     * @param personalAddress
     * @return
     */
    public boolean checkPersonalAddr(Personal personal,PersonalAddress personalAddress){
        QPersonalAddress qPersonalAddress=QPersonalAddress.personalAddress;
        Iterable<PersonalAddress> personalAddressIterable=null;
        BooleanBuilder builder=new BooleanBuilder();
        if(Objects.nonNull(personalAddress.getRelation()) && Objects.nonNull(personalAddress.getName()) && Objects.nonNull(personalAddress.getType())){
            personalAddressIterable= persosnalAddressRepository.findAll(qPersonalAddress.personalId.eq(personal.getId())
                    .and(qPersonalAddress.relation.eq(personalAddress.getRelation()))
                    .and(qPersonalAddress.name.eq(personalAddress.getName()))
                    .and(qPersonalAddress.type.eq(personalAddress.getType())));
        }else{
            return false;
        }
        if(personalAddressIterable.iterator().hasNext()){
            return true;
        }
        return false;
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


}
