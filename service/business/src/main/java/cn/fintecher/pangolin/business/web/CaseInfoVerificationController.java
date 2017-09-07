package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.CaseInfoVerModel;
import cn.fintecher.pangolin.business.model.CaseInfoVerificationParams;
import cn.fintecher.pangolin.business.model.ListAccVerificationRecevicePool;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoVerificationRepository;
import cn.fintecher.pangolin.business.service.CaseInfoVerificationService;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.CaseInfoVerification;
import cn.fintecher.pangolin.entity.QCaseInfoVerification;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author yuanyanting
 * @version Id:CaseInfoVerificationController.java,v 0.1 2017/9/1 11:47 yuanyanting Exp $$
 */
@RestController
@RequestMapping("/api/caseInfoVerificationController")
@Api(value = "CaseInfoVerificationController", description = "核销案件操作")
public class CaseInfoVerificationController extends BaseController {

    @Inject
    private CaseInfoVerificationRepository caseInfoVerificationRepository;

    @Inject
    private CaseInfoVerificationService caseInfoVerificationService;

    @Inject
    private CaseInfoRepository caseInfoRepository;

    @PostMapping("/saveCaseInfoVerification")
    @ApiOperation(value = "核销管理", notes = "核销管理")
    public ResponseEntity saveCaseInfoVerification(@RequestBody ListAccVerificationRecevicePool request) {
        try {
            List<CaseInfo> caseInfoList = caseInfoRepository.findAll(request.getIds());
            for (int i = 0; i < caseInfoList.size(); i++) {
                if (caseInfoList.get(i).getCollectionStatus().equals(CaseInfo.CollectionStatus.CASE_OVER.getValue())) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "结案案件不能核销!")).body(null);
                }
            }
            List<CaseInfoVerification> caseInfoVerificationList = new ArrayList<>();
            CaseInfoVerification caseInfoVerification = new CaseInfoVerification();
            for (CaseInfo caseInfo : caseInfoList) {
                if (caseInfo.getCollectionStatus() != CaseInfo.CollectionStatus.CASE_OVER.getValue()) {
                    BeanUtils.copyProperties(caseInfo, caseInfoVerification);
                    CaseInfoVerification caseInfoVerification1 = caseInfoVerificationRepository.save(caseInfoVerification);
                    caseInfoVerificationList.add(caseInfoVerification1);
                    caseInfoRepository.delete(caseInfo);
                }
            }
            return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", "CaseInfoVerificationModel")).body(caseInfoVerificationList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "操作失败!")).body(null);
        }
    }

    @RequestMapping(value = "/getCaseInfoVerification", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "案件按催收类型查询", notes = "案件按条件类型查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity getCaseInfoVerification(@QuerydslPredicate(root = CaseInfoVerification.class) Predicate predicate,
                                                  @ApiIgnore Pageable pageable,
                                                  @RequestHeader(value = "X-UserToken") String token,
                                                  @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        BooleanBuilder builder = new BooleanBuilder(predicate);
        if (Objects.isNull(user.getCompanyCode())) {
            if (Objects.isNull(companyCode)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "请选择公司")).body(null);
            }
            builder.and(QCaseInfoVerification.caseInfoVerification.companyCode.eq(companyCode));
        } else {
            builder.and(QCaseInfoVerification.caseInfoVerification.companyCode.eq(user.getCompanyCode()));
        }
        Page<CaseInfoVerification> page = caseInfoVerificationRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", "caseInfoVerification")).body(page);
    }

    @GetMapping("/exportVerification")
    @ApiOperation(value = "核销管理导出", notes = "核销管理导出")
    public ResponseEntity<String> exportVerification(@RequestHeader(value = "X-UserToken") String token, @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode) {
        User user;
        try {
            user = getUserByToken(token);
            BooleanBuilder booleanBuilder = new BooleanBuilder();
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.isNull(companyCode)) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseRepair", "", "请选择公司")).body(null);
                }
                booleanBuilder.and(QCaseInfoVerification.caseInfoVerification.companyCode.eq(companyCode));
            } else {
                booleanBuilder.and(QCaseInfoVerification.caseInfoVerification.companyCode.eq(user.getCompanyCode()));
            }
            Iterator<CaseInfoVerification> caseInfoVerifications = caseInfoVerificationRepository.findAll(booleanBuilder).iterator();
            List<CaseInfoVerification> caseInfoVerificationList = IteratorUtils.toList(caseInfoVerifications);
            String url = caseInfoVerificationService.exportCaseInfoVerification(caseInfoVerificationList);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("导出成功", "caseInfoVerification")).body(url);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "导出失败")).body(null);
        }
    }

    @GetMapping("/getVerificationReportBycondition")
    @ApiOperation(value = "多条件分页查询核销报表", notes = "多条件分页查询核销报表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query", value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query", value = "每页大小."),
            @ApiImplicitParam(name = "sort", dataType = "string", paramType = "query", value = "依据什么排序: 属性名(,asc|desc). ", allowMultiple = true)
    })
    public ResponseEntity<Page<CaseInfoVerModel>> getVerificationReportBycondition(@RequestHeader(value = "X-UserToken") String token,
                                                                                   @ApiIgnore Pageable pageable,
                                                                                   CaseInfoVerificationParams caseInfoVerificationParams) {
        User user;
        List<CaseInfoVerModel> caseInfoVerificationReport = null;
        try {
            user = getUserByToken(token);
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.isNull(caseInfoVerificationParams.getCompanyCode())) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "请选择公司")).body(null);
                }
            }
            caseInfoVerificationReport = caseInfoVerificationService.getList(caseInfoVerificationParams,user);
            Integer totalCount = caseInfoVerificationRepository.getTotalCount();
            Page<CaseInfoVerModel> page = new PageImpl<>(caseInfoVerificationReport, pageable, totalCount);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/caseInfoVerificationController/getVerificationReportBycondition");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "查询失败")).body(null);
        }
    }

    @GetMapping("/exportReport")
    @ApiOperation(value = "导出核销报表", notes = "导出核销报表")
    public ResponseEntity<String> exportReport(CaseInfoVerificationParams caseInfoVerificationParams,
                                               @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.isNull(caseInfoVerificationParams.getCompanyCode())) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "请选择公司")).body(null);
                }
            }
            String url = caseInfoVerificationService.exportReport(caseInfoVerificationParams,user);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "caseInfoVerification")).body(url);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "导出失败")).body(null);
        }
    }
}

