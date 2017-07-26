package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.*;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.business.service.CaseInfoService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.util.ZWDateUtil;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.inject.Inject;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : xiaqun
 * @Description : 电催相关业务
 * @Date : 16:01 2017/7/17
 */

@RestController
@RequestMapping("/api/AccTelPoolController")
public class AccTelPoolController extends BaseController {
    final Logger log = LoggerFactory.getLogger(AccTelPoolController.class);

    private static final String ENTITY_CASEINFO = "CaseInfo";

    private static final String ENTITY_PERSONAL = "Personal";

    private static final String ENTITY_CASEPAYAPPLY = "CasePayApply";

    private static final String ENTITY_CASEFOLLOWUPRECORD = "CaseFollowupRecord";

    private static final String ENTITY_CASEASSISTAPPLY = "CaseAssistApply";

    private static final String ENTITY_PERSONALCONTACT = "PersonalContact";

    private static final String ENTITY_UPLOADFILE = "UploadFile";

    @Inject
    CaseInfoService caseInfoService;

    @Inject
    CaseInfoRepository caseInfoRepository;

    @Inject
    CasePayApplyRepository casePayApplyRepository;

    @Inject
    CaseFollowupRecordRepository caseFollowupRecordRepository;

    @Inject
    CaseAssistApplyRepository caseAssistApplyRepository;

    @Inject
    SendMessageRecordRepository sendMessageRecordRepository;

    @Inject
    PersonalContactRepository personalContactRepository;

    @Inject
    RabbitTemplate rabbitTemplate;

