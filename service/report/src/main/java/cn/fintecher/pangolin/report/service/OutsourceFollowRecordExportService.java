package cn.fintecher.pangolin.report.service;

import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.report.model.ExcportOutsourceResultModel;
import cn.fintecher.pangolin.report.model.FollowupExportModel;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/8/15.
 */
@Service
public class OutsourceFollowRecordExportService {
    private final Logger log = LoggerFactory.getLogger(OutsourceFollowRecordExportService.class);

    public List<FollowupExportModel> getFollowupData(List<ExcportOutsourceResultModel> excportResultModels) {
        List<FollowupExportModel> followupExportModels = new ArrayList<>();
        int i = 0;
        for (ExcportOutsourceResultModel excportResultModel : excportResultModels) {
            log.info("第"+ ++i +"条信息正在导出。。。。。");
            List<CaseFollowupRecord> outsourceFollowupRecords = null;
            if(Objects.nonNull(excportResultModel.getOutsourceFollowRecords())){
                outsourceFollowupRecords = excportResultModel.getOutsourceFollowRecords();
            }


            if (Objects.nonNull(outsourceFollowupRecords)) {
                for (CaseFollowupRecord record : outsourceFollowupRecords) {
                    FollowupExportModel followupExportModel = new FollowupExportModel();
                    AreaCode city = excportResultModel.getAreaCode();
                    if (Objects.nonNull(city)) {
                        AreaCode province = city.getParent();
                        if (Objects.nonNull(province)) {
                            followupExportModel.setProvinceName(province.getAreaName());
                        }
                        followupExportModel.setCityName(city.getAreaName());

                    }
                    getOutsourceData(excportResultModel, followupExportModel, record);
                    followupExportModel.setFollTime(ZWDateUtil.fomratterDate(record.getFollowTime(), null));//跟进时间
                    followupExportModel.setFollTargetName(record.getTargetName());//跟进对象姓名
                    followupExportModel.setFollContent(record.getContent());//跟进内容
                    followupExportModel.setFollOperator(record.getFollowPerson());//跟进人名称
                    CaseFollowupRecord.EffectiveCollection[] feedBacks = CaseFollowupRecord.EffectiveCollection.values(); //有效催收反馈
                    for (int j = 0; j < feedBacks.length; j++) {
                        if (Objects.nonNull(record.getCollectionFeedback())) {
                            if (Objects.equals(record.getCollectionFeedback(), feedBacks[j].getValue())) {
                                followupExportModel.setFollFeedback(feedBacks[j].getRemark());
                                break;
                            }
                        }
                    }
                    CaseFollowupRecord.InvalidCollection[] InvalidFeedBacks = CaseFollowupRecord.InvalidCollection.values(); //无效催收反馈
                    for (int j = 0; j < InvalidFeedBacks.length; j++) {
                        if (Objects.nonNull(record.getCollectionFeedback())) {
                            if (Objects.equals(record.getCollectionFeedback(), InvalidFeedBacks[j].getValue())) {
                                followupExportModel.setFollFeedback(InvalidFeedBacks[j].getRemark());
                                break;
                            }
                        }
                    }

                    CaseFollowupRecord.Target[] objectType = CaseFollowupRecord.Target.values(); //跟进对象
                    for (int j = 0; j < objectType.length; j++) {
                        if (Objects.nonNull(record.getTarget())) {
                            if (Objects.equals(record.getTarget(), objectType[j].getValue())) {
                                followupExportModel.setFollTarget(objectType[j].getRemark());
                                break;
                            }

                        }

                    }

                    CaseFollowupRecord.ContactState[] telStatus = CaseFollowupRecord.ContactState.values(); //电话状态
                    for (int j = 0; j < telStatus.length; j++) {
                        if (Objects.nonNull(record.getContactState())) {
                            if (Objects.equals(record.getContactState(), telStatus[j].getValue())) {
                                followupExportModel.setFollPhoneNum(telStatus[j].getRemark());
                                break;
                            }
                        }

                    }
                    CaseFollowupRecord.Type[] followType = CaseFollowupRecord.Type.values(); //跟进方式
                    for (int j = 0; j < followType.length; j++) {
                        if (Objects.nonNull(record.getType())) {
                            if (Objects.equals(record.getType(), followType[j].getValue())) {
                                followupExportModel.setFollType(followType[j].getRemark());
                                break;
                            }
                        }
                    }

                    followupExportModels.add(followupExportModel);
                }
            }
        }
        return followupExportModels;
    }

