package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.*;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.business.service.CaseAssistService;
import cn.fintecher.pangolin.business.service.CaseInfoService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.apache.commons.collections4.IterableUtils;
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
import java.net.URISyntaxException;
import java.util.*;

/**
 * @Author : sunyanping
 * @Description : 案件协催
 * @Date : 2017/7/20.
 */
@RestController
@RequestMapping("/api/caseAssistController")
@Api(value = "CaseAssistController", description = "案件协催")
public class CaseAssistController extends BaseController {

    private final static String ENTITY_NAME = "CaseAssistController";

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
    @Inject
    private CasePayApplyRepository casePayApplyRepository;
    @Inject
    private CaseAssistApplyRepository caseAssistApplyRepository;
    @Inject
    private CaseTurnRecordRepository caseTurnRecordRepository;
    @Inject
    private CaseAssistService caseAssistService;

    @PostMapping("/cancelLeaveCaseAssist")
    @ApiOperation(value = "协催案件取消留案操作", notes = "协催案件取消留案操作")
    public ResponseEntity<LeaveCaseModel> cancelLeaveCaseAssist(@RequestBody LeaveCaseParams leaveCaseParams,
                                                                @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to cancelLeaveCaseAssist");
        User user ;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage())).body(null);
        }
        try {
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.nonNull(leaveCaseParams.getCompanyCode())) {
                    user.setCompanyCode(leaveCaseParams.getCompanyCode());
                }
            }
            Integer num = caseAssistService.leaveCaseAssistNum(user); //可留案案件数
            Iterable<CaseAssist> all = caseAssistRepository.findAll(QCaseAssist.caseAssist.id.in(leaveCaseParams.getCaseIds()));
            Iterator<CaseAssist> it = all.iterator();
            if (!it.hasNext()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,"","请选择要留案的案件!")).body(null);
            }
            List<CaseAssist> caseAssistList = new ArrayList<>();
            while (it.hasNext()) {
                CaseAssist next = it.next();
                if (Objects.equals(next.getLeaveCaseFlag(), CaseInfo.leaveCaseFlagEnum.NO_LEAVE.getValue())) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,"","未留案的案件不能取消留案!")).body(null);
                }
                if (!Objects.equals(next.getAssistCollector(), user)) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,"","不能操作不属于协催员自己的案件!")).body(null);
                }
                caseAssistService.cancelLeaveCaseAssist(next, user);
                caseAssistList.add(next);
            }
            caseAssistRepository.save(caseAssistList);
            LeaveCaseModel leaveCaseModel = new LeaveCaseModel();
            leaveCaseModel.setCaseNum(num+caseAssistList.size());
            return ResponseEntity.ok().body(leaveCaseModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage())).body(null);
        }
    }

    @PostMapping("/leaveCaseAssist")
    @ApiOperation(value = "协催案件留案操作", notes = "协催案件留案操作")
    public ResponseEntity<LeaveCaseModel> leaveCaseAssist(@RequestBody LeaveCaseParams leaveCaseParams,
                                                          @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to leave case");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "leaveCaseAssist", e.getMessage())).body(null);
        }
        try {
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.nonNull(leaveCaseParams.getCompanyCode())) {
                    user.setCompanyCode(leaveCaseParams.getCompanyCode());
                }
            }
            Integer num = caseAssistService.leaveCaseAssistNum(user);
            List<String> caseIds = leaveCaseParams.getCaseIds();
            if (caseIds.size() >= num) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "所选案件数量超过可留案案件数!")).body(null);
            }
            for (String caseId : caseIds) {
                CaseAssist one = caseAssistRepository.findOne(caseId);
                if (Objects.isNull(one)) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "所选案件未找到!")).body(null);
                }
                if (Objects.nonNull(one.getAssistCollector()) && !Objects.equals(one.getAssistCollector().getId(), user.getId())) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "只能对自己所持有的案件进行留案操作!")).body(null);
                }
                if (Objects.equals(one.getLeaveCaseFlag(), CaseInfo.leaveCaseFlagEnum.YES_LEAVE.getValue())) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "所选案件存在已经留案的案件!")).body(null);
                }
                if (Objects.equals(one.getAssistWay(), CaseAssist.AssistWay.ONCE_ASSIST.getValue())) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "单次协催的案件不允许留案!")).body(null);
                }
                one.setLeaveCaseFlag(CaseInfo.leaveCaseFlagEnum.YES_LEAVE.getValue()); //留案标志
                one.setLeaveDate(new Date()); //留案日期
                caseAssistRepository.save(one);
            }
            LeaveCaseModel leaveCaseModel = new LeaveCaseModel();
            leaveCaseModel.setCaseNum(num-caseIds.size());
            return ResponseEntity.ok().body(leaveCaseModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", e.getMessage())).body(null);
        }
    }

    @GetMapping("/closeCaseAssist")
    @ApiOperation(value = "结束协催", notes = "结束协催")
    public ResponseEntity closeCaseAssist(@RequestParam @ApiParam("协催案件ID") String assistId,
                                          @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to closeCaseAssist");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "closeCaseAssist", e.getMessage())).body(null);
        }
        try {
            CaseAssist caseAssist = caseAssistRepository.findOne(assistId);
            caseAssistService.endCaseAssist(caseAssist,user);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功!", "")).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "closeCaseAssist", "系统异常!")).body(null);
        }
    }

    @GetMapping("/findCaseInfoAssistRecord")
    @ApiOperation(value = "查询案件协催记录", notes = "查询案件协催记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseAssistApply>> findCaseInfoAssistRecord(@QuerydslPredicate(root = CaseAssistApply.class) Predicate predicate,
                                                                          @RequestParam @ApiParam("案件ID") String caseId,
                                                                          @ApiIgnore Pageable pageable,
                                                                          @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to findCaseInfoAssistRecord");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "findCaseInfoAssistRecord", e.getMessage())).body(null);
        }
        try {
            BooleanBuilder exp = new BooleanBuilder(predicate);
            QCaseAssistApply qCaseAssistApply = QCaseAssistApply.caseAssistApply;
            exp.and(qCaseAssistApply.caseId.eq(caseId));
            Page<CaseAssistApply> page = caseAssistApplyRepository.findAll(exp, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "findCaseInfoAssistRecord", "系统异常!")).body(null);
        }
    }

    @GetMapping("/findAssistCasePayRecord")
    @ApiOperation(value = "查询还款申请/记录", notes = "查询还款申请/记录")
    public ResponseEntity<Page<CasePayApply>> findAssistCasePayRecord(@QuerydslPredicate(root = CasePayApply.class) Predicate predicate,
                                                                      @RequestParam @ApiParam("案件ID") String assistId,
                                                                      @ApiIgnore Pageable pageable,
                                                                      @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to findAssistCasePayRecord");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "findAssistCaseMessageRecord", e.getMessage())).body(null);
        }
        try {
            QCasePayApply qCasePayApply = QCasePayApply.casePayApply;
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(qCasePayApply.companyCode.eq(user.getCompanyCode()));
            builder.and(qCasePayApply.caseId.eq(assistId));
            Page<CasePayApply> page = casePayApplyRepository.findAll(builder, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "findAssistCasePayRecord", "系统异常!")).body(null);
        }
    }

    @GetMapping("/findAssistCaseMessageRecord")
    @ApiOperation(value = "协催案件查询短信记录", notes = "协催案件查询短信记录")
    public ResponseEntity<Page<SendMessageRecord>> findAssistCaseMessageRecord(@QuerydslPredicate(root = SendMessageRecord.class) Predicate predicate,
                                                                               @ApiIgnore Pageable pageable,
                                                                               @RequestParam @ApiParam("案件ID") String caseId,
                                                                               @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to findAssistCaseMessageRecord");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "findAssistCaseMessageRecord", e.getMessage())).body(null);
        }
        try {
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(QSendMessageRecord.sendMessageRecord.companyCode.eq(user.getCompanyCode()));
            builder.and(QSendMessageRecord.sendMessageRecord.caseId.eq(caseId));
            Page<SendMessageRecord> page = sendMessageRecordRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/caseAssistController/findAssistCaseMessageRecord");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "findAssistCaseMessageRecord", "系统异常!")).body(null);
        }
    }

    @PutMapping("/assistCaseMarkColor")
    @ApiOperation(value = "协催案件颜色打标", notes = "协催案件颜色打标")
    public ResponseEntity<Void> assistCaseMarkColor(@RequestBody AssistCaseMarkParams caseMarkParams,
                                                    @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to mark color");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "assistCaseMarkColor", e.getMessage())).body(null);
        }
        try {
            List<String> assistIds = caseMarkParams.getAssistIds();
            if (assistIds.isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "assistCaseMarkColor", "请选择要打标的案件!")).body(null);
            }
            for (String id : assistIds) {
                CaseAssist caseAssist = caseAssistRepository.findOne(id);
                caseAssist.setMarkId(caseMarkParams.getMarkId()); //打标
                caseAssist.setOperator(user); //操作人
                caseAssist.setOperatorTime(new Date()); //操作时间
                caseAssistRepository.saveAndFlush(caseAssist);
            }

            return ResponseEntity.ok().headers(HeaderUtil.createAlert("打标成功", "")).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "assistCaseMarkColor", "系统异常!")).body(null);
        }
    }

    @GetMapping("/assistWithdraw")
    @ApiOperation(value = "协催页面还款撤回", notes = "协催页面还款撤回")
    public ResponseEntity<Void> assistWithdraw(@RequestParam @ApiParam(value = "还款审批ID", required = true) String payApplyId,
                                               @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to withdraw by {}", payApplyId);
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "assistWithdraw", e.getMessage())).body(null);
        }
        try {
            try {
                caseInfoService.payWithdraw(payApplyId, user);
            } catch (final Exception e) {
                log.debug(e.getMessage());
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "assistWithdraw", e.getMessage())).body(null);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("还款成功", "")).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "assistWithdraw", "系统异常!")).body(null);
        }
    }

    @PostMapping("/doAssistPay")
    @ApiOperation(value = "协催页面还款操作", notes = "协催页面还款操作")
    public ResponseEntity<Void> doTelPay(@RequestBody PayApplyParams payApplyParams,
                                         @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to apply payment");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "doTelPay", e.getMessage())).body(null);
        }
        try {
            try {
                caseInfoService.doPay(payApplyParams, user);
                CaseAssist one = caseAssistRepository.findOne(QCaseAssist.caseAssist.caseId.id.eq(payApplyParams.getCaseId())
                        .and(QCaseAssist.caseAssist.assistStatus.ne(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())));
                if (!Objects.equals(one.getAssistStatus(),CaseInfo.AssistStatus.ASSIST_COLLECTING.getValue())) {
                    one.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COLLECTING.getValue());
                    CaseInfo caseId = one.getCaseId();
                    caseId.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COLLECTING.getValue());
                    one.setCaseId(caseId);
                    caseAssistRepository.save(one);
                }
            } catch (final Exception e) {
                log.debug(e.getMessage());
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "doTelPay", e.getMessage())).body(null);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("还款成功!", "")).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "doTelPay", "系统异常!")).body(null);
        }
    }

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

    @GetMapping("/getFollowupRecord/{caseNumber}")
    @ApiOperation(value = "协催页面多条件查询跟进记录", notes = "协催页面多条件查询跟进记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseFollowupRecord>> getFollowupRecord(@QuerydslPredicate(root = CaseFollowupRecord.class) Predicate predicate,
                                                                      @PathVariable @ApiParam(value = "案件编号", required = true) String caseNumber,
                                                                      @ApiIgnore Pageable pageable) {
        log.debug("REST request to get case followup records by {caseNumber}", caseNumber);
        try {
            BooleanBuilder builder = new BooleanBuilder(predicate);
            QCaseFollowupRecord qCaseFollowupRecord = QCaseFollowupRecord.caseFollowupRecord;
            builder.and(qCaseFollowupRecord.caseNumber.eq(caseNumber));
            /*builder.and(qCaseFollowupRecord.caseId.id.eq(caseId));*/
            Page<CaseFollowupRecord> page = caseFollowupRecordRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/caseAssistController/getFollowupRecord");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "getFollowupRecord", "系统异常!")).body(null);
        }
    }

    @PostMapping("/saveFollowupRecord")
    @ApiOperation(value = "协催案件页面添加跟进记录", notes = "协催案件页面添加跟进记录")
    public ResponseEntity<CaseFollowupRecord> saveFollowupRecord(@RequestBody CaseFollowupParams caseFollowupParams,
                                                                 @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to save {}", caseFollowupParams);
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "saveFollowupRecord", e.getMessage())).body(null);
        }
        try {
            CaseFollowupRecord result = null;
            try {
                result = caseInfoService.saveFollowupRecord(caseFollowupParams, user);
                CaseAssist one = caseAssistRepository.findOne(QCaseAssist.caseAssist.caseId.id.eq(caseFollowupParams.getCaseId())
                        .and(QCaseAssist.caseAssist.assistStatus.notIn(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())));
                if (Objects.equals(one.getAssistStatus(), CaseInfo.AssistStatus.ASSIST_WAIT_ACC.getValue())) {
                    one.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COLLECTING.getValue());
                    caseAssistRepository.save(one);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "saveFollowupRecord", e.getMessage())).body(null);
            }
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "saveFollowupRecord", "系统异常!")).body(null);
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
                return ResponseEntity.badRequest().headers(HeaderUtil.createEntityCreationAlert("协催案件不存在", ENTITY_NAME)).body(null);
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
                return ResponseEntity.badRequest().headers(HeaderUtil.createEntityCreationAlert("请求异常", ENTITY_NAME)).body(null);
            }
            CaseAssist save = caseAssistRepository.save(one);
            return new ResponseEntity<>(save, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createEntityCreationAlert("设置挂起失败", ENTITY_NAME)).body(null);
        }
    }

    @PostMapping("/endCaseAssist")
    @ApiOperation(value = "协催案件结案", notes = "协催案件结案")
    public ResponseEntity<Void> endCaseAssist(@RequestBody CloseAssistCaseModel closeAssistCaseModel,
                                              @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to end case by {}", closeAssistCaseModel);
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "endCaseAssist", e.getMessage())).body(null);
        }
        try {
            String assistId = closeAssistCaseModel.getAssistId();
            CaseAssist one = caseAssistRepository.findOne(assistId);
            // 修改协催案件状态
//            one.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue());
//            one.setOperator(user);
//            one.setOperatorTime(new Date());
            EndCaseParams endCaseParams = new EndCaseParams();
            endCaseParams.setCaseId(one.getCaseId().getId());
            endCaseParams.setEndRemark(closeAssistCaseModel.getRemark());
            endCaseParams.setIsAssist(true);
            endCaseParams.setEndType(closeAssistCaseModel.getType());
//            caseAssistRepository.save(one);
            try {
                caseInfoService.endCase(endCaseParams, user);
            } catch (final Exception e) {
                log.debug(e.getMessage());
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "endCaseAssist", e.getMessage())).body(null);
            }
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "endCaseAssist", "系统错误!")).body(null);
        }
    }

    @GetMapping("/getBatchInfo")
    @ApiOperation(value = "协催页面获取分配信息", notes = "协催页面获取分配信息")
    public ResponseEntity<BatchDistributeModel> getBatchInfo(@RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get batch info");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "getBatchInfo", e.getMessage())).body(null);
        }
        try {
            // 查询部门下所有外访的用户
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(QUser.user.status.eq(0));//启用用户
            builder.and(QUser.user.type.eq(User.Type.VISIT.getValue()));//外访
            builder.and(QUser.user.department.code.startsWith(user.getDepartment().getCode()));  //登录人部门下
            Iterable<User> all = userRepository.findAll(builder);
            List<User> userList = IterableUtils.toList(all);
            Integer avgCaseNum = 0; //人均案件数
            Integer caseNum = 0; //登录用户部门下的所有启用用户持有未结案案件总数
            List<BatchInfoModel> batchInfoModels = new ArrayList<>();
            if (!Objects.equals(userList.size(),0)) {
                for (User u : userList) {
                    BatchInfoModel batchInfoModel = new BatchInfoModel();
                    Integer caseCount = caseInfoRepository.getCaseCount(u.getId());
                    batchInfoModel.setCaseCount(caseCount); //持有案件数
                    batchInfoModel.setCollectionUser(u); //催收人
                    batchInfoModels.add(batchInfoModel);
                    caseNum = caseNum + caseCount;
                }
                avgCaseNum = (caseNum % userList.size() == 0) ? caseNum / userList.size() : (caseNum / userList.size() + 1);
            }
            BatchDistributeModel batchDistributeModel = new BatchDistributeModel();
            batchDistributeModel.setAverageNum(avgCaseNum);
            batchDistributeModel.setBatchInfoModelList(batchInfoModels);
            return ResponseEntity.ok().body(batchDistributeModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "getBatchInfo", "系统异常!")).body(null);
        }
    }


    @PostMapping("/batchCaseAssist")
    @ApiOperation(value = "协催页面批量分配", notes = "协催页面批量分配")
    public ResponseEntity<Void> batchCaseAssist(@RequestBody AssistCaseBatchModel assistCaseBatchModel,
                                                @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to batch assist case");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "batchCaseAssist", e.getMessage())).body(null);
        }
        try {
            List<String> assistIds = assistCaseBatchModel.getAssistIds();
            List<CaseAssist> all = caseAssistRepository.findAll(assistIds); //要分配的所有协催案件
            List<BatchInfoModel> batchInfoModelList = assistCaseBatchModel.getBatchInfoModelList();//分配信息
            List<CaseAssist> caseAssistList = new ArrayList<>();
            List<CaseTurnRecord> caseTurnRecordList = new ArrayList<>();
            int total = 0;
            for (BatchInfoModel batchInfoModel : batchInfoModelList) {
                Integer caseCount = batchInfoModel.getDistributionCount(); //分配案件数
                if (0 == caseCount) {
                    continue;
                }
                if (!Objects.equals(batchInfoModel.getCollectionUser().getType(), User.Type.VISIT.getValue())) { //接收人不为外访
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "batchCaseAssist", "协催案件不能分配给外访以外的人员!")).body(null);
                }
                for (int i = 0; i < caseCount; i++) {
                    //修改协催案件和原案件
                    CaseAssist caseAssist = all.get(total);
                    caseAssistService.distributeCaseAssist(caseAssist,user,batchInfoModel,caseAssistList,caseTurnRecordList);
                    total++;
                }
            }
            caseAssistRepository.save(caseAssistList);
            caseTurnRecordRepository.save(caseTurnRecordList);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("分配成功", "")).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "batchCaseAssist", "系统异常!")).body(null);
        }
    }

    @PostMapping("/assignCaseAssist")
    @ApiOperation(value = "协催案件分配", notes = "协催案件分配")
    public ResponseEntity assignCaseAssist(@RequestBody AssignAssistParam assignAssistParam,
                                           @RequestHeader(value = "X-UserToken") String token) {
        log.debug("Rest request to assignCaseAssist");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "assignCaseAssist", e.getMessage())).body(null);
        }
        try {
            CaseAssist caseAssist = caseAssistRepository.findOne(assignAssistParam.getCaseAssistId());
            User assistor = userRepository.findOne(assignAssistParam.getAssistorId());

            if (!Objects.equals(assistor.getType(), User.Type.VISIT.getValue())) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "getBatchInfo", assistor.getUserName().concat("不是外访人员"))).body(null);
            }
            // 协催分配
            ReDistributionParams reDistributionParams = new ReDistributionParams();
            reDistributionParams.setCaseId(caseAssist.getCaseId().getId());//案件ID
            reDistributionParams.setUserName(assistor.getUserName()); //协催员用户名
            reDistributionParams.setIsAssist(true);
            try {
                caseInfoService.reDistribution(reDistributionParams, user);
            } catch (final Exception e) {
                log.error(e.getMessage(), e);
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "assignCaseAssist", e.getMessage())).body(null);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("分配成功", "")).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "assignCaseAssist", "系统错误!")).body(null);
        }
    }

    @GetMapping("/getAllRecordAssistCase")
    @ApiOperation(value = "多条件查询协催已处理记录", notes = "多条件查询协催已处理记录")
    public ResponseEntity<Page<CaseAssist>> getAllRecordAssistCase(@QuerydslPredicate(root = CaseAssist.class) Predicate predicate,
                                                                   @ApiIgnore Pageable pageable,
                                                                   @RequestHeader(value = "X-UserToken") String token,
                                                                   @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode) {
        log.debug("REST request to getAllRecordAssistCase");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "findAllCaseAssist", e.getMessage())).body(null);
        }
        try {
            QCaseAssist qCaseAssist = QCaseAssist.caseAssist;
            BooleanBuilder exp = new BooleanBuilder(predicate);
            // 超级管理员 权限
            if(Objects.isNull(user.getCompanyCode())){
                if(Objects.nonNull(companyCode)){
                    exp.and(qCaseAssist.companyCode.eq(companyCode));
                }
            }else{
                exp.and(qCaseAssist.companyCode.eq(user.getCompanyCode()));
            }
            exp.and(qCaseAssist.assistStatus.eq(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())); //协催完成
            Page<CaseAssist> page = null;
            if (Objects.equals(user.getManager(), 0) && Objects.equals(user.getType(), User.Type.VISIT.getValue())) {
                // 协催员智能看见自己的协催案件
                exp.and(qCaseAssist.assistCollector.userName.eq(user.getUserName()));
            }
            page = caseAssistRepository.findAll(exp, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "getAllRecordAssistCase", "系统错误!")).body(null);
        }
    }

    @GetMapping("/findAllCaseAssist")
    @ApiOperation(value = "获取所有协催案件", notes = "获取所有协催案件")
    public ResponseEntity<Page<CaseAssist>> findAllCaseAssist(@QuerydslPredicate(root = CaseAssist.class) Predicate predicate,
                                                              @ApiIgnore Pageable pageable,
                                                              @RequestHeader(value = "X-UserToken") String token,
                                                              @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode) {
        log.debug("Rest request to findAllCaseAssist");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "findAllCaseAssist", e.getMessage())).body(null);
        }
        try {

            QCaseAssist qCaseAssist = QCaseAssist.caseAssist;
            BooleanBuilder exp = new BooleanBuilder(predicate);
            // 超级管理员 权限
            if(Objects.isNull(user.getCompanyCode())){
                if(Objects.nonNull(companyCode)){
                    exp.and(qCaseAssist.companyCode.eq(companyCode));
                }
            }else{
                exp.and(qCaseAssist.companyCode.eq(user.getCompanyCode()));
            }
            Page<CaseAssist> page = null;
            exp.and(qCaseAssist.assistStatus.notIn(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()));
            if (Objects.equals(user.getManager(), 0) && Objects.equals(user.getType(), User.Type.VISIT.getValue())) {
                // 协催员智能看见自己的协催案件
                exp.and(qCaseAssist.assistCollector.userName.eq(user.getUserName()));
            }
            page = caseAssistRepository.findAll(exp, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "findAllCaseAssist", "系统错误!")).body(null);
        }
    }


    //    @PutMapping("/receiveCaseAssist/{id}")
//    @ApiOperation(value = "协催案件抢单", notes = "协催案件抢单")
    public ResponseEntity receiveCaseAssist(@PathVariable("id") @ApiParam("协催案件ID") String id,
                                            @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("Rest request to receiveCaseAssist");
        User user = getUserByToken(token);

        try {
            CaseAssist caseAssist = caseAssistRepository.findOne(id);
            if (Objects.isNull(caseAssist)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "receiveCaseAssist", "该协催案件不存在!")).body(null);
            }
            // 若案件状态为 协催待分配 则可以抢单
            if (Objects.equals(caseAssist.getAssistStatus(), CaseInfo.AssistStatus.ASSIST_WAIT_ASSIGN.getValue())) {
                synchronized (this) {
                    CaseAssist caseAssist1 = caseAssistRepository.findOne(id);
                    if (Objects.equals(caseAssist1.getAssistStatus(), CaseInfo.AssistStatus.ASSIST_WAIT_ASSIGN.getValue())) {
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

}
