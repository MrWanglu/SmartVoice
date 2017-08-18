package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.PaymentModel;
import cn.fintecher.pangolin.business.model.PaymentParams;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.business.utils.ExcelExportHelper;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.apache.commons.io.FileUtils;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author : xiaqun
 * @Description : 还款审批业务
 * @Date : 9:43 2017/7/29
 */

@Service("paymentService")
public class PaymentService {
    final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Inject
    CasePayApplyRepository casePayApplyRepository;

    @Inject
    CaseInfoRepository caseInfoRepository;

    @Inject
    CaseAssistApplyRepository caseAssistApplyRepository;

    @Inject
    CaseAssistRepository caseAssistRepository;

    @Inject
    CaseInfoService caseInfoService;

    @Inject
    CaseTurnRecordRepository caseTurnRecordRepository;

    @Inject
    RestTemplate restTemplate;

    /**
     * @Description 还款信息展示
     */
    public PaymentModel getPaymentInfo(String casePayApplyId) {
        CasePayApply casePayApply = casePayApplyRepository.findOne(casePayApplyId); //获取还款记录信息
        if (Objects.isNull(casePayApply)) {
            throw new RuntimeException("该还款记录未找到");
        }
        CaseInfo caseInfo = caseInfoRepository.findOne(casePayApply.getCaseId()); //获取案件信息
        if (Objects.isNull(caseInfo)) {
            throw new RuntimeException("该案件信息未找到");
        }
        PaymentModel paymentModel = new PaymentModel();
        BeanUtils.copyProperties(caseInfo, paymentModel);
        paymentModel.setName(casePayApply.getPersonalName()); //客户姓名
        paymentModel.setIdCardNum(caseInfo.getPersonalInfo().getIdCard()); //客户身份证号
        paymentModel.setPhone(casePayApply.getPersonalPhone()); //客户手机号
        paymentModel.setDerateAmt(casePayApply.getApplyDerateAmt()); //减免费用
        paymentModel.setDerateRemark(casePayApply.getApproveDerateRemark()); //减免备注
        paymentModel.setPrincipalName(caseInfo.getPrincipalId().getName()); //委托方
        paymentModel.setPaymentAmt(casePayApply.getApplyPayAmt()); //还款金额
        paymentModel.setPaymentRemark(casePayApply.getPayMemo()); //还款备注
        paymentModel.setPayType(casePayApply.getPayType()); //还款类型
        paymentModel.setPayWay(casePayApply.getPayWay()); //还款方式
        paymentModel.setApplyDate(casePayApply.getApplyDate()); //申请日期
        return paymentModel;
    }

