package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.*;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.business.service.CaseInfoService;
import cn.fintecher.pangolin.business.service.CaseInfoVerificationService;
import cn.fintecher.pangolin.business.service.FollowRecordExportService;
import cn.fintecher.pangolin.business.utils.ExcelExportHelper;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.entity.strategy.CaseStrategy;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import cn.fintecher.pangolin.web.ResponseUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.swagger.annotations.*;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import javax.inject.Inject;
import java.io.*;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by ChenChang on 2017/5/23.
 */
@RestController
@RequestMapping("/api/caseInfoController")
@Api(value = "CaseInfoController", description = "案件操作")
public class CaseInfoController extends BaseController {

    private static final String ENTITY_NAME = "caseInfo";
    private static final String ENTITY_CASEINFO_RETURN = "CaseInfoReturn";
    private static final String ENTITY_CASEINFO_REMARK = "CaseInfoRemark";
    private final Logger log = LoggerFactory.getLogger(CaseInfoController.class);
    private final CaseInfoRepository caseInfoRepository;

    @Inject
    private CaseInfoService caseInfoService;
    @Inject
    private CaseFollowupRecordRepository caseFollowupRecordRepository;
    @Inject
    private CaseTurnRecordRepository caseTurnRecordRepository;
    @Inject
    private RestTemplate restTemplate;
    @Inject
    private FollowRecordExportService followRecordExportService;
    @Inject
    private Configuration freemarkerConfiguration;
    @Inject
    private PersonalRepository personalRepository;
    @Inject
    private CaseInfoFileRepository caseInfoFileRepository;
    @Inject
    private CaseInfoHistoryRepository caseInfoHistoryRepository;
    @Inject
    private CaseInfoReturnRepository caseInfoReturnRepository;
    @Inject
    private CaseInfoVerificationService caseInfoVerificationService;
    @Inject
    private CaseInfoVerificationApplyRepository caseInfoVerificationApplyRepository;
    @Inject
    private CaseInfoRemarkRepository caseInfoRemarkRepository;

    public CaseInfoController(CaseInfoRepository caseInfoRepository) {
        this.caseInfoRepository = caseInfoRepository;
    }


