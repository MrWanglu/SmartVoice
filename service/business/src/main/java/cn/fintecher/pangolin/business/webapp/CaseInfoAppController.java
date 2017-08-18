package cn.fintecher.pangolin.business.webapp;

import cn.fintecher.pangolin.business.model.MapModel;
import cn.fintecher.pangolin.business.repository.CaseAssistRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.service.AccMapService;
import cn.fintecher.pangolin.business.service.CaseInfoService;
import cn.fintecher.pangolin.business.web.BaseController;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import com.querydsl.core.types.Predicate;
import java.util.Objects;

/**
 * @author : gaobeibei
 * @Description : APP案件信息查询
 * @Date : 16:01 2017/7/20
 */
@RestController
@RequestMapping(value = "/api/caseInfoAppController")
@Api(value = "APP催收任务查询", description = "APP催收任务查询")
public class CaseInfoAppController extends BaseController {
    final Logger log = LoggerFactory.getLogger(CaseInfoAppController.class);
    @Inject
    CaseInfoRepository caseInfoRepository;
    @Inject
    CaseAssistRepository caseAssistRepository;
    @Inject
    CaseInfoService caseInfoService;
    @Inject
    AccMapService accMapService;

    @GetMapping("/queryAssistDetail")
    @ApiOperation(value = "协催案件查询", notes = "协催案件查询")
    public ResponseEntity<Page<CaseAssist>> getAssistDetail(@QuerydslPredicate(root = CaseAssist.class) Predicate predicate,
                                                            Pageable pageable,
                                                            @RequestHeader(value = "X-UserToken") String token) throws Exception {
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        BooleanBuilder builder = new BooleanBuilder(predicate);
        builder.and(QCaseAssist.caseAssist.companyCode.eq(user.getCompanyCode()));
        if (user.getManager() == 1) {
            builder.and(QCaseAssist.caseAssist.assistCollector.department.code.startsWith(user.getDepartment().getCode()));
        } else {
            builder.and(QCaseAssist.caseAssist.assistCollector.id.eq(user.getId()));
        }
        builder.and(QCaseAssist.caseAssist.assistStatus.ne(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()));
        builder.and(QCaseAssist.caseAssist.caseId.caseType.eq(CaseInfo.CaseType.DISTRIBUTE.getValue()));
        Page<CaseAssist> page = caseAssistRepository.findAll(builder, pageable);
        page.forEach(e->{
            if(Objects.isNull(e.getCaseId().getPersonalInfo().getLongitude())
                    || Objects.isNull(e.getCaseId().getPersonalInfo().getLatitude())){
                MapModel model = accMapService.getAddLngLat(e.getCaseId().getPersonalInfo().getLocalHomeAddress());
                e.getCaseId().getPersonalInfo().setLatitude(model.getLatitude());
                e.getCaseId().getPersonalInfo().setLongitude(model.getLongitude());
            }
        });
        caseAssistRepository.save(page);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/queryAssistDetail");
        return new ResponseEntity<>(page, headers, HttpStatus.OK);
    }

