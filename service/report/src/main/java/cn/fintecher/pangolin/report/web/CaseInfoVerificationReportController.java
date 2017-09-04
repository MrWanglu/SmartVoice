package cn.fintecher.pangolin.report.web;

import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.report.model.CaseInfoVerModel;
import cn.fintecher.pangolin.report.model.CaseInfoVerificationParams;
import cn.fintecher.pangolin.report.service.CaseInfoVerificationReportService;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import static cn.fintecher.pangolin.util.ZWDateUtil.getUtilDate;

/**
 * @author yuanyanting
 * @Description:核销管理的报表导出
 */
@RestController
@RequestMapping("/api/caseInfoVerificationReportController")
@Api(value = "CaseInfoVerificationReportController", description = "核销案件报表操作")
public class CaseInfoVerificationReportController extends BaseController {

    @Autowired
    private CaseInfoVerificationReportService caseInfoVerificationReportService;

    @GetMapping("/getVerificationReportBycondition")
    @ApiOperation(value = "多条件分页查询核销报表", notes = "多条件分页查询核销报表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query", value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query", value = "每页大小."),
            @ApiImplicitParam(name = "sort", dataType = "string", paramType = "query", value = "依据什么排序: 属性名(,asc|desc). ", allowMultiple = true)
    })
    public ResponseEntity<List<CaseInfoVerModel>> getVerificationReportBycondition(@RequestHeader(value = "X-UserToken") String token,
                                                                                   CaseInfoVerificationParams caseInfoVerificationParams) {
        User user;
        List<CaseInfoVerModel> caseInfoVerReport;
        try {
            getUtilDate(caseInfoVerificationParams.getStartTime(),"yyyy-MM-dd");
            getUtilDate(caseInfoVerificationParams.getEndTime(),"yyyy-MM-dd");
            int page = caseInfoVerificationParams.getPage() * (caseInfoVerificationParams.getSize());
            caseInfoVerificationParams.setPage(page);
            user = getUserByToken(token);
            caseInfoVerReport = caseInfoVerificationReportService.getVerificationReport(caseInfoVerificationParams, user);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "caseInfoVerification")).body(caseInfoVerReport);
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
            User tokenUser = getUserByToken(token);
            ResponseEntity<String> responseEntity = caseInfoVerificationReportService.exportReport(caseInfoVerificationParams, tokenUser);
            String body = responseEntity.getBody();
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "caseInfoVerification")).body(body);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "导出失败")).body(null);
        }
    }
}
