package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.entity.*;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author : sunyanping
 * @Description : 客户信息导出
 * @Date : 2017/7/27.
 */
@Service("personalInfoExportService")
public class PersonalInfoExportService {

    private static final String[] COLLECT_DATA = {"机构名称", "客户姓名", "身份证号", "联系电话", "归属城市", "总期数", "逾期天数", "逾期金额", "贷款日期", "还款状态", "催收员"};
    private static final String[] COLLECT_PRO = {"deptName", "custName", "idCard", "phone", "city", "periods", "overDays", "overAmt", "loanDate", "payStatus", "collector"};

    private static final String[] BASE_INFO_DATA = {"客户姓名", "身份证号", "归属城市", "手机号", "身份证户籍地址", "家庭地址", "家庭电话"};
    private static final String[] BASE_INFO_PRO = {"custName", "idCard", "city", "phone", "idCardAddress", "homeAddress", "homePhone"};

    private static final String[] WORK_INFO_DATA = {"工作单位名称", "工作单位地址", "工作单位电话"};
    private static final String[] WORK_INFO_PRO = {"workName", "workAddress", "workPhone"};

    private static final String[] CONTACT_INFO_DATA = {"关系", "姓名", "手机号码", "住宅电话", "现居住地址", "工作单位", "单位电话"};
    private static final String[] CONTACT_INFO_PRO = {"relation", "contactName", "contactPhone", "contactHomePhone", "contactAddress", "contactWorkCompany", "contactWorkPhone"};

    private static final String[] BANK_INFO_DATA = {"还款卡银行", "还款卡号"};
    private static final String[] BANK_INFO_PRO = {"bankName", "bankCard"};

    private static final String[] BATCH_NUM_DATA = {"机构名称", "客户姓名", "身份证号", "联系电话", "归属城市", "总期数", "逾期天数", "逾期金额", "贷款日期", "还款状态", "批次号"};
    private static final String[] BATCH_NUM_PRO = {"deptName", "custName", "idCard", "phone", "city", "periods", "overDays", "overAmt", "loanDate", "payStatus", "batchNum"};

    private static final String[] CASE_STATUS_DATA = {"机构名称", "客户姓名", "身份证号", "联系电话", "归属城市", "总期数", "逾期天数", "逾期金额", "贷款日期", "还款状态", "案件状态"};
    private static final String[] CASE_STATUS_PRO = {"deptName", "custName", "idCard", "phone", "city", "periods", "overDays", "overAmt", "loanDate", "payStatus", "caseStatus"};

