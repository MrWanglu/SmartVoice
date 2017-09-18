package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.*;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.business.service.CaseInfoService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author : baizhangyu
 * @Description : 外访相关业务
 * @Date : 16:01 2017/7/19
 */

@RestController
@RequestMapping("/api/accVisitPoolController")
public class AccVisitPoolController extends BaseController {
    final Logger log = LoggerFactory.getLogger(AccVisitPoolController.class);
    private static final String ENTITY_NAME = "CaseInfo";

    private static final String ENTITY_PERSONAL = "Personal";

    private static final String ENTITY_CASEPAYAPPLY = "CasePayApply";

    private static final String ENTITY_CASEFOLLOWUPRECORD = "CaseFollowupRecord";

    private static final String ENTITY_CASEASSISTAPPLY = "CaseAssistApply";


    @Inject
    CaseInfoService caseInfoService;

    @Autowired
    CaseInfoRepository caseInfoRepository;

    @Autowired
    CaseAssistRepository caseAssistRepository;

    @Inject
    CaseFollowupRecordRepository caseFollowupRecordRepository;

    @Inject
    CaseFlowupFileRepository caseFlowupFileRepository;

    @Autowired
    RestTemplate restTemplate;

    @Inject
    CasePayFileRepository casePayFileRepository;

    @Inject
    CasePayApplyRepository casePayApplyRepository;

    @Inject
    CaseAssistApplyRepository caseAssistApplyRepository;

    @Inject
    SendMessageRecordRepository sendMessageRecordRepository;

    @Inject
    CaseAdvanceTurnApplayRepository caseAdvanceTurnApplayRepository;

