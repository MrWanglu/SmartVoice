package cn.fintecher.pangolin.report.service;

import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.report.model.ExcportResultModel;
import cn.fintecher.pangolin.report.model.ExportFollowupParams;
import cn.fintecher.pangolin.report.model.FollowupExportModel;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/8/15.
 */
@Service
public class FollowRecordExportService {
    private final Logger log = LoggerFactory.getLogger(FollowRecordExportService.class);

    private static final String[] TITLE_LIST = {"案件编号", "批次号", "委托方", "跟进时间", "跟进方式", "客户姓名", "客户身份证", "催收对象", "姓名", "电话/地址", "定位地址", "催收反馈", "跟进内容", "客户号", "账户号", "手数"};
    private static final String[] PRO_NAMES = {"caseNumber", "batchNumber", "principalName", "follTime", "follType", "personalName", "idcard", "follTarget", "follTargetName", "follPhoneNum", "location", "follFeedback", "follContent", "personalNum", "accountNum", "handNum"};

    public List parseConnect(List<String> list) {
        List<String> result = new ArrayList<>();
        for (String item : list) {
            if (Objects.equals(item, "姓名")) {
                result.add("联系人姓名");
            } else if (Objects.equals(item, "手机号码")) {
                result.add("联系人手机号码");
            } else {
                result.add(item);
            }
        }
        for (int i = 1; i <= 4; i++) {
            if (result.contains("联系人姓名")) {
                result.add("联系人" + i + "姓名");
            }
            if (result.contains("联系人手机号码")) {
                result.add("联系人" + i + "手机号码");
            }
            if (result.contains("住宅电话")) {
                result.add("联系人" + i + "住宅电话");
            }
            if (result.contains("现居地址")) {
                result.add("联系人" + i + "现居地址");
            }
            if (result.contains("与客户关系")) {
                result.add("联系人" + i + "与客户关系");
            }
            if (result.contains("工作单位")) {
                result.add("联系人" + i + "工作单位");
            }
            if (result.contains("单位电话")) {
                result.add("联系人" + i + "单位电话");
            }
        }
        result.remove("联系人姓名");
        result.remove("联系人手机号码");
        result.remove("住宅电话");
        result.remove("现居地址");
        result.remove("与客户关系");
        result.remove("工作单位");
        result.remove("单位电话");
        return result;
    }

    public List parseFollow(List<String> list) {
        List<String> result = new ArrayList<>();
        for (String item : list) {
            if (Objects.equals(item, "姓名")) {
                result.add("催收对象姓名");
            } else {
                result.add(item);
            }
        }
        return result;
    }

    public Map<String, String> createHead() {
        Map<String, String> headMap = new LinkedHashMap<>();
        for (int i = 0; i < PRO_NAMES.length; i++) {
            headMap.put(PRO_NAMES[i], TITLE_LIST[i]);
        }
        return headMap;
    }

