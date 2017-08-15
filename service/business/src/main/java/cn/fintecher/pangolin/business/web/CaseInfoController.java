package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.AccCaseInfoDisModel;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.service.CaseInfoService;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.QCaseInfo;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import cn.fintecher.pangolin.web.ResponseUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(qCaseInfo.companyCode.eq(user.getCompanyCode())); //公司
            builder.and(qCaseInfo.collectionStatus.notIn(CaseInfo.CollectionStatus.CASE_OVER.getValue())); //以结案
            builder.and(qCaseInfo.collectionStatus.notIn(CaseInfo.CollectionStatus.CASE_OUT.getValue())); //已委外
            if (Objects.equals(user.getManager(), User.MANAGER_TYPE.DATA_AUTH.getValue())) { //管理者
                builder.and(qCaseInfo.department.code.like(user.getDepartment().getCode().concat("%")));
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
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "distributeCeaseInfoAgain", e.getMessage())).body(null);
        }
        try {
            caseInfoService.distributeCeaseInfoAgain(accCaseInfoDisModel, user);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", ENTITY_NAME)).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "distributeCeaseInfoAgain", "系统错误!")).body(null);
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
    public ResponseEntity<Page<CaseFollowupRecord>> getCaseInfoFollowRecord(@QuerydslPredicate(root = CaseFollowupRecord.class) Predicate predicate,
                                                                            @ApiIgnore Pageable pageable,
                                                                            @RequestParam("caseId") @ApiParam("案件ID") String caseId) {
        QCaseFollowupRecord qCaseFollowupRecord = QCaseFollowupRecord.caseFollowupRecord;
        BooleanBuilder builder = new BooleanBuilder(predicate);
        builder.and(qCaseFollowupRecord.caseId.eq(caseId));
        Page<CaseFollowupRecord> page = caseFollowupRecordRepository.findAll(builder, pageable);
        return ResponseEntity.ok().body(page);
    }

    @GetMapping("/getCaseInfoTurnRecord")
    @ApiOperation(value = "案件流转记录", notes = "案件流转记录")
    public ResponseEntity<Page<CaseTurnRecord>> getCaseInfoTurnRecord(@QuerydslPredicate(root = CaseTurnRecord.class) Predicate predicate,
                                                                      @ApiIgnore Pageable pageable,
                                                                      @RequestParam("caseId") @ApiParam("案件ID") String caseId) {
        QCaseTurnRecord qCaseTurnRecord = QCaseTurnRecord.caseTurnRecord;
        BooleanBuilder builder = new BooleanBuilder(predicate);
        builder.and(qCaseTurnRecord.caseId.eq(caseId));
        Page<CaseTurnRecord> page = caseTurnRecordRepository.findAll(builder, pageable);
        return ResponseEntity.ok().body(page);
    }
}
