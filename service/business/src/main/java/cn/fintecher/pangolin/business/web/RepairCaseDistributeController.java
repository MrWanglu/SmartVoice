package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.AccCaseInfoDisModel;
import cn.fintecher.pangolin.business.model.BatchDistributeModel;
import cn.fintecher.pangolin.business.repository.CaseRepairRepository;
import cn.fintecher.pangolin.business.service.CaseInfoService;
import cn.fintecher.pangolin.entity.CaseRepair;
import cn.fintecher.pangolin.entity.QCaseRepair;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Objects;

/**
 * Created by  gaobeibei.
 * Description:
 * Date: 2017-08-09
 */
@RestController
@RequestMapping("/api/repairCaseDistributeController")
@Api(value = "修复案件管理", description = "修复案件管理")
public class RepairCaseDistributeController extends BaseController{

    @Autowired
    CaseInfoService caseInfoService;

    @Autowired
    CaseRepairRepository caseRepairRepository;

    @RequestMapping(value = "/distributeRepairCase", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    @ApiOperation(value = "修复分配", notes = "修复分配")
    public ResponseEntity distributeRepairCase(@RequestBody AccCaseInfoDisModel accCaseInfoDisModel,
                                              @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        try {
            User user = getUserByToken(token);
            caseInfoService.distributeRepairCase(accCaseInfoDisModel, user);
            return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", "RepairCaseDistributeController")).body(null);
        } catch (Exception e) {
            String msg = Objects.isNull(e.getMessage()) ? "系统异常" : e.getMessage();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("RepairCaseDistributeController", "error", msg)).body(null);
        }
    }
    /**
     * @Description 修复案件查询
     */
    @GetMapping("/getAllRepairedCase")
    @ApiOperation(value = "修复案件查询", notes = "修复案件查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseRepair>> getAllTelCase(@QuerydslPredicate(root = CaseRepair.class) Predicate predicate,
                                                          @ApiIgnore Pageable pageable,
                                                          @RequestHeader(value = "X-UserToken") String token) {
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        BooleanBuilder builder = new BooleanBuilder(predicate);
        builder.and(QCaseRepair.caseRepair.caseId.companyCode.eq(user.getCompanyCode()));
        Page<CaseRepair> page = caseRepairRepository.findAll(builder,pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", "RepairCaseDistributeController")).body(page);
    }

    /**
     * @Description 修复页面获取专员分配信息
     */
    @GetMapping("/getAttachBatchInfo")
    @ApiOperation(value = "修复页面获取专员分配信息", notes = "修复页面获取分配信息")
    public ResponseEntity<BatchDistributeModel> getAttachBatchInfo(@RequestHeader(value = "X-UserToken") String token,
                                                             @ApiParam("催收员ID集合")@RequestParam(required = false) List<String> userIds) {
        try {
            BatchDistributeModel batchDistributeModel = caseInfoService.getAttachBatchDistribution(userIds);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("获取分配信息成功", "RepairCaseDistributeController")).body(batchDistributeModel);
        } catch (Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取分配信息失败", "caseInfo", e.getMessage())).body(null);
        }
    }

    /**
     * @Description 修复页面获取机构分配信息
     */
    @GetMapping("/getDeptBatchInfo")
    @ApiOperation(value = "修复页面获取机构分配信息", notes = "修复页面获取机构分配信息")
    public ResponseEntity<Long> getDeptBatchInfo(@RequestHeader(value = "X-UserToken") String token,
                                                             @ApiParam("机构ID")@RequestParam(required = false) String deptId) {
        try {
            Long count = caseInfoService.getDeptBatchDistribution(deptId);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("获取分配信息成功", "RepairCaseDistributeController")).body(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取分配信息失败", "caseInfo", e.getMessage())).body(null);
        }
    }
}
