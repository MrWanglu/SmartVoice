package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.CasePayApplyParams;
import cn.fintecher.pangolin.business.model.CasePayApplys;
import cn.fintecher.pangolin.business.repository.CaseAssistRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.CasePayApplyRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.BaseObject;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.net.URISyntaxException;
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
    private CaseAssistRepository caseAssistRepository;


    /**
     * @Description 费用减免审批页面多条件查询减免记录
     */
    @GetMapping("/getCasePayApply")
    @ApiOperation(value = "费用减免审批多条件查询减免记录", notes = "费用减免审批多条件查询减免记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query", value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query", value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query", value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CasePayApply>> getCasePayApply(CasePayApplyParams casePayApplyParams,
                                                              @QuerydslPredicate(root = CasePayApply.class) Predicate predicate,
                                                              @ApiIgnore Pageable pageable) throws URISyntaxException {
        BooleanBuilder builder = new BooleanBuilder(predicate);
        if (Objects.nonNull(casePayApplyParams.getPersonalName())) {
            builder.and(QCasePayApply.casePayApply.personalName.eq(casePayApplyParams.getPersonalName()));
        }
        if (Objects.nonNull(casePayApplyParams.getPersonalPhone())) {
            builder.and(QCasePayApply.casePayApply.personalPhone.eq(casePayApplyParams.getPersonalPhone()));
        }
        if (Objects.nonNull(casePayApplyParams.getBatchNumber())) {
            builder.and(QCasePayApply.casePayApply.batchNumber.eq(casePayApplyParams.getBatchNumber()));
        }
        if (Objects.nonNull(casePayApplyParams.getApplyDerateAmt())) {
            builder.and(QCasePayApply.casePayApply.applyDerateAmt.between(casePayApplyParams.getPayaApplyMinAmt(), casePayApplyParams.getPayaApplyMaxAmt()));
        }
        if (Objects.nonNull(casePayApplyParams.getApproveType())) {
            builder.and(QCasePayApply.casePayApply.approveType.eq(casePayApplyParams.getApproveType()));
        }
        if (Objects.nonNull(casePayApplyParams.getApproveCostresult())) {
            builder.and(QCasePayApply.casePayApply.approveCostresult.eq(casePayApplyParams.getApproveCostresult()));
        }
        if (Objects.nonNull(casePayApplyParams.getPrincipalId())) {
            builder.and(QCasePayApply.casePayApply.principalId.eq(casePayApplyParams.getPrincipalId()));
        }

        Page<CasePayApply> page = casePayApplyRepository.findAll(builder, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/accDerateController/getCasePayApply");
        return new ResponseEntity<>(page, headers, HttpStatus.OK);
    }

    /**
     * @Description 还款审核页面多条件查询减免记录
     */
    @GetMapping("/getAccReimbursementApply")
    @ApiOperation(value = "还款审核多条件查询减免记录", notes = "还款审核多条件查询减免记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query", value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query", value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query", value = "依据什么排序: 属性名,asc/desc.")
    })
    public ResponseEntity<Page<CasePayApply>> getAccReimbursementApply(CasePayApplyParams casePayApplyParams,
                                                                       @QuerydslPredicate(root = CasePayApply.class) Predicate predicate,
                                                                       @ApiIgnore Pageable pageable) throws URISyntaxException {
        BooleanBuilder builder = new BooleanBuilder(predicate);
        if (Objects.nonNull(casePayApplyParams.getPersonalName())) {
            builder.and(QCasePayApply.casePayApply.personalName.eq(casePayApplyParams.getPersonalName()));
        }
        if (Objects.nonNull(casePayApplyParams.getPersonalPhone())) {
            builder.and(QCasePayApply.casePayApply.personalPhone.eq(casePayApplyParams.getPersonalPhone()));
        }
        if (Objects.nonNull(casePayApplyParams.getBatchNumber())) {
            builder.and(QCasePayApply.casePayApply.batchNumber.eq(casePayApplyParams.getBatchNumber()));
        }
        if (Objects.nonNull(casePayApplyParams.getPayType())) {
            builder.and(QCasePayApply.casePayApply.payType.eq(casePayApplyParams.getPayType()));
        }
        if (Objects.nonNull(casePayApplyParams.getPayWay())) {
            builder.and(QCasePayApply.casePayApply.payWay.eq(casePayApplyParams.getPayWay()));
        }
        if (Objects.nonNull(casePayApplyParams.getApproveStatus())) {
            builder.and(QCasePayApply.casePayApply.approveStatus.eq(casePayApplyParams.getApproveStatus()));
        }
        if (Objects.nonNull(casePayApplyParams.getApplayUserName())) {
            builder.and(QCasePayApply.casePayApply.applayUserName.eq(casePayApplyParams.getApplayUserName()));
        }
        if (Objects.nonNull(casePayApplyParams.getPrincipalId())) {
            builder.and(QCasePayApply.casePayApply.principalId.eq(casePayApplyParams.getPrincipalId()));
        }

        Page<CasePayApply> page = casePayApplyRepository.findAll(builder, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/accDerateController/getAccReimbursementApply");
        return new ResponseEntity<>(page, headers, HttpStatus.OK);
    }


//    @GetMapping("/exportPayApply")
//    @ApiOperation(value = "导出还款记录", notes = "导出还款记录")
//    public ResponseEntity exportPayApplyModel(ExportPayApply exportPayApply,
//                                              @QuerydslPredicate(root = CasePayApply.class) Predicate predicate,
//                                              @RequestHeader(value = "X-UserToken") String token) {
//        log.debug("entry the export the pay records");
//        User user;
//        HSSFWorkbook workbook = null;
//        File file = null;
//        ByteArrayOutputStream out = null;
//        FileOutputStream fileOutputStream = null;
//        try {
//        User userToken;
//        try {
//            userToken = getUserByToken(token);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
//        }
//        BooleanBuilder builder = new BooleanBuilder(predicate);
//
//        //eq未做
//
//        Iterable<CasePayApply> iterable = casePayApplyRepository.findAll(builder);
//        List<ExportPayApplyModel> list = new ArrayList<>();
//        for (CasePayApply casePayApply : iterable){
//            ExportPayApplyModel exportPayApplyModel = new ExportPayApplyModel();
//            exportPayApplyModel.setApproveStatus(Objects.toString(casePayApply.getApproveStatus()));//审批结果
//            exportPayApplyModel.setApplayDate(Objects.toString(casePayApply.getApplayDate()));//申请日期
//            exportPayApplyModel.setApplayUserName(casePayApply.getApplayUserName());//申请人
//            exportPayApplyModel.setApplyPayAmt(Objects.toString(casePayApply.getApplyPayAmt()));//还款金额
//            exportPayApplyModel.setBatchNumber(casePayApply.getBatchNumber());//批次号
//            exportPayApplyModel.setPayType(Objects.toString(casePayApply.getPayType()));//还款类型
//            exportPayApplyModel.setCaseNumber(casePayApply.getCaseNumber());//案件编号
//            exportPayApplyModel.setPayWay(Objects.toString(casePayApply.getPayWay()));//还款方式
//            exportPayApplyModel.setPersonalPhone(casePayApply.getPersonalPhone());//客户电话
//            exportPayApplyModel.setPrincipalId(casePayApply.getPrincipalId());//委托方
//            exportPayApplyModel.setPersonalName(casePayApply.getPersonalName());//客户姓名
//            exportPayApplyModel.setCaseAmt(Objects.toString(casePayApply.getCaseInfo().getOverdueAmount()));//案件金额
//            list.add(exportPayApplyModel);
//        }
//        if (Objects.isNull(list)){
//            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
//                    "No exported data", "无导出数据")).body(null);
//        }
//        String[] titleList = {"案件编号", "批次号", "委托方", "客户姓名", "手机号", "案件金额(元)", "还款金额(元)", "还款类型", "还款方式", "审核结果", "申请日期", "申请人"};
//        String[] proNames = {"caseNumber", "batchNumber", "principalId", "personalName", "personalPhone", "CaseAmt", "applyPayAmt", "payType", "payWay", "approveStatus", "applayDate", "applayUserName"};
//            workbook = new HSSFWorkbook();
//            HSSFSheet sheet = workbook.createSheet("sheet1");
//            ExcelUtil.createExcel(workbook, sheet, list, titleList, proNames, 0, 0);
//            out = new ByteArrayOutputStream();
//            workbook.write(out);
//            String filePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(DateTime.now().toString("yyyyMMddhhmmss") + "还款记录.xls");
//            file = new File(filePath);
//            fileOutputStream = new FileOutputStream(file);
//            fileOutputStream.write(out.toByteArray());
//            FileSystemResource resource = new FileSystemResource(file);
//            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
//            param.add("file", resource);
//            ResponseEntity<String> url = restTemplate.postForEntity("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class);
//            if (url == null) {
//                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
//                        "The upload server failed", "上传服务器失败")).body(null);
//            } else {
//                return ResponseEntity.ok().build();
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            log.error(ex.getMessage());
//            return ResponseEntity.badRequest().body(null);
//        } finally {
//            // 关闭流
//            if (workbook != null) {
//                try {
//                    workbook.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (out != null) {
//                try {
//                    out.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (fileOutputStream != null) {
//                try {
//                    fileOutputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            // 删除文件
//            if (file != null) {
//                file.delete();
//            }
//        }
//
//    }


    /**
     * 操作减免审批
     */
    @GetMapping("/handleCasePayApply")
    @ApiOperation(value = "操作审批信息", notes = "操作审批信息")
    public ResponseEntity handleCasePayApply(@RequestParam(value = "id") @ApiParam("减免审批id") String id) throws URISyntaxException {
        log.debug("REST request to handle AccDerateApply : {}", id);
        List<Map<String, Object>> mapList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        //获取客户姓名、身份证号、手机号、合同金额、已还期数、逾期天数加入到mapList
        CasePayApply casePayApply = casePayApplyRepository.findOne(id);

        //判断是否协催前端判断

        if (Objects.isNull(casePayApply)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "The exemption information does not exist", "减免信息不存在")).body(null);
        }
        CaseInfo caseInfo = caseInfoRepository.findOne(casePayApply.getCaseId());
        if (Objects.isNull(caseInfo)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "The case information does not exist", "案件信息不存在")).body(null);
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
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id)).build();
    }

    /**
     * 处理审批信息
     */
    @PostMapping("/updateAccDerateApply")
    @ApiOperation(value = "处理审批信息", notes = "处理审批信息")
    public ResponseEntity updateAccDerateApply(@RequestBody @ApiParam("减免审批对象") CasePayApply casePayApply,
                                               @RequestHeader(value = "X-UserToken") @ApiParam("操作者的token") String token) throws URISyntaxException {
        User userToken;
        try {
            userToken = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        CasePayApply payApply = casePayApplyRepository.findOne(casePayApply.getId());
        if (Objects.isNull(payApply)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "The exemption information does not exist", "减免信息不存在")).body(null);
        }
        CaseInfo caseInfo = caseInfoRepository.findOne(casePayApply.getCaseId());
        if (Objects.isNull(caseInfo)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "The case information does not exist", "案件信息不存在")).body(null);
        }
        if (!Objects.equals(casePayApply.getApproveCostresult(), CasePayApply.ApproveCostresult.TO_AUDIT.getValue())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "The approval has been processed", "减免审批已经处理")).body(null);
        }
        if (Objects.equals(casePayApply.getApproveCostresult(), CasePayApply.ApproveCostresult.REVOCATION.getValue())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "The approval has been withdrawn", "减免审批已经撤回")).body(null);
        }
        if (Objects.equals(caseInfo.getHandUpFlag(), CaseInfo.HandUpFlag.YES_HANG.getValue())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "Pending cases are not allowed to operate", "挂起案件不允许操作")).body(null);
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
        casePayApply.setApproveDerateUser(userToken.getId());   //减免审批人
        casePayApply.setApproveDerateName(userToken.getRealName());     //减免审批人姓名
        casePayApply.setApproveDerateMemo(casePayApply.getApprovePayMemo());    //审批意见
        casePayApply.setOperatorUserName(userToken.getUserName());  //操作人用户名
        casePayApply.setOperatorRealName(userToken.getRealName());  //操作人姓名
        casePayApplyRepository.save(casePayApply);
        caseInfoRepository.save(caseInfo);
        return ResponseEntity.ok().build();
    }


    /**
     * 操作审核信息们
     */
    @GetMapping("/handleAccPayApply")
    @ApiOperation(value = "操作审核信息", notes = "操作审核信息")
    public ResponseEntity handleAccPayApply(@RequestParam(value = "id") @ApiParam(value = "案件ID", required = true) String id) throws URISyntaxException {
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
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id)).build();
    }

    /**
     * 处理审核信息
     */
    @PostMapping("/updateAccPayApply")
    @ApiOperation(value = "处理审核信息", notes = "处理审核信息")
    public ResponseEntity updateAccPayApply(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                            @RequestBody CasePayApplys casePayApplys, @RequestHeader(value = "X-UserToken") String token) throws URISyntaxException {
        User userToken;
        try {
            userToken = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        BooleanBuilder builder = new BooleanBuilder(predicate);
        builder.and(QCaseInfo.caseInfo.caseNumber.eq(casePayApplys.getCaseNumber()));

        //Code码
        //builder.and(QCaseInfo.caseInfo.companyCode.eq(userToken.getCompanyCode()));

        builder.and(QCaseInfo.caseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue())); //过滤已结案案件
        Iterable<CaseInfo> caseInfos = caseInfoRepository.findAll(builder);     //查找所有过滤已结案的案件
        if (caseInfos.iterator().hasNext()) {
            if (Objects.equals(caseInfos.iterator().next().getHandUpFlag(), CaseInfo.HandUpFlag.YES_HANG.getValue())) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                        "Pending cases are not allowed to operate", "挂起案件不允许操作")).body(null);
            }
        } else {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "The approval has been processed", "查无与此关联的案件")).body(null);
        }

        CasePayApply payApply = casePayApplyRepository.findOne(casePayApplys.getId());//根据审批ID获得还款审批对象
        if (Objects.equals(payApply.getApproveStatus(), CasePayApply.ApproveStatus.AUDIT_AGREE.getValue())) { //审批状态为通过
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "approved", "审批已通过")).body(null);
        }
        if (Objects.equals(payApply.getApproveStatus(), CasePayApply.ApproveStatus.AUDIT_REJECT.getValue())) { //审批状态为拒绝
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "Approval has been rejected", "审批已拒绝")).body(null);
        }
        if (Objects.equals(payApply.getApproveStatus(), CasePayApply.ApproveStatus.REVOKE.getValue())) { //审批状态为撤回
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "Approval has been withdrawn", "审批已撤回")).body(null);
        }

        Iterable<CasePayApply> casePayApplies = casePayApplyRepository.findAll(QCasePayApply.casePayApply.id.eq(casePayApplys.getId()));    //查找所有
        //上面查询所有案件的审批状态为审批标识


        if (casePayApplies.iterator().hasNext()) {
            //对象不为空
            CasePayApply casePayApply = casePayApplies.iterator().next();
            if (Objects.equals(CasePayApply.ApproveCostresult.AUDIT_REJECT.getValue(), casePayApply.getApproveCostresult())) {   //减免结果为审批拒绝
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                        "Approval has been rejected", "审批已拒绝")).body(null);
            }
            if (Objects.equals(CasePayApply.ApproveCostresult.TO_AUDIT.getValue(), casePayApply.getApproveCostresult())) {   //减免结果为待审批
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                        "In the case of approval, please approve the approval first", "存在到审批的案件，请先进行减免审批")).body(null);
            }
            if (Objects.equals(CasePayApply.ApproveCostresult.AUDIT_AGREE.getValue(), casePayApply.getApproveCostresult())) {   //减免结果为审批同意
                passCaseinfo(casePayApplys, userToken);
            }
        } else {
            //对象结果为空
            passCaseinfo(casePayApplys, userToken);
        }
        return ResponseEntity.ok().build();
    }

    private void passCaseinfo(CasePayApplys casePayApplys, User userToken) {
        Iterable<CaseInfo> caseinfoallBycaseover = caseInfoRepository.findAll(QCaseInfo.caseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue()).and(QCaseInfo.caseInfo.id.eq("11")));
        //上面的id是自己加便于测试
        if (Objects.isNull(caseinfoallBycaseover.iterator().next().getEarlyDerateAmt())) {
            caseinfoallBycaseover.iterator().next().setEarlyDerateAmt(BigDecimal.ZERO);
        }
        if (Objects.isNull(caseinfoallBycaseover.iterator().next().getEarlyDerateAmt())) {
            caseinfoallBycaseover.iterator().next().setEarlyDerateAmt(BigDecimal.ZERO);
        }
        //逾期减免金额未写

        CasePayApply casePayApply = casePayApplyRepository.findOne(casePayApplys.getId());//查找减免审批的案件
        CaseInfo caseInfo = caseInfoRepository.findOne(casePayApplys.getCaseId());//查找案件
        CaseAssist caseAssist = caseAssistRepository.findOne(casePayApplys.getCaseId());//查找协催案件

        if (Objects.equals(casePayApplys.getApproveResult(), CasePayApply.ApproveResult.REJECT.getValue())) {     //如果审核意见为驳回
            casePayApply.setApproveStatus(CasePayApply.ApproveStatus.AUDIT_REJECT.getValue());      //审核状态改为：审核拒绝
            casePayApply.setApproveResult(CasePayApply.ApproveResult.REJECT.getValue());        //审核结果该我：驳回
            casePayApply.setApprovePayDatetime(ZWDateUtil.getNowDate());   //审批时间为当前时间

            if (caseInfo.getCollectionStatus().equals(CaseInfo.CollectionType.VISIT.getValue()) && caseInfo.getAssistFlag().equals(CaseInfo.AssistFlag.YES_ASSIST.getValue())) {    //是协催案件
                caseAssist.setAssistStatus(CaseInfo.CollectionStatus.COLLECTIONING.getValue());     //协催案件状态变为：催收中
                caseAssist.setOperatorTime(ZWDateUtil.getNowDate());    //处理日期为当前日期
                caseAssistRepository.save(caseAssist);
                //同步更新原来案件
                caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.COLLECTIONING.getValue());       //原案件状态变为：催收中
                caseInfo.setOperatorTime(ZWDateUtil.getNowDate());      //处理日期为当前日期
                caseInfoRepository.save(caseInfo);
            } else {    //不是协催案件
                //同步更新原来案件
                caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.COLLECTIONING.getValue());       //原案件状态变为：催收中
                caseInfo.setOperatorTime(ZWDateUtil.getNowDate());      //处理日期为当前日期
                caseInfoRepository.save(caseInfo);

                //查询是否有协催
                if (Objects.equals(caseInfo.getAssistFlag(), 1)) { //有协催标识
                    caseAssist.setAssistStatus(CaseInfo.CollectionStatus.COLLECTIONING.getValue());     //协催案件状态变为：催收中
                    caseAssist.setOperatorTime(ZWDateUtil.getNowDate());    //处理日期为当前日期
                    caseAssistRepository.save(caseAssist);
                }
            }
        } else {     //审核结果为入账
            BigDecimal applyDerateAmt = casePayApply.getApplyDerateAmt();//减免金额
            if (Objects.isNull(applyDerateAmt)) {
                applyDerateAmt = new BigDecimal(0);
            }
            //逾期还款
            if (Objects.equals(casePayApplys.getPayType(), CasePayApply.PayType.PARTOVERDUE.getValue())) {     //部分逾期还款
                if (caseInfo.getCollectionStatus().equals(CaseInfo.CollectionType.VISIT.getValue()) && caseInfo.getAssistFlag().equals(CaseInfo.AssistFlag.YES_ASSIST.getValue())) {    //是协催案件
                    caseInfo.setRealPayAmount(casePayApply.getApplyPayAmt().add(caseInfo.getRealPayAmount()));  //实际还款金额
                    caseInfo.setOperatorTime(ZWDateUtil.getNowDate());//处理日期为当前日期

                    int result = caseInfo.getRealPayAmount().compareTo(caseInfo.getOverdueAmount());    //比较实际还款金额与逾期总金额
                    if (result == -1) {
                        caseAssist.setAssistStatus(CaseInfo.CollectionStatus.COLLECTIONING.getValue());     //协催案件状态变为：催收中
                        caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.COLLECTIONING.getValue());       //原案件状态变为：催收中
                    } else {
                        caseAssist.setAssistStatus(CaseInfo.CollectionStatus.OVER_PAYING.getValue());     //协催案件状态变为：逾期还款中
                        caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.OVER_PAYING.getValue());       //原案件状态变为：逾期还款中
                    }
                    caseInfoRepository.save(caseInfo);
                    caseAssistRepository.save(caseAssist);
                } else {    //不是协催案件
                    caseInfo.setRealPayAmount(casePayApply.getApplyPayAmt().add(caseInfo.getRealPayAmount()));  //实际还款金额
                    caseInfo.setOperatorTime(ZWDateUtil.getNowDate());//处理日期为当前日期
                    int result = caseInfo.getRealPayAmount().compareTo(caseInfo.getOverdueAmount());    //比较实际还款金额与预期总金额
                    if (result == -1) {
                        caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.COLLECTIONING.getValue());       //原案件状态变为：催收中
                    } else {
                        caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.OVER_PAYING.getValue());       //原案件状态变为：逾期还款中
                    }
                    caseInfoRepository.save(caseInfo);

                }
            } else if (Objects.equals(casePayApplys.getPayType(), CasePayApply.PayType.ALLOVERDUE.getValue())  //全额逾期还款
                    || Objects.equals(casePayApplys.getPayType(), CasePayApply.PayType.DERATEOVERDUE)) {   //减免逾期还款
                if (caseInfo.getCollectionStatus().equals(CaseInfo.CollectionType.VISIT.getValue()) && caseInfo.getAssistFlag().equals(CaseInfo.AssistFlag.YES_ASSIST.getValue())) {    //是协催案件
                    caseInfo.setRealPayAmount(casePayApply.getApplyPayAmt().add(caseInfo.getRealPayAmount()));  //实际还款金额
                    caseInfo.setOperatorTime(ZWDateUtil.getNowDate());//处理日期为当前日期
                    caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.OVER_PAYING.getValue());       //原案件状态变为：逾期还款中
                    caseInfo.setDerateAmt(applyDerateAmt);  //减免金额
                    caseInfoRepository.save(caseInfo);

                }
                //提前结清
            } else if (Objects.equals(casePayApplys.getPayType(), CasePayApply.PayType.PARTADVANCE)) {      //部分提前结清
                if (caseInfo.getCollectionStatus().equals(CaseInfo.CollectionType.VISIT.getValue()) && caseInfo.getAssistFlag().equals(CaseInfo.AssistFlag.YES_ASSIST.getValue())) {    //是协催案件
                    caseInfo.setRealPayAmount(casePayApply.getApplyPayAmt().add(caseInfo.getRealPayAmount()));  //实际还款金额
                    caseInfo.setOperatorTime(ZWDateUtil.getNowDate());//处理日期为当前日期
                    int result = caseInfo.getRealPayAmount().compareTo(caseInfo.getOverdueAmount());
                    if (result == -1) {
                        caseAssist.setAssistStatus(CaseInfo.CollectionStatus.COLLECTIONING.getValue());     //协催案件状态变为：催收中
                        caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.COLLECTIONING.getValue());       //原案件状态变为：催收中
                    } else {
                        caseAssist.setAssistStatus(CaseInfo.CollectionStatus.EARLY_PAYING.getValue());     //协催案件状态变为：提前结清还款中
                        caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.EARLY_PAYING.getValue());       //原案件状态变为：提前结清还款中
                    }
                    caseInfoRepository.save(caseInfo);
                    caseAssistRepository.save(caseAssist);
                } else {      //不是协催案件
                    caseInfo.setRealPayAmount(casePayApply.getApplyPayAmt().add(caseInfo.getRealPayAmount()));  //实际还款金额
                    caseInfo.setOperatorTime(ZWDateUtil.getNowDate());//处理日期为当前日期
                    int result = caseInfo.getRealPayAmount().compareTo(caseInfo.getOverdueAmount());    //比较实际还款金额与预期总金额
                    if (result == -1) {
                        caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.COLLECTIONING.getValue());       //原案件状态变为：催收中
                    } else {
                        caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.EARLY_PAYING.getValue());       //原案件状态变为：提前结清还款中
                    }
                    caseInfoRepository.save(caseInfo);
                }
            } else if (Objects.equals(casePayApplys.getPayType(), CasePayApply.PayType.ALLADVANCE.getValue())  //全额提前结清
                    || Objects.equals(casePayApplys.getPayType(), CasePayApply.PayType.DERATEADVANCE)) {    //减免提前结清
                if (caseInfo.getCollectionStatus().equals(CaseInfo.CollectionType.VISIT.getValue()) && caseInfo.getAssistFlag().equals(CaseInfo.AssistFlag.YES_ASSIST.getValue())) {    //是协催案件
                    caseInfo.setRealPayAmount(casePayApply.getApplyPayAmt().add(caseInfo.getRealPayAmount()));  //实际还款金额
                    caseInfo.setOperatorTime(ZWDateUtil.getNowDate());//处理日期为当前日期
                    caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.EARLY_PAYING.getValue());       //原案件状态变为：提前结清还款中
                    caseInfo.setDerateAmt(applyDerateAmt);  //减免金额
                    caseInfoRepository.save(caseInfo);
                }
            }
            casePayApply.setApproveStatus(CasePayApply.ApproveStatus.AUDIT_AGREE.getValue());   //审核通过
            casePayApply.setApproveResult(CasePayApply.ApproveResult.AGREE.getValue());     //入账
            casePayApply.setApprovePayDatetime(ZWDateUtil.getNowDate());    //还款审批时间
        }
        casePayApply.setApprovePayName(userToken.getUserName());    //审批人用户名
        casePayApply.setApprovePayMemo(casePayApplys.getApprovePayMemo());  //审核意见
        casePayApplyRepository.save(casePayApply);
    }


}