    public int getMaxNum(List<ExcportOutsourceResultModel> list) {
        // 遍历获取到联系人信息做多的数目
        int maxNum = 0;
        for (ExcportOutsourceResultModel model : list) {
            Set<PersonalContact> personalContacts = model.getPersonalInfo().getPersonalContacts();
            if (personalContacts.size() > maxNum) {
                maxNum = personalContacts.size();
            }
        }
        return maxNum > 4 ? 4 : maxNum;
    }

    public List<FollowupExportModel> getOutsourceRecordData(List<ExcportOutsourceResultModel> excportResultModels) {
        List<FollowupExportModel> followupExportModels = new ArrayList<>();
        int i = 0;
        for (ExcportOutsourceResultModel excportResultModel : excportResultModels) {
            log.info("第" + ++i + "条信息正在导出。。。。。");
            if(Objects.nonNull(excportResultModel)){
                FollowupExportModel followupExportModel = new FollowupExportModel();
                CaseFollowupRecord caseFollowupRecord = new CaseFollowupRecord();
                getOutsourceData(excportResultModel, followupExportModel, caseFollowupRecord);
                followupExportModels.add(followupExportModel);
            }
        }

        return followupExportModels;
    }

    private void getOutsourceData(ExcportOutsourceResultModel excportResultModel, FollowupExportModel followupExportModel, CaseFollowupRecord record){

        followupExportModel.setBatchNumber(excportResultModel.getBatchNumber());//案件批次号
        followupExportModel.setCaseNumber(excportResultModel.getCaseNumber());//案件编号
        followupExportModel.setProductName(excportResultModel.getProductName());//产品名称
        followupExportModel.setContractNumber(excportResultModel.getContractNumber());//合同编号
        followupExportModel.setContractAmount(excportResultModel.getContractAmount());//合同金额
        followupExportModel.setOverdueAmount(excportResultModel.getOverdueAmount());//逾期总金额(元)
        followupExportModel.setLeftCapital(excportResultModel.getLeftCapital());//剩余本金(元)
        followupExportModel.setOverdueCapital(excportResultModel.getOverdueCapital());//逾期本金(元)
        followupExportModel.setOverdueInterest(excportResultModel.getOverdueInterest());//逾期利息(元)
        followupExportModel.setPeriods(excportResultModel.getPeriods());//还款期数
        followupExportModel.setPerDueDate(excportResultModel.getPerDueDate());//每期还款日
        followupExportModel.setOverduePeriods(excportResultModel.getOverduePeriods());//逾期期数
        followupExportModel.setOverdueDays(excportResultModel.getOverdueDays());//逾期天数
        followupExportModel.setHasPayAmount(excportResultModel.getHasPayAmount());//已还款金额
        followupExportModel.setHasPayPeriods(excportResultModel.getHasPayPeriods());//已还款期数
        followupExportModel.setLeftDays(excportResultModel.getLeftDays());//剩余天数
        followupExportModel.setPayStatus(excportResultModel.getPayStatus());//还款状态
        followupExportModel.setPrincipalName(excportResultModel.getPrincipalName());//委托方
        CaseInfo.CollectionStatus[] followType = CaseInfo.CollectionStatus.values(); //催收状态
        for (int j = 0; j < followType.length; j++) {
            if (Objects.nonNull(record.getType())) {
                if (Objects.equals(record.getType(), followType[j].getValue())) {
                    followupExportModel.setCollectionStatus(followType[j].getRemark());
                    break;
                }
            }
        }
        followupExportModel.setCommissionRate(excportResultModel.getCommissionRate());//佣金比例(%)
        followupExportModel.setLoanDate(excportResultModel.getLoanDate());//贷款日期
        followupExportModel.setOverdueManageFee(excportResultModel.getOverdueManageFee());//逾期管理费
        followupExportModel.setOutsourceBackAmount(excportResultModel.getHasPayAmountOutsource());//委外回款金额(元)
        followupExportModel.setOutsName(excportResultModel.getOutsName());//委外方
        followupExportModel.setOutsourceTotalAmount(excportResultModel.getOutsourceTotalAmount());//委外案件金额(元)
        followupExportModel.setLeftAmount(excportResultModel.getLeftAmount());//剩余金额(元)

        followupExportModel.setLeftDaysOutsource(excportResultModel.getLeftDaysOutsource());//剩余委托时间(天)
        followupExportModel.setBatchNumberOutsource(excportResultModel.getBatchNumberOutsource());//委外批次号
        followupExportModel.setOutTime(excportResultModel.getOutTime());//委案日期
        followupExportModel.setEndOutTime(excportResultModel.getEndOutTime());//结案日期
        followupExportModel.setOverOutTime(excportResultModel.getOverOutsourceTime());//委案到期日期
        if (excportResultModel.getOutStatus() == 168) {
            followupExportModel.setCollectionStatus("催收中");//催收状态
        }
        if (excportResultModel.getOutStatus() == 170) {
            followupExportModel.setCollectionStatus("已结案");//催收状态
        }
        followupExportModel.setCommissionRateOutsource(excportResultModel.getCommissionRateOutsource());//委外佣金比例

        Personal personalInfo = excportResultModel.getPersonalInfo();
        followupExportModel.setDepositBank(Objects.isNull(personalInfo) ? "" : (personalInfo.getPersonalBankInfos().isEmpty() ? "" : personalInfo.getPersonalBankInfos().iterator().next().getDepositBank()));//客户银行
        followupExportModel.setCardNumber(Objects.isNull(personalInfo) ? "" : (personalInfo.getPersonalBankInfos().isEmpty() ? "" : personalInfo.getPersonalBankInfos().iterator().next().getCardNumber()));//客户卡号
        followupExportModel.setPersonalName(Objects.isNull(personalInfo) ? "" : personalInfo.getName());//客户姓名
        followupExportModel.setIdCard(Objects.isNull(personalInfo) ? "" : personalInfo.getIdCard());//客户身份证号
        followupExportModel.setMobileNo(personalInfo.getMobileNo());//客户手机号
        followupExportModel.setIdCardAddress(personalInfo.getIdCardAddress());//客户身份证地址
        followupExportModel.setLocalHomeAddress(personalInfo.getLocalHomeAddress());//客户家庭地址
        followupExportModel.setLocalPhoneNo(personalInfo.getLocalPhoneNo());//固定电话
        followupExportModel.setCompanyName(Objects.isNull(personalInfo) ? "" : (personalInfo.getPersonalJobs().isEmpty() ? "" : personalInfo.getPersonalJobs().iterator().next().getCompanyName()));//工作单位名称
        followupExportModel.setCompanyPhone(Objects.isNull(personalInfo) ? "" : (personalInfo.getPersonalJobs().isEmpty() ? "" : personalInfo.getPersonalJobs().iterator().next().getPhone()));//工作单位电话
        followupExportModel.setCompanyAddress(Objects.isNull(personalInfo) ? "" : (personalInfo.getPersonalJobs().isEmpty() ? "" : personalInfo.getPersonalJobs().iterator().next().getAddress()));//工作单位地址

        CaseFollowupRecord.Target[] relationValues = CaseFollowupRecord.Target.values(); //联系人关系
        Iterator<PersonalContact> iterator = personalInfo.getPersonalContacts().iterator();
        if (iterator.hasNext()) {
            PersonalContact personalContact = iterator.next();
            followupExportModel.setConcat1Name(personalContact.getName());
            followupExportModel.setConcat1Phone(personalContact.getPhone());
            followupExportModel.setConcat1Mobile(personalContact.getMobile());
            followupExportModel.setConcat1Address(personalContact.getAddress());
            followupExportModel.setConcat1Employer(personalContact.getEmployer());
            followupExportModel.setConcat1WorkPhone(personalContact.getWorkPhone());
            for (int j = 0; j < relationValues.length; j++) {
                if (Objects.nonNull(record.getTarget())  && Objects.equals(record.getTarget(), relationValues[j].getValue())) {
                    followupExportModel.setConcat1Relation(relationValues[j].getRemark());
                    break;
                }
            }
        }
        if (iterator.hasNext()) {
            PersonalContact personalContact = iterator.next();
            followupExportModel.setConcat2Name(personalContact.getName());
            followupExportModel.setConcat2Phone(personalContact.getPhone());
            followupExportModel.setConcat2Mobile(personalContact.getMobile());
            followupExportModel.setConcat2Address(personalContact.getAddress());
            followupExportModel.setConcat2Employer(personalContact.getEmployer());
            followupExportModel.setConcat2WorkPhone(personalContact.getWorkPhone());
            for (int j = 0; j < relationValues.length; j++) {
                if (Objects.nonNull(record.getTarget()) && Objects.equals(record.getTarget(), relationValues[j].getValue())) {
                    followupExportModel.setConcat2Relation(relationValues[j].getRemark());
                    break;
                }
            }
        }
        if (iterator.hasNext()) {
            PersonalContact personalContact = iterator.next();
            followupExportModel.setConcat3Name(personalContact.getName());
            followupExportModel.setConcat3Phone(personalContact.getPhone());
            followupExportModel.setConcat3Mobile(personalContact.getMobile());
            followupExportModel.setConcat3Address(personalContact.getAddress());
            followupExportModel.setConcat3Employer(personalContact.getEmployer());
            followupExportModel.setConcat3WorkPhone(personalContact.getWorkPhone());
            for (int j = 0; j < relationValues.length; j++) {
                if (Objects.nonNull(record.getTarget()) && Objects.equals(record.getTarget(), relationValues[j].getValue())) {
                    followupExportModel.setConcat3Relation(relationValues[j].getRemark());
                    break;
                }
            }
        }
        if (iterator.hasNext()) {
            PersonalContact personalContact = iterator.next();
            followupExportModel.setConcat4Name(personalContact.getName());
            followupExportModel.setConcat4Phone(personalContact.getPhone());
            followupExportModel.setConcat4Mobile(personalContact.getMobile());
            followupExportModel.setConcat4Address(personalContact.getAddress());
            followupExportModel.setConcat4Employer(personalContact.getEmployer());
            followupExportModel.setConcat4WorkPhone(personalContact.getWorkPhone());
            for (int j = 0; j < relationValues.length; j++) {
                if (Objects.nonNull(record.getTarget()) && Objects.equals(record.getTarget(), relationValues[j].getValue())) {
                    followupExportModel.setConcat4Relation(relationValues[j].getRemark());
                    break;
                }
            }
        }
        if (iterator.hasNext()) {
            PersonalContact personalContact = iterator.next();
            followupExportModel.setConcat5Name(personalContact.getName());
            followupExportModel.setConcat5Phone(personalContact.getPhone());
            followupExportModel.setConcat5Mobile(personalContact.getMobile());
            followupExportModel.setConcat5Address(personalContact.getAddress());
            followupExportModel.setConcat5Employer(personalContact.getEmployer());
            followupExportModel.setConcat5WorkPhone(personalContact.getWorkPhone());
            for (int j = 0; j < relationValues.length; j++) {
                if (Objects.nonNull(record.getTarget()) && Objects.equals(record.getTarget(), relationValues[j].getValue())) {
                    followupExportModel.setConcat5Relation(relationValues[j].getRemark());
                    break;
                }
            }
        }
        if (iterator.hasNext()) {
            PersonalContact personalContact = iterator.next();
            followupExportModel.setConcat6Name(personalContact.getName());
            followupExportModel.setConcat6Phone(personalContact.getPhone());
            followupExportModel.setConcat6Mobile(personalContact.getMobile());
            followupExportModel.setConcat6Address(personalContact.getAddress());
            followupExportModel.setConcat6Employer(personalContact.getEmployer());
            followupExportModel.setConcat6WorkPhone(personalContact.getWorkPhone());
            for (int j = 0; j < relationValues.length; j++) {
                if (Objects.nonNull(record.getTarget()) && Objects.equals(record.getTarget(), relationValues[j].getValue())) {
                    followupExportModel.setConcat6Relation(relationValues[j].getRemark());
                    break;
                }
            }
        }
        if (iterator.hasNext()) {
            PersonalContact personalContact = iterator.next();
            followupExportModel.setConcat7Name(personalContact.getName());
            followupExportModel.setConcat7Phone(personalContact.getPhone());
            followupExportModel.setConcat7Mobile(personalContact.getMobile());
            followupExportModel.setConcat7Address(personalContact.getAddress());
            followupExportModel.setConcat7Employer(personalContact.getEmployer());
            followupExportModel.setConcat7WorkPhone(personalContact.getWorkPhone());
            for (int j = 0; j < relationValues.length; j++) {
                if (Objects.nonNull(record.getTarget()) && Objects.equals(record.getTarget(), relationValues[j].getValue())) {
                    followupExportModel.setConcat7Relation(relationValues[j].getRemark());
                    break;
                }
            }
        }
        if (iterator.hasNext()) {
            PersonalContact personalContact = iterator.next();
            followupExportModel.setConcat8Name(personalContact.getName());
            followupExportModel.setConcat8Phone(personalContact.getPhone());
            followupExportModel.setConcat8Mobile(personalContact.getMobile());
            followupExportModel.setConcat8Address(personalContact.getAddress());
            followupExportModel.setConcat8Employer(personalContact.getEmployer());
            followupExportModel.setConcat8WorkPhone(personalContact.getWorkPhone());
            for (int j = 0; j < relationValues.length; j++) {
                if (Objects.nonNull(record.getTarget()) && Objects.equals(record.getTarget(), relationValues[j].getValue())) {
                    followupExportModel.setConcat8Relation(relationValues[j].getRemark());
                    break;
                }
            }
        }
        if (iterator.hasNext()) {
            PersonalContact personalContact = iterator.next();
            followupExportModel.setConcat9Name(personalContact.getName());
            followupExportModel.setConcat9Phone(personalContact.getPhone());
            followupExportModel.setConcat9Mobile(personalContact.getMobile());
            followupExportModel.setConcat9Address(personalContact.getAddress());
            followupExportModel.setConcat9Employer(personalContact.getEmployer());
            followupExportModel.setConcat9WorkPhone(personalContact.getWorkPhone());
            for (int j = 0; j < relationValues.length; j++) {
                if (Objects.nonNull(record.getTarget()) && Objects.equals(record.getTarget(), relationValues[j].getValue())) {
                    followupExportModel.setConcat9Relation(relationValues[j].getRemark());
                    break;
                }
            }
        }
        if (iterator.hasNext()) {
            PersonalContact personalContact = iterator.next();
            followupExportModel.setConcat10Name(personalContact.getName());
            followupExportModel.setConcat10Phone(personalContact.getPhone());
            followupExportModel.setConcat10Mobile(personalContact.getMobile());
            followupExportModel.setConcat10Address(personalContact.getAddress());
            followupExportModel.setConcat10Employer(personalContact.getEmployer());
            followupExportModel.setConcat10WorkPhone(personalContact.getWorkPhone());
            for (int j = 0; j < relationValues.length; j++) {
                if (Objects.nonNull(record.getTarget()) && Objects.equals(record.getTarget(), relationValues[j].getValue())) {
                    followupExportModel.setConcat10Relation(relationValues[j].getRemark());
                    break;
                }
            }
        }
        if (iterator.hasNext()) {
            PersonalContact personalContact = iterator.next();
            followupExportModel.setConcat11Name(personalContact.getName());
            followupExportModel.setConcat11Phone(personalContact.getPhone());
            followupExportModel.setConcat11Mobile(personalContact.getMobile());
            followupExportModel.setConcat11Address(personalContact.getAddress());
            followupExportModel.setConcat11Employer(personalContact.getEmployer());
            followupExportModel.setConcat11WorkPhone(personalContact.getWorkPhone());
            for (int j = 0; j < relationValues.length; j++) {
                if (Objects.nonNull(record.getTarget()) && Objects.equals(record.getTarget(), relationValues[j].getValue())) {
                    followupExportModel.setConcat11Relation(relationValues[j].getRemark());
                    break;
                }
            }
        }
        if (iterator.hasNext()) {
            PersonalContact personalContact = iterator.next();
            followupExportModel.setConcat12Name(personalContact.getName());
            followupExportModel.setConcat12Phone(personalContact.getPhone());
            followupExportModel.setConcat12Mobile(personalContact.getMobile());
            followupExportModel.setConcat12Address(personalContact.getAddress());
            followupExportModel.setConcat12Employer(personalContact.getEmployer());
            followupExportModel.setConcat12WorkPhone(personalContact.getWorkPhone());
            for (int j = 0; j < relationValues.length; j++) {
                if (Objects.nonNull(record.getTarget()) && Objects.equals(record.getTarget(), relationValues[j].getValue())) {
                    followupExportModel.setConcat12Relation(relationValues[j].getRemark());
                    break;
                }
            }
        }
        if (iterator.hasNext()) {
            PersonalContact personalContact = iterator.next();
            followupExportModel.setConcat13Name(personalContact.getName());
            followupExportModel.setConcat13Phone(personalContact.getPhone());
            followupExportModel.setConcat13Mobile(personalContact.getMobile());
            followupExportModel.setConcat13Address(personalContact.getAddress());
            followupExportModel.setConcat13Employer(personalContact.getEmployer());
            followupExportModel.setConcat13WorkPhone(personalContact.getWorkPhone());
            for (int j = 0; j < relationValues.length; j++) {
                if (Objects.nonNull(record.getTarget()) && Objects.equals(record.getTarget(), relationValues[j].getValue())) {
                    followupExportModel.setConcat13Relation(relationValues[j].getRemark());
                    break;
                }
            }
        }
        if (iterator.hasNext()) {
            PersonalContact personalContact = iterator.next();
            followupExportModel.setConcat14Name(personalContact.getName());
            followupExportModel.setConcat14Phone(personalContact.getPhone());
            followupExportModel.setConcat14Mobile(personalContact.getMobile());
            followupExportModel.setConcat14Address(personalContact.getAddress());
            followupExportModel.setConcat14Employer(personalContact.getEmployer());
            followupExportModel.setConcat14WorkPhone(personalContact.getWorkPhone());
            for (int j = 0; j < relationValues.length; j++) {
                if (Objects.nonNull(record.getTarget()) && Objects.equals(record.getTarget(), relationValues[j].getValue())) {
                    followupExportModel.setConcat14Relation(relationValues[j].getRemark());
                    break;
                }
            }
        }
        if (iterator.hasNext()) {
            PersonalContact personalContact = iterator.next();
            followupExportModel.setConcat15Name(personalContact.getName());
            followupExportModel.setConcat15Phone(personalContact.getPhone());
            followupExportModel.setConcat15Mobile(personalContact.getMobile());
            followupExportModel.setConcat15Address(personalContact.getAddress());
            followupExportModel.setConcat15Employer(personalContact.getEmployer());
            followupExportModel.setConcat15WorkPhone(personalContact.getWorkPhone());
            for (int j = 0; j < relationValues.length; j++) {
                if (Objects.nonNull(record.getTarget()) && Objects.equals(record.getTarget(), relationValues[j].getValue())) {
                    followupExportModel.setConcat15Relation(relationValues[j].getRemark());
                    break;
                }
            }
        }

    }
}
