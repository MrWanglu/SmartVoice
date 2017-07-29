package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.CasePayApplyParams;
import cn.fintecher.pangolin.business.model.ExportPayApplyModel;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.CasePayApplyRepository;
import cn.fintecher.pangolin.business.service.CaseInfoService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.ExcelUtil;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.BaseObject;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;


/**
 * @Author: LvGuoRong
 * @Description:审批管理信息
 * @Date: 2017/7/19
 */

@RestController
@RequestMapping("/api/accDerateController")
@Api(value = "AccDerateController", description = "案件审批及案件还款审核")
public class AccDerateController extends BaseController {
    final Logger log = LoggerFactory.getLogger(AccDerateController.class);
    private static final String ENTITY_NAME = "casePayApply";
    private static final String APPLY_DATE = "applayDate";
    @Inject
    private CasePayApplyRepository casePayApplyRepository;
    @Inject
    private CaseInfoRepository caseInfoRepository;
    @Inject
    private CaseInfoService caseInfoService;
    @Autowired
    private RestTemplate restTemplate;


    /**
     * @Description 费用减免审批页面多条件查询减免记录
     */
    @GetMapping("/getCasePayApply")
    @ApiOperation(value = "费用减免审批及还款审核多条件查询减免记录", notes = "费用减免审批及还款审核多条件查询减免记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query", value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query", value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query", value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CasePayApply>> getCasePayApply(@RequestParam @ApiParam(value = "减免标识") Integer derateFlag,
                                                              @QuerydslPredicate(root = CasePayApply.class) Predicate predicate,
                                                              @ApiIgnore Pageable pageable){
        try {
        BooleanBuilder builder = new BooleanBuilder(predicate);
        builder.and(QCasePayApply.casePayApply.derateFlag.eq(derateFlag));
        Page<CasePayApply> page = casePayApplyRepository.findAll(builder, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/accDerateController/getCasePayApply");
        return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", ENTITY_NAME, e.getMessage())).body(null);
        }
    }


    @GetMapping("/exportPayApply")
    @ApiOperation(value = "导出还款记录", notes = "导出还款记录")
    public ResponseEntity exportPayApplyModel(@QuerydslPredicate(root = CasePayApply.class) Predicate predicate){
        log.debug("entry the export the pay records");
        HSSFWorkbook workbook = null;
        File file = null;
        ByteArrayOutputStream out = null;
        FileOutputStream fileOutputStream = null;
        try {
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(QCasePayApply.casePayApply.approveStatus.eq(CasePayApply.ApproveStatus.AUDIT_AGREE.getValue()));
            Iterator<CasePayApply> iterable = casePayApplyRepository.findAll(builder).iterator();
            List<CasePayApply> casePayApplies = IteratorUtils.toList(iterable);
            List<ExportPayApplyModel> list = new ArrayList<>();

                for (CasePayApply casePayApply : casePayApplies) {
                    if (Objects.nonNull(casePayApply.getCaseId())){
                    CaseInfo caseInfo = caseInfoRepository.findOne(casePayApply.getCaseId());
                    ExportPayApplyModel exportPayApplyModel = new ExportPayApplyModel();
                    exportPayApplyModel.setApproveStatus(Objects.toString(casePayApply.getApproveStatus()));//审批结果
                    exportPayApplyModel.setApplayDate(Objects.toString(casePayApply.getApplayDate()));//申请日期
                    exportPayApplyModel.setApplayUserName(casePayApply.getApplayUserName());//申请人
                    exportPayApplyModel.setApplyPayAmt(Objects.toString(casePayApply.getApplyPayAmt()));//还款金额
                    exportPayApplyModel.setBatchNumber(casePayApply.getBatchNumber());//批次号
                    exportPayApplyModel.setPayType(Objects.toString(casePayApply.getPayType()));//还款类型
                    exportPayApplyModel.setCaseNumber(casePayApply.getCaseNumber());//案件编号
                    exportPayApplyModel.setPayWay(Objects.toString(casePayApply.getPayWay()));//还款方式
                    exportPayApplyModel.setPersonalPhone(casePayApply.getPersonalPhone());//客户电话
                    exportPayApplyModel.setPrincipalId(casePayApply.getPrincipalId());//委托方
                    exportPayApplyModel.setPersonalName(casePayApply.getPersonalName());//客户姓名
                    exportPayApplyModel.setCaseAmt(Objects.toString(Objects.toString(caseInfo.getOverdueAmount())));//案件金额
                    list.add(exportPayApplyModel);
                     }
                 }

            if (Objects.isNull(list)) {
                throw new RuntimeException("无导出数据");
            }
            String[] titleList = {"案件编号", "批次号", "委托方", "客户姓名", "手机号", "案件金额(元)", "还款金额(元)", "还款类型", "还款方式", "审核结果", "申请日期", "申请人"};
            String[] proNames = {"caseNumber", "batchNumber", "principalId", "personalName", "personalPhone", "CaseAmt", "applyPayAmt", "payType", "payWay", "approveStatus", "applayDate", "applayUserName"};
            workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("sheet1");
            ExcelUtil.createExcel(workbook, sheet, list, titleList, proNames, 0, 0);
            out = new ByteArrayOutputStream();
            workbook.write(out);
            String filePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(DateTime.now().toString("yyyyMMddhhmmss") + "还款记录.xls");
            file = new File(filePath);
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(out.toByteArray());
            FileSystemResource resource = new FileSystemResource(file);
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.add("file", resource);
            ResponseEntity<String> url = restTemplate.postForEntity("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class);
            if (url == null) {
                throw new RuntimeException("上传服务器失败");
            } else {
                return ResponseEntity.ok().build();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage());
            return ResponseEntity.badRequest().body(null);
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
     * 操作减免审批
     */
    @GetMapping("/handleCasePayApply")
    @ApiOperation(value = "操作审批信息", notes = "操作审批信息")
    public ResponseEntity handleCasePayApply(@RequestParam(value = "id") @ApiParam("减免审批id") String id){
        log.debug("REST request to handle AccDerateApply : {}", id);
        try {
        List<Map<String, Object>> mapList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        //获取客户姓名、身份证号、手机号、合同金额、已还期数、逾期天数加入到mapList
        CasePayApply casePayApply = casePayApplyRepository.findOne(id);
        if (Objects.isNull(casePayApply)) {
            throw new RuntimeException("减免信息不存在");
        }
        CaseInfo caseInfo = caseInfoRepository.findOne(casePayApply.getCaseId());
        if (Objects.isNull(caseInfo)) {
            throw new RuntimeException("案件信息不存在");
        }
        String idCard = caseInfo.getPersonalInfo().getIdCard();//客户身份证
        String personalPhone = casePayApply.getPersonalPhone();//客户手机号
        BigDecimal contractAmount = caseInfo.getContractAmount();//合同金额
        Integer overduePeriods = caseInfo.getOverduePeriods();//期数
        Integer overdueDays = caseInfo.getOverdueDays();//逾期天数
        map.put("casePayApply", casePayApply);
        map.put("idCard", idCard);
        map.put("personalPhone", personalPhone);
        map.put("contractAmount", contractAmount);
        map.put("overduePeriods", overduePeriods);
        map.put("overdueDays", overdueDays);
        mapList.add(map);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id)).body(mapList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("审批失败", ENTITY_NAME, e.getMessage())).body(null);
        }
    }

    /**
     * 处理审批信息
     */
    @PostMapping("/updateAccDerateApply")
    @ApiOperation(value = "处理审批信息", notes = "处理审批信息")
    public ResponseEntity updateAccDerateApply(@RequestBody @ApiParam("减免审批对象") CasePayApply casePayApply,
                                               @RequestHeader(value = "X-UserToken") @ApiParam("操作者的token") String token){
        try {
        User user= getUserByToken(token);
        if (Objects.isNull(user)){
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "用户登陆失败")).body(null);
        }
        CasePayApply payApply = casePayApplyRepository.findOne(casePayApply.getId());
        if (Objects.isNull(payApply)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "减免信息不存在")).body(null);
        }
        CaseInfo caseInfo = caseInfoRepository.findOne(casePayApply.getCaseId());
        if (Objects.isNull(caseInfo)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "案件信息不存在")).body(null);
        }
        if (!Objects.equals(casePayApply.getApproveCostresult(), CasePayApply.ApproveCostresult.TO_AUDIT.getValue())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "减免审批已经处理")).body(null);
        }
        if (Objects.equals(casePayApply.getApproveCostresult(), CasePayApply.ApproveCostresult.REVOCATION.getValue())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "减免审批已经撤回")).body(null);
        }
        if (Objects.equals(caseInfo.getHandUpFlag(), CaseInfo.HandUpFlag.YES_HANG.getValue())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "挂起案件不允许操作")).body(null);
        }

        if (Objects.equals(casePayApply.getApproveCostresult(), CasePayApply.ApproveCostresult.AUDIT_AGREE.getValue())) {   //如果费用减免结果为审批同意
            casePayApply.setApproveCostresult(CasePayApply.ApproveCostresult.AUDIT_AGREE.getValue());//审批同意
            casePayApply.setApproveStatus(CasePayApply.ApproveStatus.PAY_TO_AUDIT.getValue());//还款待审核
            casePayApply.setApproveResult(CasePayApply.ApproveResult.DERATE_AGREE.getValue());//减免同意
        } else if (Objects.equals(casePayApply.getApproveCostresult(), CasePayApply.ApproveCostresult.AUDIT_REJECT.getValue())) {   //如果费用减免结果为审批拒绝
            casePayApply.setApproveCostresult(CasePayApply.ApproveCostresult.AUDIT_REJECT.getValue());//审批拒绝
            casePayApply.setApproveStatus(CasePayApply.ApproveStatus.DERATE_AUDIT_REJECT.getValue());//减免审核驳回
            casePayApply.setApproveResult(CasePayApply.ApproveResult.DERATE_REJECT.getValue());//减免拒绝
            caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.COLLECTIONING.getValue());//案件状态：催收中
        }

        //处理审批要记录审批人的名称和审批人的id及操作人的ID和姓名
        casePayApply.setApproveDerateUser(user.getId());   //减免审批人
        casePayApply.setApproveDerateName(user.getRealName());     //减免审批人姓名
        casePayApply.setApproveDerateMemo(casePayApply.getApprovePayMemo());    //审批意见
        casePayApply.setOperatorUserName(user.getUserName());  //操作人用户名
        casePayApply.setOperatorRealName(user.getRealName());  //操作人姓名
        casePayApplyRepository.save(casePayApply);
        caseInfoRepository.save(caseInfo);