    public List<Map<String, Object>> createData(List<ExportFollowupParams> records) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (ExportFollowupParams record : records) {
            Map<String, Object> dataMap = new LinkedHashMap<>();
            dataMap.put("caseNumber", record.getCaseNum()); //案件编号
            dataMap.put("batchNumber", record.getBatchNum()); //批次号
            dataMap.put("principalName", record.getPrincipalName()); //委托方
            dataMap.put("follTime", ZWDateUtil.fomratterDate((Date) record.getFollowTime(), null)); //跟进时间
            CaseFollowupRecord.Type[] values = CaseFollowupRecord.Type.values(); //跟进方式
            for (int i = 0; i < values.length; i++) {
                if (Objects.equals(record.getFollowType(), values[i].getValue())) {
                    dataMap.put("follType", values[i].getRemark());
                    break;
                }
            }
            dataMap.put("personalName", record.getPersonName()); //客户姓名
            dataMap.put("idcard", record.getIdCard()); //身份证号
            CaseFollowupRecord.Target[] values1 = CaseFollowupRecord.Target.values(); //催收对象
            for (int i = 0; i < values1.length; i++) {
                if (Objects.equals(record.getTarget(), values1[i].getValue())) {
                    dataMap.put("follTarget", values1[i].getRemark());
                    break;
                }
            }
            dataMap.put("follTargetName", record.getTargetName()); //催收对象姓名

            // 电话催收
            if (Objects.equals(record.getFollowType(), CaseFollowupRecord.Type.TEL.getValue())) {
//                CaseFollowupRecord.ContactState[] values2 = CaseFollowupRecord.ContactState.values();
//                for (int i = 0; i < values2.length; i++) {
//                    if (Objects.equals(record.getContactState(), values2[i].getValue())) {
//                        dataMap.put("follContype", values2[i].getRemark());  //电话/地址状态
//                        break;
//                    }
//                }
                dataMap.put("follPhoneNum", record.getContactPhone()); //电话/地址
            }
            // 外访/协催
            if (!Objects.equals(record.getFollowType(), CaseFollowupRecord.Type.TEL.getValue())) {
//                Personal.AddrStatus[] values2 = Personal.AddrStatus.values();
//                for (int i = 0; i < values2.length; i++) {
//                    if (Objects.equals(record.getAddrStatus(), values2[i].getValue())) {
//                        dataMap.put("follContype", values2[i].getRemark()); //电话/地址状态
//                        break;
//                    }
//                }
                dataMap.put("follPhoneNum", record.getDetail()); //电话/地址
            }
            dataMap.put("location", record.getCollectionLocation());//定位地址
            CaseFollowupRecord.EffectiveCollection[] values2 = CaseFollowupRecord.EffectiveCollection.values(); //催收反馈
            for (int i = 0; i < values2.length; i++) {
                if (Objects.equals(record.getCollectionFeedback(), values2[i].getValue())) {
                    dataMap.put("follFeedback", values2[i].getRemark());
                    break;
                }
            }
            CaseFollowupRecord.InvalidCollection[] values3 = CaseFollowupRecord.InvalidCollection.values();
            for (int i = 0; i < values3.length; i++) {
                if (Objects.equals(record.getCollectionFeedback(), values3[i].getValue())) {
                    dataMap.put("follFeedback", values3[i].getRemark());
                    break;
                }
            }
            dataMap.put("follContent", record.getContent()); //跟进内容
            dataMap.put("personalNum", record.getPersonNumber()); //客户号
            dataMap.put("accountNum", record.getAccountNumber()); //账户号
            dataMap.put("handNum", record.getHandNumber()); //手数
            dataList.add(dataMap);
        }
        return dataList;
    }

    public List<Map<String, Object>> createNewData(List<ExcportResultModel> records) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (ExcportResultModel record : records) {
            log.debug(record.toString());
            Map<String, Object> dataMap = new LinkedHashMap<>();
            dataMap.put("caseNumber", record.getCaseNumber()); //案件编号
            dataMap.put("batchNumber", record.getBatchNumber()); //批次号
            if (Objects.nonNull(record.getPrincipal())) {
                dataMap.put("principalName", record.getPrincipal().getName()); //委托方
            }
            if (Objects.nonNull(record.getPersonalInfo())) {
                dataMap.put("personalName", record.getPersonalInfo().getName()); //客户姓名
                dataMap.put("idcard", record.getPersonalInfo().getIdCard()); //身份证号
            }
            if (Objects.nonNull(record.getPersonalInfo().getPersonalBankInfos())
                    && !record.getPersonalInfo().getPersonalBankInfos().isEmpty()) {
                dataMap.put("accountNum", record.getPersonalInfo().getPersonalBankInfos().iterator().next().getAccountNumber()); //账户号
            }
            dataMap.put("handNum", record.getHandNumber()); //手数
            dataMap.put("personalNum", record.getPersonalInfo().getNumber()); //客户号
            for (int i = 0; i < record.getCaseFollowupRecords().size(); i++) {
                CaseFollowupRecord caseFollowupRecord = record.getCaseFollowupRecords().get(i);
                dataMap.put("follTime", ZWDateUtil.fomratterDate((Date) caseFollowupRecord.getOperatorTime(), null)); //跟进时间
                dataMap.put("follTargetName", caseFollowupRecord.getTargetName()); //催收对象姓名
                dataMap.put("location", caseFollowupRecord.getCollectionLocation());//定位地址
                dataMap.put("follContent", caseFollowupRecord.getContent()); //跟进内容
                CaseFollowupRecord.Type[] values = CaseFollowupRecord.Type.values(); //跟进方式
                for (int j = 0; j < values.length; j++) {
                    if (Objects.equals(caseFollowupRecord.getType(), values[j].getValue())) {
                        dataMap.put("follType", values[j].getRemark());
                        break;
                    }
                }
                CaseFollowupRecord.Target[] values1 = CaseFollowupRecord.Target.values(); //催收对象
                for (int j = 0; j < values1.length; j++) {
                    if (Objects.equals(caseFollowupRecord.getTarget(), values1[j].getValue())) {
                        dataMap.put("follTarget", values1[j].getRemark());
                        break;
                    }
                }
                // 电话催收
                if (Objects.equals(caseFollowupRecord.getType(), CaseFollowupRecord.Type.TEL.getValue())) {
                    dataMap.put("follPhoneNum", caseFollowupRecord.getContactPhone()); //电话/地址
                }
                if (!Objects.equals(caseFollowupRecord.getType(), CaseFollowupRecord.Type.TEL.getValue())) {
                    dataMap.put("follPhoneNum", caseFollowupRecord.getDetail()); //电话/地址
                }
                CaseFollowupRecord.EffectiveCollection[] values2 = CaseFollowupRecord.EffectiveCollection.values(); //催收反馈
                for (int j = 0; j < values2.length; j++) {
                    if (Objects.equals(caseFollowupRecord.getCollectionFeedback(), values2[j].getValue())) {
                        dataMap.put("follFeedback", values2[j].getRemark());
                        break;
                    }
                }
                CaseFollowupRecord.InvalidCollection[] values3 = CaseFollowupRecord.InvalidCollection.values();
                for (int j = 0; j < values3.length; j++) {
                    if (Objects.equals(caseFollowupRecord.getCollectionFeedback(), values3[j].getValue())) {
                        dataMap.put("follFeedback", values3[j].getRemark());
                        break;
                    }
                }
                dataList.add(dataMap);
            }
        }
        return dataList;
    }


    public List<FollowupExportModel> getFollowupData(List<ExcportResultModel> excportResultModels) {
        List<FollowupExportModel> followupExportModels = new ArrayList<>();
        int i = 0;
        for (ExcportResultModel excportResultModel : excportResultModels) {
            log.info("第" + ++i + "条信息正在导出。。。。。");
            FollowupExportModel followupExportModel = new FollowupExportModel();
            followupExportModel.setContractNumber(excportResultModel.getContractNumber());//合同编号
            followupExportModel.setLoanDate(excportResultModel.getLoanDate());//贷款日期
            followupExportModel.setContractAmount(excportResultModel.getContractAmount());//合同金额
            AreaCode city = excportResultModel.getAreaCode();
            if (Objects.nonNull(city)) {
                followupExportModel.setCityName(city.getAreaName());
                AreaCode province = city.getParent();
                if (Objects.nonNull(province)) {
                    followupExportModel.setProvinceName(province.getAreaName());
                }
            }
            followupExportModel.setLeftCapital(excportResultModel.getLeftCapital());//剩余本金
            followupExportModel.setLeftInterest(excportResultModel.getLeftInterest());//剩余利息
            followupExportModel.setOverdueAmount(excportResultModel.getOverdueAmount());//逾期金额
            followupExportModel.setOverdueCapital(excportResultModel.getOverdueCapital());//逾期本金
            followupExportModel.setOverdueInterest(excportResultModel.getOverdueInterest());//逾期利息
            followupExportModel.setOverdueFine(excportResultModel.getOverdueFine());//逾期罚息
            followupExportModel.setPeriods(excportResultModel.getPeriods());//还款期数
            followupExportModel.setPerPayAmount(excportResultModel.getPerPayAmount());//每期还款金额
            followupExportModel.setOtherAmt(excportResultModel.getOtherAmt());//其他费用
            followupExportModel.setOverDueDate(excportResultModel.getOverDueDate());//逾期日期
            followupExportModel.setOverduePeriods(excportResultModel.getOverduePeriods());//逾期期数
            followupExportModel.setOverdueDays(excportResultModel.getOverdueDays());//逾期天数
            followupExportModel.setHasPayAmount(excportResultModel.getHasPayAmount());//已还款金额
            followupExportModel.setHasPayPeriods(excportResultModel.getHasPayPeriods());//已还款期数
            followupExportModel.setLatelyPayDate(excportResultModel.getLatelyPayDate());//最近还款日期
            followupExportModel.setLatelyPayAmount(excportResultModel.getLatelyPayAmount());//最近还款金额
            followupExportModel.setCommissionRate(excportResultModel.getCommissionRate());//佣金比例
            followupExportModel.setSeries(excportResultModel.getProduct().getProductSeries().getSeriesName());//产品系列
            Personal personalInfo = excportResultModel.getPersonalInfo();
            followupExportModel.setDepositBank(Objects.isNull(personalInfo) ? "" : (personalInfo.getPersonalBankInfos().isEmpty() ? "" : personalInfo.getPersonalBankInfos().iterator().next().getDepositBank()));//客户银行
            followupExportModel.setCardNumber(Objects.isNull(personalInfo) ? "" : (personalInfo.getPersonalBankInfos().isEmpty() ? "" : personalInfo.getPersonalBankInfos().iterator().next().getCardNumber()));//客户卡号
            followupExportModel.setPersonalName(Objects.isNull(personalInfo) ? "" : personalInfo.getName());//客户姓名
            followupExportModel.setIdCard(Objects.isNull(personalInfo) ? "" : personalInfo.getIdCard());//客户身份证号
            followupExportModel.setMobileNo(personalInfo.getMobileNo());//客户手机号
            followupExportModel.setIdCardAddress(personalInfo.getIdCardAddress());//客户身份证地址
            followupExportModel.setLocalHomeAddress(personalInfo.getLocalHomeAddress());//客户监听地址
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
                    if (Objects.equals(personalContact.getRelation(), relationValues[j].getValue())) {
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
                    if (Objects.equals(personalContact.getRelation(), relationValues[j].getValue())) {
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
                    if (Objects.equals(personalContact.getRelation(), relationValues[j].getValue())) {
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
                    if (Objects.equals(personalContact.getRelation(), relationValues[j].getValue())) {
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
                    if (Objects.equals(personalContact.getRelation(), relationValues[j].getValue())) {
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
                    if (Objects.equals(personalContact.getRelation(), relationValues[j].getValue())) {
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
                    if (Objects.equals(personalContact.getRelation(), relationValues[j].getValue())) {
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
                    if (Objects.equals(personalContact.getRelation(), relationValues[j].getValue())) {
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
                    if (Objects.equals(personalContact.getRelation(), relationValues[j].getValue())) {
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
                    if (Objects.equals(personalContact.getRelation(), relationValues[j].getValue())) {
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
                    if (Objects.equals(personalContact.getRelation(), relationValues[j].getValue())) {
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
                    if (Objects.equals(personalContact.getRelation(), relationValues[j].getValue())) {
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
                    if (Objects.equals(personalContact.getRelation(), relationValues[j].getValue())) {
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
                    if (Objects.equals(personalContact.getRelation(), relationValues[j].getValue())) {
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
                    if (Objects.equals(personalContact.getRelation(), relationValues[j].getValue())) {
                        followupExportModel.setConcat15Relation(relationValues[j].getRemark());
                        break;
                    }
                }
            }

            List<CaseFollowupRecord> caseFollowupRecords = excportResultModel.getCaseFollowupRecords();
            if (Objects.nonNull(caseFollowupRecords) && !caseFollowupRecords.isEmpty()) {
                for (CaseFollowupRecord record : caseFollowupRecords) {
                    FollowupExportModel result = new FollowupExportModel();
                    BeanUtils.copyProperties(followupExportModel, result);
                    result.setFollTime(ZWDateUtil.fomratterDate(record.getOperatorTime(), null));//跟进时间
                    result.setFollTargetName(record.getTargetName());//跟进对象姓名
                    result.setFollContent(record.getContent());//跟进内容
                    result.setFollOperator(record.getOperatorName());//跟进人名称
                    CaseFollowupRecord.Type[] values = CaseFollowupRecord.Type.values(); //跟进方式
                    for (int j = 0; j < values.length; j++) {
                        if (Objects.equals(record.getType(), values[j].getValue())) {
                            result.setFollType(values[j].getRemark());
                            break;
                        }
                    }
                    CaseFollowupRecord.Target[] values1 = CaseFollowupRecord.Target.values(); //催收对象
                    for (int j = 0; j < values1.length; j++) {
                        if (Objects.equals(record.getTarget(), values1[j].getValue())) {
                            result.setFollTarget(values1[j].getRemark());
                            break;
                        }
                    }
                    // 电话催收
                    if (Objects.equals(record.getType(), CaseFollowupRecord.Type.TEL.getValue())) {
                        result.setFollPhoneNum(record.getContactPhone());
                    }
                    if (!Objects.equals(record.getType(), CaseFollowupRecord.Type.TEL.getValue())) {
                        result.setFollPhoneNum(record.getDetail());
                    }
                    CaseFollowupRecord.EffectiveCollection[] values2 = CaseFollowupRecord.EffectiveCollection.values(); //催收反馈
                    for (int j = 0; j < values2.length; j++) {
                        if (Objects.equals(record.getCollectionFeedback(), values2[j].getValue())) {
                            result.setFollFeedback(values2[j].getRemark());
                            break;
                        }
                    }
                    CaseFollowupRecord.InvalidCollection[] values3 = CaseFollowupRecord.InvalidCollection.values();
                    for (int j = 0; j < values3.length; j++) {
                        if (Objects.equals(record.getCollectionFeedback(), values3[j].getValue())) {
                            result.setFollFeedback(values3[j].getRemark());
                            break;
                        }
                    }
                    followupExportModels.add(result);
                }
            } else {
                followupExportModels.add(followupExportModel);
            }
        }
        return followupExportModels;
    }

    public int getMaxNum(List<ExcportResultModel> list) {
        // 遍历获取到联系人信息做多的数目
        int maxNum = 0;
        for (ExcportResultModel model : list) {
            Set<PersonalContact> personalContacts = model.getPersonalInfo().getPersonalContacts();
            if (personalContacts.size() > maxNum) {
                maxNum = personalContacts.size();
            }
        }
        return maxNum > 4 ? 4 : maxNum;
    }

    public String[] getTitle(List<String> exportItemList, int maxNum) {
        for (int i = 1; i <= maxNum; i++) {
            if (exportItemList.contains("联系人姓名")) {
                exportItemList.add("联系人" + i + "姓名");
            }
            if (exportItemList.contains("联系人手机号码")) {
                exportItemList.add("联系人" + i + "手机号码");
            }
            if (exportItemList.contains("住宅电话")) {
                exportItemList.add("联系人" + i + "住宅电话");
            }
            if (exportItemList.contains("现居地址")) {
                exportItemList.add("联系人" + i + "现居地址");
            }
            if (exportItemList.contains("与客户关系")) {
                exportItemList.add("联系人" + i + "与客户关系");
            }
            if (exportItemList.contains("工作单位")) {
                exportItemList.add("联系人" + i + "工作单位");
            }
            if (exportItemList.contains("单位电话")) {
                exportItemList.add("联系人" + i + "单位电话");
            }
        }
        exportItemList.remove("联系人姓名");
        exportItemList.remove("联系人手机号码");
        exportItemList.remove("住宅电话");
        exportItemList.remove("现居地址");
        exportItemList.remove("与客户关系");
        exportItemList.remove("工作单位");
        exportItemList.remove("单位电话");
        return exportItemList.toArray(new String[exportItemList.size()]);
    }
}