    /**
     * @Description 外访主页面多条件查询外访案件
     */
    @GetMapping("/getAllVisitCase")
    @ApiOperation(value = "外访主页面多条件查询外访案件", notes = "外访主页面多条件查询外访案件")
    public ResponseEntity<Page<CaseInfo>> getAllVisitCase(@RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
                                                          @QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                          @ApiIgnore Pageable pageable,
                                                          @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get all Visit case");
        Sort.Order followupBackOrder = new Sort.Order(Sort.Direction.ASC, "followupBack", Sort.NullHandling.NULLS_LAST); //催收反馈默认排序
        Sort.Order followupTime1 = new Sort.Order(Sort.Direction.ASC, "followupTime", Sort.NullHandling.NULLS_LAST); //跟进时间正序
        Sort.Order followupTime2 = new Sort.Order(Sort.Direction.DESC, "followupTime", Sort.NullHandling.NULLS_LAST); //跟进时间倒序
        Sort.Order color = new Sort.Order(Sort.Direction.DESC, "caseMark", Sort.NullHandling.NULLS_LAST); //案件打标
        Sort.Order personalName = new Sort.Order(Sort.Direction.ASC, "personalInfo.name"); //客户姓名正序
        try {
            User tokenUser = getUserByToken(token);
            BooleanBuilder builder = new BooleanBuilder(predicate);
            if (Objects.isNull(tokenUser.getCompanyCode())) {
                if (Objects.nonNull(companyCode)) {
                    builder.and(QCaseInfo.caseInfo.companyCode.eq(companyCode));
                }
            } else {
                builder.and(QCaseInfo.caseInfo.companyCode.eq(tokenUser.getCompanyCode())); //限制公司code码
            }
            if (Objects.equals(tokenUser.getManager(), 1)) {
                builder.and(QCaseInfo.caseInfo.currentCollector.department.code.startsWith(tokenUser.getDepartment().getCode())); //权限控制
            } else {
                builder.and(QCaseInfo.caseInfo.currentCollector.id.eq(tokenUser.getId()));
            }
            builder.and(QCaseInfo.caseInfo.caseType.in(CaseInfo.CaseType.DISTRIBUTE.getValue(), CaseInfo.CaseType.OUTLEAVETURN.getValue())); //只查案件类型为案件分配的
            builder.and(QCaseInfo.caseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue())); //不查询已结案案件
            builder.and(QCaseInfo.caseInfo.collectionType.eq(CaseInfo.CollectionType.VISIT.getValue())); //只查询外访案件
            if (pageable.getSort().toString().contains("followupBack")) {
                pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), new Sort(followupBackOrder));
            }
            if (pageable.getSort().toString().contains("followupTime") && pageable.getSort().toString().contains("ASC")) {
                pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), new Sort(followupTime1));
            }
            if (pageable.getSort().toString().contains("followupTime") && pageable.getSort().toString().contains("DESC")) {
                pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), new Sort(followupTime2));
            }
            pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().and(new Sort(color)).and(new Sort(personalName)));
            Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/accVisitPoolController/getAllVisitCase");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", ENTITY_NAME, e.getMessage())).body(null);
        }
    }

    /**
     * @Description 多条件查询外访已处理记录
     */
    @GetMapping("/getAllHandleVisitCase")
    @ApiOperation(value = "多条件查询外访已处理记录", notes = "多条件查询外访已处理记录")
    public ResponseEntity<Page<CaseInfo>> getAllHandleVisitCase(@RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
                                                                @QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                                @ApiIgnore Pageable pageable,
                                                                @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get all handle Visit case");
        try {
            User tokenUser = getUserByToken(token);
            BooleanBuilder builder = new BooleanBuilder(predicate);
            if (Objects.isNull(tokenUser.getCompanyCode())) {
                if (Objects.nonNull(companyCode)) {
                    builder.and(QCaseInfo.caseInfo.companyCode.eq(companyCode));
                }
            } else {
                builder.and(QCaseInfo.caseInfo.companyCode.eq(tokenUser.getCompanyCode())); //限制公司code码
            }
            if (Objects.equals(tokenUser.getManager(), 1)) {
                builder.and(QCaseInfo.caseInfo.currentCollector.department.code.startsWith(tokenUser.getDepartment().getCode())); //权限控制
            } else {
                builder.and(QCaseInfo.caseInfo.currentCollector.id.eq(tokenUser.getId()));
            }
            builder.and(QCaseInfo.caseInfo.collectionStatus.eq(CaseInfo.CollectionStatus.CASE_OVER.getValue())); //只查询已结案案件
            builder.and(QCaseInfo.caseInfo.collectionType.eq(CaseInfo.CollectionType.VISIT.getValue())); //只查询外访案件
            Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/accVisitPoolController/getAllHandleVisitCase");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", ENTITY_NAME, e.getMessage())).body(null);
        }
    }

    //此接口用于设置案件是否挂起
    @GetMapping("/handUp")
    @ResponseBody
    @ApiOperation(value = "是否挂起", notes = "是否挂起")
    public ResponseEntity<CaseInfo> handUp(@RequestParam @ApiParam(value = "案件id", required = true) String id, @RequestParam @ApiParam(value = "是否挂起id:1挂起;2取消挂起", required = true) Integer cupoPause) {
        log.debug("REST request to handUp : {}", id);
        try {
            CaseInfo accRecevicePool = caseInfoRepository.findOne(id);
            if (1 == cupoPause) {//挂起请求
                accRecevicePool.setHandUpFlag(CaseInfo.HandUpFlag.YES_HANG.getValue());
            } else if (2 == cupoPause) {//取消挂起请求
                accRecevicePool.setHandUpFlag(CaseInfo.HandUpFlag.NO_HANG.getValue());
            } else {
                return ResponseEntity.badRequest().headers(HeaderUtil.createEntityCreationAlert("请求异常", ENTITY_NAME)).body(null);
            }
            CaseInfo caseInfo = caseInfoRepository.save(accRecevicePool);
            return new ResponseEntity<>(caseInfo, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createEntityCreationAlert("设置挂起失败", ENTITY_NAME)).body(null);
        }
    }

    /**
     * @Description 外访页面添加跟进记录
     */
    @PostMapping("/saveFollowupRecord")
    @ApiOperation(value = "外访页面添加跟进记录", notes = "外访页面添加跟进记录")
    public ResponseEntity<CaseFollowupRecord> saveFollowupRecord(@RequestBody CaseFollowupParams caseFollowupParams,
                                                                 @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to save {caseFollowupRecord}", caseFollowupParams);
        try {
            User tokenUser = getUserByToken(token);
            CaseFollowupRecord result = caseInfoService.saveFollowupRecord(caseFollowupParams, tokenUser);
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("添加失败", ENTITY_CASEFOLLOWUPRECORD, e.getMessage())).body(null);
        }
    }

    /**
     * @Description 外访页面多条件查询跟进记录
     */
    @GetMapping("/getFollowupRecord/{caseNumber}")
    @ApiOperation(value = "外访页面多条件查询跟进记录", notes = "外访页面多条件查询跟进记录")
    public ResponseEntity<Page<CaseFollowupRecord>> getFollowupRecord(@PathVariable @ApiParam(value = "案件编号", required = true) String caseNumber,
                                                                      @QuerydslPredicate(root = CaseFollowupRecord.class) Predicate predicate,
                                                                      @ApiIgnore Pageable pageable) {
        log.debug("REST request to get case followup records by {caseNumber}", caseNumber);
        try {
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(QCaseFollowupRecord.caseFollowupRecord.caseNumber.eq(caseNumber));
            Page<CaseFollowupRecord> page = caseFollowupRecordRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/accVisitPoolController/getFollowupRecord");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", ENTITY_CASEFOLLOWUPRECORD, e.getMessage())).body(null);
        }
    }


    @GetMapping("/getVisitFiles")
    @ApiOperation(value = "下载外访资料", notes = "下载外访资料")
    public ResponseEntity<List<UploadFile>> getVisitFiles(@ApiParam(value = "跟进ID", required = true) @RequestParam String follId) {
        //下载外访资料
        List<UploadFile> uploadFiles = new ArrayList<>();//文件对象集合
        try {
            QCaseFlowupFile qCaseFlowupFile = QCaseFlowupFile.caseFlowupFile;
            Iterable<CaseFlowupFile> caseFlowupFiles = caseFlowupFileRepository.findAll(qCaseFlowupFile.followupId.id.eq(follId));
            Iterator<CaseFlowupFile> it = caseFlowupFiles.iterator();
            while (it.hasNext()) {
                CaseFlowupFile caseFlowupFile = it.next();
                ResponseEntity<UploadFile> entity = restTemplate.getForEntity("http://file-service/api/uploadFile/" + caseFlowupFile.getFileid(), UploadFile.class);
                if (!entity.hasBody()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("UploadFile", "Load Fail", "下载失败")).body(null);
                } else {
                    UploadFile uploadFile = entity.getBody();//文件对象
                    uploadFiles.add(uploadFile);
                }
            }
            return new ResponseEntity<>(uploadFiles, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("UploadFile", "Load Fail", "下载失败")).body(null);
        }
    }

    @GetMapping("/getRepaymentVoucher")
    @ApiOperation(value = "查看还款凭证", notes = "查看还款凭证")
    public ResponseEntity<List<UploadFile>> getRepaymentVoucher(@ApiParam(value = "还款ID", required = true) @RequestParam String payId) {
        try {
            List<UploadFile> uploadFiles = caseInfoService.getRepaymentVoucher(payId);
            return new ResponseEntity<>(uploadFiles, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("UploadFile", "Load Fail", "下载失败")).body(null);
        }
    }

    @PostMapping("/visitCaseDistribution")
    @ApiOperation(value = "外访案件重新分配", notes = "外访案件重新分配")
    public ResponseEntity<Void> visitCaseDistribution(@RequestBody ReDistributionParams reDistributionParams,
                                                      @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to reDistribute");
        try {
            User tokenUser = getUserByToken(token);
            caseInfoService.reDistribution(reDistributionParams, tokenUser);
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("分配失败", ENTITY_NAME, e.getMessage())).body(null);
        }
    }