    /**
     * 创建表头
     *
     * @param exportType 导出维度
     * @param list       选择的配置项
     * @param maxNum     筛选的数据中联系人最大数
     * @return
     */
    public Map<String, String> createHeadMap(Integer exportType, List<List<String>> list, Integer maxNum) {
        Map<String, String> headMap = new LinkedHashMap<>(); //存储头信息
        if (Objects.equals(exportType, 0)) {
            List<String> collect = list.get(0);
            // 遍历collect
            for (int i = 0; i < collect.size(); i++) {
                for (int k = 0; k < COLLECT_DATA.length; k++) {
                    if (Objects.equals(collect.get(i), COLLECT_DATA[k])) {
                        headMap.put(COLLECT_PRO[k], collect.get(i));
                        break;
                    }
                }
            }
        }
        if (Objects.equals(exportType, 1)) {
            List<String> baseInfo = list.get(0); // 基本信息
            List<String> workInfo = list.get(1); // 工作信息
            List<String> contactInfo = list.get(2); // 联系人信息
            List<String> bankInfo = list.get(3); // 开户信息
            // 遍历选项
            for (int i = 0; i < baseInfo.size(); i++) {
                for (int k = 0; k < BASE_INFO_DATA.length; k++) {
                    if (Objects.equals(baseInfo.get(i), BASE_INFO_DATA[k])) {
                        headMap.put(BASE_INFO_PRO[k], baseInfo.get(i));
                        break;
                    }
                }
            }
            for (int i = 0; i < workInfo.size(); i++) {
                for (int k = 0; k < WORK_INFO_DATA.length; k++) {
                    if (Objects.equals(workInfo.get(i), WORK_INFO_DATA[k])) {
                        headMap.put(WORK_INFO_PRO[k], workInfo.get(i));
                        break;
                    }
                }
            }
            if (Objects.nonNull(maxNum) && !Objects.equals(maxNum, 0)) {
                if (Objects.equals(maxNum, 1)) {
                    for (String con : contactInfo) {
                        for (int k = 0; k < CONTACT_INFO_DATA.length; k++) {
                            if (Objects.equals(con, CONTACT_INFO_DATA[k])) {
                                headMap.put(CONTACT_INFO_PRO[k], con);
                                break;
                            }
                        }
                    }
                } else {
                    for (int m = 1; m <= maxNum; m++) {
                        for (String con : contactInfo) {
                            for (int k = 0; k < CONTACT_INFO_DATA.length; k++) {
                                if (Objects.equals(con, CONTACT_INFO_DATA[k])) {
                                    headMap.put(CONTACT_INFO_PRO[k] + m, con + m);
                                    break;
                                }
                            }
                        }
                    }
                }
            }


            for (int i = 0; i < bankInfo.size(); i++) {
                for (int k = 0; k < BANK_INFO_DATA.length; k++) {
                    if (Objects.equals(bankInfo.get(i), BANK_INFO_DATA[k])) {
                        headMap.put(BANK_INFO_PRO[k], bankInfo.get(i));
                        break;
                    }
                }
            }
        }
        if (Objects.equals(exportType, 2)) {
            List<String> batch = list.get(0);
            // 遍历batch
            for (int i = 0; i < batch.size(); i++) {
                for (int k = 0; k < BATCH_NUM_DATA.length; k++) {
                    if (Objects.equals(batch.get(i), BATCH_NUM_DATA[k])) {
                        headMap.put(BATCH_NUM_PRO[k], batch.get(i));
                        break;
                    }
                }
            }
        }
        if (Objects.equals(exportType, 3)) {
            List<String> caseStatus = list.get(0);
            // 遍历caseStatus
            for (int i = 0; i < caseStatus.size(); i++) {
                for (int k = 0; k < CASE_STATUS_DATA.length; k++) {
                    if (Objects.equals(caseStatus.get(i), CASE_STATUS_DATA[k])) {
                        headMap.put(CASE_STATUS_PRO[k], caseStatus.get(i));
                        break;
                    }
                }
            }
        }
        return headMap;
    }

