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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @Author : sunyanping
 * @Description : 案件协催
 * @Date : 2017/7/20.
 */
@RestController
@RequestMapping("/api/caseAssistController")
@Api(value = "CaseAssistController", description = "案件协催")
public class CaseAssistController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(CaseAssistController.class);

    @Inject
    private CaseInfoService caseInfoService;
    @Inject
    private CaseAssistRepository caseAssistRepository;
    @Inject
    private UserRepository userRepository;
    @Inject
    private CaseFollowupRecordRepository caseFollowupRecordRepository;
    @Inject
    private AccVisitPoolController accVisitPoolController;
    @Inject
    private CaseInfoRepository caseInfoRepository;
    @Inject
    private SendMessageRecordRepository sendMessageRecordRepository;

    @GetMapping("/findAssistCaseMessageRecord")
    @ApiOperation(value = "协催案件查询短信记录",notes = "协催案件查询短信记录")
    public ResponseEntity<Page<SendMessageRecord>> findAssistCaseMessageRecord(@QuerydslPredicate(root = SendMessageRecord.class) Predicate predicate,
                                                                               @ApiIgnore Pageable pageable,
                                                                               @RequestParam @ApiParam("案件ID") String caseId) {
        try {
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(QSendMessageRecord.sendMessageRecord.caseId.eq(caseId));
            Page<SendMessageRecord> page = sendMessageRecordRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/caseAssistController/findAssistCaseMessageRecord");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", "findAssistCaseMessageRecord", e.getMessage())).body(null);
        }
    }

    @PutMapping("/assistCaseMarkColor")
    @ApiOperation(value = "协催案件颜色打标", notes = "协催案件颜色打标")
    public ResponseEntity<CaseAssist> assistCaseMarkColor(@RequestBody AssistCaseMarkParams caseMarkParams,
                                                          @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to mark color");
        try {
            User tokenUser = getUserByToken(token);
            CaseAssist caseAssist = caseAssistRepository.findOne(caseMarkParams.getAssistId());
            if (Objects.isNull(caseAssist)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("协催案件未找到", "assistCaseMarkColor", "协催案件未找到")).body(null);
            }

            caseAssist.setMarkId(caseMarkParams.getMarkId()); //打标
            caseAssist.setOperator(tokenUser); //操作人
            caseAssist.setOperatorTime(new Date()); //操作时间
            CaseAssist save = caseAssistRepository.save(caseAssist);
            return ResponseEntity.ok().body(save);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("打标失败", "assistCaseMarkColor", e.getMessage())).body(null);
        }
    }

    @GetMapping("/assistWithdraw")
    @ApiOperation(value = "协催页面还款撤回", notes = "协催页面还款撤回")
    public ResponseEntity<Void> assistWithdraw(@RequestParam @ApiParam(value = "还款审批ID", required = true) String payApplyId,
                                               @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to withdraw by {}", payApplyId);
        try {
            User tokenUser = getUserByToken(token);
            caseInfoService.payWithdraw(payApplyId, tokenUser);
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("撤回失败", "assistWithdraw", e.getMessage())).body(null);
        }
    }

    @PostMapping("/doAssistPay")
    @ApiOperation(value = "协催页面还款操作", notes = "协催页面还款操作")
    public ResponseEntity<Void> doTelPay(@RequestBody PayApplyParams payApplyParams,
                                         @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to apply payment");
        try {
            User tokenUser = getUserByToken(token);
            caseInfoService.doPay(payApplyParams, tokenUser);
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("还款失败", "doTelPay", e.getMessage())).body(null);
        }
    }