    /**
     * @Description 电催案件重新分配
     */
    @PostMapping("/telCaseDistribution")
    @ApiOperation(value = "电催案件重新分配", notes = "电催案件重新分配")
    public ResponseEntity<Void> telCaseDistribution(@RequestBody ReDistributionParams reDistributionParams,
                                                    @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to reDistribute");
        try {
            User tokenUser = getUserByToken(token);
            caseInfoService.reDistribution(reDistributionParams, tokenUser);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("分配成功", ENTITY_CASEINFO)).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASEINFO, "user", "重新分配失败")).body(null);
        }
    }

    /**
     * @Description 电催详情页面的客户信息
     */
    @GetMapping("/getTelCustInfo")
    @ApiOperation(value = "电催详情页面的客户信息", notes = "电催详情页面的客户信息")
    public ResponseEntity<PersonalInfoModel> getTelCustInfo(@RequestParam @ApiParam(value = "案件ID", required = true) String caseId) {
        log.debug("REST request to get customer information ");
        try {
            PersonalInfoModel personalInfoModel = caseInfoService.getCustInfo(caseId);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("获取客户信息成功", ENTITY_PERSONAL)).body(personalInfoModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_PERSONAL, "user", "获取客户信息失败")).body(null);
        }
    }

    /**
     * @Description 通过案件ID获得案件信息
     */
    @GetMapping("/getCaseInfoByCaseId")
    @ApiOperation(value = "通过案件ID获得案件信息", notes = "通过案件ID获得案件信息")
    public ResponseEntity<CaseInfo> getCaseInfoByCaseId(@RequestParam @ApiParam(value = "案件ID", required = true) String caseId) {
        log.debug("REST request to get caseInfo by {caseId}", caseId);
        try {
            CaseInfo caseInfo = caseInfoRepository.findOne(caseId);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("获取案件信息成功", ENTITY_CASEINFO)).body(caseInfo);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASEINFO, "caseInfo", "获取案件信息失败")).body(null);
        }
    }

    /**
     * @Description 电催页面申请还款操作
     */
    @PostMapping("/doTelPay")
    @ApiOperation(value = "电催页面还款操作", notes = "电催页面还款操作")
    public ResponseEntity<Void> doTelPay(@RequestBody PayApplyParams payApplyParams,
                                         @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to apply payment");
        try {
            User tokenUser = getUserByToken(token);
            caseInfoService.doPay(payApplyParams, tokenUser);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("还款操作成功", ENTITY_CASEPAYAPPLY)).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASEPAYAPPLY, "casePayApply", "申请还款失败")).body(null);
        }
    }

    /**
     * @Description 电催页面还款撤回
     */
    @GetMapping("/telWithdraw")
    @ApiOperation(value = "电催页面还款撤回", notes = "电催页面还款撤回")
    public ResponseEntity<Void> telWithdraw(@RequestParam @ApiParam(value = "还款审批ID", required = true) String payApplyId,
                                            @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to withdraw by {payApplyId}", payApplyId);
        try {
            User tokenUser = getUserByToken(token);
            caseInfoService.payWithdraw(payApplyId, tokenUser);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("还款撤回成功", ENTITY_CASEPAYAPPLY)).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASEPAYAPPLY, "casePayApply", "撤回失败")).body(null);
        }
    }

    /**
     * @Description 电催页面多条件查询还款记录
     */
    @GetMapping("/getPaymentRecord")
    @ApiOperation(value = "电催页面多条件查询还款记录", notes = "电催页面多条件查询还款记录")
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
                                                               @ApiIgnore Pageable pageable) throws URISyntaxException {
        log.debug("REST request to get payment records by {caseId}", caseId);
        try {
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(QCasePayApply.casePayApply.caseId.eq(caseId)); //只查当前案件的还款记录
            Page<CasePayApply> page = casePayApplyRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/AccTelPoolController/getPaymentRecord");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASEPAYAPPLY, "casePayApply", "查询失败")).body(null);
        }
    }

    /**
     * @Description 电催页面添加跟进记录
     */
    @PostMapping("/saveFollowupRecord")
    @ApiOperation(value = "电催页面添加跟进记录", notes = "电催页面添加跟进记录")
    public ResponseEntity<CaseFollowupRecord> saveFollowupRecord(@RequestBody CaseFollowupRecord caseFollowupRecord,
                                                                 @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to save {caseFollowupRecord}", caseFollowupRecord);
        try {
            User tokenUser = getUserByToken(token);
            CaseFollowupRecord result = caseInfoService.saveFollowupRecord(caseFollowupRecord, tokenUser);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("跟进记录添加成功", ENTITY_CASEFOLLOWUPRECORD)).body(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASEFOLLOWUPRECORD, "caseFollowupRecord", "添加跟进记录失败")).body(null);
        }
    }

    /**
     * @Description 电催页面多条件查询跟进记录
     */
    @GetMapping("/getFollowupRecord")
    @ApiOperation(value = "电催页面多条件查询跟进记录", notes = "电催页面多条件查询跟进记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseFollowupRecord>> getFollowupRecord(@RequestParam @ApiParam(value = "案件ID", required = true) String caseId,
                                                                      @QuerydslPredicate(root = CaseFollowupRecord.class) Predicate predicate,
                                                                      @ApiIgnore Pageable pageable) throws URISyntaxException {
        log.debug("REST request to get case followup records by {caseId}", caseId);
        try {
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(QCaseFollowupRecord.caseFollowupRecord.caseId.id.eq(caseId));
            builder.and(QCaseFollowupRecord.caseFollowupRecord.collectionWay.eq(1)); //只查催记方式为手动的
            Page<CaseFollowupRecord> page = caseFollowupRecordRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/AccTelPoolController/getFollowupRecord");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASEFOLLOWUPRECORD, "caseFollowupRecord", "查询失败")).body(null);
        }
    }

    /**
     * @Description 电催主页面多条件查询电催案件
     */
    @GetMapping("/getAllTelCase")
    @ApiOperation(value = "电催主页面多条件查询电催案件", notes = "电催主页面多条件查询电催案件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseInfo>> getAllTelCase(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                        @ApiIgnore Pageable pageable,
                                                        @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to get all tel case");
        List<Integer> list = new ArrayList<>();
        list.add(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue()); //待分配
        list.add(CaseInfo.CollectionStatus.WAITCOLLECTION.getValue()); //待催收
        list.add(CaseInfo.CollectionStatus.COLLECTIONING.getValue()); //催收中
        list.add(CaseInfo.CollectionStatus.OVER_PAYING.getValue()); //逾期还款中
        list.add(CaseInfo.CollectionStatus.EARLY_PAYING.getValue()); //提前结清还款中
        try {
            User tokenUser = getUserByToken(token);
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(QCaseInfo.caseInfo.companyCode.eq(tokenUser.getCompanyCode())); //限制公司code码
            builder.and(QCaseInfo.caseInfo.currentCollector.department.code.startsWith(tokenUser.getDepartment().getCode())); //权限控制
            builder.and(QCaseInfo.caseInfo.collectionStatus.in(list)); //不查询已结案案件
            builder.and(QCaseInfo.caseInfo.collectionType.eq(CaseInfo.CollectionType.TEL.getValue())); //只查询电催案件
            Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/AccTelPoolController/getAllTelCase");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASEINFO, "caseInfo", "查询失败")).body(null);
        }
    }

    /**
     * @Description 多条件查询电催已处理记录
     */
    @GetMapping("/getAllHandleTelCase")
    @ApiOperation(value = "多条件查询电催已处理记录", notes = "多条件查询电催已处理记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseInfo>> getAllHandleTelCase(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                              @ApiIgnore Pageable pageable,
                                                              @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to get all handle tel case");
        try {
            User tokenUser = getUserByToken(token);
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(QCaseInfo.caseInfo.companyCode.eq(tokenUser.getCompanyCode())); //限制公司code码
            builder.and(QCaseInfo.caseInfo.currentCollector.department.code.startsWith(tokenUser.getDepartment().getCode())); //权限控制
            builder.and(QCaseInfo.caseInfo.collectionStatus.eq(CaseInfo.CollectionStatus.CASE_OVER.getValue())); //只查询已结案案件
            builder.and(QCaseInfo.caseInfo.collectionType.eq(CaseInfo.CollectionType.TEL.getValue())); //只查询电催案件
            Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/AccTelPoolController/getAllHandleTelCase");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASEINFO, "caseInfo", "查询失败")).body(null);
        }
    }

    /**
     * @Description 电催案件结案
     */
    @GetMapping("/endTelCase")
    @ApiOperation(value = "电催案件结案", notes = "电催案件结案")
    public ResponseEntity<Void> endTelCase(EndCaseParams endCaseParams,
                                           @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to end case by {endCaseParams}", endCaseParams);
        try {
            User tokenUser = getUserByToken(token);
            caseInfoService.endCase(endCaseParams, tokenUser);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("结案成功", ENTITY_CASEINFO)).body(null);
        } catch (Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASEINFO, "caseInfo", "结案失败")).body(null);
        }
    }

    /**
     * @Description 协催申请
     */
    @PostMapping("/assistApply")
    @ApiOperation(value = "协催申请", notes = "协催申请")
    public ResponseEntity<Void> assistApply(AssistApplyParams assistApplyParams,
                                            @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to save assist apply");
        try {
            User tokenUser = getUserByToken(token);
            caseInfoService.saveAssistApply(assistApplyParams, tokenUser);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("协催申请成功", ENTITY_CASEASSISTAPPLY)).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASEASSISTAPPLY, "caseAssistApply", "协催申请失败")).body(null);
        }
    }

    /**
     * @Description 电催页面多条件查询协催记录
     */
    @GetMapping("/getAllAssistApplyRecord")
    @ApiOperation(value = "电催页面多条件查询协催记录", notes = "电催页面多条件查询协催记录")
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
                                                                         @RequestParam @ApiParam(value = "案件ID", required = true) String caseId) throws URISyntaxException {
        log.debug("REST request to get all assist apply record by {caseId}", caseId);
        try {
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(QCaseAssistApply.caseAssistApply.caseId.eq(caseId)); //只查当前案件的协催申请记录
            Page<CaseAssistApply> page = caseAssistApplyRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/AccTelPoolController/getAllAssistApplyRecord");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASEASSISTAPPLY, "caseAssistApply", "查询失败")).body(null);
        }
    }

    /**
     * @Description 电催页面多条件查询发送信息记录
     */
    @GetMapping("/getAllSendMessageRecord")
    @ApiOperation(value = "电催页面多条件查询发送信息记录", notes = "电催页面多条件查询发送信息记录")
    public ResponseEntity<Page<SendMessageRecord>> getAllSendMessageRecord(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                                           @ApiIgnore Pageable pageable,
                                                                           @RequestParam @ApiParam(value = "案件ID", required = true) String caseId) throws URISyntaxException {
        log.debug("REST request to get all send message record by {caseId}", caseId);
        try {
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(QSendMessageRecord.sendMessageRecord.caseId.eq(caseId)); //只查当前案件的信息发送记录
            Page<SendMessageRecord> page = sendMessageRecordRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/AccTelPoolController/getAllSendMessageRecord");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASEASSISTAPPLY, "caseAssistApply", "查询失败")).body(null);
        }
    }

    /**
     * @Description 电催页面新增联系人电话或邮箱地址
     */
    @PostMapping("/savePersonalContactPhone")
    @ApiOperation(value = "电催页面新增联系人电话或邮箱地址", notes = "电催页面新增联系人电话或邮箱地址")
    public ResponseEntity<PersonalContact> savePersonalContactPhone(@RequestBody PersonalContact personalContact,
                                                                    @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to save new phone number");
        try {
            User tokenUser = getUserByToken(token);
            personalContact.setId(null); //主键置空
            personalContact.setOperator(tokenUser.getUserName()); //操作人
            personalContact.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
            personalContactRepository.saveAndFlush(personalContact);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("新增成功", ENTITY_PERSONALCONTACT)).body(personalContact);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_PERSONALCONTACT, "personalContact", "新增联系人信息失败")).body(null);
        }
    }

    /**
     * @Description 电催页面获取分配信息
     */
    @GetMapping("/getBatchInfo")
    @ApiOperation(value = "电催页面获取分配信息", notes = "电催页面获取分配信息")
    public ResponseEntity<BatchDistributeModel> getBatchInfo(@RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to get batch info");
        try {
            User tokenUser = getUserByToken(token);
            BatchDistributeModel batchDistributeModel = caseInfoService.getBatchDistribution(tokenUser);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("获取分配信息成功", ENTITY_CASEINFO)).body(batchDistributeModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASEINFO, "caseInfo", "获取分配信息失败")).body(null);
        }
    }

    /**
     * @Description 电催页面批量分配
     */
    @PostMapping("/batchTelCase")
    @ApiOperation(value = "电催页面批量分配", notes = "电催页面批量分配")
    public ResponseEntity<Void> batchTelCase(@RequestBody BatchDistributeModel batchDistributeModel,
                                             @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to batch case");
        try {
            User tokenUser = getUserByToken(token);
            caseInfoService.batchCase(batchDistributeModel, tokenUser);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("批量分配成功", ENTITY_CASEINFO)).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASEINFO, "caseInfo", "批量分配失败")).body(null);
        }
    }

    /**
     * @Description 电催案件颜色打标
     */
    @PutMapping("/telCaseMarkColor")
    @ApiOperation(value = "电催案件颜色打标", notes = "电催案件颜色打标")
    public ResponseEntity<CaseInfo> telCaseMarkColor(@RequestBody CaseMarkParams caseMarkParams,
                                                     @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to mark color");
        try {
            User tokenUser = getUserByToken(token);
            CaseInfo caseInfo = caseInfoService.caseMarkColor(caseMarkParams, tokenUser);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("案件颜色打标成功", ENTITY_CASEINFO)).body(caseInfo);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASEINFO, "caseInfo", "案件颜色打标失败")).body(null);
        }
    }

    /**
     * @Description 修改联系人电话状态
     */
    @PutMapping("/modifyPhoneStatus")
    @ApiOperation(value = "修改联系人电话状态", notes = "修改联系人电话状态")
    public ResponseEntity<PersonalContact> modifyPhoneStatus(@RequestBody PhoneStatusParams phoneStatusParams,
                                                             @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to modify phone status");
        try {
            User tokenUser = getUserByToken(token);
            PersonalContact personalContact = caseInfoService.modifyPhoneStatus(phoneStatusParams, tokenUser);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("联系人电话状态修改成功", ENTITY_PERSONALCONTACT)).body(personalContact);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_PERSONALCONTACT, "personalContact", "修改失败")).body(null);
        }
    }

    /**
     * @Description 电催页面查询客户联系人信息
     */
    @GetMapping("/getPersonalContact")
    @ApiOperation(value = "电催页面查询客户联系人信息", notes = "电催页面查询客户联系人信息")
    public ResponseEntity<List<PersonalContact>> getPersonalContact(@RequestParam @ApiParam(value = "客户信息ID", required = true) String personalId) {
        log.debug("REST request to get personal contacts by {personalId}", personalId);
        try {
            List<PersonalContact> personalContacts = caseInfoService.getPersonalContact(personalId);
            if (personalContacts.isEmpty()) {
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("联系人信息为空", ENTITY_PERSONALCONTACT)).body(personalContacts);
            } else {
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("联系人信息查询成功", ENTITY_PERSONALCONTACT)).body(personalContacts);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_PERSONALCONTACT, "personalContact", "查询失败")).body(null);
        }
    }

    /**
     * @Description 电催页面添加修复信息
     */
    @PostMapping("/saveRepairInfo")
    @ApiOperation(value = "电催页面添加修复信息", notes = "电催页面添加修复信息")
    public ResponseEntity<PersonalContact> saveRepairInfo(@RequestBody RepairInfoModel repairInfoModel,
                                                          @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to save repair information");
        try {
            User tokenUser = getUserByToken(token);
            PersonalContact personalContact = caseInfoService.saveRepairInfo(repairInfoModel, tokenUser);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("添加成功", ENTITY_PERSONALCONTACT)).body(personalContact);
        } catch (Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_PERSONALCONTACT, "personalContact", "添加失败")).body(null);
        }
    }

    /**
     * @Description 查看凭证
     */
    @GetMapping("/getPayProof")
    @ApiOperation(value = "查看凭证", notes = "查看凭证")
    public ResponseEntity<List<UploadFile>> getPayProof(@RequestParam @ApiParam(value = "还款审批ID") String casePayId) {
        log.debug("REST request to get payment proof");
        try {
            List<UploadFile> uploadFiles = caseInfoService.getRepaymentVoucher(casePayId);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("下载成功", ENTITY_UPLOADFILE)).body(uploadFiles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_UPLOADFILE, "uploadFile", "下载失败")).body(null);
        }
    }
}