//        CaseInfo info = caseInfoRepository.findOne(casePayApply.getCaseId());
//        User userName = userRepository.findByUserName(casePayApply.getOperatorUserName());
//        ReminderMessage reminderMessage = new ReminderMessage();
//        reminderMessage.setType(ReminderType.DERATE);
//        reminderMessage.setState(ReminderMessage.ReadStatus.UnRead);
//        reminderMessage.setCreateTime(ZWDateUtil.getNowDateTime());
//        reminderMessage.setCupoCaseNum(casePayApply.getCaseNumber());
//        reminderMessage.setCupoId(casePayApply.getCaseId());
//        reminderMessage.setCustId(casePayApply.getPersonalId());
//        reminderMessage.setCupoRecType(info.getCollectionType());
//        reminderMessage.setUserId(userName.getId());
//        reminderMessage.setContent(casePayApply.getApproveDerateMemo());
//
//
//        try {
//            String custName = casePayApply.getPersonalName();
//            if (Objects.equals(casePayApply.getApproveCostresult(),CasePayApply.ApproveCostresult.AUDIT_AGREE.getValue())) {
//                reminderMessage.setTitle("客户[" + custName + "]的减免审批已通过。");
//                reminderClient.createReminderMessage(reminderMessage);
//            } else if (Objects.equals(casePayApply.getApproveCostresult(),CasePayApply.ApproveCostresult.AUDIT_REJECT.getValue())) {
//                reminderMessage.setTitle("客户[" + custName + "]的减免审批已驳回。");
//                reminderClient.createReminderMessage(reminderMessage);
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            log.error("工作台消息推送失败");
//        }

        return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("操作失败", ENTITY_NAME, e.getMessage())).body(null);
        }
    }


    /**
     * 操作审核信息们
     */
    @GetMapping("/handleAccPayApply")
    @ApiOperation(value = "操作审核信息", notes = "操作审核信息")
    public ResponseEntity handleAccPayApply(@RequestParam(value = "id") @ApiParam(value = "案件ID", required = true) String id){
        try {
        Map<String, Object> map = new HashMap<>();
        CasePayApply casePayApply = casePayApplyRepository.findOne(id);
        CaseInfo caseInfo = caseInfoRepository.findOne(casePayApply.getCaseId());
        String principalName = casePayApply.getPrincipalName();//客户姓名
        String idCard = caseInfo.getPersonalInfo().getIdCard();//客户身份证
        String personalPhone = casePayApply.getPersonalPhone();//客户手机号
        String batchNumber = caseInfo.getBatchNumber();//案件批次号
        Principal principalId = caseInfo.getPrincipalId();//委托方
        Integer payType = casePayApply.getPayType();//还款类型
        BigDecimal applyPayAmt = casePayApply.getApplyPayAmt();//减免金额
        BigDecimal applyDerateAmt = casePayApply.getApplyDerateAmt();//合同金额
        Integer payWay = casePayApply.getPayWay();//还款方式
        String payMemo = casePayApply.getPayMemo();//审核说明
        map.put("principalName", principalName);
        map.put("idCard", idCard);
        map.put("personalPhone", personalPhone);
        map.put("batchNumber", batchNumber);
        map.put("principalId", principalId);
        map.put("payType", payType);
        map.put("applyPayAmt", applyPayAmt);
        map.put("applyDerateAmt", applyDerateAmt);
        map.put("payWay", payWay);
        map.put("payMemo", payMemo);
        map.put(APPLY_DATE, ZWDateUtil.fomratterDate(casePayApply.getApplayDate(), BaseObject.DATE_FORMAT));//申请日期
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id)).body(map);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("操作审核失败", ENTITY_NAME, e.getMessage())).body(null);
        }
    }

    /**
     * 处理审核信息
     */
    @PostMapping("/updateAccPayApply")
    @ApiOperation(value = "处理审核信息", notes = "处理审核信息")
    public ResponseEntity updateAccPayApply(@RequestBody CasePayApplyParams casePayApplyParams, @RequestHeader(value = "X-UserToken") String token){
        try {
        User user = getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "用户登陆失败")).body(null);
            }
        CasePayApply casePayApply = casePayApplyRepository.findOne(casePayApplyParams.getCasePayId());
        CaseInfo caseInfo = caseInfoRepository.findOne(casePayApply.getCaseId());
        if (Objects.isNull(caseInfo)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "查无与此关联的案件")).body(null);
        }
        if (Objects.equals(caseInfo.getHandUpFlag(), CaseInfo.HandUpFlag.YES_HANG.getValue())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "挂起案件不允许操作")).body(null);
        }
        if (Objects.equals(casePayApply.getApproveStatus(), CasePayApply.ApproveStatus.AUDIT_AGREE.getValue())) { //审批状态为通过
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "审批已通过")).body(null);
        }
        if (Objects.equals(casePayApply.getApproveStatus(), CasePayApply.ApproveStatus.AUDIT_REJECT.getValue())) { //审批状态为拒绝
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "审批已拒绝")).body(null);
        }
        if (Objects.equals(casePayApply.getApproveStatus(), CasePayApply.ApproveStatus.REVOKE.getValue())) { //审批状态为撤回
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "审批已撤回")).body(null);
        }

        Iterable<CasePayApply> casePayApplies = casePayApplyRepository.findAll(QCasePayApply.casePayApply.id.eq(casePayApplyParams.getCasePayId()));
        if (casePayApplies.iterator().hasNext()) {
            //对象不为空
            CasePayApply casePayApply1 = casePayApplies.iterator().next();
            if (Objects.equals(CasePayApply.ApproveCostresult.AUDIT_REJECT.getValue(), casePayApply1.getApproveCostresult())) {   //减免结果为审批拒绝
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "审批已拒绝")).body(null);
            }
            if (Objects.equals(CasePayApply.ApproveCostresult.TO_AUDIT.getValue(), casePayApply1.getApproveCostresult())) {   //减免结果为待审批
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "存在到审批的案件，请先进行减免审批")).body(null);
            }
            if (Objects.equals(CasePayApply.ApproveCostresult.AUDIT_AGREE.getValue(), casePayApply1.getApproveCostresult())) {   //减免结果为审批同意
                caseInfoService.passCaseinfo(casePayApplyParams, user);
            }
        } else {
            //对象结果为空
            caseInfoService.passCaseinfo(casePayApplyParams, user);
        }
