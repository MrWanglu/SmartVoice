package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.CaseInfoVerModel;
import cn.fintecher.pangolin.business.model.CaseInfoVerficationModel;
import cn.fintecher.pangolin.business.model.CaseInfoVerificationParams;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.business.service.CaseInfoVerificationService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author yuanyanting
 * @version Id:CaseInfoVerificationController.java,v 0.1 2017/9/1 11:47 yuanyanting Exp $$
 */
@RestController
@RequestMapping("/api/caseInfoVerificationController")
@Api(value = "CaseInfoVerificationController", description = "核销案件操作")
public class CaseInfoVerificationController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(CaseAssistApplyController.class);

    @Inject
    private CaseInfoVerificationRepository caseInfoVerificationRepository;

    @Inject
    private CaseInfoVerificationService caseInfoVerificationService;

    @Inject
    private CaseInfoRepository caseInfoRepository;

    @Inject
    private CaseAssistRepository caseAssistRepository;

    @Inject
    private CaseInfoVerificationPackagingRepository caseInfoVerificationPackagingRepository;

    @Inject
    private CaseInfoVerificationApplyRepository caseInfoVerificationApplyRepository;

    @Inject
    private CaseFollowupRecordRepository caseFollowupRecordRepository;

    @PostMapping("/saveCaseInfoVerification")
    @ApiOperation(value = "案件申请审批", notes = "案件申请审批")
    public ResponseEntity saveCaseInfoVerification(@RequestBody CaseInfoVerficationModel caseInfoVerficationModel,
                                                   @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            List<CaseInfo> caseInfoList = caseInfoRepository.findAll(caseInfoVerficationModel.getIds());
            List<CaseAssist> caseAssistList = new ArrayList<>();
            for (int i = 0; i < caseInfoList.size(); i++) {
                if (caseInfoList.get(i).getCollectionStatus().equals(CaseInfo.CollectionStatus.CASE_OVER.getValue())) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "结案案件不能核销!")).body(null);
                }
                if (caseInfoList.get(i).getCollectionStatus().equals(CaseInfo.CollectionStatus.CASE_OUT.getValue())) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "已委外案件不能核销!")).body(null);
                }
            }
            CaseInfoVerificationApply caseInfoVerificationApply = new CaseInfoVerificationApply();
            for (CaseInfo caseInfo : caseInfoList) {
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
                }
                caseInfoVerificationApply.setOperator(user.getRealName()); // 操作人
                caseInfoVerificationApply.setOperatorTime(ZWDateUtil.getNowDateTime()); // 操作时间
                caseInfoVerificationApply.setApplicant(user.getRealName()); // 申请人
                caseInfoVerificationApply.setApplicationDate(ZWDateUtil.getNowDateTime()); // 申请日期
                caseInfoVerificationApply.setApplicationReason(caseInfoVerficationModel.getApplicationReason()); // 申请理由
                caseInfoVerificationApply.setApprovalStatus(CaseInfoVerificationApply.ApprovalStatus.approval_pending.getValue()); // 申请状态：审批待通过
                if (Objects.nonNull(caseInfo)) {
                    caseInfoVerificationApply.setCaseId(caseInfo.getId()); // 案件Id
                    caseInfoVerificationApply.setCaseNumber(caseInfo.getCaseNumber()); // 案件编号
                    caseInfoVerificationApply.setBatchNumber(caseInfo.getBatchNumber()); // 批次号
                    caseInfoVerificationApply.setOverdueAmount(caseInfo.getOverdueAmount()); // 逾期金额
                    caseInfoVerificationApply.setOverdueDays(caseInfo.getOverdueDays()); // 逾期天数
                    caseInfoVerificationApply.setPayStatus(caseInfo.getPayStatus()); // 还款状态
                    caseInfoVerificationApply.setContractNumber(caseInfo.getContractNumber()); // 合同编号
                    caseInfoVerificationApply.setContractAmount(caseInfo.getContractAmount()); // 合同金额
                    caseInfoVerificationApply.setOverdueCapital(caseInfo.getOverdueCapital()); // 逾期本金
                    caseInfoVerificationApply.setOverdueDelayFine(caseInfo.getOverdueDelayFine()); // 逾期滞纳金
                    caseInfoVerificationApply.setOverdueFine(caseInfo.getOverdueFine()); // 逾期罚息
                    caseInfoVerificationApply.setOverdueInterest(caseInfo.getOverdueInterest()); // 逾期利息
                    caseInfoVerificationApply.setHasPayAmount(caseInfo.getHasPayAmount()); // 已还款金额
                    caseInfoVerificationApply.setHasPayPeriods(caseInfo.getHasPayPeriods()); // 已还款期数
                    caseInfoVerificationApply.setLatelyPayAmount(caseInfo.getLatelyPayAmount()); // 最近还款金额
                    caseInfoVerificationApply.setLatelyPayDate(caseInfo.getLatelyPayDate()); // 最近还款日期
                    caseInfoVerificationApply.setPeriods(caseInfo.getPeriods()); // 还款期数
                    caseInfoVerificationApply.setCommissionRate(caseInfo.getCommissionRate()); // 佣金比例
                    if (Objects.nonNull(caseInfo.getArea())) {
                        caseInfoVerificationApply.setCity(caseInfo.getArea().getAreaName()); // 城市
                        if (Objects.nonNull(caseInfo.getArea().getParent())) {
                            caseInfoVerificationApply.setProvince(caseInfo.getArea().getParent().getAreaName()); // 省份
                        }
                    }
                    if (Objects.nonNull(caseInfo.getPrincipalId())) {
                        caseInfoVerificationApply.setPrincipalName(caseInfo.getPrincipalId().getName()); // 委托方名称
                    }
                    if (Objects.nonNull(caseInfo.getPersonalInfo())) {
                        caseInfoVerificationApply.setPersonalName(caseInfo.getPersonalInfo().getName()); // 客户名称
                        caseInfoVerificationApply.setMobileNo(caseInfo.getPersonalInfo().getMobileNo()); // 电话号
                        caseInfoVerificationApply.setIdCard(caseInfo.getPersonalInfo().getIdCard()); // 身份证号
                    }
                }
                if (Objects.isNull(user.getCompanyCode())) { // 公司code码
                    if (Objects.nonNull(caseInfoVerficationModel.getCompanyCode())) {
                        caseInfoVerificationApply.setCompanyCode(caseInfoVerficationModel.getCompanyCode());
                    }
                }else {
                    caseInfoVerificationApply.setCompanyCode(user.getCompanyCode());
                }
                caseInfoVerificationApplyRepository.save(caseInfoVerificationApply);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", "CaseInfoVerificationModel")).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "操作失败!")).body(null);
        }
    }

    @RequestMapping(value = "/getCaseInfoVerificationApproval", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "审批待通过案件查询", notes = "审批待通过案件查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity getCaseInfoVerificationApply(@QuerydslPredicate(root = CaseInfoVerificationApply.class) Predicate predicate,
                                                       @ApiIgnore Pageable pageable,
                                                       @RequestHeader(value = "X-UserToken") String token,
                                                       @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        BooleanBuilder builder = new BooleanBuilder(predicate);
        if (Objects.isNull(user.getCompanyCode())) {
            if (StringUtils.isNotBlank(companyCode)) {
                builder.and(QCaseInfoVerificationApply.caseInfoVerificationApply.companyCode.eq(companyCode));
            }
        } else {
            builder.and(QCaseInfoVerificationApply.caseInfoVerificationApply.companyCode.eq(user.getCompanyCode()));
        }
        builder.and(QCaseInfoVerificationApply.caseInfoVerificationApply.approvalStatus.in(CaseInfoVerificationApply.ApprovalStatus.approval_pending.getValue(),CaseInfoVerificationApply.ApprovalStatus.approval_disapprove.getValue())); // 审批状态：待通过、审批拒绝
        Page<CaseInfoVerificationApply> page = caseInfoVerificationApplyRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", "caseInfoVerification")).body(page);
    }

    @GetMapping("/caseInfoVerification")
    @ApiOperation(value = "核销单个案件查询",notes = "核销单个案件查询")
    public ResponseEntity<CaseInfoVerificationApply> caseInfoVerification(String id) {
        try{
            CaseInfoVerificationApply caseInfoVerificationApply = caseInfoVerificationApplyRepository.findOne(id);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "caseInfoVerification")).body(caseInfoVerificationApply);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "查看失败")).body(null);
        }
    }

    @PostMapping("/caseInfoVerificationApply")
    @ApiOperation(value = "案件申请审批通过",notes = "案件申请审批通过")
    public ResponseEntity caseInfoVerificationApply(@RequestBody CaseInfoVerficationModel caseInfoVerficationModel,
                                                    @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try{
            user = getUserByToken(token);
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
            } else { // 核销审批通过
                caseInfoVerificationApply.setApprovalResult(CaseInfoVerificationApply.ApprovalResult.approve.getValue()); // 审批结果：通过
                caseInfoVerificationApply.setApprovalStatus(CaseInfoVerificationApply.ApprovalStatus.approval_approve.getValue()); // 审批状态：审批通过
            }
            caseInfoVerificationApplyRepository.save(caseInfoVerificationApply);
            caseInfoVerification.setOperator(user.getRealName()); // 操作人
            caseInfoVerification.setOperatorTime(ZWDateUtil.getNowDateTime()); // 操作时间
            caseInfoVerification.setState(caseInfoVerficationModel.getState()); // 核销说明
            CaseInfo caseInfo = caseInfoRepository.findOne(caseInfoVerificationApply.getCaseId());
            caseInfo.setEndType(CaseInfo.EndType.CLOSE_CASE.getValue()); // 结案类型：核销结案
            caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.CASE_OVER.getValue()); // 催收状态：已结案
            caseInfoRepository.save(caseInfo);
            caseInfoVerification.setCaseInfo(caseInfo);
            caseInfoVerificationRepository.save(caseInfoVerification);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "caseInfoVerification")).body(null);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "查看失败")).body(null);
        }
    }

    @RequestMapping(value = "/getCaseInfoFollow", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "跟进记录", notes = "跟进记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity getCaseInfoFollow(@QuerydslPredicate(root = CaseFollowupRecord.class) Predicate predicate,
                                            @ApiIgnore Pageable pageable,
                                            @RequestHeader(value = "X-UserToken") String token,
                                            @RequestParam("caseId") @ApiParam("案件编号") String caseId) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        CaseInfoVerificationApply caseInfoVerificationApply = caseInfoVerificationApplyRepository.findOne(QCaseInfoVerificationApply.caseInfoVerificationApply.caseId.eq(caseId));
        BooleanBuilder booleanBuilder = new BooleanBuilder(predicate);
        if (Objects.isNull(user.getCompanyCode())) { // 超级管理员
            if (StringUtils.isNotBlank(caseInfoVerificationApply.getCompanyCode())) {
                booleanBuilder.and(QCaseFollowupRecord.caseFollowupRecord.companyCode.eq(caseInfoVerificationApply.getCompanyCode()));
            }
        } else { // 普通管理员
            booleanBuilder.and(QCaseFollowupRecord.caseFollowupRecord.companyCode.eq(user.getCompanyCode()));
        }
        Page<CaseFollowupRecord> page = caseFollowupRecordRepository.findAll(booleanBuilder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", "caseInfoVerification")).body(page);
    }


    @RequestMapping(value = "/getCaseInfoVerification", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "核销审批通过案件查询", notes = "核销审批通过案件查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity getCaseInfoVerification(@QuerydslPredicate(root = CaseInfoVerification.class) Predicate predicate,
                                                  @ApiIgnore Pageable pageable,
                                                  @RequestHeader(value = "X-UserToken") String token,
                                                  @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        BooleanBuilder builder = new BooleanBuilder(predicate);
        if (Objects.isNull(user.getCompanyCode())) { // 超级管理员
            if (StringUtils.isNotBlank(companyCode)) {
                builder.and(QCaseInfoVerification.caseInfoVerification.companyCode.eq(companyCode));
            }
        } else { // 普通管理员
            builder.and(QCaseInfoVerification.caseInfoVerification.companyCode.eq(user.getCompanyCode()));
        }
        builder.and(QCaseInfoVerification.caseInfoVerification.caseInfo.endType.eq(CaseInfo.EndType.CLOSE_CASE.getValue())); // 结案方式：核销结案
        Page<CaseInfoVerification> page = caseInfoVerificationRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", "caseInfoVerification")).body(page);
    }

    @PostMapping("/exportVerification")
    @ApiOperation(value = "核销管理导出", notes = "核销管理导出")
    public ResponseEntity<String> exportVerification(@RequestHeader(value = "X-UserToken") String token,
                                                     @RequestBody CaseInfoVerficationModel caseInfoVerficationModel) {
        User user;
        try {
            user = getUserByToken(token);
            List<String> ids = caseInfoVerficationModel.getIds();
            if (ids.isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "请选择案件!")).body(null);
            }
            List<Object[]> caseInfoVerificationList = caseInfoVerificationService.getCastInfoList(caseInfoVerficationModel, user);
            if (caseInfoVerificationList.isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "", "要导出的核销案件数据为空!")).body(null);
            }
            if (caseInfoVerificationList.size() > 10000) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "", "不支持导出数据超过10000条!")).body(null);
            }
            int sum = 0;
            BigDecimal amount = new BigDecimal(sum);
            for (String id : ids) {
                CaseInfoVerification caseInfoVerification = caseInfoVerificationRepository.findOne(id);
                CaseInfo caseInfo = caseInfoVerification.getCaseInfo();
                BigDecimal overdueAmount = caseInfo.getOverdueAmount();
                amount = amount.add(overdueAmount);
            }
            String url = caseInfoVerificationService.exportCaseInfoVerification(caseInfoVerificationList);
            CaseInfoVerificationPackaging caseInfoVerificationPackaging = new CaseInfoVerificationPackaging();
            caseInfoVerificationPackaging.setPackagingTime(ZWDateUtil.getNowDateTime()); // 打包时间
            caseInfoVerificationPackaging.setPackagingState(caseInfoVerficationModel.getState()); // 打包说明
            caseInfoVerificationPackaging.setCount(ids.size()); // 案件数量
            caseInfoVerificationPackaging.setDownloadCount(1); // 下载次数
            caseInfoVerificationPackaging.setTotalAmount(amount); // 总金额
            caseInfoVerificationPackaging.setDownloadAddress(url); // 下载地址
            caseInfoVerificationPackaging.setOperator(user.getRealName()); // 操作人
            caseInfoVerificationPackaging.setOperatorTime(ZWDateUtil.getNowDateTime()); // 操作时间
            if (Objects.isNull(user.getCompanyCode())) { // 超级管理员
                if (Objects.nonNull(caseInfoVerficationModel.getCompanyCode())) {
                    caseInfoVerificationPackaging.setCompanyCode(caseInfoVerficationModel.getCompanyCode());
                }
            }else { // 普通管理员
                caseInfoVerificationPackaging.setCompanyCode(user.getCompanyCode());
            }
            caseInfoVerificationPackagingRepository.save(caseInfoVerificationPackaging);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("导出成功", "caseInfoVerification")).body(url);
        } catch (Exception e) {
            e.printStackTrace();
             return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "导出失败")).body(null);
        }
    }

    @RequestMapping(value = "/getCaseInfoVerificationPackaging", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "核销案件打包的查询", notes = "核销案件打包的查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity getCaseInfoVerificationPackaging(@QuerydslPredicate(root = CaseInfoVerificationPackaging.class) Predicate predicate,
                                                           @ApiIgnore Pageable pageable,
                                                           @RequestHeader(value = "X-UserToken") String token,
                                                           @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        BooleanBuilder builder = new BooleanBuilder(predicate);
        if (Objects.isNull(user.getCompanyCode())) { // 超级管理员
            if (StringUtils.isNotBlank(companyCode)) {
                builder.and(QCaseInfoVerificationPackaging.caseInfoVerificationPackaging.companyCode.eq(companyCode));
            }
        } else { // 普通管理员
            builder.and(QCaseInfoVerificationPackaging.caseInfoVerificationPackaging.companyCode.eq(user.getCompanyCode()));
        }
        Page<CaseInfoVerificationPackaging> page = caseInfoVerificationPackagingRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", "caseInfoVerification")).body(page);
    }

    @PostMapping("/batchDownload")
    @ApiOperation(value = "立刻下载",notes = "立刻下载")
    public ResponseEntity<List<String>> batchDownload(@RequestBody CaseInfoVerficationModel caseInfoVerficationModel) {
        try{
            List<String> ids = caseInfoVerficationModel.getIds();
            if (ids.isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "请至少选择一个案件！")).body(null);
            }
            List<String> urlList = new ArrayList<>();
            for (String id : ids) {
                CaseInfoVerificationPackaging caseInfoVerificationPackaging = caseInfoVerificationPackagingRepository.findOne(id);
                if (Objects.nonNull(caseInfoVerificationPackaging.getDownloadCount())) {
                    caseInfoVerificationPackaging.setDownloadCount(caseInfoVerificationPackaging.getDownloadCount() + 1); // 下载次数
                }
                caseInfoVerificationPackagingRepository.save(caseInfoVerificationPackaging);
                String url = caseInfoVerificationPackaging.getDownloadAddress(); // 下载地址
                urlList.add(url);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "caseInfoVerification")).body(urlList);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "查看失败")).body(null);
        }
    }

    @PostMapping("/download")
    @ApiOperation(value = "单个立刻下载",notes = "单个立即下载")
    public ResponseEntity<String> download(@RequestBody CaseInfoVerficationModel caseInfoVerficationModel) {
        try{
            CaseInfoVerificationPackaging caseInfoVerificationPackaging = new CaseInfoVerificationPackaging();
            if(Objects.nonNull(caseInfoVerficationModel.getId())) {
                caseInfoVerificationPackaging = caseInfoVerificationPackagingRepository.findOne(caseInfoVerficationModel.getId());
            }
            if (Objects.nonNull(caseInfoVerificationPackaging.getDownloadCount())) {
                caseInfoVerificationPackaging.setDownloadCount(caseInfoVerificationPackaging.getDownloadCount() + 1); // 下载次数
            }
            caseInfoVerificationPackagingRepository.save(caseInfoVerificationPackaging);
            String url = caseInfoVerificationPackaging.getDownloadAddress();
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "caseInfoVerification")).body(url);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "查看失败")).body(null);
        }
    }

    @GetMapping("/getVerificationReportBycondition")
    @ApiOperation(value = "多条件分页查询核销报表", notes = "多条件分页查询核销报表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query", value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query", value = "每页大小."),
            @ApiImplicitParam(name = "sort", dataType = "string", paramType = "query", value = "依据什么排序: 属性名(,asc|desc). ", allowMultiple = true)
    })
    public ResponseEntity<Page<CaseInfoVerModel>> getVerificationReportBycondition(@RequestHeader(value = "X-UserToken") String token,
                                                                                   @ApiIgnore Pageable pageable,
                                                                                   CaseInfoVerificationParams caseInfoVerificationParams) {
        User user;
        List<CaseInfoVerModel> caseInfoVerificationReport = null;
        try {
            user = getUserByToken(token);
            caseInfoVerificationReport = caseInfoVerificationService.getList(caseInfoVerificationParams, user);
            Integer totalCount = caseInfoVerificationRepository.getTotalCount();
            Page<CaseInfoVerModel> page = new PageImpl<>(caseInfoVerificationReport, pageable, totalCount);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/caseInfoVerificationController/getVerificationReportBycondition");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "查询失败")).body(null);
        }
    }

    @GetMapping("/exportReport")
    @ApiOperation(value = "导出核销报表", notes = "导出核销报表")
    public ResponseEntity<String> exportReport(CaseInfoVerificationParams caseInfoVerificationParams,
                                               @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.isNull(caseInfoVerificationParams.getCompanyCode())) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "请选择公司")).body(null);
                }
            }
            String url = caseInfoVerificationService.exportReport(caseInfoVerificationParams, user);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "caseInfoVerification")).body(url);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "导出失败")).body(null);
        }
    }
}

