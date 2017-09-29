package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.CaseInfoVerModel;
import cn.fintecher.pangolin.business.model.CaseInfoVerficationModel;
import cn.fintecher.pangolin.business.model.CaseInfoVerificationParams;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.business.utils.ExcelExportHelper;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.message.SendReminderMessage;
import cn.fintecher.pangolin.entity.util.ExcelUtil;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author yuanyanting
 * @version Id:CaseInfoVerificationService.java,v 0.1 2017/8/31 19:30 yuanyanting Exp $$
 */
@Service(value = "caseInfoVerificationService")
public class CaseInfoVerificationService {

    private final Logger logger = LoggerFactory.getLogger(CaseInfoVerificationService.class);

    @Inject
    private RestTemplate restTemplate;

    @Inject
    private EntityManager em;

    @Inject
    private DataDictRepository dataDictRepository;

    @Inject
    private CaseInfoVerificationApplyRepository caseInfoVerificationApplyRepository;

    @Inject
    private CaseInfoRepository caseInfoRepository;

    @Inject
    private CaseAssistRepository caseAssistRepository;

    @Inject
    private CaseTurnRecordRepository caseTurnRecordRepository;

    @Inject
    private CaseInfoVerificationRepository caseInfoVerificationRepository;

    @Inject
    private ReminderService reminderService;

    /**
     * @Description 查询核销案件
     */
    public List<Object[]> getCastInfoList(CaseInfoVerficationModel caseInfoVerificationModel, User tokenUser) {
        StringBuilder queryCon = new StringBuilder("SELECT b.case_number,c.`name`,c.mobile_no,c.id_card,b.batch_number,b.overdue_days,b.overdue_amount,d.`name` AS pname,e.real_name,b.collection_status " +
                "FROM (SELECT id,case_id FROM case_info_verification WHERE 1=1 ");
        List<String> ids = caseInfoVerificationModel.getIds();
        if (Objects.nonNull(caseInfoVerificationModel.getIds())) {
            queryCon.append(" AND id in (");
            for(int i = 0; i < ids.size(); i++) {
                if (i < ids.size() - 1) {
                    queryCon.append("'").append(ids.get(i)).append("',");
                }else {
                    queryCon.append("'").append(ids.get(i)).append("'");
                }
            }
            queryCon.append(") ");
        }
        if (Objects.isNull(tokenUser.getCompanyCode())) {
            if (Objects.nonNull(caseInfoVerificationModel.getCompanyCode())) {
                queryCon.append(" AND company_code = ").append("'").append(caseInfoVerificationModel.getCompanyCode()).append("'");
            }
        }else {
            queryCon.append(" AND company_code = ").append("'").append(tokenUser.getCompanyCode()).append("'");
        }
        queryCon.append(") AS a LEFT JOIN case_info b ON a.case_id = b.id LEFT JOIN personal c ON b.personal_id = c.id LEFT JOIN principal d ON b.principal_id = d.id LEFT JOIN `user` e ON b.current_collector = e.id");
        logger.debug(queryCon.toString());
        List<Object[]> resultList = em.createNativeQuery(queryCon.toString()).getResultList();
        return resultList;
    }

