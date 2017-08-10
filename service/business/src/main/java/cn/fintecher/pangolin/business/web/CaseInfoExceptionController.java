package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.CaseInfoExceptionRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.inject.Inject;

/**
 * @Author : sunyanping
 * @Description : 异常案件操作
 * @Date : 2017/8/9.
 */
@RestController
@RequestMapping(value = "/api/caseInfoExceptionController")
@Api(value = "CaseInfoExceptionController", description = "异常案件操作")
public class CaseInfoExceptionController extends BaseController{

    private final Logger logger= LoggerFactory.getLogger(CaseInfoExceptionController.class);

    @Inject
    private CaseInfoExceptionRepository caseInfoExceptionRepository;

    @GetMapping("/findCaseInfoException")
    @ApiOperation(value = "异常案件页面（多条件查询）", notes = "异常案件页面（多条件查询）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseInfoException>> findCaseInfoException(@QuerydslPredicate(root = CaseInfoException.class) Predicate predicate,
                                                                          @ApiIgnore Pageable pageable,
                                                                          @RequestHeader(value = "X-UserToken") String token) {
        logger.debug("REST request to findCaseInfoException");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("CaseInfoExceptionController", "findCaseInfoException", e.getMessage()))
                    .body(null);
        }
        try {
            QCaseInfoException qd = QCaseInfoException.caseInfoException;
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(qd.companyCode.eq(user.getCompanyCode()));
            Page<CaseInfoException> page = caseInfoExceptionRepository.findAll(builder, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("CaseInfoExceptionController", "findCaseInfoException", "系统异常!"))
                    .body(null);
        }
    }
}