//
//    @GetMapping("/getAssistCustInfo")
//    @ApiOperation(value = "协催案件详情页面的客户信息", notes = "协催案件详情页面的客户信息")
//    public ResponseEntity<Personal> getAssistCustInfo(@RequestParam @ApiParam(value = "案件ID", required = true) String caseId) {
//        log.debug("REST request to get customer information ");
//        try {
//            Personal custInfo = caseInfoService.getCustInfo(caseId);
//            return ResponseEntity.ok().body(custInfo);
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", "getAssistCustInfo", e.getMessage())).body(null);
//        }
//    }

    @GetMapping("/getAssistRepaymentVoucher")
    @ApiOperation(value = "协催案件查看还款凭证", notes = "协催案件查看还款凭证")
    public ResponseEntity<List<UploadFile>> getAssistRepaymentVoucher(@ApiParam(value = "还款ID", required = true) @RequestParam String payId) {
        return accVisitPoolController.getRepaymentVoucher(payId);
    }

    @GetMapping("/getAssistVisitFiles")
    @ApiOperation(value = "下载协催案件外访资料", notes = "下载协催案件外访资料")
    public ResponseEntity<List<UploadFile>> getAssistVisitFiles(@ApiParam(value = "跟进ID", required = true) @RequestParam String follId) {
        return accVisitPoolController.getVisitFiles(follId);
    }

    @GetMapping("/getFollowupRecord")
    @ApiOperation(value = "协催页面多条件查询跟进记录",notes = "协催页面多条件查询跟进记录")
    public ResponseEntity<Page<CaseFollowupRecord>> getFollowupRecord(@RequestParam @ApiParam(value = "案件ID", required = true) String caseId,
                                                                      @QuerydslPredicate(root = CaseFollowupRecord.class) Predicate predicate,
                                                                      @ApiIgnore Pageable pageable) throws URISyntaxException {
        log.debug("REST request to get case followup records by {caseId}",caseId);
        try {
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(QCaseFollowupRecord.caseFollowupRecord.caseId.id.eq(caseId));
            Page<CaseFollowupRecord> page = caseFollowupRecordRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/caseAssistController/getFollowupRecord");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", "getFollowupRecord", e.getMessage())).body(null);
        }
    }


    @PostMapping("/saveFollowupRecord")
    @ApiOperation(value = "协催案件页面添加跟进记录", notes = "协催案件页面添加跟进记录")
    public ResponseEntity<CaseFollowupRecord> saveFollowupRecord(@RequestBody CaseFollowupRecord caseFollowupRecord,
                                                                 @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to save {}", caseFollowupRecord);
        try {
            User tokenUser = getUserByToken(token);
            CaseFollowupRecord result = caseInfoService.saveFollowupRecord(caseFollowupRecord, tokenUser);
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("添加失败", "saveFollowupRecord", e.getMessage())).body(null);
        }
    }

    // TODO 协催案件暂时不做挂起功能