    /**
     * @Description 还款审批
     */
    public void approvalPayment(PaymentParams paymentParams, User tokenUser) {
        CasePayApply casePayApply = casePayApplyRepository.findOne(paymentParams.getCasePayApplyId()); //获取还款审批记录
        if (Objects.isNull(casePayApply)) {
            throw new RuntimeException("该还款审批记录未找到");
        }
        CaseInfo caseInfo = caseInfoRepository.findOne(casePayApply.getCaseId()); //获取案件信息
        if (Objects.isNull(caseInfo)) {
            throw new RuntimeException("该案件未找到");
        }
        if (Objects.equals(paymentParams.getFlag(), 0)) { //是减免审批
            if (Objects.equals(paymentParams.getResult(), 0)) { //减免审批拒绝
                //更新还款审批信息
                casePayApply.setApproveStatus(CasePayApply.ApproveStatus.DERATE_AUDIT_REJECT.getValue()); //审批状态 56-减免审批驳回

                //更新原案件信息
                caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.COLLECTIONING.getValue()); //催收状态 21-催收中
                caseInfo.setOperator(tokenUser); //操作人
                caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
            } else { //减免审批通过
                //更新还款审批信息
                casePayApply.setApproveStatus(CasePayApply.ApproveStatus.PAY_TO_AUDIT.getValue()); //审批状态 57-还款待审批

            }
            casePayApply.setApproveDerateUser(tokenUser.getUserName()); //减免审批人
            casePayApply.setApproveDerateName(tokenUser.getRealName()); //减免审批人姓名
            casePayApply.setApproveDerateMemo(paymentParams.getOpinion()); //减免审批意见
            casePayApply.setApproveDerateDatetime(ZWDateUtil.getNowDateTime()); //减免审批时间
        } else { //是还款审批
            if (Objects.equals(paymentParams.getResult(), 0)) { //还款审批拒绝
                //更新还款审批信息
                casePayApply.setApproveStatus(CasePayApply.ApproveStatus.AUDIT_REJECT.getValue()); //还款审批拒绝 59-审批拒绝

                //更新原案件信息
                caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.COLLECTIONING.getValue()); //催收状态 21-催收中
                caseInfo.setOperator(tokenUser); //操作人
                caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
            } else { //还款审核通过
                //更新还款审批信息
                casePayApply.setApproveStatus(CasePayApply.ApproveStatus.AUDIT_AGREE.getValue()); //还款审批状态 58-审核通过

                //更新原案件信息
                if (Objects.equals(casePayApply.getPayType(), CasePayApply.PayType.PARTOVERDUE.getValue())) { //41-部分逾期还款

                    caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.PART_REPAID.getValue()); //催收状态 172-部分已还款
                    caseInfo.setRealPayAmount(casePayApply.getApplyPayAmt().add(caseInfo.getRealPayAmount())); //逾期实际还款金额

                } else if (Objects.equals(casePayApply.getPayType(), CasePayApply.PayType.ALLOVERDUE.getValue()) //42-全额逾期还款
                        || Objects.equals(casePayApply.getPayType(), CasePayApply.PayType.DERATEOVERDUE.getValue())) { //减免逾期还款

                    caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.REPAID.getValue()); //催收状态 171-已还款
                    caseInfo.setDerateAmt(casePayApply.getApplyDerateAmt().add(caseInfo.getDerateAmt())); //逾期还款减免金额
                    caseInfo.setRealPayAmount(casePayApply.getApplyPayAmt().add(caseInfo.getRealPayAmount())); //逾期实际还款金额

                } else if (Objects.equals(casePayApply.getPayType(), CasePayApply.PayType.PARTADVANCE.getValue())) { //44-部分提前结清

                    caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.PART_REPAID.getValue()); //催收状态 172-部分已还款
                    caseInfo.setEarlyRealSettleAmt(casePayApply.getApplyPayAmt().add(caseInfo.getEarlyRealSettleAmt())); //提前结清实际还款金额

                } else if (Objects.equals(casePayApply.getPayType(), CasePayApply.PayType.ALLADVANCE.getValue()) //45-全额提前结清
                        || Objects.equals(casePayApply.getPayType(), CasePayApply.PayType.DERATEADVANCE.getValue())) { //46-减免提前结清

                    caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.REPAID.getValue()); //催收状态 171-已还款
                    caseInfo.setEarlyDerateAmt(casePayApply.getApplyDerateAmt().add(caseInfo.getEarlyDerateAmt())); //提前结清减免金额
                    caseInfo.setEarlyRealSettleAmt(casePayApply.getApplyPayAmt().add(caseInfo.getEarlyRealSettleAmt())); //提前结清实际还款金额

                } else {
                    throw new RuntimeException("该还款类型未找到");
                }

                //判断是否有协催案件或协催申请
                QCaseAssistApply qCaseAssistApply = QCaseAssistApply.caseAssistApply;
                QCaseAssist qCaseAssist = QCaseAssist.caseAssist;
                List<Integer> list = new ArrayList<>(); //协催审批状态列表
                list.add(CaseAssistApply.ApproveStatus.TEL_APPROVAL.getValue()); // 32-电催待审批
                list.add(CaseAssistApply.ApproveStatus.VISIT_APPROVAL.getValue()); // 34-外访待审批
                if (Objects.equals(caseInfo.getAssistFlag(), 1)) { //有协催标识
                    if (Objects.equals(caseInfo.getAssistStatus(), CaseInfo.AssistStatus.ASSIST_APPROVEING.getValue())) { //有协催申请
                        CaseAssistApply caseAssistApply = caseAssistApplyRepository.findOne(qCaseAssistApply.caseId.eq(caseInfo.getId())
                                .and(qCaseAssistApply.approvePhoneResult.in(list)));
                        if (!Objects.isNull(caseAssistApply)) {
                            caseAssistApply = caseInfoService.getCaseAssistApply(caseInfo.getId(), tokenUser, "案件还款强制拒绝");
                            caseAssistApplyRepository.saveAndFlush(caseAssistApply);
                        } else { //有协催案件
                            CaseAssist caseAssist = caseAssistRepository.findOne(qCaseAssist.caseId.id.eq(caseInfo.getId()).
                                    and(qCaseAssist.assistStatus.ne(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())));
                            if (Objects.isNull(caseAssist)) {
                                throw new RuntimeException("协催案件未找到");
                            }
                            caseAssist.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //协催状态 29-协催完成
                            caseAssist.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                            caseAssist.setOperator(tokenUser); //操作员
                            caseAssistRepository.saveAndFlush(caseAssist);

                            //同步更新原案件协催状态
                            caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //29-协催完成

                            //协催结束新增一条流转记录
                            CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
                            BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
                            caseTurnRecord.setId(null); //主键置空
                            caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
                            caseTurnRecord.setDepartId(caseInfo.getDepartment().getId()); //部门ID
                            caseTurnRecord.setReceiveUserRealName(caseInfo.getCurrentCollector().getRealName()); //接受人名称
                            caseTurnRecord.setReceiveDeptName(caseInfo.getCurrentCollector().getDepartment().getName()); //接收部门名称
                            caseTurnRecord.setOperatorUserName(tokenUser.getUserName()); //操作员用户名
                            caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                            caseTurnRecordRepository.saveAndFlush(caseTurnRecord);
                        }
                    }
                }
            }
            casePayApply.setApprovePayUser(tokenUser.getUserName()); //还款审批人
            casePayApply.setApprovePayName(tokenUser.getRealName()); //还款审批人姓名
            casePayApply.setApprovePayMemo(paymentParams.getOpinion()); //还款审批意见
            casePayApply.setApprovePayDatetime(ZWDateUtil.getNowDateTime()); //还款审批时间
            casePayApply.setOperatorUserName(tokenUser.getUserName()); //操作人
            casePayApply.setOperatorRealName(tokenUser.getRealName()); //操作人姓名
            casePayApply.setOperatorDate(ZWDateUtil.getNowDateTime()); //操作时间
        }
        caseInfoRepository.saveAndFlush(caseInfo);
        casePayApplyRepository.saveAndFlush(casePayApply);
    }

    /**
     * @Description 导出还款记录
     */
    public String exportCasePayApply(List<CasePayApply> casePayApplyList) {
        if (casePayApplyList.isEmpty()) {
            throw new RuntimeException("没有数据");
        }
        HSSFWorkbook workbook = null;
        File file = null;
        ByteArrayOutputStream out = null;
        FileOutputStream fileOutputStream = null;

        Map<String, String> headMap = new HashMap<>(); //excel头
        List<Map<String, Object>> dataList = new ArrayList<>();

        headMap.put("caseNumber", "案件编号");
        headMap.put("batchNumber", "批次号");
        headMap.put("principalName", "委托方");
        headMap.put("personalName", "客户姓名");
        headMap.put("personalPhone", "手机号");
        headMap.put("caseAmt", "案件金额(元)");
        headMap.put("applyPayAmt", "还款金额(元)");
        headMap.put("payType", "还款类型");
        headMap.put("payWay", "还款方式");
        headMap.put("approveStatus", "审核状态");
        headMap.put("approveResult", "审核结果");
        headMap.put("applayDate", "申请日期");
        headMap.put("applayRealName", "申请人");

        try {
            Map<String, Object> map;
            for (CasePayApply casePayApply : casePayApplyList) {
                map = new HashMap<>();
                map.put("caseNumber", casePayApply.getCaseNumber());
                map.put("batchNumber", casePayApply.getBatchNumber());
                map.put("principalName", casePayApply.getPrincipalName());
                map.put("personalName", casePayApply.getPersonalName());
                map.put("personalPhone", casePayApply.getPersonalPhone());
                map.put("caseAmt", casePayApply.getCaseAmt());
                map.put("applyPayAmt", casePayApply.getApplyPayAmt());
                map.put("payType", casePayApply.getPayType());
                map.put("payWay", casePayApply.getPayWay());
                map.put("approveStatus", casePayApply.getApproveStatus());
                map.put("approveResult", casePayApply.getApproveResult());
                map.put("applayDate", casePayApply.getApplyDate());
                map.put("applayRealName", casePayApply.getApplyRealName());
                dataList.add(map);
            }

            workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("还款审批记录");
            ExcelExportHelper.createExcel(workbook, sheet, headMap, dataList, 0, 0);
            out = new ByteArrayOutputStream();
            workbook.write(out);
            String filePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(DateTime.now().toString("yyyyMMddhhmmss") + "客户信息表.xls");
            file = new File(filePath);
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(out.toByteArray());
            FileSystemResource resource = new FileSystemResource(file);
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.add("file", resource);
            return restTemplate.postForEntity("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class).getBody();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        } finally {
            // 关闭流
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
            // 删除文件
            if (file != null) {
                file.delete();
            }
        }
    }
}