package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.QCaseInfo;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Objects;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-26-16:15
 */
@RestController
@RequestMapping("/api/caseJudicialCollectionController")
@Api(value = "司法催收", description = "司法催收")
public class CaseJudicialCollectionController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(CaseJudicialCollectionController.class);
    private static final String ENTITY_NAME = "CaseJudicialCollection";
    @Autowired
    private CaseInfoRepository caseInfoRepository;

    /**
     * @Description : 分页,多条件查询司法催收案件信息
     */
    @GetMapping("/queryCaseInfo")
    @ApiOperation(value = "司法催收案件查詢", notes = "司法催收案件查詢")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseInfo>> queryCaseInfo(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                        @ApiIgnore Pageable pageable,
                                                        @RequestParam(required = false) String companyCode,
                                                        @RequestHeader(value = "X-UserToken") String token) {

        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        BooleanBuilder builder = new BooleanBuilder(predicate);
        builder.and(QCaseInfo.caseInfo.endType.eq(CaseInfo.EndType.JUDGMENT_CLOSED.getValue()));
        if (Objects.nonNull(user.getCompanyCode())) {
            builder.and(QCaseInfo.caseInfo.companyCode.eq(user.getCompanyCode()));
        }else{
            if (Objects.nonNull(companyCode)){
                builder.and(QCaseInfo.caseInfo.companyCode.eq(companyCode));
            }
        }
        Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功","operation successfully")).body(page);
    }
}