    /**
     * @Description 查询核销案件的报表
     */
    public List<CaseInfoVerModel> getList(CaseInfoVerificationParams caseInfoVerificationParams, User tokenUser) {
        StringBuilder queryCon = new StringBuilder("SELECT count(a.id),c.area_name,sum(b.overdue_amount) from (case_info_verification a LEFT JOIN case_info b on a.case_id = b.id)LEFT JOIN area_code c ON b.area_id = c.id where 1=1");
        //公司code码
        if (Objects.isNull(tokenUser.getCompanyCode())) {
            if (StringUtils.isNotBlank(caseInfoVerificationParams.getCompanyCode())) {
                queryCon.append(" and a.company_code = ").append(caseInfoVerificationParams.getCompanyCode());
            }
        } else {
            queryCon.append(" and a.company_code = ").append("'").append(tokenUser.getCompanyCode()).append("'");
        }
        //开始时间
        if (StringUtils.isNotBlank(caseInfoVerificationParams.getStartTime())) {
            queryCon.append(" and b.case_follow_in_time >= ").append("'").append(caseInfoVerificationParams.getStartTime()).append(" 00:00:00").append("'");
        }
        //结束时间
        if (StringUtils.isNotBlank(caseInfoVerificationParams.getEndTime())) {
            queryCon.append(" and b.case_follow_in_time <= ").append("'").append(caseInfoVerificationParams.getEndTime()).append(" 23:59:59").append("'");
        }
        queryCon.append(" group by c.area_name");
        logger.debug(queryCon.toString());
        List<Object[]> objects = em.createNativeQuery(queryCon.toString()).getResultList();
        List<CaseInfoVerModel> caseInfoVerModels = new ArrayList<>();
        if (!objects.isEmpty()) {
            for (Object[] obj : objects) {
                CaseInfoVerModel caseInfoVerModel = new CaseInfoVerModel();
                caseInfoVerModel.setCityCount(Integer.parseInt(checkValueIsNull(obj[0])));
                caseInfoVerModel.setCity(checkValueIsNull(obj[1]));
                caseInfoVerModel.setAmount(new BigDecimal(checkValueIsNull(obj[2])));
                caseInfoVerModel.setAmountStr(caseInfoVerModel.getAmount().toString());
                caseInfoVerModels.add(caseInfoVerModel);
            }
        }
        return caseInfoVerModels;
    }
    
