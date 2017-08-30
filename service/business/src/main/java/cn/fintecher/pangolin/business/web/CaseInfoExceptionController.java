package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.CaseUpdateParams;
import cn.fintecher.pangolin.business.repository.CaseInfoExceptionRepository;
import cn.fintecher.pangolin.business.service.CaseInfoExceptionService;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.CaseInfoDistributed;
import cn.fintecher.pangolin.entity.CaseInfoException;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

/**
 * @author : DuChao
 * @Description : 异常案件操作
 * @Date : 2017/7/20.
 */

@RestController
@RequestMapping("/api/CaseInfoExceptionController")
@Api(value = "CaseInfoExceptionController", description = "异常案件操作")
public class CaseInfoExceptionController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(CaseInfoController.class);
    private static final String ENTITY_NAME = "caseInfoException";
    private static final String CASE_INFO_ENTITY = "caseInfo";
    @Autowired
    private CaseInfoExceptionService caseInfoExceptionService;
    @Autowired
    private CaseInfoExceptionRepository caseInfoExceptionRepository;

    /**
     * 获取所有异常案件
     *
     * @return CaseInfoExceptionList
     */
    @GetMapping("/findAllCaseInfoException")
    @ApiOperation(value = "获取所有异常案件", notes = "获取所有异常案件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseInfoException>> getAllCaseInfoException(@QuerydslPredicate(root = CaseInfoException.class) Predicate predicate,
                                                                           @ApiIgnore Pageable pageable) {
        log.debug("REST request to get all CaseInfoExceptions");
        Page<CaseInfoException> page = caseInfoExceptionRepository.findAll(predicate, pageable);
        return ResponseEntity.ok().body(page);
    }

    /**
     * 新增案件
     *
     * @param caseInfoExceptionId
     * @param token
     * @return
     * @throws URISyntaxException
     */
    @GetMapping("/addCaseInfoException")
    @ApiOperation(value = "新增案件", notes = "新增案件")
    public ResponseEntity<CaseInfoDistributed> addCaseInfo(@RequestParam @ApiParam(value = "异常案件id") String caseInfoExceptionId,
                                                           @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) throws Exception {
        log.debug("REST request to add case to CaseInfoDistributed");
        CaseInfoDistributed caseInfoDistributed = caseInfoExceptionService.addCaseInfoDistributed(caseInfoExceptionId, getUserByToken(token));
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert(CASE_INFO_ENTITY, caseInfoDistributed.getId())).body(caseInfoDistributed);
    }

    /**
     * 更新案件
     *
     * @param caseUpdateParams
     * @param token
     * @return
     * @throws URISyntaxException
     */
    @PostMapping("/updateCaseInfoException")
    @ApiOperation(value = "更新案件", notes = "更新案件")
    public ResponseEntity<CaseInfo> updateCaseInfoException(@RequestBody CaseUpdateParams caseUpdateParams,
                                                            @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) throws Exception {
        log.debug("REST request to update CaseInfo");
        CaseInfoException caseInfoException = caseInfoExceptionRepository.findOne(caseUpdateParams.getCaseInfoExceptionId());
        if (Objects.isNull(caseInfoException)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Exception Case", "异常案件已经被更新")).body(null);
        }
        List<CaseInfo> caseInfoList = caseInfoExceptionService.updateCaseInfoException(caseUpdateParams.getCaseInfoExceptionId(), caseUpdateParams.getCaseInfoIds(), getUserByToken(token));
        if (caseInfoList.size() > 0) {
            return ResponseEntity.ok().body(null);
        }
        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseNotFound", "不存在相同案件，无法更新")).body(null);
    }

    /**
     * 删除异常池案件
     *
     * @param caseInfoExceptionId
     * @return
     */
    @DeleteMapping("/deleteCaseInfoException")
    @ApiOperation(value = "删除异常池案件", notes = "删除异常池案件")
    public ResponseEntity<Void> deleteCaseInfoException(@RequestParam @ApiParam(value = "异常案件id") String caseInfoExceptionId) {
        try {
        log.debug("REST request to delete caseInfoException : {}", caseInfoExceptionId);
            caseInfoExceptionService.deleteCaseInfoException(caseInfoExceptionId);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,"delete error","案件删除失败，请检查案件是否已被删除")).body(null);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, caseInfoExceptionId)).build();
    }

    @GetMapping("/checkExceptionCase")
    @ApiOperation(value = "检查异常案件", notes = "检查异常案件")
    public ResponseEntity<CaseInfoException> checkExceptionCase(@RequestParam(value = "companyCode", required = false) String companyCode,
                                                                @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        log.debug("REST request to checkExceptionCase");
        try {
            User user = getUserByToken(token);
            if (Objects.isNull(user.getCompanyCode())) {
                if (StringUtils.isNotBlank(companyCode)) {
                    user.setCompanyCode(companyCode);
                } else {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "checkExceptionCase", "请先选择公司!")).body(null);
                }
            }
            if (caseInfoExceptionService.checkCaseExceptionExist(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "异常池有异常案件，请先处理!")).body(null);
            }
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "系统异常!")).body(null);
        }
    }
}