//        try {
//            CaseInfo info = caseInfoRepository.findOne(payApply.getCaseId());
//            User userName = userRepository.findByUserName(payApply.getOperatorUserName());
//            ReminderMessage reminderMessage = new ReminderMessage();
//            reminderMessage.setType(ReminderType.APPLY);
//            reminderMessage.setState(ReminderMessage.ReadStatus.UnRead);
//            reminderMessage.setCreateTime(ZWDateUtil.getNowDateTime());
//            reminderMessage.setCupoCaseNum(payApply.getCaseNumber());
//            reminderMessage.setCupoId(payApply.getCaseId());
//            reminderMessage.setCustId(payApply.getPersonalId());
//            reminderMessage.setCupoRecType(info.getCollectionType());
//            reminderMessage.setUserId(userName.getId());
//            reminderMessage.setContent(payApply.getApproveDerateMemo());
//
//            try {
//                if (Objects.equals(payApply.getApproveStatus(),CasePayApply.ApproveStatus.AUDIT_AGREE.getValue())) {
//                    reminderMessage.setTitle("客户[" + payApply.getPersonalName() + "]的还款审核已通过。");
//                    reminderClient.createReminderMessage(reminderMessage);
//                } else if (Objects.equals(payApply.getApproveStatus(),CasePayApply.ApproveStatus.AUDIT_REJECT.getValue())) {
//                    reminderMessage.setTitle("客户[" + payApply.getPersonalName() + "]的还款审核已驳回。");
//                    reminderClient.createReminderMessage(reminderMessage);
//                }
//            } catch (Exception e) {
//                log.error(e.getMessage(), e);
//                log.error("工作台消息推送失败");
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
        return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "操作失败")).body(null);
        }
    }




}
