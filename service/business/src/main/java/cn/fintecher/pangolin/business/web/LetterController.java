package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.QCaseInfo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.inject.Inject;

/**
 * @Author : sunyanping
 * @Description : 信函催收
 * @Date : 2017/7/20.
 */
@RestController
@RequestMapping("/api/letterController")
@Api(value = "LetterController", description = "信函催收")
public class LetterController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(LetterController.class);

    @Inject
    private CaseInfoRepository caseInfoRepository;

    @GetMapping("/getCaseInfo")
    @ApiOperation(value = "查询案件", notes = "查询案件")
    public ResponseEntity<Page<CaseInfo>> getCaseInfo(@QuerydslPredicate Predicate predicate,
                                                      @ApiIgnore Pageable pageable) {
        log.debug("Rest request to getCaseInfo");
        QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
        // 过滤掉 已结案、待分配的
        BooleanBuilder builder = new BooleanBuilder(predicate);
        builder.and(qCaseInfo.collectionStatus.notIn(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue(),
                CaseInfo.CollectionStatus.CASE_OVER.getValue()));
        Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
        return ResponseEntity.ok().body(page);
    }
}
