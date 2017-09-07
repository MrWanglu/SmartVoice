package cn.fintecher.pangolin.report.service;

import cn.fintecher.pangolin.entity.CaseFollowupRecord;
import cn.fintecher.pangolin.report.model.ExportFollowupParams;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/8/15.
 */
@Service
public class FollowRecordExportService {

    private static final String[] TITLE_LIST = {"案件编号", "批次号", "委托方", "跟进时间", "跟进方式", "客户姓名", "客户身份证", "催收对象", "姓名", "电话/地址", "定位地址","催收反馈", "跟进内容","客户号","账户号","手数"};
    private static final String[] PRO_NAMES = {"caseNumber", "batchNumber", "principalName", "follTime", "follType", "personalName", "idcard", "follTarget", "follTargetName", "follPhoneNum", "location","follFeedback", "follContent","personalNum","accountNum","handNum"};


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
            if (Objects.equals(record.getFollowType(),CaseFollowupRecord.Type.TEL.getValue())) {
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
            if (!Objects.equals(record.getFollowType(),CaseFollowupRecord.Type.TEL.getValue())) {
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
}
