package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.CaseInfoVerModel;
import cn.fintecher.pangolin.business.model.CaseInfoVerficationModel;
import cn.fintecher.pangolin.business.model.CaseInfoVerificationParams;
import cn.fintecher.pangolin.business.repository.CaseAssistRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoVerificationPackagingRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoVerificationRepository;
import cn.fintecher.pangolin.business.service.CaseInfoVerificationService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
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

    @Inject
    private CaseAssistRepository caseAssistRepository;

    @Inject
    private CaseInfoVerificationPackagingRepository caseInfoVerificationPackagingRepository;

    @PostMapping("/saveCaseInfoVerification")
    @ApiOperation(value = "核销管理", notes = "核销管理")
    public ResponseEntity saveCaseInfoVerification(@RequestBody CaseInfoVerficationModel request,
                                                   @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            List<CaseInfo> caseInfoList = caseInfoRepository.findAll(request.getIds());
            List<CaseAssist> caseAssistList = new ArrayList<>();
            for (int i = 0; i < caseInfoList.size(); i++) {
                if (caseInfoList.get(i).getCollectionStatus().equals(CaseInfo.CollectionStatus.CASE_OVER.getValue())) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "结案案件不能核销!")).body(null);
                }
                if (caseInfoList.get(i).getCollectionStatus().equals(CaseInfo.CollectionStatus.CASE_OUT.getValue())) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "已委外案件不能核销!")).body(null);
                }
            }
            for (CaseInfo caseInfo : caseInfoList) {
                //处理协催案件
                if (Objects.equals(caseInfo.getAssistFlag(), 1)) { //协催标识
                    //结束协催案件
                    CaseAssist one = caseAssistRepository.findOne(QCaseAssist.caseAssist.caseId.eq(caseInfo).and(QCaseAssist.caseAssist.assistStatus.notIn(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())));
                    if (Objects.nonNull(one)) {
                        one.setAssistCloseFlag(0); //手动结束
                        one.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue()); //协催结束
                        one.setOperator(user);
                        one.setOperatorTime(new Date());
                        one.setCaseFlowinTime(new Date()); //流入时间
                        caseAssistList.add(one);
                    }
                    caseInfo.setAssistFlag(0); //协催标识置0
                    caseInfo.setAssistStatus(null);//协催状态置空
                    caseInfo.setAssistWay(null);
                    caseInfo.setAssistCollector(null);
                }
                CaseInfoVerification caseInfoVerification = new CaseInfoVerification();
                caseInfo.setEndType(CaseInfo.EndType.CLOSE_CASE.getValue());
                caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.CASE_OVER.getValue());
                caseInfoVerification.setCompanyCode(caseInfo.getCompanyCode()); // 公司code
                caseInfoVerification.setOperator(user.getRealName()); // 操作人
                caseInfoVerification.setOperatorTime(ZWDateUtil.getNowDateTime()); // 操作时间
                caseInfoVerification.setCaseInfo(caseInfo); // 案件信息
                caseInfoVerification.setState(request.getState()); // 核销说明
                caseInfoVerificationRepository.save(caseInfoVerification);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", "CaseInfoVerificationModel")).body(null);
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
            if (StringUtils.isNotBlank(companyCode)) {
                builder.and(QCaseInfoVerification.caseInfoVerification.companyCode.eq(companyCode));
            }
        } else {
            builder.and(QCaseInfoVerification.caseInfoVerification.companyCode.eq(user.getCompanyCode()));
        }
        builder.and(QCaseInfoVerification.caseInfoVerification.caseInfo.endType.eq(CaseInfo.EndType.CLOSE_CASE.getValue()));
        Page<CaseInfoVerification> page = caseInfoVerificationRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", "caseInfoVerification")).body(page);
    }

    @PostMapping("/exportVerification")
    @ApiOperation(value = "核销管理导出", notes = "核销管理导出")
    public ResponseEntity<String> exportVerification(@RequestHeader(value = "X-UserToken") String token,
                                                     @RequestBody CaseInfoVerficationModel caseInfoVerficationModel) {
        User user;
        try {
            user = getUserByToken(token);
            List<String> ids = caseInfoVerficationModel.getIds();
            if (ids.isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "请选择案件!")).body(null);
            }
            List<Object[]> caseInfoVerificationList = caseInfoVerificationService.getCastInfoList(caseInfoVerficationModel, user);
            if (caseInfoVerificationList.isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "", "要导出的核销案件数据为空!")).body(null);
            }
            if (caseInfoVerificationList.size() > 10000) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "", "不支持导出数据超过10000条!")).body(null);
            }
            int sum = 0;
            BigDecimal amount = new BigDecimal(sum);
            for (String id : ids) {
                CaseInfoVerification caseInfoVerification = caseInfoVerificationRepository.findOne(id);
                CaseInfo caseInfo = caseInfoVerification.getCaseInfo();
                BigDecimal overdueAmount = caseInfo.getOverdueAmount();
                amount = amount.add(overdueAmount);
            }
            String url = caseInfoVerificationService.exportCaseInfoVerification(caseInfoVerificationList);
            CaseInfoVerificationPackaging caseInfoVerificationPackaging = new CaseInfoVerificationPackaging();
            caseInfoVerificationPackaging.setPackagingTime(ZWDateUtil.getNowDateTime()); // 打包时间
            caseInfoVerificationPackaging.setPackagingState(caseInfoVerficationModel.getState()); // 打包说明
            caseInfoVerificationPackaging.setCount(ids.size());
            caseInfoVerificationPackaging.setDownloadCount(1);
            caseInfoVerificationPackaging.setTotalAmount(amount); // 总金额
            caseInfoVerificationPackaging.setDownloadAddress(url); // 下载地址
            caseInfoVerificationPackaging.setOperator(user.getRealName()); // 操作人
            caseInfoVerificationPackaging.setOperatorTime(ZWDateUtil.getNowDateTime()); // 操作时间
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.nonNull(caseInfoVerficationModel.getCompanyCode())) {
                    caseInfoVerificationPackaging.setCompanyCode(caseInfoVerficationModel.getCompanyCode());
                }
            }else {
                caseInfoVerificationPackaging.setCompanyCode(user.getCompanyCode());
            }
            caseInfoVerificationPackagingRepository.save(caseInfoVerificationPackaging);
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
            caseInfoVerificationReport = caseInfoVerificationService.getList(caseInfoVerificationParams, user);
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
            String url = caseInfoVerificationService.exportReport(caseInfoVerificationParams, user);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "caseInfoVerification")).body(url);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "导出失败")).body(null);
        }
    }

    @PostMapping("/batchDownload")
    @ApiOperation(value = "立刻下载",notes = "立刻下载")
    public ResponseEntity<List<String>> batchDownload(@RequestBody CaseInfoVerficationModel caseInfoVerficationModel) {
        try{
            List<String> ids = caseInfoVerficationModel.getIds();
            if (ids.isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "请至少选择一个案件！")).body(null);
            }
            List<String> urlList = new ArrayList<>();
            for (String id : ids) {
                CaseInfoVerificationPackaging caseInfoVerificationPackaging = caseInfoVerificationPackagingRepository.findOne(id);
                if (Objects.nonNull(caseInfoVerificationPackaging.getDownloadCount())) {
                    caseInfoVerificationPackaging.setDownloadCount(caseInfoVerificationPackaging.getDownloadCount() + 1); // 下载次数
                }
                caseInfoVerificationPackagingRepository.save(caseInfoVerificationPackaging);
                String url = caseInfoVerificationPackaging.getDownloadAddress();
                urlList.add(url);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "caseInfoVerification")).body(urlList);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "查看失败")).body(null);
        }
    }

    @PostMapping("/download")
    @ApiOperation(value = "单个立刻下载",notes = "单个立即下载")
    public ResponseEntity<String> download(@RequestBody CaseInfoVerficationModel caseInfoVerficationModel) {
        try{
            CaseInfoVerificationPackaging caseInfoVerificationPackaging = new CaseInfoVerificationPackaging();
            if(Objects.nonNull(caseInfoVerficationModel.getId())) {
                caseInfoVerificationPackaging = caseInfoVerificationPackagingRepository.findOne(caseInfoVerficationModel.getId());
            }
            if (Objects.nonNull(caseInfoVerificationPackaging.getDownloadCount())) {
                caseInfoVerificationPackaging.setDownloadCount(caseInfoVerificationPackaging.getDownloadCount() + 1); // 下载次数
            }
            caseInfoVerificationPackagingRepository.save(caseInfoVerificationPackaging);
            String url = caseInfoVerificationPackaging.getDownloadAddress();
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "caseInfoVerification")).body(url);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "查看失败")).body(null);
        }
    }

    @RequestMapping(value = "/getCaseInfoVerificationPackaging", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "核销案件打包的查询", notes = "核销案件打包的查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity getCaseInfoVerificationPackaging(@QuerydslPredicate(root = CaseInfoVerificationPackaging.class) Predicate predicate,
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
            if (StringUtils.isNotBlank(companyCode)) {
                builder.and(QCaseInfoVerificationPackaging.caseInfoVerificationPackaging.companyCode.eq(companyCode));
            }
        } else {
            builder.and(QCaseInfoVerificationPackaging.caseInfoVerificationPackaging.companyCode.eq(user.getCompanyCode()));
        }
        Page<CaseInfoVerificationPackaging> page = caseInfoVerificationPackagingRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", "caseInfoVerification")).body(page);
    }
}