    @PostMapping("/caseInfo")
    public ResponseEntity<CaseInfo> createCaseInfo(@RequestBody CaseInfo caseInfo) throws URISyntaxException {
        log.debug("REST request to save caseInfo : {}", caseInfo);
        if (caseInfo.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "新增案件不应该含有ID")).body(null);
        }
        CaseInfo result = caseInfoRepository.save(caseInfo);
        return ResponseEntity.created(new URI("/api/caseInfo/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    @PutMapping("/caseInfo")
    public ResponseEntity<CaseInfo> updateCaseInfo(@RequestBody CaseInfo caseInfo) throws URISyntaxException {
        log.debug("REST request to update CaseInfo : {}", caseInfo);
        if (caseInfo.getId() == null) {
            return createCaseInfo(caseInfo);
        }
        CaseInfo result = caseInfoRepository.save(caseInfo);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, caseInfo.getId().toString()))
                .body(result);
    }

    @GetMapping("/caseInfo")
    public List<CaseInfo> getAllCaseInfo() {
        log.debug("REST request to get all CaseInfo");
        List<CaseInfo> caseInfoList = caseInfoRepository.findAll();
        return caseInfoList;
    }

    @GetMapping("/queryCaseInfo")
    public ResponseEntity<Page<CaseInfo>> queryCaseInfo(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                        @ApiIgnore Pageable pageable,
                                                        @RequestHeader(value = "X-UserToken") String token) throws Exception {
        User user = getUserByToken(token);
        BooleanBuilder builder = new BooleanBuilder(predicate);
        builder.and(QCaseInfo.caseInfo.department.code.startsWith(""));
        Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/queryCaseInfo");
        return new ResponseEntity<>(page, headers, HttpStatus.OK);
    }

    @GetMapping("/caseInfo/{id}")
    public ResponseEntity<CaseInfo> getCaseInfo(@PathVariable String id) {
        log.debug("REST request to get caseInfo : {}", id);
        CaseInfo caseInfo = caseInfoRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(caseInfo));
    }

    @DeleteMapping("/caseInfo/{id}")
    public ResponseEntity<Void> deleteCaseInfo(@PathVariable String id) {
        log.debug("REST request to delete caseInfo : {}", id);
        caseInfoRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id)).build();
    }

    @GetMapping("/getAllBatchNumber")
    @ApiOperation(value = "获取所有批次号", notes = "获取所有批次号")
    public ResponseEntity<List<String>> getAllBatchNumber(@RequestHeader(value = "X-UserToken") String token,
                                                          @RequestParam(value = "companyCode", required = false) @ApiParam("公司Code") String companyCode) {
        log.debug("REST request to getAllBatchNumber");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "getAllBatchNumber", e.getMessage())).body(null);
        }
        try {
            List<String> distinctByBatchNumber;

            if (Objects.isNull(user.getCompanyCode())) {
                distinctByBatchNumber = caseInfoRepository.findAllDistinctByBatchNumber();
            } else {
                distinctByBatchNumber = caseInfoRepository.findDistinctByBatchNumber(user.getCompanyCode());
            }
            return ResponseEntity.ok().body(distinctByBatchNumber);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "getAllBatchNumber", "系统异常!")).body(null);
        }
    }

    @GetMapping("/getAllCaseInfo")
    @ApiOperation(value = "分页查询案件管理案件", notes = "分页查询案件管理案件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseInfo>> getAllCaseInfo(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                         @ApiIgnore Pageable pageable,
                                                         @RequestHeader(value = "X-UserToken") String token,
                                                         @RequestParam(value = "companyCode", required = false) @ApiParam("公司Code码") String companyCode) {
        log.debug("REST request to getAllCaseInfo");
        Sort.Order personalName = new Sort.Order(Sort.Direction.ASC, "personalInfo.name"); //客户姓名正序
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "getAllCaseInfo", e.getMessage())).body(null);
        }
        try {
            QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
            BooleanBuilder builder = new BooleanBuilder(predicate);
            if (Objects.isNull(user.getCompanyCode())) {
                if (StringUtils.isNotBlank(companyCode)) {
                    builder.and(qCaseInfo.companyCode.eq(companyCode)); //公司
                }
            } else {
                builder.and(qCaseInfo.companyCode.eq(user.getCompanyCode())); //公司
            }
//            builder.and(qCaseInfo.collectionStatus.notIn(CaseInfo.CollectionStatus.CASE_OVER.getValue())); //以结案
//            builder.and(qCaseInfo.endType.notIn(CaseInfo.EndType.JUDGMENT_CLOSED.getValue(),CaseInfo.EndType.OUTSIDE_CLOSED.getValue())); //不查司法、委外的
//            builder.andAnyOf(qCaseInfo.endType.notIn(CaseInfo.EndType.JUDGMENT_CLOSED.getValue(),
//                    CaseInfo.EndType.OUTSIDE_CLOSED.getValue(),
//                    CaseInfo.EndType.CLOSE_CASE.getValue()),
//                    qCaseInfo.endType.isNull());
            List<String> allCaseInfoIds = caseInfoHistoryRepository.findAllCaseInfoIds();
            if (!allCaseInfoIds.isEmpty()) {
                builder.and(qCaseInfo.id.notIn(allCaseInfoIds));
            }
//            if (Objects.equals(user.getManager(), User.MANAGER_TYPE.DATA_AUTH.getValue())) { //管理者
//                builder.and(qCaseInfo.department.code.startsWith(user.getDepartment().getCode()));
//            }
//            if (Objects.equals(user.getManager(), User.MANAGER_TYPE.NO_DATA_AUTH.getValue())) { //不是管理者
//                builder.and(qCaseInfo.currentCollector.eq(user).or(qCaseInfo.assistCollector.eq(user)));
//            }
            pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().and(new Sort(personalName)));
            Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "getAllCaseInfo", "系统异常!")).body(null);
        }
    }

    @PostMapping(value = "/distributeCeaseInfoAgain")
    @ApiOperation(value = "案件重新分配", notes = "案件重新分配")
    public ResponseEntity distributeCeaseInfoAgain(@RequestBody AccCaseInfoDisModel accCaseInfoDisModel,
                                                   @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        log.debug("REST request to distributeCeaseInfoAgain");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "distributeCeaseInfoAgain", e.getMessage())).body(null);
        }
        try {
            caseInfoService.distributeCeaseInfoAgain(accCaseInfoDisModel, user);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", ENTITY_NAME)).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "errorMessage", e.getMessage())).body(null);
        }
    }

    @PostMapping(value = "/distributePreview")
    @ApiOperation(value = "内催待分配预览", notes = "内催待分配预览")
    public ResponseEntity<List<CaseInfoInnerDistributeModel>> distributePreview(@RequestBody AccCaseInfoDisModel accCaseInfoDisModel,
                                                                                @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        log.debug("REST request to distributeCeaseInfo");
        try {
            List<CaseInfoInnerDistributeModel> list = caseInfoService.distributePreview(accCaseInfoDisModel);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", ENTITY_NAME)).body(list);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "errorMessage", e.getMessage())).body(null);
        }
    }

    @PostMapping(value = "/distributeCaseInfo")
    @ApiOperation(value = "内催待分配案件分配", notes = "内催待分配案件分配")
    public ResponseEntity distributeCaseInfo(@RequestBody AccCaseInfoDisModel accCaseInfoDisModel,
                                             @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        log.debug("REST request to distributeCeaseInfo");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "distributeCeaseInfoAgain", e.getMessage())).body(null);
        }
        try {
            caseInfoService.distributeCeaseInfo(accCaseInfoDisModel, user);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", ENTITY_NAME)).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "errorMessage", e.getMessage())).body(null);
        }
    }

    @GetMapping("/getCaseInfoDetails")
    @ApiOperation(value = "案件详情查询操作", notes = "案件详情查询操作")
    public ResponseEntity<CaseInfo> getCaseInfoDetails(@RequestParam("id") String id) {
        CaseInfo caseInfo = caseInfoRepository.findOne(id);
        return ResponseEntity.ok().body(caseInfo);
    }

    @GetMapping("/getTelCaseInfo")
    @ApiOperation(value = "分页查询电催案件", notes = "分页查询电催案件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseInfo>> getTelCaseInfo(
            @ApiIgnore Pageable pageable,
            @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to getAllCaseInfo");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "getAllCaseInfo", e.getMessage())).body(null);
        }
        try {
            QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
            BooleanBuilder builder = new BooleanBuilder();
            Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "getAllCaseInfo", "系统异常!")).body(null);
        }
    }

    @GetMapping("/getCaseInfoFollowRecord")
    @ApiOperation(value = "案件跟进记录", notes = "案件跟进记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseFollowupRecord>> getCaseInfoFollowRecord(@QuerydslPredicate(root = CaseFollowupRecord.class) Predicate predicate,
                                                                            @ApiIgnore Pageable pageable,
                                                                            @RequestParam("caseNumber") @ApiParam("案件ID") String caseNumber) {
        QCaseFollowupRecord qCaseFollowupRecord = QCaseFollowupRecord.caseFollowupRecord;
        BooleanBuilder builder = new BooleanBuilder(predicate);
        builder.and(qCaseFollowupRecord.caseNumber.eq(caseNumber));
        builder.and(QCaseFollowupRecord.caseFollowupRecord.collectionWay.eq(1)); //只查催记方式为手动的
        Page<CaseFollowupRecord> page = caseFollowupRecordRepository.findAll(builder, pageable);
        return ResponseEntity.ok().body(page);
    }

    @GetMapping("/getCaseInfoTurnRecord")
    @ApiOperation(value = "案件流转记录", notes = "案件流转记录")
    public ResponseEntity<List<CaseTurnRecord>> getCaseInfoTurnRecord(@QuerydslPredicate(root = CaseTurnRecord.class) Predicate predicate,
                                                                      @RequestParam("caseId") @ApiParam("案件ID") String caseId) {
        QCaseTurnRecord qCaseTurnRecord = QCaseTurnRecord.caseTurnRecord;
        BooleanBuilder builder = new BooleanBuilder(predicate);
        builder.and(qCaseTurnRecord.caseId.eq(caseId));
        Iterable<CaseTurnRecord> all = caseTurnRecordRepository.findAll(builder);
        List<CaseTurnRecord> caseTurnRecords = IterableUtils.toList(all);
        return ResponseEntity.ok().body(caseTurnRecords);
    }

    @GetMapping("/electricSmallCirculation")
    @ApiOperation(value = "电催小流转池", notes = "电催小流转池")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity electricSmallCirculation(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                   @ApiIgnore Pageable pageable,
                                                   @RequestHeader(value = "X-UserToken") String token,
                                                   @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode) {
        User user;
        try {
            user = getUserByToken(token);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "User not exists", e.getMessage())).body(null);
        }
        BooleanBuilder booleanBuilder = new BooleanBuilder(predicate);
        if (Objects.isNull(user.getCompanyCode())) {
            if (Objects.nonNull(companyCode)) {
                booleanBuilder.and(QCaseInfo.caseInfo.companyCode.eq(companyCode));
            }
        } else {
            booleanBuilder.and(QCaseInfo.caseInfo.companyCode.eq(user.getCompanyCode()));
        }
        List<Integer> list = new ArrayList<>();
        list.add(CaseInfo.CaseType.PHNONESMALLTURN.getValue());
        list.add(CaseInfo.CaseType.PHNONEFAHEADTURN.getValue());
        booleanBuilder.and(QCaseInfo.caseInfo.caseType.in(list));
        Page<CaseInfo> page = caseInfoRepository.findAll(booleanBuilder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "electricSmallCirculation")).body(page);
    }

    @GetMapping("/electricForceCirculation")
    @ApiOperation(value = "电催强制流转池", notes = "电催强制流转池")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity electricForceCirculation(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                   @ApiIgnore Pageable pageable,
                                                   @RequestHeader(value = "X-UserToken") String token,
                                                   @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode) {
        User user;
        BooleanBuilder booleanBuilder = new BooleanBuilder(predicate);
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "User not exists", e.getMessage())).body(null);
        }
        if (Objects.isNull(user.getCompanyCode())) {
            if (Objects.nonNull(companyCode)) {
                booleanBuilder.and(QCaseInfo.caseInfo.companyCode.eq(companyCode));
            }
        } else {
            booleanBuilder.and(QCaseInfo.caseInfo.companyCode.eq(user.getCompanyCode()));
        }
        List<Integer> list = new ArrayList<>();
        list.add(CaseInfo.CaseType.PHNONEFORCETURN.getValue());
        list.add(CaseInfo.CaseType.PHNONELEAVETURN.getValue());
        booleanBuilder.and(QCaseInfo.caseInfo.caseType.in(list));
        Page<CaseInfo> page = caseInfoRepository.findAll(booleanBuilder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "electricSmallCirculation")).body(page);
    }

    @GetMapping("/outSmallCirculation")
    @ApiOperation(value = "外访小流转池", notes = "外访小流转池")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity outSmallCirculation(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                              @ApiIgnore Pageable pageable,
                                              @RequestHeader(value = "X-UserToken") String token,
                                              @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "User not exists", e.getMessage())).body(null);
        }

        BooleanBuilder booleanBuilder = new BooleanBuilder(predicate);
        if (Objects.isNull(user.getCompanyCode())) {
            if (Objects.nonNull(companyCode)) {
                booleanBuilder.and(QCaseInfo.caseInfo.companyCode.eq(companyCode));
            }
        } else {
            booleanBuilder.and(QCaseInfo.caseInfo.companyCode.eq(user.getCompanyCode()));
        }
        List<Integer> list = new ArrayList<>();
        list.add(CaseInfo.CaseType.OUTSMALLTURN.getValue());
        list.add(CaseInfo.CaseType.OUTFAHEADTURN.getValue());
        booleanBuilder.and(QCaseInfo.caseInfo.caseType.in(list));
        Page<CaseInfo> page = caseInfoRepository.findAll(booleanBuilder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "outSmallCirculation")).body(page);
    }

    @GetMapping("/outForceCirculation")
    @ApiOperation(value = "外访强制流转池", notes = "外访强制流转池")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity outForceCirculation(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                              @ApiIgnore Pageable pageable,
                                              @RequestHeader(value = "X-UserToken") String token,
                                              @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "User not exists", e.getMessage())).body(null);
        }
        BooleanBuilder booleanBuilder = new BooleanBuilder(predicate);
        if (Objects.isNull(user.getCompanyCode())) {
            if (Objects.nonNull(companyCode)) {
                booleanBuilder.and(QCaseInfo.caseInfo.companyCode.eq(companyCode));
            }
        } else {
            booleanBuilder.and(QCaseInfo.caseInfo.companyCode.eq(user.getCompanyCode()));
        }
        List<Integer> list = new ArrayList<>();
        list.add(CaseInfo.CaseType.OUTFORCETURN.getValue());
        list.add(CaseInfo.CaseType.OUTLEAVETURN.getValue());
        booleanBuilder.and(QCaseInfo.caseInfo.caseType.in(list));
        Page<CaseInfo> page = caseInfoRepository.findAll(booleanBuilder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "outForceCirculation")).body(page);
    }

    @PostMapping("/exportCaseInfoFollowRecord")
    @ApiOperation(value = "导出跟进记录", notes = "导出跟进记录")
    public ResponseEntity exportCaseInfoFollowRecord(@RequestBody ExportCaseNum exportCaseNum,
                                                     @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        XSSFWorkbook workbook = null;
        File file = null;
        ByteArrayOutputStream out = null;
        FileOutputStream fileOutputStream = null;

        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "exportCaseInfoFollowRecord", e.getMessage())).body(null);
        }
        try {
            if (Objects.isNull(user.getCompanyCode())) {
                if (StringUtils.isNotBlank(exportCaseNum.getCompanyCode())) {
                    user.setCompanyCode(exportCaseNum.getCompanyCode());
                }
            }
            List<String> caseNumberList = exportCaseNum.getCaseNumberList();
            if (caseNumberList.isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "请选择案件!")).body(null);
            }
            List<Object[]> caseFollowupRecords = caseFollowupRecordRepository.findFollowup(caseNumberList, user.getCompanyCode());
            if (caseFollowupRecords.isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "", "要导出的跟进记录数据为空!")).body(null);
            }
            if (caseFollowupRecords.size() > 10000) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "", "不支持导出数据超过10000条!")).body(null);
            }
            workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("sheet1");
            out = new ByteArrayOutputStream();
            Map<String, String> head = followRecordExportService.createHead();
            List<Map<String, Object>> data = followRecordExportService.createData(caseFollowupRecords);
            ExcelExportHelper.createExcel(workbook, sheet, head, data, 0, 0);
            workbook.write(out);
            String filePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(DateTime.now().toString("yyyyMMddhhmmss") + "跟进记录.xlsx");
            file = new File(filePath);
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(out.toByteArray());
            FileSystemResource resource = new FileSystemResource(file);
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.add("file", resource);
            ResponseEntity<String> url = restTemplate.postForEntity("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class);
            if (url == null) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "exportCaseInfoFollowRecord", "系统错误!")).body(null);
            } else {
                return ResponseEntity.ok().body(url.getBody());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "exportCaseInfoFollowRecord", "上传文件服务器失败")).body(null);
        } finally {
            // 关闭流
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 删除文件
            if (file != null) {
                file.delete();
            }
        }
    }

    @GetMapping("/exportFollowRecord")
    @ApiOperation(value = "导出跟进记录(单案件)", notes = "导出跟进记录(单案件)")
    public ResponseEntity exportFollowRecord(@QuerydslPredicate(root = CaseFollowupRecord.class) Predicate predicate,
                                             @RequestParam(value = "companyCode", required = false) @ApiParam("公司Code") String companyCode,
                                             @RequestParam("caseNumber") @ApiParam("案件编号") String caseNumber,
                                             @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        XSSFWorkbook workbook = null;
        File file = null;
        ByteArrayOutputStream out = null;
        FileOutputStream fileOutputStream = null;

        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "exportCaseInfoFollowRecord", e.getMessage())).body(null);
        }

        try {
            if (Objects.isNull(user.getCompanyCode())) {
                if (StringUtils.isNotBlank(companyCode)) {
                    user.setCompanyCode(companyCode);
                }
            }
            List<Object[]> caseFollowupRecords = caseFollowupRecordRepository.findFollowupSingl(caseNumber, user.getCompanyCode());
            if (caseFollowupRecords.isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "exportCaseInfoFollowRecord", "跟进记录数据为空!")).body(null);
            }
            if (caseFollowupRecords.size() > 10000) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "", "不支持导出数据超过10000条!")).body(null);
            }
            workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("sheet1");
            out = new ByteArrayOutputStream();
            Map<String, String> head = followRecordExportService.createHead();
            List<Map<String, Object>> data = followRecordExportService.createData(caseFollowupRecords);
            ExcelExportHelper.createExcel(workbook, sheet, head, data, 0, 0);
            workbook.write(out);
            String filePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(DateTime.now().toString("yyyyMMddhhmmss") + "跟进记录.xlsx");
            file = new File(filePath);
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(out.toByteArray());
            FileSystemResource resource = new FileSystemResource(file);
            MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
            param.add("file", resource);
            ResponseEntity<String> url = restTemplate.postForEntity("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class);
            if (url == null) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "exportCaseInfoFollowRecord", "系统错误!")).body(null);
            } else {
                return ResponseEntity.ok().body(url.getBody());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseinfo", ex.getMessage())).body(null);
        } finally {
            // 关闭流
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 删除文件
            if (file != null) {
                file.delete();
            }
        }
    }


    /**
     * @Description : 查询共债案件
     */

    @GetMapping("/queryCaseInfoList")
    @ApiOperation(value = "查询共债案件", notes = "查询共债案件")
    public ResponseEntity<Page<CaseInfo>> queryCaseInfoList(@RequestParam String companyCode,
                                                            @RequestParam(required = false) String id,
                                                            @ApiIgnore Pageable pageable,
                                                            @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }

        CaseInfo caseInfo = caseInfoRepository.findOne(id);
        QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
        BooleanBuilder builder = new BooleanBuilder();
        if (Objects.nonNull(companyCode)) {
            builder.and(qCaseInfo.companyCode.eq(companyCode));
        }
        if (Objects.nonNull(caseInfo.getPersonalInfo())) {
            builder.and(qCaseInfo.personalInfo.name.eq(caseInfo.getPersonalInfo().getName()).and(qCaseInfo.personalInfo.idCard.eq(caseInfo.getPersonalInfo().getIdCard())).and(qCaseInfo.id.ne(caseInfo.getId())));
        }
        Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(page);
    }

    @GetMapping("/updateAllScoreStrategyManual")
    @ApiOperation(value = "更新案件评分(手动)", notes = "更新案件评分(手动)")
    public ResponseEntity updateAllScoreStrategyManual(@RequestParam @ApiParam(required = true) Integer strategyType,
                                                       @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            String companyCode = user.getCompanyCode();
            StopWatch watch1 = new StopWatch();
            watch1.start();
            KieSession kieSession = null;
            try {
                kieSession = createSorceRule(companyCode, strategyType);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage())).body(null);
            }
            Iterable<CaseInfo> caseInfoList = caseInfoRepository.findAll(QCaseInfo.caseInfo.collectionStatus.in(CaseInfo.CollectionStatus.COLLECTIONING.getValue(),
                    CaseInfo.CollectionStatus.EARLY_PAYING.getValue(),
                    CaseInfo.CollectionStatus.OVER_PAYING.getValue(),
                    CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue(),
                    CaseInfo.CollectionStatus.PART_REPAID.getValue(),
                    CaseInfo.CollectionStatus.WAITCOLLECTION.getValue())
                    .and(QCaseInfo.caseInfo.companyCode.eq(user.getCompanyCode())));
            List<CaseInfo> accCaseInfoList = new ArrayList<>();
            List<CaseInfo> caseInfoList1 = new ArrayList<>();
            caseInfoList.forEach(single -> accCaseInfoList.add(single));
            ScoreNumbersModel scoreNumbersModel = new ScoreNumbersModel();
            scoreNumbersModel.setTotal(accCaseInfoList.size());
            if (accCaseInfoList.size() > 0) {
                for (CaseInfo caseInfo : accCaseInfoList) {
                    ScoreRuleModel scoreRuleModel = new ScoreRuleModel();
                    int age = caseInfo.getPersonalInfo().getAge();
                    scoreRuleModel.setAge(age);
                    scoreRuleModel.setOverDueAmount(caseInfo.getOverdueAmount().doubleValue());
                    scoreRuleModel.setOverDueDays(caseInfo.getOverdueDays());
                    scoreRuleModel.setProId(caseInfo.getArea().getId());//省份id
                    Personal personal = personalRepository.findOne(caseInfo.getPersonalInfo().getId());
                    if (Objects.nonNull(personal) && Objects.nonNull(personal.getPersonalJobs())) {
                        scoreRuleModel.setIsWork(1);
                    } else {
                        scoreRuleModel.setIsWork(0);
                    }
                    kieSession.insert(scoreRuleModel);//插入
                    kieSession.fireAllRules();//执行规则
                    caseInfo.setScore(new BigDecimal(scoreRuleModel.getCupoScore()));
                    caseInfoList1.add(caseInfo);
                }
                kieSession.dispose();
                caseInfoRepository.save(caseInfoList1);
                watch1.stop();
                log.info("耗时：" + watch1.getTotalTimeMillis());
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("评分完成", "success")).body(scoreNumbersModel);
            }
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseinfo", "failure", "案件为空")).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "exportCaseInfoFollowRecord", "上传文件服务器失败")).body(null);
        }
    }

    /**
     * 动态生成规则
     *
     * @return
     * @throws IOException
     * @throws
     */
    private KieSession createSorceRule(String comanyCode, Integer strategyType) {
        try {
            Template scoreFormulaTemplate = freemarkerConfiguration.getTemplate("scoreFormula.ftl", "UTF-8");
            Template scoreRuleTemplate = freemarkerConfiguration.getTemplate("scoreRule.ftl", "UTF-8");
            ResponseEntity<ScoreRules> responseEntity = restTemplate.getForEntity(Constants.SCOREL_SERVICE_URL.concat("getScoreRules").concat("?comanyCode=").concat(comanyCode).concat("&strategyType=").concat(strategyType.toString()), ScoreRules.class);
            List<ScoreRule> rules = null;
            if (responseEntity.hasBody()) {
                ScoreRules scoreRules = responseEntity.getBody();
                rules = scoreRules.getScoreRules();
            }
            StringBuilder sb = new StringBuilder();
            if (Objects.nonNull(rules)) {
                for (ScoreRule rule : rules) {
                    for (int i = 0; i < rule.getFormulas().size(); i++) {
                        ScoreFormula scoreFormula = rule.getFormulas().get(i);
                        Map<String, String> map = new HashMap<>();
                        map.put("id", rule.getId());
                        map.put("index", String.valueOf(i));
                        map.put("strategy", scoreFormula.getStrategy());
                        map.put("score", String.valueOf(scoreFormula.getScore()));
                        map.put("weight", String.valueOf(rule.getWeight()));
                        sb.append(FreeMarkerTemplateUtils.processTemplateIntoString(scoreFormulaTemplate, map));
                    }
                }
            }
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem kfs = kieServices.newKieFileSystem();
            Map<String, String> map = new HashMap<>();
            map.put("allRules", sb.toString());
            String text = FreeMarkerTemplateUtils.processTemplateIntoString(scoreRuleTemplate, map);
            kfs.write("src/main/resources/simple.drl",
                    kieServices.getResources().newReaderResource(new StringReader(text)));
            KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();
            Results results = kieBuilder.getResults();
            if (results.hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
                log.error(results.getMessages().toString());
                throw new IllegalStateException("策略生成错误");
            }
            KieContainer kieContainer =
                    kieServices.newKieContainer(kieBuilder.getKieModule().getReleaseId());
            KieSession kieSession = kieContainer.newKieSession();
            return kieSession;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("策略生成失败");
        }
    }

    @GetMapping("/findUpload")
    @ApiOperation(value = "查看附件", notes = "查看附件")
    public ResponseEntity<List<CaseInfoFile>> findUpload(@RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token,
                                                         @RequestParam(value = "caseNumber", required = true) @ApiParam("案件编号") String caseNumber,
                                                         @RequestParam(value = "companyCode", required = false) String companyCode) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        Iterable<CaseInfoFile> all = caseInfoFileRepository.findAll(QCaseInfoFile.caseInfoFile.caseNumber.eq(caseNumber));
        List<CaseInfoFile> caseInfoFiles = IterableUtils.toList(all);
        return ResponseEntity.ok().body(caseInfoFiles);
    }

    /**
     * @Description 查看凭证
     */
    @GetMapping("/getFollowupFile")
    @ApiOperation(value = "下载凭证", notes = "下载凭证")
    public ResponseEntity<List<UploadFile>> getFollowupFile(@RequestParam @ApiParam(value = "跟进记录ID") String followId) {
        log.debug("REST request to get flowup file");
        try {
            List<UploadFile> caseFlowupFiles = caseInfoService.getFollowupFile(followId);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("下载成功", "")).body(caseFlowupFiles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("下载失败", "uploadFile", "下载失败")).body(null);
        }
    }

    /**
     * @Description 案件查找
     */
    @GetMapping("/findCaseInfo")
    @ApiOperation(value = "案件查找", notes = "案件查找")
    public ResponseEntity<Page<CaseInfo>> findCaseInfo(@RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
                                                       @QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                       @ApiIgnore Pageable pageable,
                                                       @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to find case");
        try {
            User tokenUser = getUserByToken(token);
            BooleanBuilder builder = new BooleanBuilder(predicate);
            if (Objects.isNull(tokenUser.getCompanyCode())) { //超级管理员
                if (Objects.nonNull(companyCode)) {
                    builder.and(QCaseInfo.caseInfo.companyCode.eq(companyCode));
                }
            } else { //不是超级管理员
                builder.and(QCaseInfo.caseInfo.companyCode.eq(tokenUser.getCompanyCode()));
            }
            Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/CaseInfoController/findCaseInfo");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", "查询失败")).body(null);
        }
    }

    /**
     * @Description 修改备注
     */
    @PostMapping("/modifyCaseMemo")
    @ApiOperation(value = "修改备注", notes = "修改备注")
    public ResponseEntity<Void> modifyCaseMemo(@RequestBody ModifyMemoParams modifyMemoParams,
                                               @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to modify case memo");
        try {
            User tokenUser = getUserByToken(token);
            caseInfoService.modifyCaseMemo(modifyMemoParams, tokenUser);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("修改成功", ENTITY_NAME)).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseInfo", e.getMessage())).body(null);
        }
    }

    /**
     * @Description 案件退案
     */
    @PostMapping("/returnCase")
    @ApiOperation(value = "案件退案", notes = "案件退案")
    public ResponseEntity<Void> returnCase(@RequestBody ReturnCaseParams returnCaseParams,
                                           @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to return case");
        try {
            User tokenUser = getUserByToken(token);
            caseInfoService.returnCase(returnCaseParams, tokenUser);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("退案成功", ENTITY_CASEINFO_RETURN)).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASEINFO_RETURN, "caseInfoReturn", e.getMessage())).body(null);
        }
    }

    /**
     * @Description 多条件查询退案案件
     */
    @GetMapping("/getAllCaseInfoReturn")
    @ApiOperation(value = "多条件查询退案案件", notes = "多条件查询退案案件")
    public ResponseEntity<Page<CaseInfoReturn>> getAllCaseInfoReturn(@RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
                                                                     @QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                                     @ApiIgnore Pageable pageable,
                                                                     @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get all case info return");
        Sort.Order operatorTimeSort = new Sort.Order(Sort.Direction.DESC, "operatorTime", Sort.NullHandling.NULLS_LAST); //操作时间倒序
        Sort.Order personalNameSort = new Sort.Order(Sort.Direction.ASC, "caseId.personalInfo.name", Sort.NullHandling.NULLS_LAST); //客户姓名正序
        try {
            User tokenUser = getUserByToken(token);
            BooleanBuilder builder = new BooleanBuilder(predicate);
            if (Objects.isNull(tokenUser.getCompanyCode())) { //超级管理员
                if (Objects.nonNull(companyCode)) {
                    builder.and(QCaseInfoReturn.caseInfoReturn.caseId.companyCode.eq(companyCode));
                }
            } else { //不是超级管理员
                builder.and(QCaseInfoReturn.caseInfoReturn.caseId.companyCode.eq(tokenUser.getCompanyCode()));
            }
            pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort().and(new Sort(operatorTimeSort)).and(new Sort(personalNameSort)));
            Page<CaseInfoReturn> page = caseInfoReturnRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/CaseInfoController/getAllCaseInfoReturn");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASEINFO_RETURN, "caseInfoReturn", "查询失败")).body(null);
        }
    }

    /**
     * @Description 多条件查询备注信息
     */
    @GetMapping("/getCaseInfoRemark")
    @ApiOperation(value = "查询备注信息", notes = "查询备注信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseInfoRemark>> getCaseInfoRemark(@RequestParam @ApiParam(value = "案件ID", required = true) String caseId,
                                                                  @QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                                  @ApiIgnore Pageable pageable) {
        log.debug("REST request to get case info remark");
        try {
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(QCaseInfoRemark.caseInfoRemark.caseId.eq(caseId)); //案件ID
            Page<CaseInfoRemark> page = caseInfoRemarkRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/caseInfoController/getCaseInfoRemark");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASEINFO_REMARK, "caseInfoRemark", "查询失败")).body(null);
        }
    }

    @PostMapping("/moveToDistribution")
    @ApiOperation(value = "移入待分配案件池", notes = "移入待分配案件池")
    public ResponseEntity moveToDistribution(@RequestHeader(value = "X-UserToken") String token,
                                             @RequestBody @ApiParam(value = "案件ID集合", required = true) CaseInfoIdList caseIds) {
        log.debug("REST request to moveToDistribution");
        try {
            User user = getUserByToken(token);
            caseInfoService.moveToDistribution(caseIds, user);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功!", "")).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage())).body(null);
        }
    }

    /**
     * @Description 内催按批次号查询催收中案件
     */
    @GetMapping("/getCollectingCase")
    @ApiOperation(value = "内催按批次号查询催收中案件", notes = "内催按批次号查询催收中案件")
    public ResponseEntity<Page<CaseInfo>> getCollectingCase(@RequestHeader(value = "X-UserToken") String token,
                                                            @ApiIgnore Pageable pageable,
                                                            @RequestParam @ApiParam(value = "批次号", required = true) String batchNumber) {
        log.debug("REST request to get case info remark");
        try {
            User tokenUser = getUserByToken(token);
            List<Integer> status = new ArrayList<>();
            status.add(CaseInfo.CollectionStatus.COLLECTIONING.getValue());
            status.add(CaseInfo.CollectionStatus.OVER_PAYING.getValue());
            status.add(CaseInfo.CollectionStatus.EARLY_PAYING.getValue());
            status.add(CaseInfo.CollectionStatus.PART_REPAID.getValue());
            status.add(CaseInfo.CollectionStatus.REPAID.getValue());
            status.add(CaseInfo.CollectionStatus.WAITCOLLECTION.getValue());
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(QCaseInfo.caseInfo.batchNumber.eq(batchNumber));
            if (Objects.nonNull(tokenUser.getCompanyCode())) {
                builder.and(QCaseInfo.caseInfo.companyCode.eq(tokenUser.getCompanyCode()));
            }
            builder.and(QCaseInfo.caseInfo.casePoolType.eq(CaseInfo.CasePoolType.INNER.getValue()));
            builder.and(QCaseInfo.caseInfo.department.code.startsWith(tokenUser.getDepartment().getCode()));
            builder.andAnyOf(QCaseInfo.caseInfo.collectionStatus.in(status), QCaseInfo.caseInfo.collectionStatus.eq(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue())
                    .and(QCaseInfo.caseInfo.department.isNotNull()));
            Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", "")).body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "查询失败")).body(null);
        }
    }

    @GetMapping("/findAllCaseInfoReturn")
    @ApiOperation(notes = "内催案件管理查询回收案件", value = "内催案件管理查询回收案件")
    public ResponseEntity<Page<CaseInfoReturn>> findAllCaseInfoReturn(@RequestHeader(value = "X-UserToken") String token,
                                                                      @ApiIgnore Pageable pageable,
                                                                      @RequestParam(value = "companyCode", required = false) @ApiParam("公司Code") String companyCode,
                                                                      @QuerydslPredicate(root = CaseInfoReturn.class) Predicate predicate) {
        try {
            User user = getUserByToken(token);
            QCaseInfoReturn qCaseInfoReturn = QCaseInfoReturn.caseInfoReturn;
            BooleanBuilder builder = new BooleanBuilder(predicate);
            if (Objects.isNull(user.getCompanyCode())) {
                if (StringUtils.isNotBlank(companyCode)) {
                    builder.and(qCaseInfoReturn.caseId.companyCode.eq(companyCode));//公司
                }
            } else {
                builder.and(qCaseInfoReturn.caseId.companyCode.eq(user.getCompanyCode()));//公司
            }
            builder.and(qCaseInfoReturn.caseId.casePoolType.eq(CaseInfo.CasePoolType.INNER.getValue()));//内催
            builder.and(qCaseInfoReturn.caseId.department.code.startsWith(user.getDepartment().getCode()));//部门下
            Page<CaseInfoReturn> all = caseInfoReturnRepository.findAll(builder, pageable);
            return ResponseEntity.ok().body(all);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "查询失败")).body(null);
        }
    }

    @PostMapping("/verifyApply")
    @ApiOperation(notes = "内催案件管理案件回收核销申请", value = "内催案件管理案件回收核销申请")
    public ResponseEntity verifyApply(@RequestHeader(value = "X-UserToken") String token,
                                      @RequestBody @ApiParam(value = "回收案件ID集合", required = true) VerificationApplyModel model) {
        try {
            User user = getUserByToken(token);
            if (Objects.isNull(model.getIds()) || model.getIds().isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请选择要核销的案件!")).body(null);
            }
            BooleanBuilder builder = new BooleanBuilder();
            QCaseInfoReturn qCaseInfoReturn = QCaseInfoReturn.caseInfoReturn;
            builder.and(qCaseInfoReturn.id.in(model.getIds()));
            Iterable<CaseInfoReturn> all = caseInfoReturnRepository.findAll(builder);
            Iterator<CaseInfoReturn> iterator = all.iterator();
            while (iterator.hasNext()) {
                CaseInfoReturn next = iterator.next();
                CaseInfoVerificationApply apply = new CaseInfoVerificationApply();
                caseInfoVerificationService.setVerificationApply(apply, next.getCaseId(), user, model.getReason());
                caseInfoVerificationApplyRepository.save(apply);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("申请成功!", "")).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "申请失败!")).body(null);
        }
    }

    @GetMapping("/getInnerWaitCollectCase")
    @ApiOperation(value = "分页查询内催待分配案件", notes = "分页查询内催待分配案件")
    public ResponseEntity<Page<CaseInfo>> getInnerWaitCollectCase(
            @ApiIgnore Pageable pageable,
            @RequestHeader(value = "X-UserToken") String token,
            @RequestParam(value = "companyCode", required = false) @ApiParam("公司Code") String companyCode) {
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "", e.getMessage())).body(null);
        }
        try {
            QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
            BooleanBuilder builder = new BooleanBuilder();
            if (Objects.isNull(user.getCompanyCode())) {
                if (StringUtils.isNotBlank(companyCode)) {
                    builder.and(qCaseInfo.companyCode.eq(companyCode)); //公司
                }
            } else {
                builder.and(qCaseInfo.companyCode.eq(user.getCompanyCode())); //公司
            }
            builder.and(qCaseInfo.department.isNull());
            builder.and(qCaseInfo.collectionStatus.eq(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue()));
            builder.and(qCaseInfo.casePoolType.eq(CaseInfo.CasePoolType.INNER.getValue()));
            Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "", "查询失败")).body(null);
        }
    }

    @GetMapping("/getInnerOverCase")
    @ApiOperation(value = "分页查询内催结案案件", notes = "分页查询内催结案案件")
    public ResponseEntity<Page<CaseInfo>> getInnerOverCase(
            @ApiIgnore Pageable pageable,
            @RequestHeader(value = "X-UserToken") String token,
            @RequestParam(value = "companyCode", required = false) @ApiParam("公司Code") String companyCode) {
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "", e.getMessage())).body(null);
        }
        try {
            QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
            BooleanBuilder builder = new BooleanBuilder();
            if (Objects.isNull(user.getCompanyCode())) {
                if (StringUtils.isNotBlank(companyCode)) {
                    builder.and(qCaseInfo.companyCode.eq(companyCode)); //公司
                }
            } else {
                builder.and(qCaseInfo.companyCode.eq(user.getCompanyCode())); //公司
            }
            builder.and(qCaseInfo.department.code.startsWith(user.getDepartment().getCode()));
            builder.and(qCaseInfo.collectionStatus.eq(CaseInfo.CollectionStatus.CASE_OVER.getValue()));
            builder.and(qCaseInfo.casePoolType.eq(CaseInfo.CasePoolType.INNER.getValue()));
            Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "", "查询失败")).body(null);
        }
    }

    /**
     * 内催案件 待分配案件 策略分配
     */
    @GetMapping("/innerStrategyDistribute")
    @ApiOperation(value = "内催案件 待分配案件 策略分配", notes = "内催案件 待分配案件 策略分配")
    public ResponseEntity<List<CaseInfo>> innerStrategyDistribute(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                                  @RequestHeader(value = "X-UserToken") String token,
                                                                  @ApiParam(value = "所有的待分配案件ID集合") CaseDistributeInfoModel caseDistributeInfoModel) {
        User user = null;
        try {
            user = getUserByToken(token);
            ParameterizedTypeReference<List<CaseStrategy>> responseType = new ParameterizedTypeReference<List<CaseStrategy>>() {
            };
            ResponseEntity<List<CaseStrategy>> caseStrategies = restTemplate.exchange(Constants.CASE_STRATEGY_URL
                    .concat("companyCode=").concat(user.getCompanyCode())
                    .concat("&strategyType=").concat(CaseStrategy.StrategyType.INNER.getValue().toString()), HttpMethod.GET, null, responseType);
            if (Objects.isNull(caseStrategies) || !caseStrategies.hasBody() || caseStrategies.getBody().size() == 0) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "", "没有分配策略信息")).body(null);
            }
            if (Objects.isNull(caseDistributeInfoModel) || Objects.isNull(caseDistributeInfoModel.getCaseIdList()) || caseDistributeInfoModel.getCaseIdList().isEmpty()) {
                //没有勾选案件,分配所有的案件
                QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
                BooleanBuilder booleanBuilder = new BooleanBuilder(predicate);
                booleanBuilder.and(qCaseInfo.collectionStatus.eq(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue()));
                booleanBuilder.and(qCaseInfo.casePoolType.eq(CaseInfo.CasePoolType.INNER.getValue()));
                Iterable<CaseInfo> caseInfos = caseInfoRepository.findAll(booleanBuilder);
                if (!caseInfos.iterator().hasNext()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "", "没有待分配的案件信息")).body(null);
                }
                caseInfoService.innerStrategyDistribute(caseStrategies.getBody(), IterableUtils.toList(caseInfos), user);
            } else {
                //分配勾选的案件
                QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
                BooleanBuilder booleanBuilder = new BooleanBuilder(predicate);
                booleanBuilder.and(qCaseInfo.collectionStatus.eq(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue()));
                booleanBuilder.and(qCaseInfo.casePoolType.eq(CaseInfo.CasePoolType.INNER.getValue()));
                booleanBuilder.and(qCaseInfo.id.in(caseDistributeInfoModel.getCaseIdList()));
                Iterable<CaseInfo> caseInfos = caseInfoRepository.findAll(booleanBuilder);
                if (!caseInfos.iterator().hasNext()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "", "没有待分配的案件信息")).body(null);
                }
                caseInfoService.innerStrategyDistribute(caseStrategies.getBody(), IterableUtils.toList(caseInfos), user);
            }
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "", e.getMessage())).body(null);
        }
        try {
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "", "分配失败")).body(null);
        }
    }

    /**
     * @Description 查询公债案件数量
     */
    @GetMapping("/getCommonCaseCount")
    @ApiOperation(value = "查询公债案件数量", notes = "查询公债案件数量")
    public ResponseEntity<Integer> getCommonCaseCount(@RequestParam @ApiParam(value = "案件ID", required = true) String caseId) {
        try {
            Integer count = caseInfoService.getCommonCaseCount(caseId);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", "")).body(count);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", e.getMessage())).body(null);
        }
    }
}


