package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.ListAccVerificationRecevicePool;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoVerificationRepository;
import cn.fintecher.pangolin.business.service.CaseInfoVerificationService;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.CaseInfoVerificationModel;
import cn.fintecher.pangolin.entity.QCaseInfoVerificationModel;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
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
public class CaseInfoVerificationController extends BaseController{

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
            List<CaseInfoVerificationModel> caseInfoVerificationList = new ArrayList<>();
            CaseInfoVerificationModel caseInfoVerification = new CaseInfoVerificationModel();
            for (CaseInfo caseInfo : caseInfoList) {
                if (caseInfo.getCollectionStatus() != CaseInfo.CollectionStatus.CASE_OVER.getValue()) {
                    BeanUtils.copyProperties(caseInfo, caseInfoVerification);
                    CaseInfoVerificationModel caseInfoVerification1 = caseInfoVerificationRepository.save(caseInfoVerification);
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
    public ResponseEntity getCaseInfoVerification(@QuerydslPredicate(root = CaseInfoVerificationModel.class) Predicate predicate,
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
            builder.and(QCaseInfoVerificationModel.caseInfoVerificationModel.companyCode.eq(companyCode));
        } else {
            builder.and(QCaseInfoVerificationModel.caseInfoVerificationModel.companyCode.eq(user.getCompanyCode()));
        }
        Page<CaseInfoVerificationModel> page = caseInfoVerificationRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", "caseInfoVerification")).body(page);
    }

    @GetMapping("/exportVerification")
    @ApiOperation(value = "核销管理导出", notes = "核销管理导出")
    public ResponseEntity<String> exportVerification(@RequestHeader(value = "X-UserToken") String token, @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode) {
        User user;
        try {
            user = getUserByToken(token);
            BooleanBuilder booleanBuilder = new BooleanBuilder();
            if(Objects.isNull(user.getCompanyCode())){
                if(Objects.isNull(companyCode)){
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseRepair", "", "请选择公司")).body(null);
                }
                booleanBuilder.and(QCaseInfoVerificationModel.caseInfoVerificationModel.companyCode.eq(companyCode));
            }else{
                booleanBuilder.and(QCaseInfoVerificationModel.caseInfoVerificationModel.companyCode.eq(user.getCompanyCode()));
            }
            Iterator<CaseInfoVerificationModel> caseInfoVerifications = caseInfoVerificationRepository.findAll(booleanBuilder).iterator();
            List<CaseInfoVerificationModel> caseInfoVerificationList = IteratorUtils.toList(caseInfoVerifications);
            String url = caseInfoVerificationService.exportCaseInfoVerification(caseInfoVerificationList);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("导出成功", "caseInfoVerification")).body(url);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "导出失败")).body(null);
        }
    }
}