//    @GetMapping("/handUp")
//    @ApiOperation(value = "协催案件挂起操作", notes = "协催案件挂起操作")
    public ResponseEntity<CaseAssist> handUp(@RequestParam @ApiParam(value = "协催案件id", required = true) String id,
                                             @RequestParam @ApiParam(value = "是否挂起id:1挂起;2取消挂起", required = true) Integer cupoPause) {
        log.debug("REST request to handUp : {}", id);
        try {
            CaseAssist one = caseAssistRepository.findOne(id);
            if (Objects.isNull(one)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createEntityCreationAlert("协催案件不存在","CaseAssistController")).body(null);
            }
            CaseInfo caseInfo = one.getCaseId();
            if (Objects.equals(1, cupoPause)) {//挂起请求
                // 修改协催案件为挂起
                one.setHandupFlag(CaseInfo.HandUpFlag.YES_HANG.getValue());
                //修改原案件为挂起
                caseInfo.setHandUpFlag(CaseInfo.HandUpFlag.YES_HANG.getValue());
                one.setCaseId(caseInfo);
            } else if (Objects.equals(2, cupoPause)) {//取消挂起请求
                one.setHandupFlag(CaseInfo.HandUpFlag.NO_HANG.getValue());
                //取消原案件挂起
                caseInfo.setHandUpFlag(CaseInfo.HandUpFlag.NO_HANG.getValue());
                one.setCaseId(caseInfo);
            } else {
                return ResponseEntity.badRequest().headers(HeaderUtil.createEntityCreationAlert("请求异常","CaseAssistController")).body(null);
            }
            CaseAssist save = caseAssistRepository.save(one);
            return new ResponseEntity<>(save, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createEntityCreationAlert("设置挂起失败","CaseAssistController")).body(null);
        }
    }

    @GetMapping("/getAllRecordAssistCase")
    @ApiOperation(value = "多条件查询协催已处理记录", notes = "多条件查询协催已处理记录")
    public ResponseEntity<Page<CaseAssist>> getAllRecordAssistCase(@QuerydslPredicate(root = CaseAssist.class) Predicate predicate,
                                                                   @ApiIgnore Pageable pageable,
                                                                   @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to getAllRecordAssistCase");
        try {
            User tokenUser = getUserByToken(token);
            BooleanBuilder builder = new BooleanBuilder(predicate);
//            builder.and(QCaseAssist.caseAssist.currentCollector.department.code.startsWith(tokenUser.getDepartment().getCode())); //权限控制
            builder.and(QCaseAssist.caseAssist.assistStatus.eq(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())); //查询协催结束的协催案件
            Page<CaseAssist> page = caseAssistRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/caseAssistController/getAllRecordAssistCase");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", "getAllRecordAssistCase", e.getMessage())).body(null);
        }
    }

    @PostMapping("/endCaseAssist")
    @ApiOperation(value = "协催案件结案", notes = "协催案件结案")
    public ResponseEntity<Void> endCaseAssist(@RequestBody CloseAssistCaseModel closeAssistCaseModel,
                                              @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to end case by {}", closeAssistCaseModel);
        try {
            User tokenUser = getUserByToken(token);
            String assistId = closeAssistCaseModel.getAssistId();
            CaseAssist one = caseAssistRepository.findOne(assistId);
            // 修改协催案件状态
            one.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue());
            one.setOperator(tokenUser);
            one.setOperatorTime(new Date());

            EndCaseParams endCaseParams = new EndCaseParams();
            endCaseParams.setCaseId(one.getCaseId().getId());
            endCaseParams.setEndRemark(closeAssistCaseModel.getRemark());
            endCaseParams.setIsAssist(true);
            endCaseParams.setEndType(closeAssistCaseModel.getType());

            caseAssistRepository.save(one);
            caseInfoService.endCase(endCaseParams, tokenUser);
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("结案失败", "CaseAssistController", e.getMessage())).body(null);
        }
    }

    @GetMapping("/getBatchInfo")
    @ApiOperation(value = "协催页面获取分配信息", notes = "协催页面获取分配信息")
    public ResponseEntity<BatchDistributeModel> getBatchInfo(@RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to get batch info");
        try {
            User tokenUser = getUserByToken(token);
            BatchDistributeModel batchDistributeModel = caseInfoService.getBatchDistribution(tokenUser);
            return ResponseEntity.ok().body(batchDistributeModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取分配信息失败", "CaseAssistController", e.getMessage())).body(null);
        }
    }


    @PostMapping("/batchCaseAssist")
    @ApiOperation(value = "协催页面批量分配", notes = "协催页面批量分配")
    public ResponseEntity<Void> batchCaseAssist(@RequestBody AssistCaseBatchModel assistCaseBatchModel,
                                                @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("REST request to batch assist case");
        try {
            User tokenUser = getUserByToken(token);
            List<String> assistIds = assistCaseBatchModel.getAssistIds();
            List<CaseAssist> all = caseAssistRepository.findAll(assistIds);
            List<String> caseInfoIds = new ArrayList<>();
            all.forEach(e -> caseInfoIds.add(e.getCaseId().getId()));
            BatchDistributeModel batchDistributeModel = new BatchDistributeModel();
            BeanUtils.copyProperties(assistCaseBatchModel, batchDistributeModel);
            batchDistributeModel.setCaseIds(caseInfoIds);
            caseInfoService.batchCase(batchDistributeModel, tokenUser);
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("分配失败", "batchCaseAssist", e.getMessage())).body(null);
        }
    }

    @PutMapping("/assignCaseAssist")
    @ApiOperation(value = "协催案件分配", notes = "协催案件分配")
    public ResponseEntity assignCaseAssist(@RequestParam("caseAssistId") @ApiParam("协催案件ID") String caseAssistId,
                                           @RequestParam("assistorId") @ApiParam("要分配的协催员ID") String assistorId,
                                           @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("Rest request to assignCaseAssist");
        User user = getUserByToken(token);
        try {
            CaseAssist caseAssist = caseAssistRepository.findOne(caseAssistId);
            User assistor = userRepository.findOne(assistorId);

            // 协催分配
            ReDistributionParams reDistributionParams = new ReDistributionParams();
            reDistributionParams.setCaseId(caseAssist.getCaseId().getId());//案件ID
            reDistributionParams.setUserName(assistor.getUserName()); //协催员用户名
            reDistributionParams.setIsAssist(true);
            caseInfoService.reDistribution(reDistributionParams, user);
//        caseAssistRepository.save(caseAssist);
            return ResponseEntity.ok().body("分配成功!");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("分配失败", "assignCaseAssist", e.getMessage())).body(null);
        }
    }

    @PutMapping("/receiveCaseAssist/{id}")
    @ApiOperation(value = "协催案件抢单", notes = "协催案件抢单")
    public ResponseEntity receiveCaseAssist(@PathVariable("id") @ApiParam("协催案件ID") String id,
                                            @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("Rest request to receiveCaseAssist");
        User user = getUserByToken(token);

        try {
            CaseAssist caseAssist = caseAssistRepository.findOne(id);
            if (Objects.isNull(caseAssist)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseAssistController", "receiveCaseAssist", "该协催案件不存在!")).body(null);
            }

            // 若案件状态为 协催待分配 则可以抢单
            if (Objects.equals(caseAssist.getAssistStatus(), CaseInfo.AssistStatus.ASSIST_WAIT_ASSIGN)) {
                synchronized (this) {
                    CaseAssist caseAssist1 = caseAssistRepository.findOne(id);
                    if (Objects.equals(caseAssist1.getAssistStatus(), CaseInfo.AssistStatus.ASSIST_WAIT_ASSIGN)) {
                        //更改协催案件信息
                        caseAssist.setDepartId(user.getDepartment().getId()); //协催部门ID
                        caseAssist.setCompanyCode(user.getCompanyCode()); //协催公司码
                        caseAssist.setAssistCollector(user); //协催员
                        //更改案件信息
                        CaseInfo caseInfo = caseAssist.getCaseId();
                        caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_WAIT_ACC.getValue()); //协催状态
                        caseInfo.setAssistCollector(user); //协催员
                        caseAssist.setCaseId(caseInfo);
                        caseAssistRepository.save(caseAssist);
                    } else {
                        return ResponseEntity.ok().body("抢单失败!");
                    }
                }
                return ResponseEntity.ok().body("抢单成功!");
            }
            return ResponseEntity.ok().body("已被抢单!");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("系统错误", "receiveCaseAssist", e.getMessage())).body(null);
        }
    }

    @GetMapping("/findCaseAssistInfo/{id}")
    @ApiOperation(value = "根据协催案件ID查找对应案件信息",notes = "根据协催案件ID查找对应案件信息")
    public ResponseEntity<CaseInfo> findCaseAssistInfo (@PathVariable("id") @ApiParam("协催案件ID") String id) {
        try {
            CaseAssist one = caseAssistRepository.findOne(id);
            if (Objects.isNull(one)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseAssistController","findCaseAssistInfo","协催案件不存在")).body(null);
            }
            CaseInfo caseInfo = one.getCaseId();
            return ResponseEntity.ok().body(caseInfo);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("系统错误", "findCaseAssistInfo", e.getMessage())).body(null);
        }
    }

    @GetMapping("/findAllCaseAssist")
    @ApiOperation(value = "获取所有协催案件", notes = "获取所有协催案件")
    public ResponseEntity<Page<CaseAssist>> findAllCaseAssist(@QuerydslPredicate(root = CaseAssist.class) Predicate predicate,
                                                              @ApiIgnore Pageable pageable) {
        log.debug("Rest request to findAllCaseAssist");
        try {
            QCaseAssist qCaseAssist = QCaseAssist.caseAssist;
            BooleanBuilder exp = new BooleanBuilder(predicate);
            // 过滤掉协催结束的协催案件
            exp.and(qCaseAssist.assistStatus.notIn(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()));
            Page<CaseAssist> page = caseAssistRepository.findAll(exp, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("系统错误", "findAllCaseAssist", e.getMessage())).body(null);
        }
    }

    public ResponseEntity<Page<CaseAssist>> findAllCaseAssistByAssistor(@QuerydslPredicate(root = CaseAssist.class) Predicate predicate,
                                                                        @ApiIgnore Pageable pageable,
                                                                        @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("Rest request to findAllCaseAssistByAssistor");
        User user = getUserByToken(token);
        //左边案件列表展示协催员自己的所有协催案件
        QCaseAssist qCaseAssist = QCaseAssist.caseAssist;
        BooleanBuilder exp = new BooleanBuilder(predicate);
        exp.and(qCaseAssist.assistCollector.userName.eq(user.getUserName()));
        Page<CaseAssist> page = caseAssistRepository.findAll(exp, pageable);
        return ResponseEntity.ok().body(page);
    }
}