    @GetMapping("/queryVisitDetail")
    @ApiOperation(value = "外访案件查询", notes = "外访案件查询")
    public ResponseEntity<Page<CaseInfo>> getVisitDetail(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                         Pageable pageable,
                                                         @RequestHeader(value = "X-UserToken") String token) throws Exception {
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        BooleanBuilder builder = new BooleanBuilder(predicate);
        builder.and(QCaseInfo.caseInfo.collectionType.eq(CaseInfo.CollectionType.VISIT.getValue()));
        builder.and(QCaseInfo.caseInfo.companyCode.eq(user.getCompanyCode()));
        builder.and(QCaseInfo.caseInfo.assistFlag.eq(CaseInfo.AssistFlag.NO_ASSIST.getValue()));
        if (user.getManager() == 1) {
            builder.and(QCaseInfo.caseInfo.currentCollector.department.code.startsWith(user.getDepartment().getCode()));
        } else {
            builder.and(QCaseInfo.caseInfo.currentCollector.id.eq(user.getId()));
        }
        builder.and(QCaseInfo.caseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue()));
        builder.and(QCaseInfo.caseInfo.caseType.eq(CaseInfo.CaseType.DISTRIBUTE.getValue()));
        Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
        page.forEach(e->{
            if(Objects.isNull(e.getPersonalInfo().getLongitude())
                    || Objects.isNull(e.getPersonalInfo().getLatitude())){
                MapModel model = accMapService.getAddLngLat(e.getPersonalInfo().getLocalHomeAddress());
                e.getPersonalInfo().setLatitude(model.getLatitude());
                e.getPersonalInfo().setLongitude(model.getLongitude());
            }
        });
        caseInfoRepository.save(page);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/queryVisitDetail");
        return new ResponseEntity<>(page, headers, HttpStatus.OK);
    }

    @GetMapping("/queryCaseDetail")
    @ApiOperation(value = "案件查询", notes = "案件查询")
    public ResponseEntity<Page<CaseInfo>> getCaseDetail(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                        Pageable pageable,
                                                        @RequestHeader(value = "X-UserToken") String token) throws Exception {
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        BooleanBuilder builder = new BooleanBuilder(predicate);
        if (user.getManager() == 1) {
            builder.and(QCaseInfo.caseInfo.department.code.startsWith(user.getDepartment().getCode()));
        } else {
            builder.andAnyOf(QCaseInfo.caseInfo.currentCollector.id.eq(user.getId()), QCaseInfo.caseInfo.assistCollector.id.eq(user.getId()));
        }
        builder.andAnyOf(QCaseInfo.caseInfo.collectionStatus.eq(CaseInfo.CollectionStatus.WAITCOLLECTION.getValue()),
                QCaseInfo.caseInfo.assistStatus.eq(CaseInfo.AssistStatus.ASSIST_WAIT_ACC.getValue()));
        builder.and(QCaseInfo.caseInfo.caseType.eq(CaseInfo.CaseType.DISTRIBUTE.getValue()));
        Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
        page.forEach(e->{
            if(Objects.isNull(e.getPersonalInfo().getLongitude())
                    || Objects.isNull(e.getPersonalInfo().getLatitude())){
                MapModel model = accMapService.getAddLngLat(e.getPersonalInfo().getLocalHomeAddress());
                e.getPersonalInfo().setLatitude(model.getLatitude());
                e.getPersonalInfo().setLongitude(model.getLongitude());
            }
        });
        caseInfoRepository.save(page);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/queryCaseDetail");
        return new ResponseEntity<>(page, headers, HttpStatus.OK);
    }

    @GetMapping("/queryDetail")
    @ApiOperation(value = "通过案件ID查询案件信息", notes = "通过案件ID查询案件信息")
    public ResponseEntity<CaseInfo> queryDetail(@RequestParam @ApiParam(value = "案件ID", required = true) String id) {
        log.debug("REST request to get caseInfo : {}", id);
        CaseInfo caseInfo = caseInfoRepository.findOne(id);
        return ResponseEntity.ok().body(caseInfo);
    }

    @GetMapping("/endCaseAssistForApp")
    @ApiOperation(value = "协催结束", notes = "协催结束")
    public ResponseEntity<Void> endCaseAssist(@RequestParam @ApiParam(value = "协催案件ID", required = true) String caseId,
                                              @RequestHeader(value = "X-UserToken") String token) {
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseAssistController", "endCaseAssist", e.getMessage())).body(null);
        }
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QCaseAssist.caseAssist.caseId.id.eq(caseId));
        builder.andAnyOf(QCaseAssist.caseAssist.assistStatus.eq(CaseInfo.AssistStatus.ASSIST_COLLECTING.getValue()),
                QCaseAssist.caseAssist.assistStatus.eq(CaseInfo.AssistStatus.ASSIST_WAIT_ACC.getValue()));
        CaseAssist caseAssist = caseAssistRepository.findOne(builder);
        if (Objects.isNull(caseAssist)) {
            throw new RuntimeException("该协催未找到");
        }
        caseAssist.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //协催状态 29-协催完成
        caseAssist.setAssistCloseFlag(0);
        caseAssist.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
        caseAssist.setOperator(user); //操作员
        caseAssistRepository.saveAndFlush(caseAssist);
        CaseInfo caseInfo = caseInfoRepository.findOne(caseAssist.getCaseId().getId());
        if (Objects.isNull(caseInfo)) {
            throw new RuntimeException("该案件未找到");
        }
        caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //协催状态 29-协催完成
        caseInfo.setAssistCollector(null); //协催员置空
        caseInfo.setAssistWay(null); //协催方式置空
        caseInfo.setAssistFlag(0); //协催标识 0-否
        caseInfo.setOperator(user); //操作人
        caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
        caseInfoRepository.saveAndFlush(caseInfo);
        return ResponseEntity.ok().body(null);
    }
}
