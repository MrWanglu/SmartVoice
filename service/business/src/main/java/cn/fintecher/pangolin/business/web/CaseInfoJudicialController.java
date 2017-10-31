package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.CaseInfoVerficationModel;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.business.service.CaseInfoJudicialService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

/**
 * @author yuanyanting
 * @version Id:CaseInfoJudicialController.java,v 0.1 2017/9/27 10:48 yuanyanting Exp $$
 */
@RestController
@RequestMapping("/api/caseInfoJudicialController")
@Api(value = "CaseInfoJudicialController", description = "司法案件操作")
public class CaseInfoJudicialController extends BaseController{

    private final Logger log = LoggerFactory.getLogger(CaseInfoJudicialController.class);

    @Inject
    private CaseInfoRepository caseInfoRepository;

    @Inject
    private CaseInfoJudicialRepository caseInfoJudicialRepository;

    @Inject
    private CaseInfoJudicialApplyRepository caseInfoJudicialApplyRepository;

    @Inject
    private SysParamRepository sysParamRepository;

    @Inject
    private CaseInfoJudicialService caseInfoJudicialService;

    @PostMapping("/saveCaseInfoJudicial")
    @ApiOperation(value = "案件申请司法审批", notes = "案件申请司法审批")
    public ResponseEntity saveCaseInfoJudicial(@RequestBody CaseInfoVerficationModel caseInfoVerficationModel,
                                               @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            List<CaseInfo> caseInfoList = caseInfoRepository.findAll(caseInfoVerficationModel.getIds());
            for (int i = 0; i < caseInfoList.size(); i++) {
                if (caseInfoList.get(i).getCollectionStatus().equals(CaseInfo.CollectionStatus.CASE_OVER.getValue())) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoJudicial", "caseInfoJudicial", "已结案案件不能司法!")).body(null);
                }
                if (caseInfoList.get(i).getCollectionStatus().equals(CaseInfo.CollectionStatus.CASE_OUT.getValue())) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoJudicial", "caseInfoJudicial", "已委外案件不能司法!")).body(null);
                }
            }
            SysParam sysParam = sysParamRepository.findOne(QSysParam.sysParam.code.eq("SysParam.isApply"));
            if (Integer.parseInt(sysParam.getValue()) == 1) { // 申请审批
                CaseInfoJudicialApply caseInfoJudicialApply = new CaseInfoJudicialApply();
                for (CaseInfo caseInfo : caseInfoList) {
                    caseInfoJudicialService.setJudicialApply(caseInfoJudicialApply,caseInfo,user,caseInfoVerficationModel.getApplicationReason());
                    caseInfoJudicialApplyRepository.save(caseInfoJudicialApply);
                }
            }else {
                for (CaseInfo caseInfo : caseInfoList) {
                    List<CaseInfoJudicialApply> list = caseInfoJudicialApplyRepository.findAll();
                    for (int i=0;i<list.size();i++) {
                        if (caseInfoVerficationModel.getIds().contains(list.get(i).getCaseId())) {
                            if (Objects.equals(list.get(i).getApprovalStatus(),CaseInfoVerificationApply.ApprovalStatus.approval_pending.getValue())) {
                                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoVerification", "caseInfoVerification", "不能提交重复申请!")).body(null);
                            }
                        }
                    }
                    caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.CASE_OVER.getValue()); // 催收类型：已结案
                    caseInfo.setEndType(CaseInfo.EndType.JUDGMENT_CLOSED.getValue()); // 结案方式：司法结案
                    caseInfoRepository.save(caseInfo);
                    CaseInfoJudicial caseInfoJudicial = new CaseInfoJudicial();
                    caseInfoJudicial.setCaseInfo(caseInfo);// 核销的案件信息
                    caseInfoJudicial.setCompanyCode(caseInfo.getCompanyCode());// 公司code码
                    caseInfoJudicial.setOperatorTime(ZWDateUtil.getNowDateTime());// 操作时间
                    caseInfoJudicialRepository.save(caseInfoJudicial);
                }
            }
            return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", "caseInfoJudicial")).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoJudicial", "caseInfoJudicial", "操作失败!")).body(null);
        }
    }

    @RequestMapping(value = "/getCaseInfoJudicialApproval", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "审批待通过案件查询", notes = "审批待通过案件查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity getCaseInfoJudicialApproval(@QuerydslPredicate(root = CaseInfoJudicialApply.class) Predicate predicate,
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
                builder.and(QCaseInfoJudicialApply.caseInfoJudicialApply.companyCode.eq(companyCode));
            }
        } else {
            builder.and(QCaseInfoJudicialApply.caseInfoJudicialApply.companyCode.eq(user.getCompanyCode()));
        }
        builder.and(QCaseInfoJudicialApply.caseInfoJudicialApply.approvalStatus.in(CaseInfoVerificationApply.ApprovalStatus.approval_pending.getValue(),CaseInfoVerificationApply.ApprovalStatus.approval_disapprove.getValue())); // 审批状态：待通过、审批拒绝
        Page<CaseInfoJudicialApply> page = caseInfoJudicialApplyRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", "caseInfoJudicial")).body(page);
    }

    @GetMapping("/caseInfoJudicial")
    @ApiOperation(value = "司法审批案件查询",notes = "司法审批单个案件查询")
    public ResponseEntity<CaseInfoJudicialApply> caseInfoJudicial(String id) {
        try{
            CaseInfoJudicialApply caseInfoJudicialApply = caseInfoJudicialApplyRepository.findOne(id);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "caseInfoJudicial")).body(caseInfoJudicialApply);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoJudicial", "caseInfoJudicial", "查看失败")).body(null);
        }
    }

    @PostMapping("/caseInfoJudicialApply")
    @ApiOperation(value = "案件申请审批通过",notes = "案件申请审批通过")
    public ResponseEntity caseInfoJudicialApply(@RequestBody CaseInfoVerficationModel caseInfoVerficationModel,
                                                @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try{
            user = getUserByToken(token);
            caseInfoJudicialService.caseInfoJudicialApply(caseInfoVerficationModel,user);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "caseInfoJudicial")).body(null);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfoJudicial", "caseInfoJudicial", "查看失败")).body(null);
        }
    }

    @RequestMapping(value = "/getCaseInfoJudicial", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "司法审批通过案件查询", notes = "司法审批通过案件查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity getCaseInfoJudicial(@QuerydslPredicate(root = CaseInfoJudicial.class) Predicate predicate,
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
        if (Objects.isNull(user.getCompanyCode())) { // 超级管理员
            if (StringUtils.isNotBlank(companyCode)) {
                builder.and(QCaseInfoJudicial.caseInfoJudicial.companyCode.eq(companyCode));
            }
        } else { // 普通管理员
            builder.and(QCaseInfoJudicial.caseInfoJudicial.companyCode.eq(user.getCompanyCode()));
        }
        builder.and(QCaseInfoJudicial.caseInfoJudicial.caseInfo.endType.eq(CaseInfo.EndType.JUDGMENT_CLOSED.getValue())); // 结案方式：司法结案
        Page<CaseInfoJudicial> page = caseInfoJudicialRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", "caseInfoJudicial")).body(page);
    }

}
