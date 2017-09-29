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
            List<OutsourceFollowRecord> outsourceFollowupRecords = excportResultModel.getOutsourceFollowRecords();
            if (!outsourceFollowupRecords.isEmpty()) {
                for (OutsourceFollowRecord record : outsourceFollowupRecords) {
                    FollowupExportModel followupExportModel = new FollowupExportModel();
                    AreaCode city = excportResultModel.getAreaCode();
                    if(Objects.nonNull(city)) {
                        AreaCode province = city.getParent();
                        if(Objects.nonNull(province)){
                            followupExportModel.setProvinceName(province.getAreaName());
                        }
                        followupExportModel.setCityName(city.getAreaName());

                    }
                    followupExportModel.setOutsName(excportResultModel.getOutsName());
                    followupExportModel.setOutsourceTotalAmount(excportResultModel.getOutsourceTotalAmount());
                    followupExportModel.setLeftAmount(excportResultModel.getLeftAmount());
                    followupExportModel.setLeftDays(excportResultModel.getLeftDays());
                    followupExportModel.setOutTime(excportResultModel.getOutTime());
                    followupExportModel.setEndOutTime(excportResultModel.getEndOutTime());
                    followupExportModel.setOverOutTime(excportResultModel.getOverOutTime());
                    followupExportModel.setOutsourceCaseStatus(excportResultModel.getOutsourceCaseStatus());
                    followupExportModel.setHasPayAmount(excportResultModel.getHasPayAmount());//已还款金额
                    followupExportModel.setCommissionRate(excportResultModel.getCommissionRate());//佣金比例
                    Personal personalInfo = excportResultModel.getPersonalInfo();
                    followupExportModel.setDepositBank(Objects.isNull(personalInfo) ? "": (personalInfo.getPersonalBankInfos().isEmpty() ? "": personalInfo.getPersonalBankInfos().iterator().next().getDepositBank()));//客户银行
                    followupExportModel.setCardNumber(Objects.isNull(personalInfo) ? "": (personalInfo.getPersonalBankInfos().isEmpty() ? "": personalInfo.getPersonalBankInfos().iterator().next().getCardNumber()));//客户卡号
                    followupExportModel.setPersonalName(Objects.isNull(personalInfo) ? "" : personalInfo.getName());//客户姓名
                    followupExportModel.setIdCard(Objects.isNull(personalInfo) ? "" :personalInfo.getIdCard());//客户身份证号
                    followupExportModel.setMobileNo(personalInfo.getMobileNo());//客户手机号
                    followupExportModel.setIdCardAddress(personalInfo.getIdCardAddress());//客户身份证地址
                    followupExportModel.setLocalHomeAddress(personalInfo.getLocalHomeAddress());//客户监听地址
                    followupExportModel.setLocalPhoneNo(personalInfo.getLocalPhoneNo());//固定电话
                    followupExportModel.setCompanyName(Objects.isNull(personalInfo) ? "": (personalInfo.getPersonalJobs().isEmpty() ? "": personalInfo.getPersonalJobs().iterator().next().getCompanyName()));//工作单位名称
                    followupExportModel.setCompanyPhone(Objects.isNull(personalInfo) ? "": (personalInfo.getPersonalJobs().isEmpty() ? "": personalInfo.getPersonalJobs().iterator().next().getPhone()));//工作单位电话
                    followupExportModel.setCompanyAddress(Objects.isNull(personalInfo) ? "": (personalInfo.getPersonalJobs().isEmpty() ? "": personalInfo.getPersonalJobs().iterator().next().getAddress()));//工作单位地址
                    followupExportModel.setFollTime(ZWDateUtil.fomratterDate(record.getOperatorTime(), null));//跟进时间
                    followupExportModel.setFollTargetName(record.getUserName());//跟进对象姓名
                    followupExportModel.setFollContent(record.getFollowRecord());//跟进内容
                    followupExportModel.setFollOperator(record.getFollowPerson());//跟进人名称
                    OutsourceFollowRecord.FollowType[] values = OutsourceFollowRecord.FollowType.values(); //跟进方式
                    for (int j = 0; j < values.length; j++) {
                        if (Objects.equals(record.getFollowType(), values[j].getValue())) {
                            followupExportModel.setFollType(values[j].getRemark());
                            break;
                        }
                    }

                    CaseFollowupRecord.Target[] relationValues = CaseFollowupRecord.Target.values(); //联系人关系
                    Iterator<PersonalContact> iterator = personalInfo.getPersonalContacts().iterator();
                    if(iterator.hasNext()) {
                        PersonalContact personalContact = iterator.next();
                        followupExportModel.setConcat1Name(personalContact.getName());
                        followupExportModel.setConcat1Phone(personalContact.getPhone());
                        followupExportModel.setConcat1Mobile(personalContact.getMobile());
                        followupExportModel.setConcat1Address(personalContact.getAddress());
                        followupExportModel.setConcat1Employer(personalContact.getEmployer());
                        followupExportModel.setConcat1WorkPhone(personalContact.getWorkPhone());
                        for (int j = 0; j < relationValues.length; j++) {
                            if (Objects.equals(record.getFollowType(), relationValues[j].getValue())) {
                                followupExportModel.setConcat1Relation(relationValues[j].getRemark());
                                break;
                            }
                        }
                    }
                    if(iterator.hasNext()) {
                        PersonalContact personalContact = iterator.next();
                        followupExportModel.setConcat2Name(personalContact.getName());
                        followupExportModel.setConcat2Phone(personalContact.getPhone());
                        followupExportModel.setConcat2Mobile(personalContact.getMobile());
                        followupExportModel.setConcat2Address(personalContact.getAddress());
                        followupExportModel.setConcat2Employer(personalContact.getEmployer());
                        followupExportModel.setConcat2WorkPhone(personalContact.getWorkPhone());
                        for (int j = 0; j < relationValues.length; j++) {
                            if (Objects.equals(record.getFollowType(), relationValues[j].getValue())) {
                                followupExportModel.setConcat2Relation(relationValues[j].getRemark());
                                break;
                            }
                        }
                    }
                    if(iterator.hasNext()) {
                        PersonalContact personalContact = iterator.next();
                        followupExportModel.setConcat3Name(personalContact.getName());
                        followupExportModel.setConcat3Phone(personalContact.getPhone());
                        followupExportModel.setConcat3Mobile(personalContact.getMobile());
                        followupExportModel.setConcat3Address(personalContact.getAddress());
                        followupExportModel.setConcat3Employer(personalContact.getEmployer());
                        followupExportModel.setConcat3WorkPhone(personalContact.getWorkPhone());
                        for (int j = 0; j < relationValues.length; j++) {
                            if (Objects.equals(record.getFollowType(), relationValues[j].getValue())) {
                                followupExportModel.setConcat3Relation(relationValues[j].getRemark());
                                break;
                            }
                        }
                    }
                    if(iterator.hasNext()) {
                        PersonalContact personalContact = iterator.next();
                        followupExportModel.setConcat4Name(personalContact.getName());
                        followupExportModel.setConcat4Phone(personalContact.getPhone());
                        followupExportModel.setConcat4Mobile(personalContact.getMobile());
                        followupExportModel.setConcat4Address(personalContact.getAddress());
                        followupExportModel.setConcat4Employer(personalContact.getEmployer());
                        followupExportModel.setConcat4WorkPhone(personalContact.getWorkPhone());
                        for (int j = 0; j < relationValues.length; j++) {
                            if (Objects.equals(record.getFollowType(), relationValues[j].getValue())) {
                                followupExportModel.setConcat4Relation(relationValues[j].getRemark());
                                break;
                            }
                        }
                    }
                    if(iterator.hasNext()) {
                        PersonalContact personalContact = iterator.next();
                        followupExportModel.setConcat5Name(personalContact.getName());
                        followupExportModel.setConcat5Phone(personalContact.getPhone());
                        followupExportModel.setConcat5Mobile(personalContact.getMobile());
                        followupExportModel.setConcat5Address(personalContact.getAddress());
                        followupExportModel.setConcat5Employer(personalContact.getEmployer());
                        followupExportModel.setConcat5WorkPhone(personalContact.getWorkPhone());
                        for (int j = 0; j < relationValues.length; j++) {
                            if (Objects.equals(record.getFollowType(), relationValues[j].getValue())) {
                                followupExportModel.setConcat5Relation(relationValues[j].getRemark());
                                break;
                            }
                        }
                    }
                    if(iterator.hasNext()) {
                        PersonalContact personalContact = iterator.next();
                        followupExportModel.setConcat6Name(personalContact.getName());
                        followupExportModel.setConcat6Phone(personalContact.getPhone());
                        followupExportModel.setConcat6Mobile(personalContact.getMobile());
                        followupExportModel.setConcat6Address(personalContact.getAddress());
                        followupExportModel.setConcat6Employer(personalContact.getEmployer());
                        followupExportModel.setConcat6WorkPhone(personalContact.getWorkPhone());
                        for (int j = 0; j < relationValues.length; j++) {
                            if (Objects.equals(record.getFollowType(), relationValues[j].getValue())) {
                                followupExportModel.setConcat6Relation(relationValues[j].getRemark());
                                break;
                            }
                        }
                    }
                    if(iterator.hasNext()) {
                        PersonalContact personalContact = iterator.next();
                        followupExportModel.setConcat7Name(personalContact.getName());
                        followupExportModel.setConcat7Phone(personalContact.getPhone());
                        followupExportModel.setConcat7Mobile(personalContact.getMobile());
                        followupExportModel.setConcat7Address(personalContact.getAddress());
                        followupExportModel.setConcat7Employer(personalContact.getEmployer());
                        followupExportModel.setConcat7WorkPhone(personalContact.getWorkPhone());
                        for (int j = 0; j < relationValues.length; j++) {
                            if (Objects.equals(record.getFollowType(), relationValues[j].getValue())) {
                                followupExportModel.setConcat7Relation(relationValues[j].getRemark());
                                break;
                            }
                        }
                    }
                    if(iterator.hasNext()) {
                        PersonalContact personalContact = iterator.next();
                        followupExportModel.setConcat8Name(personalContact.getName());
                        followupExportModel.setConcat8Phone(personalContact.getPhone());
                        followupExportModel.setConcat8Mobile(personalContact.getMobile());
                        followupExportModel.setConcat8Address(personalContact.getAddress());
                        followupExportModel.setConcat8Employer(personalContact.getEmployer());
                        followupExportModel.setConcat8WorkPhone(personalContact.getWorkPhone());
                        for (int j = 0; j < relationValues.length; j++) {
                            if (Objects.equals(record.getFollowType(), relationValues[j].getValue())) {
                                followupExportModel.setConcat8Relation(relationValues[j].getRemark());
                                break;
                            }
                        }
                    }
                    if(iterator.hasNext()) {
                        PersonalContact personalContact = iterator.next();
                        followupExportModel.setConcat9Name(personalContact.getName());
                        followupExportModel.setConcat9Phone(personalContact.getPhone());
                        followupExportModel.setConcat9Mobile(personalContact.getMobile());
                        followupExportModel.setConcat9Address(personalContact.getAddress());
                        followupExportModel.setConcat9Employer(personalContact.getEmployer());
                        followupExportModel.setConcat9WorkPhone(personalContact.getWorkPhone());
                        for (int j = 0; j < relationValues.length; j++) {
                            if (Objects.equals(record.getFollowType(), relationValues[j].getValue())) {
                                followupExportModel.setConcat9Relation(relationValues[j].getRemark());
                                break;
                            }
                        }
                    }
                    if(iterator.hasNext()) {
                        PersonalContact personalContact = iterator.next();
                        followupExportModel.setConcat10Name(personalContact.getName());
                        followupExportModel.setConcat10Phone(personalContact.getPhone());
                        followupExportModel.setConcat10Mobile(personalContact.getMobile());
                        followupExportModel.setConcat10Address(personalContact.getAddress());
                        followupExportModel.setConcat10Employer(personalContact.getEmployer());
                        followupExportModel.setConcat10WorkPhone(personalContact.getWorkPhone());
                        for (int j = 0; j < relationValues.length; j++) {
                            if (Objects.equals(record.getFollowType(), relationValues[j].getValue())) {
                                followupExportModel.setConcat10Relation(relationValues[j].getRemark());
                                break;
                            }
                        }
                    }
                    if(iterator.hasNext()) {
                        PersonalContact personalContact = iterator.next();
                        followupExportModel.setConcat11Name(personalContact.getName());
                        followupExportModel.setConcat11Phone(personalContact.getPhone());
                        followupExportModel.setConcat11Mobile(personalContact.getMobile());
                        followupExportModel.setConcat11Address(personalContact.getAddress());
                        followupExportModel.setConcat11Employer(personalContact.getEmployer());
                        followupExportModel.setConcat11WorkPhone(personalContact.getWorkPhone());
                        for (int j = 0; j < relationValues.length; j++) {
                            if (Objects.equals(record.getFollowType(), relationValues[j].getValue())) {
                                followupExportModel.setConcat11Relation(relationValues[j].getRemark());
                                break;
                            }
                        }
                    }
                    if(iterator.hasNext()) {
                        PersonalContact personalContact = iterator.next();
                        followupExportModel.setConcat12Name(personalContact.getName());
                        followupExportModel.setConcat12Phone(personalContact.getPhone());
                        followupExportModel.setConcat12Mobile(personalContact.getMobile());
                        followupExportModel.setConcat12Address(personalContact.getAddress());
                        followupExportModel.setConcat12Employer(personalContact.getEmployer());
                        followupExportModel.setConcat12WorkPhone(personalContact.getWorkPhone());
                        for (int j = 0; j < relationValues.length; j++) {
                            if (Objects.equals(record.getFollowType(), relationValues[j].getValue())) {
                                followupExportModel.setConcat12Relation(relationValues[j].getRemark());
                                break;
                            }
                        }
                    }
                    if(iterator.hasNext()) {
                        PersonalContact personalContact = iterator.next();
                        followupExportModel.setConcat13Name(personalContact.getName());
                        followupExportModel.setConcat13Phone(personalContact.getPhone());
                        followupExportModel.setConcat13Mobile(personalContact.getMobile());
                        followupExportModel.setConcat13Address(personalContact.getAddress());
                        followupExportModel.setConcat13Employer(personalContact.getEmployer());
                        followupExportModel.setConcat13WorkPhone(personalContact.getWorkPhone());
                        for (int j = 0; j < relationValues.length; j++) {
                            if (Objects.equals(record.getFollowType(), relationValues[j].getValue())) {
                                followupExportModel.setConcat13Relation(relationValues[j].getRemark());
                                break;
                            }
                        }
                    }
                    if(iterator.hasNext()) {
                        PersonalContact personalContact = iterator.next();
                        followupExportModel.setConcat14Name(personalContact.getName());
                        followupExportModel.setConcat14Phone(personalContact.getPhone());
                        followupExportModel.setConcat14Mobile(personalContact.getMobile());
                        followupExportModel.setConcat14Address(personalContact.getAddress());
                        followupExportModel.setConcat14Employer(personalContact.getEmployer());
                        followupExportModel.setConcat14WorkPhone(personalContact.getWorkPhone());
                        for (int j = 0; j < relationValues.length; j++) {
                            if (Objects.equals(record.getFollowType(), relationValues[j].getValue())) {
                                followupExportModel.setConcat14Relation(relationValues[j].getRemark());
                                break;
                            }
                        }
                    }
                    if(iterator.hasNext()) {
                        PersonalContact personalContact = iterator.next();
                        followupExportModel.setConcat15Name(personalContact.getName());
                        followupExportModel.setConcat15Phone(personalContact.getPhone());
                        followupExportModel.setConcat15Mobile(personalContact.getMobile());
                        followupExportModel.setConcat15Address(personalContact.getAddress());
                        followupExportModel.setConcat15Employer(personalContact.getEmployer());
                        followupExportModel.setConcat15WorkPhone(personalContact.getWorkPhone());
                        for (int j = 0; j < relationValues.length; j++) {
                            if (Objects.equals(record.getFollowType(), relationValues[j].getValue())) {
                                followupExportModel.setConcat15Relation(relationValues[j].getRemark());
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
}