//    /**
//     * @Description 外访详情页面的客户信息
//     */
//    @GetMapping("/getVisitCustInfo")
//    @ApiOperation(value = "外访详情页面的客户信息", notes = "外访详情页面的客户信息")
//    public ResponseEntity<Personal> getVisitCustInfo(@RequestParam @ApiParam(value = "案件ID", required = true) String caseId) {
//        log.debug("REST request to get customer information ");
//        try {
//            Personal personal = caseInfoService.getCustInfo(caseId);
//            return ResponseEntity.ok().body(personal);
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", ENTITY_PERSONAL, e.getMessage())).body(null);
//        }
//    }

    /**
     * @Description 外访页面申请还款操作
     */
    @PostMapping("/doVisitPay")
    @ApiOperation(value = "外访页面还款操作", notes = "外访页面还款操作")
    public ResponseEntity<Void> doTelPay(@RequestBody PayApplyParams payApplyParams,
                                         @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to apply payment");
        try {
            User tokenUser = getUserByToken(token);
            caseInfoService.doPay(payApplyParams, tokenUser);
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("还款失败", ENTITY_CASEPAYAPPLY, e.getMessage())).body(null);
        }
    }

    /**
     * @Description 外访页面还款撤回
     */
    @GetMapping("/visitWithdraw")
    @ApiOperation(value = "外访页面还款撤回", notes = "外访页面还款撤回")
    public ResponseEntity<Void> telWithdraw(@RequestParam @ApiParam(value = "还款审批ID", required = true) String payApplyId,
                                            @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to withdraw by {payApplyId}", payApplyId);
        try {
            User tokenUser = getUserByToken(token);
            caseInfoService.payWithdraw(payApplyId, tokenUser);
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("撤回失败", ENTITY_CASEPAYAPPLY, e.getMessage())).body(null);
        }
    }

    /**
     * @Description 外访页面多条件查询还款记录
     */
    @GetMapping("/getPaymentRecord")
    @ApiOperation(value = "外访页面多条件查询还款记录", notes = "外访页面多条件查询还款记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CasePayApply>> getPaymentRecord(@RequestParam @ApiParam(value = "案件ID", required = true) String caseId,
                                                               @QuerydslPredicate(root = CasePayApply.class) Predicate predicate,
                                                               @ApiIgnore Pageable pageable) {
        log.debug("REST request to get payment records by {caseId}", caseId);
        try {
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(QCasePayApply.casePayApply.caseId.eq(caseId)); //只查当前案件的还款记录
            Page<CasePayApply> page = casePayApplyRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/accVisitPoolController/getPaymentRecord");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", ENTITY_CASEPAYAPPLY, e.getMessage())).body(null);
        }
    }

    /**
     * @Description 外访案件结案
     */
    @GetMapping("/endVisitCase")
    @ApiOperation(value = "外访案件结案", notes = "外访案件结案")
    public ResponseEntity<Void> endVisitCase(EndCaseParams endCaseParams,
                                             @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to end case by {endCaseParams}", endCaseParams);
        try {
            User tokenUser = getUserByToken(token);
            caseInfoService.endCase(endCaseParams, tokenUser);
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("结案失败", ENTITY_NAME, e.getMessage())).body(null);
        }
    }

    /**
     * @Description 协催申请
     */
    @PostMapping("assistApply")
    @ApiOperation(value = "协催申请", notes = "协催申请")
    public ResponseEntity<Void> assistApply(AssistApplyParams assistApplyParams,
                                            @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to save assist apply");
        try {
            User tokenUser = getUserByToken(token);
            caseInfoService.saveAssistApply(assistApplyParams, tokenUser);
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("申请失败", ENTITY_CASEASSISTAPPLY, e.getMessage())).body(null);
        }
    }

    /**
     * @Description 外访页面多条件查询协催记录
     */
    @GetMapping("/getAllAssistApplyRecord")
    @ApiOperation(value = "外访页面多条件查询协催记录", notes = "外访页面多条件查询协催记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseAssistApply>> getAllAssistApplyRecord(@QuerydslPredicate(root = CaseAssistApply.class) Predicate predicate,
                                                                         @ApiIgnore Pageable pageable,
                                                                         @RequestParam @ApiParam(value = "案件ID", required = true) String caseId) {
        log.debug("REST request to get all assist apply record by {caseId}", caseId);
        try {
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(QCaseAssistApply.caseAssistApply.caseId.eq(caseId)); //只查当前案件的协催申请记录
            Page<CaseAssistApply> page = caseAssistApplyRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/accVisitPoolController/getAllAssistApplyRecord");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", ENTITY_CASEASSISTAPPLY, e.getMessage())).body(null);
        }
    }

    /**
     * @Description 外访页面获取分配信息
     */
    @GetMapping("/getBatchInfo")
    @ApiOperation(value = "外访页面获取分配信息", notes = "外访页面获取分配信息")
    public ResponseEntity<BatchDistributeModel> getBatchInfo(@RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get batch info");
        try {
            User tokenUser = getUserByToken(token);
            BatchDistributeModel batchDistributeModel = caseInfoService.getBatchDistribution(tokenUser);
            return ResponseEntity.ok().body(batchDistributeModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("分配失败", ENTITY_NAME, e.getMessage())).body(null);
        }
    }

    /**
     * @Description 外访页面批量分配
     */
    @PostMapping("/batchVisitCase")
    @ApiOperation(value = "外访页面批量分配", notes = "外访页面批量分配")
    public ResponseEntity<Void> batchVisitCase(@RequestBody BatchDistributeModel batchDistributeModel,
                                               @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to batch case");
        try {
            User tokenUser = getUserByToken(token);
            caseInfoService.batchCase(batchDistributeModel, tokenUser);
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("分配失败", ENTITY_NAME, e.getMessage())).body(null);
        }
    }

    /**
     * @Description 外访案件颜色打标
     */
    @PutMapping("/visitCaseMarkColor")
    @ApiOperation(value = "外访案件颜色打标", notes = "外访案件颜色打标")
    public ResponseEntity<Void> visitCaseMarkColor(@RequestBody CaseMarkParams caseMarkParams,
                                                   @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to mark color");
        try {
            User tokenUser = getUserByToken(token);
            caseInfoService.caseMarkColor(caseMarkParams, tokenUser);
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("打标失败", ENTITY_NAME, e.getMessage())).body(null);
        }
    }

    /**
     * @Description 外访页面多条件查询发送信息记录
     */
    @GetMapping("/getAllSendMessageRecord")
    @ApiOperation(value = "外访页面多条件查询发送信息记录", notes = "外访页面多条件查询发送信息记录")
    public ResponseEntity<Page<SendMessageRecord>> getAllSendMessageRecord(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                                           @ApiIgnore Pageable pageable,
                                                                           @RequestParam @ApiParam(value = "案件ID", required = true) String caseId) {
        log.debug("REST request to get all send message record by {caseId}", caseId);
        try {
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(QSendMessageRecord.sendMessageRecord.caseId.eq(caseId)); //只查当前案件的信息发送记录
            Page<SendMessageRecord> page = sendMessageRecordRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/AccVisitPoolController/getAllSendMessageRecord");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", "SendMessageRecord", e.getMessage())).body(null);
        }
    }

    /**
     * @Description 外访案件留案操作
     */
    @PostMapping("/leaveVisitCase")
    @ApiOperation(value = "外访案件留案操作", notes = "外访案件留案操作")
    public ResponseEntity<LeaveCaseModel> leaveTelCase(@RequestBody LeaveCaseParams leaveCaseParams,
                                                       @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to leave case");
        try {
            User tokenUser = getUserByToken(token);
            LeaveCaseModel leaveCaseModel = caseInfoService.leaveCase(leaveCaseParams, tokenUser);
            return ResponseEntity.ok().body(leaveCaseModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", e.getMessage())).body(null);
        }
    }

    /**
     * @Description 外访申请提前流转
     */
    @PostMapping("/visitAdvanceCirculation")
    @ApiOperation(value = "外访申请提前流转", notes = "外访申请提前流转")
    public ResponseEntity<Void> visitAdvanceCirculation(@RequestBody AdvanceCirculationParams advanceCirculationParams,
                                                        @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to advance circulation");
        try {
            User tokenUser = getUserByToken(token);
            caseInfoService.advanceCirculation(advanceCirculationParams, tokenUser);
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", e.getMessage())).body(null);
        }
    }

    /**
     * @Description 多条件查询外访小流转待审批案件
     */
    @GetMapping("/getVisitPendingCase")
    @ApiOperation(value = "多条件查询外访小流转待审批案件", notes = "多条件查询外访小流转待审批案件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseAdvanceTurnApplay>> getCaseAdvanceTurnApplay(@RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
                                                                                @RequestParam(required = false) @ApiParam(value = "客户名称") String custName,
                                                                                @RequestParam(required = false) @ApiParam(value = "审核状态") Integer approveState,
                                                                                @ApiIgnore Pageable pageable,
                                                                                @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to getCaseAdvanceTurnApplay");
        try {
            User tokenUser = getUserByToken(token);
            QCaseAdvanceTurnApplay qCaseAdvanceTurnApplay = QCaseAdvanceTurnApplay.caseAdvanceTurnApplay;
            BooleanBuilder builder = new BooleanBuilder();
            if (Objects.isNull(tokenUser.getCompanyCode())) {
                if (Objects.nonNull(companyCode)) {
                    builder.and(qCaseAdvanceTurnApplay.companyCode.eq(companyCode));
                }
            } else {
                builder.and(qCaseAdvanceTurnApplay.companyCode.eq(tokenUser.getCompanyCode())); //限制公司code码
            }
            builder.and(qCaseAdvanceTurnApplay.collectionType.eq(1)); //外访
            if (Objects.nonNull(custName)) {
                builder.and(qCaseAdvanceTurnApplay.personalName.like(custName.concat("%")));
            }
            if (Objects.nonNull(approveState)) {
                builder.and(qCaseAdvanceTurnApplay.approveResult.eq(approveState));
            }
            builder.and(qCaseAdvanceTurnApplay.departId.startsWith(tokenUser.getDepartment().getCode())); //权限控制
            Page<CaseAdvanceTurnApplay> page = caseAdvanceTurnApplayRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/AccVisitPoolController/getVisitPendingCase");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseAdvanceTurnApplay", "CaseAdvanceTurnApplay", "查询失败")).body(null);
        }
    }

    /**
     * @Description 外访审批小流转案件
     */
    @PostMapping("/approvalVisitCirculation")
    @ApiOperation(value = "外访审批小流转案件", notes = "外访审批小流转案件")
    public ResponseEntity<Void> approvalVisitCirculation(@RequestBody CirculationApprovalParams circulationApprovalParams,
                                                         @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to approval visit circulation case");
        try {
            User tokenUser = getUserByToken(token);
            caseInfoService.approvalCirculation(circulationApprovalParams, tokenUser);
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", e.getMessage())).body(null);
        }
    }

    /**
     * @Description 外访页面修改联系人地址状态
     */
    @PutMapping("/modifyAddressStatus")
    @ApiOperation(value = "修改联系人地址状态", notes = "修改联系人地址状态")
    public ResponseEntity<PersonalAddress> modifyPhoneStatus(@RequestBody PhoneStatusParams phoneStatusParams,
                                                             @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to modify address status");
        try {
            User tokenUser = getUserByToken(token);
            PersonalAddress personalAddress = caseInfoService.modifyAddressStatus(phoneStatusParams, tokenUser);
            return ResponseEntity.ok().body(personalAddress);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("修改失败", "personalContact", e.getMessage())).body(null);
        }
    }

    /**
     * @Description 外访页面添加修复信息
     */
    @PostMapping("/saveRepairInfo")
    @ApiOperation(value = "外访页面添加修复信息", notes = "外访页面添加修复信息")
    public ResponseEntity<PersonalAddress> saveRepairInfo(@RequestBody RepairInfoModel repairInfoModel,
                                                          @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to save repair information");
        try {
            User tokenUser = getUserByToken(token);
            PersonalAddress personalAddress = caseInfoService.saveVisitRepairInfo(repairInfoModel, tokenUser);
            return ResponseEntity.ok().body(personalAddress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("添加失败", "personalAddress", e.getMessage())).body(null);
        }
    }

    /**
     * @Description 外访页面查询客户联系人
     */
    @GetMapping("/getVisitPersonalAddress")
    @ApiOperation(value = "外访页面查询客户联系人", notes = "外访页面查询客户联系人")
    public ResponseEntity<PersonalAddressModel> getTelPersonalContact(@RequestParam @ApiParam(value = "客户信息ID", required = true) String personalId) {
        log.debug("REST request to get personal contact by {personalId}", personalId);
        try {
            List<PersonalAddress> content = caseInfoService.getPersonalAddress(personalId);
            PersonalAddressModel personalAddressModel = new PersonalAddressModel();
            personalAddressModel.setContent(content);
            return ResponseEntity.ok().body(personalAddressModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "personalContact", e.getMessage())).body(null);
        }
    }
}