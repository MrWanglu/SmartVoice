package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.QCaseInfo;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.inject.Inject;
import java.util.Objects;

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
                                                      @ApiIgnore Pageable pageable,
                                                      @RequestHeader(value = "X-UserToken") String token,
                                                      @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode) {
        log.debug("Rest request to getCaseInfo");
        try {
            User tokenUser = getUserByToken(token);
            QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
            // 过滤掉 已结案、待分配的
            BooleanBuilder builder = new BooleanBuilder(predicate);
            if (Objects.isNull(tokenUser.getCompanyCode())) {
                //超级管理员
                if (Objects.nonNull(companyCode)) {
                    builder.and(QCaseInfo.caseInfo.companyCode.eq(companyCode));
                }
            } else {
                //不是超级管理员
                builder.and(QCaseInfo.caseInfo.companyCode.eq(tokenUser.getCompanyCode())); //限制公司code码
            }
            builder.and(qCaseInfo.collectionStatus.notIn(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue(),
                    CaseInfo.CollectionStatus.CASE_OVER.getValue()));
            Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "查询失败")).body(null);
        }

    }
}
