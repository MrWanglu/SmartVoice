package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.CaseInfoIdList;
import cn.fintecher.pangolin.business.model.ReDisRecoverCaseParams;
import cn.fintecher.pangolin.business.model.RecoverCaseParams;
import cn.fintecher.pangolin.business.model.VerifiyParams;
import cn.fintecher.pangolin.business.repository.CaseAssistRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoJudicialRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoReturnRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoVerificationRepository;
import cn.fintecher.pangolin.business.service.RecoverCaseService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by sunyanping on 2017/9/27.
 */
@RestController
@RequestMapping("/api/caseReturnController")
@Api(value = "CaseReturnController", description = "回收案件操作")
public class CaseReturnController extends BaseController {
    private static final String ENTITY_NAME = "CaseReturnController";
    private final Logger logger = LoggerFactory.getLogger(CaseReturnController.class);

    @Inject
    private RecoverCaseService recoverCaseService;

    @Inject
    private CaseInfoReturnRepository caseInfoReturnRepository;

    @Inject
    private CaseAssistRepository caseAssistRepository;

    @Inject
    private CaseInfoVerificationRepository caseInfoVerificationRepository;

    @Inject
    private CaseInfoJudicialRepository caseInfoJudicialRepository;

    @PostMapping("/recoverCase")
    @ApiOperation(notes = "内催回收案件", value = "内催回收案件")
    public ResponseEntity recoverCase(@RequestHeader(value = "X-UserToken") String token,
                                      @RequestBody RecoverCaseParams recoverCaseParams) {
        try {
            User user = getUserByToken(token);
            recoverCaseService.recoverCase(recoverCaseParams, user);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功!", "")).body(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage())).body(null);
        }
    }

    @PostMapping("/reDisRecoverCase")
    @ApiOperation(notes = "重新分配回收案件", value = "重新分配回收案件")
    public ResponseEntity reDisRecoverCase(@RequestBody ReDisRecoverCaseParams params,
                                           @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            recoverCaseService.reDisRecoverCase(params, user);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功!", "")).body(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage())).body(null);
        }
    }

    @PostMapping("/moveToVerification")
    @ApiOperation(notes = "回收案件移入核销案件池", value = "回收案件移入核销案件池")
    public ResponseEntity moveToVerification(@RequestBody VerifiyParams params,
                                             @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            if (Objects.isNull(params) || params.getIds().isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请选择要移入核销池的案件")).body(null);
            }
            List<CaseInfoReturn> all = caseInfoReturnRepository.findAll(params.getIds());
            List<CaseAssist> caseAssistList = new ArrayList<>();
            List<CaseInfoVerification> verificationList = new ArrayList<>();
            for (CaseInfoReturn caseInfoReturn : all) {
                CaseInfo caseInfo = caseInfoReturn.getCaseId();
                handlingAssist(caseInfo, caseAssistList, user);
                // 结案类型：核销结案
                caseInfo.setEndType(CaseInfo.EndType.CLOSE_CASE.getValue());
                // 催收状态：已结案
                caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.CASE_OVER.getValue());
                // 核销池
                caseInfo.setCasePoolType(CaseInfo.CasePoolType.DESTORY.getValue());
                CaseInfoVerification verification = new CaseInfoVerification();
                verification.setCaseInfo(caseInfo);
                verification.setOperator(user.getRealName());
                verification.setOperatorTime(ZWDateUtil.getNowDateTime());
                verification.setCompanyCode(caseInfo.getCompanyCode());
                // 核销说明
                verification.setState(params.getMemo());
                verification.setPackingStatus(CaseInfoVerification.PackingStatus.NO_PACKED.getValue());
                verificationList.add(verification);
            }
            caseAssistRepository.save(caseAssistList);
            caseInfoVerificationRepository.save(verificationList);
            caseInfoReturnRepository.delete(all);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功!", "")).body(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "移入核销池失败")).body(null);
        }
    }

    @PostMapping("/moveToJudicial")
    @ApiOperation(notes = "回收案件移入司法案件池", value = "回收案件移入司法案件池")
    public ResponseEntity moveToJudicial(@RequestBody CaseInfoIdList params,
                                         @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            if (Objects.isNull(params) || params.getIds().isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请选择要移入司法池的案件")).body(null);
            }
            List<CaseInfoReturn> all = caseInfoReturnRepository.findAll(params.getIds());
            List<CaseAssist> caseAssistList = new ArrayList<>();
            List<CaseInfoJudicial> judicialList = new ArrayList<>();
            for (CaseInfoReturn caseInfoReturn : all) {
                CaseInfo caseInfo = caseInfoReturn.getCaseId();
                handlingAssist(caseInfo, caseAssistList, user);
                // 结案类型：核销结案
                caseInfo.setEndType(CaseInfo.EndType.JUDGMENT_CLOSED.getValue());
                // 催收状态：已结案
                caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.CASE_OVER.getValue());
                // 司法池
                caseInfo.setCasePoolType(CaseInfo.CasePoolType.JUDICIAL.getValue());
                CaseInfoJudicial judicial = new CaseInfoJudicial();
                judicial.setCaseInfo(caseInfo);
                judicial.setOperatorRealName(user.getRealName());
                judicial.setCompanyCode(caseInfo.getCompanyCode());
                judicial.setOperatorTime(ZWDateUtil.getNowDateTime());
                judicial.setOperatorUserName(user.getUserName());
                judicialList.add(judicial);
            }
            caseAssistRepository.save(caseAssistList);
            caseInfoJudicialRepository.save(judicialList);
            caseInfoReturnRepository.delete(all);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功!", "")).body(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "移入司法池失败")).body(null);
        }
    }

    private void handlingAssist(CaseInfo caseInfo, List<CaseAssist> caseAssistList, User user) {
        if (Objects.equals(caseInfo.getAssistFlag(), CaseInfo.AssistFlag.YES_ASSIST.getValue())) {
            CaseAssist caseAssist = caseAssistRepository.findOne(QCaseAssist.caseAssist.caseId.eq(caseInfo)
                    .and(QCaseAssist.caseAssist.assistStatus.eq(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())));
            if (Objects.nonNull(caseAssist)) {
                caseAssist.setAssistCloseFlag(CaseAssist.AssistCloseFlagEnum.MANUAL.getValue());
                caseAssist.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue());
                caseAssist.setOperator(user);
                caseAssist.setOperatorTime(new Date());
                caseAssistList.add(caseAssist);
            }
            caseInfo.setAssistFlag(CaseInfo.AssistFlag.NO_ASSIST.getValue());
            caseInfo.setAssistStatus(null);
            caseInfo.setAssistWay(null);
            caseInfo.setAssistCollector(null);
            caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue());
        }
    }
}
