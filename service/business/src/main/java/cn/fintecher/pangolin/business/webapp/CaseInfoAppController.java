package cn.fintecher.pangolin.business.webapp;

import cn.fintecher.pangolin.business.model.LeaveCaseModel;
import cn.fintecher.pangolin.business.model.LeaveCaseParams;
import cn.fintecher.pangolin.business.model.MapModel;
import cn.fintecher.pangolin.business.repository.CaseAssistRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.business.service.AccMapService;
import cn.fintecher.pangolin.business.service.CaseAssistService;
import cn.fintecher.pangolin.business.service.CaseInfoService;
import cn.fintecher.pangolin.business.web.BaseController;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.MapUtil;
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
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.util.*;

import static cn.fintecher.pangolin.entity.QCaseInfo.caseInfo;

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
    @Inject
    SysParamRepository sysParamRepository;
    @Inject
    CaseAssistService caseAssistService;

    @GetMapping("/queryAssistDetail")
    @ApiOperation(value = "协催案件查询", notes = "协催案件查询")
    public ResponseEntity<Page<CaseAssist>> getAssistDetail(@QuerydslPredicate(root = CaseAssist.class) Predicate predicate,
                                                            Pageable pageable,
                                                            @RequestHeader(value = "X-UserToken") String token,
                                                            @RequestParam(required = false) @ApiParam(value = "客户名称") String name,
                                                            @RequestParam(required = false) @ApiParam(value = "地址") String address) throws Exception {
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        if (!Objects.equals(user.getType(), User.Type.VISIT.getValue())
                && !Objects.equals(user.getType(), User.Type.SYNTHESIZE.getValue())) {
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", "")).body(null);
        }
        BooleanBuilder builder = new BooleanBuilder(predicate);
        builder.and(QCaseAssist.caseAssist.companyCode.eq(user.getCompanyCode()));
        if (Objects.equals(user.getManager(), User.MANAGER_TYPE.DATA_AUTH.getValue())) {
            builder.and(QCaseAssist.caseAssist.assistCollector.department.code.startsWith(user.getDepartment().getCode()));
        } else {
            builder.and(QCaseAssist.caseAssist.assistCollector.id.eq(user.getId()));
        }
        builder.and(QCaseAssist.caseAssist.assistStatus.eq(CaseInfo.AssistStatus.ASSIST_COLLECTING.getValue()));
        if (Objects.nonNull(name)) {
            builder.and(QCaseAssist.caseAssist.caseId.personalInfo.name.contains(name));
        }
        if (Objects.nonNull(address)) {
            builder.and(QCaseAssist.caseAssist.caseId.personalInfo.localHomeAddress.contains(address));
        }
        Page<CaseAssist> page = caseAssistRepository.findAll(builder, pageable);
        for (int i = 0; i < page.getContent().size(); i++) {
            CaseAssist caseAssist = page.getContent().get(i);
            if (Objects.isNull(caseAssist.getCaseId().getPersonalInfo().getLongitude())
                    || Objects.isNull(caseAssist.getCaseId().getPersonalInfo().getLatitude())) {
                try {
                    MapModel model = accMapService.getAddLngLat(caseAssist.getCaseId().getPersonalInfo().getLocalHomeAddress());
                    caseAssist.getCaseId().getPersonalInfo().setLatitude(BigDecimal.valueOf(model.getLatitude()));
                    caseAssist.getCaseId().getPersonalInfo().setLongitude(BigDecimal.valueOf(model.getLongitude()));
                } catch (Exception e1) {
                    e1.getMessage();
                }
            }
        }
        caseAssistRepository.save(page);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/queryAssistDetail");
        return new ResponseEntity<>(page, headers, HttpStatus.OK);
    }

    @GetMapping("/queryVisitDetail")
    @ApiOperation(value = "外访案件查询", notes = "外访案件查询")
    public ResponseEntity<Page<CaseInfo>> getVisitDetail(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                         Pageable pageable,
                                                         @RequestHeader(value = "X-UserToken") String token,
                                                         @RequestParam(required = false) @ApiParam(value = "客户名称") String name,
                                                         @RequestParam(required = false) @ApiParam(value = "地址") String address) throws Exception {
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        if (!Objects.equals(user.getType(), User.Type.VISIT.getValue())
                && !Objects.equals(user.getType(), User.Type.SYNTHESIZE.getValue())) {
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", "")).body(null);
        }
        List<Integer> status = new ArrayList<>();
        status.add(CaseInfo.CollectionStatus.COLLECTIONING.getValue());
        status.add(CaseInfo.CollectionStatus.PART_REPAID.getValue());
        status.add(CaseInfo.CollectionStatus.EARLY_PAYING.getValue());
        status.add(CaseInfo.CollectionStatus.OVER_PAYING.getValue());
        status.add(CaseInfo.CollectionStatus.REPAID.getValue());
        BooleanBuilder builder = new BooleanBuilder(predicate);
        builder.and(QCaseInfo.caseInfo.collectionType.eq(CaseInfo.CollectionType.VISIT.getValue()));
        builder.and(QCaseInfo.caseInfo.companyCode.eq(user.getCompanyCode()));
        if (Objects.equals(user.getManager(), User.MANAGER_TYPE.DATA_AUTH.getValue())) {
            builder.and(QCaseInfo.caseInfo.currentCollector.department.code.startsWith(user.getDepartment().getCode()));
        } else {
            builder.and(QCaseInfo.caseInfo.currentCollector.id.eq(user.getId()));
        }
        builder.and(QCaseInfo.caseInfo.collectionStatus.in(status));
        builder.and(QCaseInfo.caseInfo.caseType.in(CaseInfo.CaseType.DISTRIBUTE.getValue(), CaseInfo.CaseType.OUTLEAVETURN.getValue()));
        if (Objects.nonNull(name)) {
            builder.and(QCaseInfo.caseInfo.personalInfo.name.contains(name));
        }
        if (Objects.nonNull(address)) {
            builder.and(QCaseInfo.caseInfo.personalInfo.localHomeAddress.contains(address));
        }
        Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
        page.forEach(e -> {
            if (Objects.isNull(e.getPersonalInfo().getLongitude())
                    || Objects.isNull(e.getPersonalInfo().getLatitude())) {
                try {
                    MapModel model = accMapService.getAddLngLat(e.getPersonalInfo().getLocalHomeAddress());
                    e.getPersonalInfo().setLatitude(BigDecimal.valueOf(model.getLatitude()));
                    e.getPersonalInfo().setLongitude(BigDecimal.valueOf(model.getLongitude()));
                } catch (Exception e1) {
                    e1.getMessage();
                }
            }
        });
        caseInfoRepository.save(page);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/queryVisitDetail");
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

    @PostMapping("/nearbyCase")
    @ApiOperation(value = "附近协催抢单", notes = "附近协催抢单")
    public ResponseEntity<Page<CaseAssist>> nearbyCase(@RequestBody MapModel model,
                                                       @RequestHeader(value = "X-UserToken") String token,
                                                       Pageable pageable) {
        log.debug("REST request to apply payment");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        BooleanBuilder exp = new BooleanBuilder();
        exp.and(QSysParam.sysParam.code.eq(Constants.SYS_QIANGDAN_RADIUS));
        exp.and(QSysParam.sysParam.companyCode.eq(user.getCompanyCode()));
        exp.and(QSysParam.sysParam.status.eq(SysParam.StatusEnum.Start.getValue()));
        double radius = Double.valueOf(sysParamRepository.findOne(exp).getValue());
        Map<String, Double> resultMap = MapUtil.computeOrigin4Position(model.getLongitude(), model.getLatitude(), radius);
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QCaseAssist.caseAssist.companyCode.eq(user.getCompanyCode()));
        builder.and(QCaseAssist.caseAssist.assistWay.eq(CaseAssist.AssistWay.ONCE_ASSIST.getValue()));
        builder.and(QCaseAssist.caseAssist.assistStatus.eq(CaseInfo.AssistStatus.ASSIST_WAIT_ASSIGN.getValue()));
        builder.and(QCaseAssist.caseAssist.caseId.personalInfo.latitude.between(BigDecimal.valueOf(resultMap.get("minlat")), BigDecimal.valueOf(resultMap.get("maxlat"))));
        builder.and(QCaseAssist.caseAssist.caseId.personalInfo.longitude.between(BigDecimal.valueOf(resultMap.get("minlng")), BigDecimal.valueOf(resultMap.get("maxlng"))));
        Page<CaseAssist> page = caseAssistRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("附近案件查询成功", "CaseAssist")).body(page);
    }

    @PostMapping("/nearbyOwnCase")
    @ApiOperation(value = "附近协催", notes = "附近协催")
    public ResponseEntity<Page<CaseAssist>> nearbyOwnCase(@RequestBody MapModel model,
                                                          @RequestHeader(value = "X-UserToken") String token,
                                                          Pageable pageable) {
        log.debug("REST request to apply payment");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        BooleanBuilder exp = new BooleanBuilder();
        exp.and(QSysParam.sysParam.code.eq(Constants.SYS_QIANGDAN_RADIUS));
        exp.and(QSysParam.sysParam.companyCode.eq(user.getCompanyCode()));
        exp.and(QSysParam.sysParam.status.eq(SysParam.StatusEnum.Start.getValue()));
        Double radius = Double.valueOf(sysParamRepository.findOne(exp).getValue());
        Map<String, Double> resultMap = MapUtil.computeOrigin4Position(model.getLongitude(), model.getLatitude(), radius);
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QCaseAssist.caseAssist.companyCode.eq(user.getCompanyCode()));
        builder.and(QCaseAssist.caseAssist.assistWay.eq(CaseAssist.AssistWay.ONCE_ASSIST.getValue()));
        builder.and(QCaseAssist.caseAssist.assistCollector.id.eq(user.getId()));
        builder.and(QCaseAssist.caseAssist.assistStatus.ne(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()));
        builder.and(QCaseAssist.caseAssist.caseId.personalInfo.latitude.between(resultMap.get("minlng"), resultMap.get("maxlng")));
        builder.and(QCaseAssist.caseAssist.caseId.personalInfo.longitude.between(resultMap.get("minlat"), resultMap.get("maxlat")));
        Page<CaseAssist> page = caseAssistRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("附近案件查询成功", "CaseAssist")).body(page);
    }

    @GetMapping("/receiveCaseAssist")
    @ApiOperation(value = "协催案件抢单", notes = "协催案件抢单")
    public ResponseEntity receiveCaseAssist(@RequestParam @ApiParam(value = "协催案件ID", required = true) String id,
                                            @RequestHeader(value = "X-UserToken") String token) throws Exception {
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        try {
            caseInfoService.receiveCaseAssist(id, user);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", e.getMessage())).body(null);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("抢单成功", "CaseAssist")).body(null);
    }


    @GetMapping("/getPersonalCase")
    @ApiOperation(value = "客户查询", notes = "客户查询（分页、条件）")
    public ResponseEntity<Page<CaseInfo>> getPersonalCase(@RequestParam(required = false) @ApiParam(value = "客户名称") String name,
                                                          @RequestParam(required = false) @ApiParam(value = "地址") String address,
                                                          @ApiIgnore Pageable pageable,
                                                          @RequestHeader(value = "X-UserToken") String token) {
        try {
            User tokenUser = getUserByToken(token);
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(QCaseInfo.caseInfo.companyCode.eq(tokenUser.getCompanyCode()));
            builder.andAnyOf(QCaseInfo.caseInfo.collectionType.eq(CaseInfo.CollectionType.VISIT.getValue()),
                    QCaseInfo.caseInfo.assistCollector.isNotNull());
            builder.and(QCaseInfo.caseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue()));
            if (Objects.nonNull(name)) {
                builder.and(QCaseInfo.caseInfo.personalInfo.name.startsWith(name));
            }
            if (Objects.nonNull(address)) {
                builder.and(QCaseInfo.caseInfo.personalInfo.localHomeAddress.contains(address));
            }
            Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/caseInfoAppController/getPersonalCase");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", "CaseInfo", e.getMessage())).body(null);
        }
    }

    @GetMapping("/leaveCaseAssistForApp")
    @ApiOperation(value = "协催案件留案操作", notes = "协催案件留案操作")
    public ResponseEntity leaveCaseAssistForApp(@RequestParam @ApiParam(value = "案件ID", required = true) String caseId,
                                                @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to leave case");
        User user = null;
        try {
            user = getUserByToken(token);
            QCaseInfo qCaseInfo = caseInfo;
            Integer num = caseAssistService.leaveCaseAssistNum(user);
            CaseAssist one = caseAssistRepository.findOne(QCaseAssist.caseAssist.caseId.id.eq(caseId).and(QCaseAssist.caseAssist.assistStatus.ne(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())));
            if (Objects.isNull(one)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseAssistController", "caseInfo", "所选案件未找到!")).body(null);
            }
            if (Objects.nonNull(one.getAssistCollector()) && !Objects.equals(one.getAssistCollector().getId(), user.getId())) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseAssistController", "caseInfo", "只能对自己所持有的案件进行留案操作!")).body(null);
            }
            if (Objects.equals(one.getLeaveCaseFlag(), CaseInfo.leaveCaseFlagEnum.YES_LEAVE.getValue())) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseAssistController", "caseInfo", "所选案件存在已经留案的案件!")).body(null);
            }
            if (Objects.equals(one.getAssistWay(), CaseAssist.AssistWay.ONCE_ASSIST.getValue())) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseAssistController", "caseInfo", "单次协催的案件不允许留案!")).body(null);
            }
            if (num < 1) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseAssistController", "caseInfo", "所选案件数量超过可留案案件数!")).body(null);
            }
            one.setLeaveCaseFlag(CaseInfo.leaveCaseFlagEnum.YES_LEAVE.getValue()); //留案标志
            one.setLeaveDate(ZWDateUtil.getNowDateTime()); //留案日期
            one.setHasLeaveDays(0); //留案天数
            one.setOperator(user);
            one.setOperatorTime(ZWDateUtil.getNowDateTime());
            caseAssistRepository.save(one);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("留案成功", "")).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "留案失败")).body(null);
        }
    }

    @GetMapping("/leaveVisitCaseForApp")
    @ApiOperation(value = "外访案件留案操作", notes = "外访案件留案操作")
    public ResponseEntity leaveVisitCaseForApp(@RequestParam @ApiParam(value = "案件ID", required = true) String caseId,
                                               @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to leave case");
        try {
            User user = getUserByToken(token);
            Integer caseNum = caseInfoRepository.getCaseCount(user.getId());
            //查询已留案案件数
            QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
            int flagNum = new Long(caseInfoRepository.count(qCaseInfo.currentCollector.id.eq(user.getId()).and(qCaseInfo.leaveCaseFlag.eq(1)))).intValue();
            //获得留案比例
            QSysParam qSysParam = QSysParam.sysParam;
            SysParam sysParam;
            sysParam = sysParamRepository.findOne(qSysParam.code.eq(Constants.SYS_OUTBOUNDFLOW_LEAVERATE).and(qSysParam.companyCode.eq(user.getCompanyCode())));
            Double rate = Double.parseDouble(sysParam.getValue()) / 100;
            //计算留案案件是否超过比例
            Integer leaveNum = (int) Math.floor(caseNum * rate); //可留案的案件数
            CaseInfo caseInfo = caseInfoRepository.findOne(caseId);
            if (Objects.isNull(caseInfo)) {
                throw new RuntimeException("所选案件未找到");
            }
            if (!Objects.equals(caseInfo.getCurrentCollector().getId(), user.getId())) {
                throw new RuntimeException("只能对自己所持有的案件进行留案操作");
            }
            if (Objects.equals(caseInfo.getLeaveCaseFlag(), 1)) {
                throw new RuntimeException("所选案件存在已经留案的案件");
            }
            if (flagNum >= leaveNum) {
                throw new RuntimeException("所选案件数量超过可留案案件数");
            }
            caseInfo.setLeaveCaseFlag(CaseInfo.leaveCaseFlagEnum.YES_LEAVE.getValue()); //留案标志
            caseInfo.setOperator(user); //操作人
            caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
            caseInfo.setLeaveDate(ZWDateUtil.getNowDateTime()); //留案日期
            caseInfo.setHasLeaveDays(0);
            caseInfoRepository.saveAndFlush(caseInfo);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("留案成功", "")).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "caseInfo", e.getMessage())).body(null);
        }
    }

    @GetMapping("/cancelLeaveAssistCaseForApp")
    @ApiOperation(value = "协催取消留案", notes = "协催取消留案")
    public ResponseEntity<Void> cancelLeaveAssistCaseForApp(@RequestParam @ApiParam(value = "案件ID", required = true) String caseId,
                                                            @RequestHeader(value = "X-UserToken") String token) {
        try {
            User tokenUser = getUserByToken(token);
            CaseAssist caseAssist = caseAssistRepository.findOne(QCaseAssist.caseAssist.caseId.id.eq(caseId)
                    .and(QCaseAssist.caseAssist.assistStatus.ne(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())));
            if (Objects.equals(caseAssist.getLeaveCaseFlag(), CaseInfo.leaveCaseFlagEnum.NO_LEAVE.getValue())) {
                throw new RuntimeException("所选案件为非留案案件");
            }
            if (!Objects.equals(caseAssist.getAssistCollector().getId(), tokenUser.getId())) {
                throw new RuntimeException("只能对自己所持有的案件进行取消留案操作");
            }
            caseAssistService.cancelLeaveCaseAssist(caseAssist, tokenUser);
            caseAssistRepository.save(caseAssist);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("取消留案成功", "")).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", e.getMessage())).body(null);
        }
    }

    @GetMapping("/cancelLeaveVisitCaseForApp")
    @ApiOperation(value = "外访取消留案", notes = "外访取消留案")
    public ResponseEntity<Void> cancelLeaveVisitCaseForApp(@RequestParam @ApiParam(value = "案件ID", required = true) String caseId,
                                                           @RequestHeader(value = "X-UserToken") String token) {
        try {
            User tokenUser = getUserByToken(token);
            CaseInfo caseInfo = caseInfoRepository.findOne(caseId);
            if (Objects.equals(caseInfo.getLeaveCaseFlag(), CaseInfo.leaveCaseFlagEnum.NO_LEAVE.getValue())) {
                throw new RuntimeException("所选案件为非留案案件");
            }
            if (!Objects.equals(caseInfo.getCurrentCollector().getId(), tokenUser.getId())) {
                throw new RuntimeException("只能对自己所持有的案件进行取消留案操作");
            }
            caseInfo.setLeaveCaseFlag(CaseInfo.leaveCaseFlagEnum.NO_LEAVE.getValue());
            caseInfo.setLeaveDate(null);
            caseInfo.setHasLeaveDays(0);
            caseInfo.setOperator(tokenUser);
            caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime());
            caseInfoRepository.save(caseInfo);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("取消留案成功", "")).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", e.getMessage())).body(null);
        }
    }
}