    /**
     * 核销案件的导出操作
     *
     * @param caseInfoVerificationList
     * @return
     */
    public String exportCaseInfoVerification(List<Object[]> caseInfoVerificationList) {
        HSSFWorkbook workbook = null;
        File file = null;
        ByteArrayOutputStream out = null;
        FileOutputStream fileOutputStream = null;
        Map<String, String> headMap = new LinkedHashMap<>(); // Excel头
        headMap.put("caseNumber", "案件编号");
        headMap.put("personalName", "客户姓名");
        headMap.put("personalMobileNo", "手机号");
        headMap.put("personalIdCard", "身份证号");
        headMap.put("batchNumber", "批次号");
        headMap.put("overdueDays", "逾期天数");
        headMap.put("overdueAmount", "逾期总金额");
        headMap.put("principalName", "委托方");
        headMap.put("currentCollector", "催收员");
        headMap.put("collectionStatus", "催收状态");
        List<Map<String, Object>> dataList = new ArrayList<>();
        try {
            caseInfoVerificationList.get(0);
            Map<String, Object> map;
            for (Object[] caseInfoVerification : caseInfoVerificationList) {
                map = new LinkedHashMap<>();
                map.put("caseNumber", caseInfoVerification[0]); // 案件编号
                map.put("personalName", caseInfoVerification[1]); // 客户姓名
                map.put("personalMobileNo", caseInfoVerification[2]); // 手机号
                if (Objects.nonNull(caseInfoVerification[3])) {
                    map.put("personalIdCard", caseInfoVerification[3]); // 身份证号
                }
                map.put("batchNumber", caseInfoVerification[4]); // 批次号
                map.put("overdueDays", caseInfoVerification[5]); // 逾期天数
                map.put("overdueAmount", caseInfoVerification[6]); // 逾期总金额
                if (Objects.nonNull(caseInfoVerification[7])) {
                    map.put("principalName", caseInfoVerification[7]); // 委托方
                }
                if (Objects.nonNull(caseInfoVerification[8])) {
                    map.put("currentCollector", caseInfoVerification[8]); // 催收员
                }
                DataDict dataDict = dataDictRepository.findOne(QDataDict.dataDict.id.eq(Integer.parseInt(caseInfoVerification[9].toString())));
                map.put("collectionStatus", dataDict.getName()); // 催收状态
                dataList.add(map);
            }
            workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("核销管理");
            ExcelExportHelper.createExcel(workbook, sheet, headMap, dataList, 0, 0);
            out = new ByteArrayOutputStream();
            workbook.write(out);
            String filePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(DateTime.now().toString("yyyyMMddhhmmss") + "核销管理表.xls");
            file = new File(filePath);
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(out.toByteArray());
            FileSystemResource resource = new FileSystemResource(file);
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.add("file", resource);
            return restTemplate.postForEntity("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class).getBody();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            // 关闭流
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 删除文件
            if (file != null) {
                file.delete();
            }
        }
    }

    /**
     * 核销报表的导出操作
     *
     * @param caseInfoVerificationParams
     * @param tokenUser
     * @return
     */
    public String exportReport(CaseInfoVerificationParams caseInfoVerificationParams, User tokenUser) {
        HSSFWorkbook workbook = null;
        File file = null;
        ByteArrayOutputStream out = null;
        FileOutputStream fileOutputStream = null;
        workbook = new HSSFWorkbook();
        try {
            List<CaseInfoVerModel> caseInfoVerModelList = getList(caseInfoVerificationParams, tokenUser);
            HSSFSheet sheet = workbook.createSheet("核销管理报表");
            String[] titleList = {"区域", "累计金额", "累计户数"};
            String[] proNames = {"city", "amountStr", "cityCount"};
            ExcelUtil.createExcel(workbook, sheet, caseInfoVerModelList, titleList, proNames, 0, 0);
            out = new ByteArrayOutputStream();
            workbook.write(out);
            String filePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(DateTime.now().toString("yyyyMMddhhmmss") + "核销报表.xls");
            file = new File(filePath);
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(out.toByteArray());
            FileSystemResource resource = new FileSystemResource(file);
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.add("file", resource);
            String url = restTemplate.postForEntity("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class).getBody();
            return url;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
            return null;
        } finally {
            // 关闭流
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 删除文件
            if (file != null) {
                file.delete();
            }
        }
    }

    /**
     * 检查对象是否为字符串null
     *
     * @param obj
     * @return
     */
    private String checkValueIsNull(Object obj) {
        if (Objects.nonNull(obj)) {
            return String.valueOf(obj.equals("null") ? "" : obj);
        } else {
            return null;
        }
    }

    /**
     * set核销申请属性值
     * @param apply 核销申请
     * @param caseInfo 案件
     * @param user 申请人
     * @param applyReason
     */
    public void setVerificationApply(CaseInfoVerificationApply apply, CaseInfo caseInfo, User user, String applyReason) {
        BeanUtils.copyProperties(caseInfo, apply);
        apply.setApplicant(user.getRealName()); // 申请人
        apply.setApplicationDate(ZWDateUtil.getNowDateTime()); // 申请日期
        apply.setApplicationReason(applyReason); // 申请理由
        apply.setApprovalStatus(CaseInfoVerificationApply.ApprovalStatus.approval_pending.getValue()); // 申请状态：审批待通过
        apply.setCaseId(caseInfo.getId()); // 案件Id
        if (Objects.nonNull(caseInfo.getArea())) {
            apply.setCity(caseInfo.getArea().getId()); // 城市
            if (Objects.nonNull(caseInfo.getArea().getParent())) {
                apply.setProvince(caseInfo.getArea().getParent().getId()); // 省份
            }
        }
        if (Objects.nonNull(caseInfo.getPrincipalId())) {
            apply.setPrincipalName(caseInfo.getPrincipalId().getName()); // 委托方名称
        }
        if (Objects.nonNull(caseInfo.getPersonalInfo())) {
            apply.setPersonalName(caseInfo.getPersonalInfo().getName()); // 客户名称
            apply.setMobileNo(caseInfo.getPersonalInfo().getMobileNo()); // 电话号
            apply.setIdCard(caseInfo.getPersonalInfo().getIdCard()); // 身份证号
        }
    }

    /**
     * set核销申请通过属性值
     *
     * @param caseInfoVerficationModel 核销申请
     * @param user 审批人
     */
    public void caseInfoVerificationApply(CaseInfoVerficationModel caseInfoVerficationModel,User user) {
        CaseInfoVerificationApply caseInfoVerificationApply = caseInfoVerificationApplyRepository.findOne(caseInfoVerficationModel.getId());
        CaseInfoVerification caseInfoVerification = new CaseInfoVerification();
        // 超级管理员
        if (Objects.isNull(user.getCompanyCode())) {
            if (Objects.nonNull(caseInfoVerficationModel.getCompanyCode())) {
                caseInfoVerificationApply.setCompanyCode(caseInfoVerficationModel.getCompanyCode());
                caseInfoVerification.setCompanyCode(caseInfoVerficationModel.getCompanyCode());
            }
        }else {
            caseInfoVerificationApply.setCompanyCode(user.getCompanyCode());
            caseInfoVerification.setCompanyCode(user.getCompanyCode());
        }
        if (Objects.equals(caseInfoVerficationModel.getApprovalResult(), 0)) { // 核销审批拒绝
            caseInfoVerificationApply.setApprovalResult(CaseInfoVerificationApply.ApprovalResult.disapprove.getValue()); // 审批结果：拒绝
            caseInfoVerificationApply.setApprovalStatus(CaseInfoVerificationApply.ApprovalStatus.approval_disapprove.getValue()); // 审批状态：审批拒绝
            caseInfoVerificationApplyRepository.save(caseInfoVerificationApply);
        } else { // 核销审批通过
            caseInfoVerificationApply.setApprovalResult(CaseInfoVerificationApply.ApprovalResult.approve.getValue()); // 审批结果：通过
            caseInfoVerificationApply.setApprovalStatus(CaseInfoVerificationApply.ApprovalStatus.approval_approve.getValue()); // 审批状态：审批通过
            caseInfoVerificationApply.setOperator(user.getUserName()); // 审批人
            caseInfoVerificationApply.setOperatorTime(ZWDateUtil.getNowDateTime()); // 审批时间
            CaseInfo caseInfo = caseInfoRepository.findOne(caseInfoVerificationApply.getCaseId());
            List<CaseAssist> caseAssistList = new ArrayList<>();
            //处理协催案件
            if (Objects.equals(caseInfo.getAssistFlag(), 1)) { //协催标识
                //结束协催案件
                CaseAssist one = caseAssistRepository.findOne(QCaseAssist.caseAssist.caseId.eq(caseInfo).and(QCaseAssist.caseAssist.assistStatus.notIn(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())));
                if (Objects.nonNull(one)) {
                    one.setAssistCloseFlag(0); //手动结束
                    one.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //协催结束
                    one.setOperator(user);
                    one.setOperatorTime(new Date());
                    one.setCaseFlowinTime(new Date()); //流入时间
                    caseAssistList.add(one);
                }
                caseInfo.setAssistFlag(0); //协催标识置0
                caseInfo.setAssistStatus(null);//协催状态置空
                caseInfo.setAssistWay(null);
                caseInfo.setAssistCollector(null);
                caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //29-协催完成
                //协催结束新增一条流转记录
                CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
                BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
                caseTurnRecord.setId(null); //主键置空
                caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
                caseTurnRecord.setDepartId(caseInfo.getDepartment().getId()); //部门ID
                caseTurnRecord.setReceiveUserRealName(caseInfo.getCurrentCollector().getRealName()); //接受人名称
                caseTurnRecord.setReceiveDeptName(caseInfo.getCurrentCollector().getDepartment().getName()); //接收部门名称
                caseTurnRecord.setOperatorUserName(user.getUserName()); //操作员用户名
                caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                caseTurnRecordRepository.saveAndFlush(caseTurnRecord);
            }
            caseInfo.setEndType(CaseInfo.EndType.CLOSE_CASE.getValue()); // 结案类型：核销结案
            caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.CASE_OVER.getValue()); // 催收状态：已结案
            caseInfoRepository.save(caseInfo);
            caseInfoVerification.setCaseInfo(caseInfo);
            caseInfoVerificationRepository.save(caseInfoVerification);
            caseInfoVerification.setOperator(user.getRealName()); // 操作人
            caseInfoVerification.setOperatorTime(ZWDateUtil.getNowDateTime()); // 操作时间
            caseInfoVerification.setState(caseInfoVerficationModel.getState()); // 核销说明
            caseInfoVerificationApplyRepository.save(caseInfoVerificationApply);
            //消息提醒
            SendReminderMessage sendReminderMessage = new SendReminderMessage();
            sendReminderMessage.setUserId(user.getId());
            sendReminderMessage.setTitle("客户 [" + caseInfo.getPersonalInfo().getName() + "] 的核销申请审批" + (Objects.equals(caseInfoVerficationModel.getApprovalResult(), 0) ? "拒绝" : "通过"));
            sendReminderMessage.setContent(caseInfoVerficationModel.getApprovalOpinion());
            sendReminderMessage.setType(ReminderType.verification);
            reminderService.sendReminder(sendReminderMessage);
        }
    }
}
