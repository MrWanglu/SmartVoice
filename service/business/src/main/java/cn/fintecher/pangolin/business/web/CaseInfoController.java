package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.*;
import cn.fintecher.pangolin.business.repository.CaseFollowupRecordRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.CaseTurnRecordRepository;
import cn.fintecher.pangolin.business.repository.PersonalRepository;
import cn.fintecher.pangolin.business.service.CaseInfoService;
import cn.fintecher.pangolin.business.service.FollowRecordExportService;
import cn.fintecher.pangolin.business.utils.ExcelExportHelper;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.IdcardUtils;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import cn.fintecher.pangolin.web.ResponseUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.swagger.annotations.*;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.DateTime;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
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
    public ResponseEntity<List<String>> getAllBatchNumber(@RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to getAllBatchNumber");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "getAllBatchNumber", e.getMessage())).body(null);
        }
        try {
            return ResponseEntity.ok().body(caseInfoRepository.findDistinctByBatchNumber(user.getCompanyCode()));
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
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "getAllCaseInfo", e.getMessage())).body(null);
        }
        // 超级管理员
        if (Objects.isNull(user.getCompanyCode())) {
            if (Objects.nonNull(companyCode)) {
                user.setCompanyCode(companyCode);
            } else {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "请选择公司!")).body(null);
            }
        }
        try {
            QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(qCaseInfo.companyCode.eq(user.getCompanyCode())); //公司
            builder.and(qCaseInfo.collectionStatus.notIn(CaseInfo.CollectionStatus.CASE_OVER.getValue())); //以结案
            builder.and(qCaseInfo.collectionStatus.notIn(CaseInfo.CollectionStatus.CASE_OUT.getValue())); //已委外
            if (Objects.equals(user.getManager(), User.MANAGER_TYPE.DATA_AUTH.getValue())) { //管理者
                builder.and(qCaseInfo.department.code.startsWith(user.getDepartment().getCode()));
            }
            if (Objects.equals(user.getManager(), User.MANAGER_TYPE.NO_DATA_AUTH.getValue())) { //不是管理者
                builder.and(qCaseInfo.currentCollector.eq(user).or(qCaseInfo.assistCollector.eq(user)));
            }
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
        if(Objects.isNull(user.getCompanyCode())){
            if(Objects.isNull(companyCode)){
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("electricSmallCirculation", "", "请选择公司")).body(null);
            }
            booleanBuilder.and(QCaseInfo.caseInfo.companyCode.eq(companyCode));
        }else{
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
        if(Objects.isNull(user.getCompanyCode())){
            if(Objects.isNull(companyCode)){
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("electricSmallCirculation", "", "请选择公司")).body(null);
            }
            booleanBuilder.and(QCaseInfo.caseInfo.companyCode.eq(companyCode));
        }else{
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
        if(Objects.isNull(user.getCompanyCode())){
            if(Objects.isNull(companyCode)){
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("outSmallCirculation", "", "请选择公司")).body(null);
            }
            booleanBuilder.and(QCaseInfo.caseInfo.companyCode.eq(companyCode));
        }else{
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
        if(Objects.isNull(user.getCompanyCode())){
            if(Objects.isNull(companyCode)){
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("outSmallCirculation", "", "请选择公司")).body(null);
            }
            booleanBuilder.and(QCaseInfo.caseInfo.companyCode.eq(companyCode));
        }else{
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
        HSSFWorkbook workbook = null;
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
                if(StringUtils.isBlank(exportCaseNum.getCompanyCode())){
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "请选择公司")).body(null);
                }
                user.setCompanyCode(exportCaseNum.getCompanyCode());
            }
            QCaseFollowupRecord qCaseFollowupRecord = QCaseFollowupRecord.caseFollowupRecord;
            List<String> caseNumberList = exportCaseNum.getCaseNumberList();
            if (caseNumberList.isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "请选择案件!")).body(null);
            }
            BooleanBuilder exp = new BooleanBuilder();
            exp.and(qCaseFollowupRecord.caseNumber.in(caseNumberList));
            exp.and(qCaseFollowupRecord.companyCode.eq(user.getCompanyCode()));
            exp.and(qCaseFollowupRecord.collectionWay.notIn(0)); //自动添加的排除
            Iterable<CaseFollowupRecord> all2 = caseFollowupRecordRepository.findAll(exp);
            List<CaseFollowupRecord> caseFollowupRecords = IterableUtils.toList(all2);
//            if (caseInfos.isEmpty()) {
//                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "exportCaseInfoFollowRecord", "跟进记录数据为空!")).body(null);
//            }
//            List<CaseFollowupRecord> caseFollowupRecords = new ArrayList<>();
//            for (CaseInfo caseInfo : caseInfos) {
//                Iterable<CaseFollowupRecord> all1 = caseFollowupRecordRepository.findAll(QCaseFollowupRecord.caseFollowupRecord.caseNumber.eq(caseInfo.getCaseNumber()));
//                List<CaseFollowupRecord> records = IterableUtils.toList(all1);
//                caseFollowupRecords.addAll(records);
//            }
            if (caseFollowupRecords.size() > 10000) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "", "不支持导出数据超过10000条!")).body(null);
            }
            workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("sheet1");
            out = new ByteArrayOutputStream();
            Map<String, String> head = followRecordExportService.createHead();
            List<Map<String, Object>> data = followRecordExportService.createData(caseFollowupRecords);
            ExcelExportHelper.createExcel(workbook, sheet, head, data, 0, 0);
            workbook.write(out);
            String filePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(DateTime.now().toString("yyyyMMddhhmmss") + "跟进记录.xls");
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
                                             @RequestParam("caseNumber") @ApiParam("案件编号") String caseNumber,
                                             @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        HSSFWorkbook workbook = null;
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
            QCaseFollowupRecord qCaseFollowupRecord = QCaseFollowupRecord.caseFollowupRecord;
            BooleanBuilder builder = new BooleanBuilder(predicate);
