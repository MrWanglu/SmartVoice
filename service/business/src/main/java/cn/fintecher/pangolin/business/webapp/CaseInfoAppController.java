package cn.fintecher.pangolin.business.webapp;

import cn.fintecher.pangolin.business.repository.CaseAssistRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.web.BaseController;
import cn.fintecher.pangolin.entity.CaseAssist;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.QCaseInfo;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;


/**
 * @author : gaobeibei
 * @Description : APP案件信息查询
 * @Date : 16:01 2017/7/20
 */
@RestController
@RequestMapping(value = "/api/CaseInfoAppController")
@Api(value = "催收任务查询", description = "催收任务查询")
public class CaseInfoAppController extends BaseController {
    final Logger log = LoggerFactory.getLogger(CaseInfoAppController.class);
    @Inject
    CaseInfoRepository caseInfoRepository;
    @Inject
    CaseAssistRepository caseAssistRepository;

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
        builder.and(QCaseInfo.caseInfo.currentCollector.department.code.startsWith(user.getDepartment().getCode()));
        builder.and(QCaseInfo.caseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue()));
        Page<CaseAssist> page = caseAssistRepository.findAll(predicate, pageable);
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
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        BooleanBuilder builder = new BooleanBuilder(predicate);
        builder.and(QCaseInfo.caseInfo.collectionType.eq(CaseInfo.CollectionType.VISIT.getValue()));
        builder.and(QCaseInfo.caseInfo.assistFlag.eq(CaseInfo.AssistFlag.NO_ASSIST.getValue()));
        builder.and(QCaseInfo.caseInfo.currentCollector.department.code.startsWith(user.getDepartment().getCode()));
        builder.and(QCaseInfo.caseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue()));
        Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
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
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        BooleanBuilder builder = new BooleanBuilder(predicate);
        builder.and(QCaseInfo.caseInfo.collectionType.eq(CaseInfo.CollectionType.VISIT.getValue()));
        builder.or(QCaseInfo.caseInfo.assistFlag.eq(CaseInfo.AssistFlag.YES_ASSIST.getValue()));
        builder.and(QCaseInfo.caseInfo.currentCollector.department.code.startsWith(user.getDepartment().getCode()));
        builder.and(QCaseInfo.caseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue()));
        Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/queryCaseDetail");
        return new ResponseEntity<>(page, headers, HttpStatus.OK);
    }
}