    /**
     * 生成数据
     *
     * @param caseInfoList 根据条件筛选的数据
     * @return
     */
    public List<Map<String, Object>> createDataList(List<CaseInfo> caseInfoList) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (CaseInfo caseInfo : caseInfoList) {
            Map<String, Object> map = new HashMap<>();
            map.put("deptName", Objects.isNull(caseInfo.getDepartment()) ? "": caseInfo.getDepartment().getName()); //部门名称
            if (Objects.isNull(caseInfo.getPersonalInfo())) {
                map.put("custName", ""); //客户姓名
                map.put("idCard",""); //身份证号
                map.put("phone", ""); //联系电话
                map.put("city", ""); //归属城市
                map.put("idCardAddress", ""); //身份证户籍地址
                map.put("homeAddress", ""); //家庭地址
                map.put("homePhone", ""); //家庭电话
            } else {
                map.put("custName",caseInfo.getPersonalInfo().getName()); //客户姓名
                map.put("idCard",caseInfo.getPersonalInfo().getIdCard()); //身份证号
                map.put("phone",caseInfo.getPersonalInfo().getMobileNo()); //联系电话
                map.put("city",caseInfo.getPersonalInfo().getIdCardAddress()); //归属城市
                map.put("idCardAddress", caseInfo.getPersonalInfo().getIdCardAddress()); //身份证户籍地址
                map.put("homeAddress", caseInfo.getPersonalInfo().getLocalHomeAddress()); //家庭地址
                map.put("homePhone", caseInfo.getPersonalInfo().getLocalPhoneNo()); //家庭电话
            }

            map.put("periods", caseInfo.getPeriods()); //总期数
            map.put("overDays", caseInfo.getOverdueDays()); //逾期天数
            map.put("overAmt", caseInfo.getOverdueAmount()); //逾期金额
            map.put("loanDate", caseInfo.getLoanDate()); //贷款日期
            map.put("payStatus", caseInfo.getPayStatus()); //还款状态
            map.put("batchNum", caseInfo.getBatchNumber()); //批次号
            CaseInfo.CollectionStatus[] values1 = CaseInfo.CollectionStatus.values();
            for (int i = 0; i< values1.length; i++) {
                if (Objects.equals(values1[i].getValue(), caseInfo.getCollectionStatus())) { //案件状态
                    map.put("caseStatus", values1[i].getRemark());
                    break;
                }
            }
            if (Objects.isNull(caseInfo.getCurrentCollector())) {
                map.put("collector", ""); //催收员
            } else {
                map.put("collector", caseInfo.getCurrentCollector().getRealName()); //催收员
            }
            // 工作相关
            Iterator<PersonalJob> iterator = caseInfo.getPersonalInfo().getPersonalJobs().iterator();
            if (iterator.hasNext()) {
                PersonalJob next = iterator.next();
                map.put("workName", next.getCompanyName()); //单位名称
                map.put("workAddress", next.getAddress()); //单位地址
                map.put("workPhone", next.getPhone()); //单位电话
            } else {
                map.put("workName", ""); //单位名称
                map.put("workAddress", ""); //单位地址
                map.put("workPhone", ""); //单位电话
            }
            //联系人相关
            Iterator<PersonalContact> conIterator = caseInfo.getPersonalInfo().getPersonalContacts().iterator();
            List<PersonalContact> personalContacts = IteratorUtils.toList(conIterator);
            if (personalContacts.size() > 4) {
                Collections.sort(personalContacts, (o1, o2) -> {
                    int i = (int)(o2.getOperatorTime().getTime() - o1.getOperatorTime().getTime());
                    return i;
                });
                personalContacts = personalContacts.subList(0,4);
            }
            if (Objects.equals(personalContacts.size(), 1)) {
                PersonalContact personalContact = personalContacts.get(0);
                Personal.RelationEnum[] values = Personal.RelationEnum.values();
                for (int i = 0; i< values.length; i++) {
                    if (Objects.equals(values[i].getValue(), personalContact.getRelation())) {
                        map.put("relation", values[i].getRemark());
                        break;
                    }
                }
                map.put("contactName", personalContact.getName());
                map.put("contactPhone", personalContact.getPhone());
                map.put("contactHomePhone", personalContact.getMobile());
                map.put("contactAddress", personalContact.getAddress());
                map.put("contactWorkAddress", personalContact.getAddress());
                map.put("contactWorkPhone", personalContact.getWorkPhone());
            } else if (personalContacts.size() > 1) {
                for (int i = 1; i <= personalContacts.size() && i <=4; i++) {
                    PersonalContact personalContact = personalContacts.get(i - 1);
                    Personal.RelationEnum[] values = Personal.RelationEnum.values();
                    for (int k = 0; k< values.length; k++) {
                        if (Objects.equals(values[k].getValue(), personalContact.getRelation())) {
                            map.put("relation" + i, values[k].getRemark());
                            break;
                        }
                    }
                    map.put("contactName" + i, personalContact.getName());
                    map.put("contactPhone" + i, personalContact.getMobile());
                    map.put("contactHomePhone" + i, personalContact.getPhone());
                    map.put("contactAddress" + i, personalContact.getAddress());
                    map.put("contactWorkCompany" + i, personalContact.getEmployer());
                    map.put("contactWorkPhone" + i, personalContact.getWorkPhone());
                }
            } else {
                map.put("relation", "");
                map.put("contactName", "");
                map.put("contactPhone", "");
                map.put("contactHomePhone", "");
                map.put("contactAddress", "");
                map.put("contactWorkAddress", "");
                map.put("contactWorkPhone", "");
            }
            //开户信息相关
            Iterator<PersonalBank> bankIterator = caseInfo.getPersonalInfo().getPersonalBankInfos().iterator();
            if (bankIterator.hasNext()) {
                PersonalBank next = bankIterator.next();
                map.put("bankName", next.getDepositBank());
                map.put("bankCard", next.getCardNumber());
            } else {
                map.put("bankName", "");
                map.put("bankCard", "");
            }
            dataList.add(map);
        }
        return dataList;
    }

    /**
     * 获取多个案件中联系人最多的数
     * @param caseInfoList
     * @return
     */
    public int getMaxNum(List<CaseInfo> caseInfoList) {
        // 遍历获取到联系人信息做多的数目
        int maxNum = 0;
        for (CaseInfo caseInfo : caseInfoList) {
            Set<PersonalContact> personalContacts = caseInfo.getPersonalInfo().getPersonalContacts();
            if (personalContacts.size() > maxNum) {
                maxNum = personalContacts.size();
            }
        }
        return maxNum > 4 ? 4 : maxNum;
    }
}