//            builder.and(qCaseFollowupRecord.companyCode.eq(user.getCompanyCode()));
            builder.and(qCaseFollowupRecord.caseNumber.eq(caseNumber));
            Iterable<CaseFollowupRecord> all = caseFollowupRecordRepository.findAll(builder);
            List<CaseFollowupRecord> caseFollowupRecords = IterableUtils.toList(all);
            if (caseFollowupRecords.isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "exportCaseInfoFollowRecord", "跟进记录数据为空!")).body(null);
            }
            workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("sheet1");
            out = new ByteArrayOutputStream();
            Map<String, String> head = followRecordExportService.createHead();
            List<Map<String, Object>> data = followRecordExportService.createData(caseFollowupRecords);
            ExcelExportHelper.createExcel(workbook, sheet, head, data, 0, 0);
            workbook.write(out);
            String filePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(DateTime.now().toString("yyyyMMddhhmmss") + "跟进记录.xls");
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
    public ResponseEntity updateAllScoreStrategyManual(@RequestHeader(value = "X-UserToken") String token) throws IOException {
        try {
            User user = null;
            try {
                user = getUserByToken(token);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String comanyCode = user.getCompanyCode();
            StopWatch watch1 = new StopWatch();
            watch1.start();
            KieSession  kieSession = null;
            try {
                kieSession = createSorceRule(comanyCode);
            } catch (TemplateException e) {
                e.printStackTrace();
            }
            Iterable<CaseInfo> caseInfoList = caseInfoRepository.findAll(QCaseInfo.caseInfo.collectionStatus.eq(CaseInfo.CollectionStatus.COLLECTIONING.getValue())
                .or(QCaseInfo.caseInfo.collectionStatus.eq(CaseInfo.CollectionStatus.EARLY_PAYING.getValue()))
                .or(QCaseInfo.caseInfo.collectionStatus.eq(CaseInfo.CollectionStatus.OVER_PAYING.getValue()))
                .or(QCaseInfo.caseInfo.collectionStatus.eq(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue()))
                .or(QCaseInfo.caseInfo.collectionStatus.eq(CaseInfo.CollectionStatus.WAITCOLLECTION.getValue()))
                .and(QCaseInfo.caseInfo.companyCode.eq(user.getCompanyCode())));

            List<CaseInfo> accCaseInfoList = new ArrayList<>();
            List<CaseInfo> caseInfoList1 = new ArrayList<>();
            caseInfoList.forEach(single ->accCaseInfoList.add(single));
            if (accCaseInfoList.size() > 0) {
                for(CaseInfo caseInfo : accCaseInfoList){
                    ScoreRuleModel scoreRuleModel = new ScoreRuleModel();
                    int age = IdcardUtils.getAgeByIdCard(caseInfo.getPersonalInfo().getIdCard());
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
                //log.info("耗时："+watch1.getTotalTimeMillis());
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("评分完成", "success")).body(null);
            }
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseinfo","failure","案件为空")).body(null);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return null;
        }
    }
    @GetMapping("/updateAllScoreStrategyAuto")
    @ApiOperation(value = "更新案件评分(自动)", notes = "更新案件评分(自动)")
    public ResponseEntity updateAllScoreStrategyAuto(@RequestHeader(value = "X-UserToken") String token) throws IOException {
        User user =null;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String comanyCode = user.getCompanyCode();
        try {
            KieSession  kieSession = null;
            try {
                kieSession = createSorceRule(comanyCode);
            } catch (TemplateException e) {
                e.printStackTrace();
            }

            Iterable<CaseInfo> caseInfoLists = caseInfoRepository.findAll(QCaseInfo.caseInfo.companyCode.eq(user.getCompanyCode()));
            List<CaseInfo> caseInfoList1 = new ArrayList<>();
            List<CaseInfo> accCaseInfoList = new ArrayList<>();
            caseInfoLists.forEach(single ->accCaseInfoList.add(single));
            if (accCaseInfoList.size() > 0) {
                for(CaseInfo caseInfo : accCaseInfoList){
                    ScoreRuleModel scoreRuleModel = new ScoreRuleModel();
                    int age = IdcardUtils.getAgeByIdCard(caseInfo.getPersonalInfo().getIdCard());
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
                caseInfoRepository.save(caseInfoList1);
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("评分完成", "success")).body(null);
            }
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseinfo","failure","案件为空")).body(null);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 动态生成规则
     * @return
     * @throws IOException
     * @throws
     */
    private  KieSession createSorceRule(String comanyCode) throws IOException, TemplateException {
       Template scoreFormulaTemplate = freemarkerConfiguration.getTemplate("scoreFormula.ftl", "UTF-8");
       Template scoreRuleTemplate = freemarkerConfiguration.getTemplate("scoreRule.ftl", "UTF-8");
        ResponseEntity<ScoreRules> responseEntity=restTemplate.getForEntity(Constants.SCOREL_SERVICE_URL.concat("getScoreRules").concat("?comanyCode=").concat(comanyCode),ScoreRules.class);
        List<ScoreRule> rules=null;
        if(responseEntity.hasBody()){
            ScoreRules scoreRules=responseEntity.getBody();
            rules=scoreRules.getScoreRules();
        }
        StringBuilder sb = new StringBuilder();
        if(Objects.nonNull(rules)){
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
            System.out.println(results.getMessages());
            throw new IllegalStateException("### errors ###");
        }
        KieContainer kieContainer =
                kieServices.newKieContainer(kieBuilder.getKieModule().getReleaseId());
        KieSession kieSession = kieContainer.newKieSession();
        return  kieSession;
    }
}
