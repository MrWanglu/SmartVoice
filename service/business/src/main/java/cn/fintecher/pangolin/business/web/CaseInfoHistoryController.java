package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.DeleteCaseParams;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.IterableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by ChenChang on 2017/5/23.
 */
@RestController
@RequestMapping("/api/caseInfoHistoryController")
@Api(value = "CaseInfoHistoryController", description = "结案案件操作")
public class CaseInfoHistoryController extends BaseController {

    private static final String ENTITY_NAME = "CaseInfoHistory";
    private final Logger log = LoggerFactory.getLogger(CaseInfoHistoryController.class);

    @Autowired
    private CaseInfoRepository caseInfoRepository;

    @Inject
    private CaseInfoJudicialRepository caseInfoJudicialRepository;

    @Inject
    private CaseAssistRepository caseAssistRepository;

    @Inject
    private CaseInfoVerificationRepository caseInfoVerificationRepository;

    @Inject
    private OutsourcePoolRepository outsourcePoolRepository;

    @Inject
    private CaseRepairRepository caseRepairRepository;

    @Inject
    private CaseInfoReturnRepository caseInfoReturnRepository;

    @Inject
    private CaseInfoHistoryRepository caseInfoHistoryRepository;

    @PostMapping("/deleteReturnCase")
    @ApiOperation(value = "回收案件删除", notes = "回收案件删除")
    public ResponseEntity deleteReturnCase(@RequestBody DeleteCaseParams params,
                                           @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            if (Objects.isNull(params.getIds()) || params.getIds().isEmpty()) {
                throw new RuntimeException("请选择要删除的案件");
            }

            List<CaseInfo> caseInfoList = new ArrayList<>();
            List<CaseAssist> assistList = new ArrayList<>();
            List<CaseInfoJudicial> judicialList = new ArrayList<>();
            List<CaseInfoVerification> verificationList = new ArrayList<>();
            List<OutsourcePool> outsourcePoolList = new ArrayList<>();
            List<CaseRepair> caseRepairList = new ArrayList<>();
            List<CaseInfoHistory> caseInfoHistoryList = new ArrayList<>();
            List<CaseInfoReturn> all = caseInfoReturnRepository.findAll(params.getIds());
            for (CaseInfoReturn caseInfoReturn : all) {
                CaseInfo caseInfo = caseInfoReturn.getCaseId();
                caseInfoList.add(caseInfo);
                CaseInfoHistory history = new CaseInfoHistory();
                saveHistory(caseInfo, history, caseInfoHistoryList, user);
                deleteCaseInfo(caseInfo, assistList, judicialList, verificationList, outsourcePoolList, caseRepairList);
            }
            caseInfoHistoryRepository.save(caseInfoHistoryList);
            caseAssistRepository.delete(assistList);
            caseInfoJudicialRepository.delete(judicialList);
            caseInfoVerificationRepository.delete(verificationList);
            outsourcePoolRepository.delete(outsourcePoolList);
            caseRepairRepository.delete(caseRepairList);
            caseInfoRepository.delete(caseInfoList);
            caseInfoReturnRepository.delete(all);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("删除成功", "")).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "删除失败")).body(null);
        }
    }

    @PostMapping("/deleteInnerCase")
    @ApiOperation(value = "内催已结案案件删除", notes = "内催已结案案件删除")
    public ResponseEntity deleteInnerCase(@RequestBody DeleteCaseParams params,
                                          @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            if (Objects.isNull(params.getIds()) || params.getIds().isEmpty()) {
                throw new RuntimeException("请选择要删除的案件");
            }

            List<CaseInfo> caseInfoList = new ArrayList<>();
            List<CaseAssist> assistList = new ArrayList<>();
            List<CaseInfoJudicial> judicialList = new ArrayList<>();
            List<CaseInfoVerification> verificationList = new ArrayList<>();
            List<OutsourcePool> outsourcePoolList = new ArrayList<>();
            List<CaseRepair> caseRepairList = new ArrayList<>();
            List<CaseInfoHistory> caseInfoHistoryList = new ArrayList<>();
            List<CaseInfo> all = caseInfoRepository.findAll(params.getIds());
            caseInfoList.addAll(all);
            for (CaseInfo caseInfo : all) {
                CaseInfoHistory history = new CaseInfoHistory();
                saveHistory(caseInfo, history, caseInfoHistoryList, user);
                deleteCaseInfo(caseInfo, assistList, judicialList, verificationList, outsourcePoolList, caseRepairList);
            }
            caseInfoHistoryRepository.save(caseInfoHistoryList);
            caseAssistRepository.delete(assistList);
            caseInfoJudicialRepository.delete(judicialList);
            caseInfoVerificationRepository.delete(verificationList);
            outsourcePoolRepository.delete(outsourcePoolList);
            caseRepairRepository.delete(caseRepairList);
            caseInfoRepository.delete(caseInfoList);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("删除成功", "")).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "删除失败")).body(null);
        }
    }

    @PostMapping("/deleteOuterCase")
    @ApiOperation(value = "委外已结案案件删除", notes = "委外已结案案件删除")
    public ResponseEntity deleteOuterCase(@RequestBody DeleteCaseParams params,
                                          @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            if (Objects.isNull(params.getIds()) || params.getIds().isEmpty()) {
                throw new RuntimeException("请选择要删除的案件");
            }

            List<CaseInfo> caseInfoList = new ArrayList<>();
            List<CaseAssist> assistList = new ArrayList<>();
            List<CaseInfoJudicial> judicialList = new ArrayList<>();
            List<CaseInfoVerification> verificationList = new ArrayList<>();
            List<OutsourcePool> outsourcePoolList = new ArrayList<>();
            List<CaseRepair> caseRepairList = new ArrayList<>();
            List<CaseInfoHistory> caseInfoHistoryList = new ArrayList<>();
            List<OutsourcePool> all = outsourcePoolRepository.findAll(params.getIds());
            for (OutsourcePool pool : all) {
                CaseInfo caseInfo = pool.getCaseInfo();
                caseInfoList.add(caseInfo);
                CaseInfoHistory history = new CaseInfoHistory();
                saveHistory(caseInfo, history, caseInfoHistoryList, user);
                deleteCaseInfo(caseInfo, assistList, judicialList, verificationList, outsourcePoolList, caseRepairList);
            }
            caseInfoHistoryRepository.save(caseInfoHistoryList);
            caseAssistRepository.delete(assistList);
            caseInfoJudicialRepository.delete(judicialList);
            caseInfoVerificationRepository.delete(verificationList);
            outsourcePoolRepository.delete(outsourcePoolList);
            caseRepairRepository.delete(caseRepairList);
            caseInfoRepository.delete(caseInfoList);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("删除成功", "")).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "删除失败")).body(null);
        }
    }

    private void saveHistory(CaseInfo caseInfo, CaseInfoHistory history, List<CaseInfoHistory> caseInfoHistoryList, User user) {
        BeanUtils.copyProperties(caseInfo, history);
        history.setId(null);
        history.setCaseId(caseInfo.getId());
        history.setOperator(user);
        history.setOperatorTime(new Date());
        caseInfoHistoryList.add(history);
    }

    private void deleteCaseInfo(CaseInfo caseInfo, List<CaseAssist> assistList, List<CaseInfoJudicial> judicialList,
                                List<CaseInfoVerification> verificationList, List<OutsourcePool> outsourcePoolList,
                                List<CaseRepair> caseRepairList) {
        String id = caseInfo.getId();
        // 删除协催案件
        QCaseAssist qCaseAssist = QCaseAssist.caseAssist;
        Iterable<CaseAssist> assists = caseAssistRepository.findAll(qCaseAssist.caseId.id.eq(id));
        List<CaseAssist> caseAssistList = IterableUtils.toList(assists);
        assistList.addAll(caseAssistList);
        // 删除核销
        QCaseInfoVerification qCaseInfoVerification = QCaseInfoVerification.caseInfoVerification;
        Iterable<CaseInfoVerification> verifications = caseInfoVerificationRepository.findAll(qCaseInfoVerification.caseInfo.id.eq(id));
        List<CaseInfoVerification> caseInfoVerifications = IterableUtils.toList(verifications);
        verificationList.addAll(caseInfoVerifications);
        // 删除司法
        QCaseInfoJudicial qCaseInfoJudicial = QCaseInfoJudicial.caseInfoJudicial;
        Iterable<CaseInfoJudicial> judicials = caseInfoJudicialRepository.findAll(qCaseInfoJudicial.caseInfo.id.eq(id));
        List<CaseInfoJudicial> caseInfoJudicials = IterableUtils.toList(judicials);
        judicialList.addAll(caseInfoJudicials);
        // 删除委外
        QOutsourcePool qOutsourcePool = QOutsourcePool.outsourcePool;
        Iterable<OutsourcePool> outsourcePools = outsourcePoolRepository.findAll(qOutsourcePool.caseInfo.id.eq(id));
        List<OutsourcePool> pools = IterableUtils.toList(outsourcePools);
        outsourcePoolList.addAll(pools);
        // 删除修复
        QCaseRepair qcaseRepair = QCaseRepair.caseRepair;
        Iterable<CaseRepair> caseRepairs = caseRepairRepository.findAll(qcaseRepair.caseId.id.eq(id));
        List<CaseRepair> repairs = IterableUtils.toList(caseRepairs);
        caseRepairList.addAll(repairs);
    }
}
