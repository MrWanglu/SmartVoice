package cn.fintecher.pangolin.business.webapp;

import cn.fintecher.pangolin.business.model.BackMoneyModel;
import cn.fintecher.pangolin.business.model.PayApplyParams;
import cn.fintecher.pangolin.business.repository.CaseAssistRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.CasePayApplyRepository;
import cn.fintecher.pangolin.business.service.CaseInfoService;
import cn.fintecher.pangolin.business.web.BaseController;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.file.UploadFile;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author : gaobeibei
 * @Description : APP案件还款
 * @Date : 11:28 2017/7/27
 */

@RestController
@RequestMapping(value = "/api/casePayApplyAppController")
@Api(value = "APP案件还款记录", description = "APP案件还款记录")
public class CasePayApplyAppController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(CasePayApplyAppController.class);

    @Inject
    CasePayApplyRepository casePayApplyRepository;

    @Inject
    CaseInfoRepository caseInfoRepository;

    @Inject
    CaseInfoService caseInfoService;

    @Inject
    CaseAssistRepository caseAssistRepository;
    @GetMapping("/getCasePaymentRecordForApp")
    @ApiOperation(value = "根据案件ID获取还款记录", notes = "根据案件ID获取还款记录")
    public ResponseEntity<Page<CasePayApply>> getPaymentRecord(@RequestParam @ApiParam(value = "案件ID", required = true) String id,
                                                               @ApiIgnore Pageable pageable) {
        try {
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(QCasePayApply.casePayApply.caseId.eq(id));
            Page<CasePayApply> page = casePayApplyRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/casePayApplyAppController/getCasePaymentRecord");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", "casePayApply", e.getMessage())).body(null);
        }
    }

    @GetMapping("/getCasePaymentForApp")
    @ApiOperation(value = "根据案件ID获取入账还款", notes = "根据案件ID获取入账还款")
    public ResponseEntity<Page<CasePayApply>> getPayment(@RequestParam @ApiParam(value = "案件ID", required = true) String id,
                                                               @ApiIgnore Pageable pageable){
        try {
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(QCasePayApply.casePayApply.caseId.eq(id));
            builder.and(QCasePayApply.casePayApply.approveResult.eq(CasePayApply.ApproveResult.AGREE.getValue()));
            Page<CasePayApply> page = casePayApplyRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/casePayApplyAppController/getCasePaymentForApp");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", "casePayApply", e.getMessage())).body(null);
        }
    }

    /**
     * @Description APP申请还款
     */
    @PostMapping("/doPay")
    @ApiOperation(value = "APP申请还款", notes = "APP申请还款")
    public ResponseEntity<Void> doTelPay(@RequestBody PayApplyParams payApplyParams,
                                         @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to apply payment");
        User user = null;
        try {
            user = getUserByToken(token);
            caseInfoService.doPay(payApplyParams, user);
            CaseAssist one = caseAssistRepository.findOne(QCaseAssist.caseAssist.caseId.id.eq(payApplyParams.getCaseId())
                    .and(QCaseAssist.caseAssist.assistStatus.ne(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())));
            if (Objects.nonNull(one) && !Objects.equals(one.getAssistStatus(),CaseInfo.AssistStatus.ASSIST_COLLECTING.getValue())) {
                one.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COLLECTING.getValue());
                CaseInfo caseInfo = one.getCaseId();
                caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COLLECTING.getValue());
                one.setCaseId(caseInfo);
                caseAssistRepository.save(one);
                caseInfoRepository.save(caseInfo);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("申请还款成功", "CasePayApply")).body(null);
        }catch(Exception e){
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "", e.getMessage())).body(null);
        }
    }

    /**
     * @Description APP还款撤回
     */
    @GetMapping("/payWithdrawForApp")
    @ApiOperation(value = "APP还款撤回", notes = "APP还款撤回")
    public ResponseEntity<Void> telWithdraw(@RequestParam @ApiParam(value = "还款审批ID", required = true) String id,
                                            @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to withdraw by {payApplyId}", id);
        User user = null;
        try {
            user = getUserByToken(token);
            caseInfoService.payWithdraw(id, user);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("还款撤回成功", "CasePayApply")).body(null);
        }catch(Exception e){
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "", e.getMessage())).body(null);
        }
    }

    @GetMapping("/getBackMoneyDetails")
    @ApiOperation(value = "查询催收员的回款详情APP调用", notes = "查询催收员的回款详情APP调用")
    public ResponseEntity getBackMoneyDetails(@RequestHeader(value = "X-UserToken") String token, Pageable pageable){
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "Userexists", e.getMessage())).body(null);
        }
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QCasePayApply.casePayApply.applyUserName.eq(user.getUserName()));
        builder.and(QCasePayApply.casePayApply.approveStatus.eq(CasePayApply.ApproveStatus.AUDIT_AGREE.getValue()));
        Page<CasePayApply> page = casePayApplyRepository.findAll(builder, pageable);
        if(null == page || page.getContent().isEmpty()){
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("该用户没有回款信息", "")).body(null);
        }
        List<BackMoneyModel> backMoneyModels = new ArrayList<>();
        for(CasePayApply casePayApply : page){
            CaseInfo caseInfo = caseInfoRepository.getOne(casePayApply.getCaseId());
            if(Objects.isNull(caseInfo)){
                throw new RuntimeException("案件ID为" + casePayApply.getCaseId() + "的案件未找到");
            }
            BackMoneyModel backMoneyModel = new BackMoneyModel();
            backMoneyModel.setPersonalName(caseInfo.getPersonalInfo().getName());
            backMoneyModel.setCollectionStatus(caseInfo.getCollectionStatus());
            backMoneyModel.setPayWay(casePayApply.getPayWay());
            backMoneyModel.setCreateTime(casePayApply.getOperatorDate());
            backMoneyModel.setPayamt(casePayApply.getApplyPayAmt());
            backMoneyModel.setOverdueDays(caseInfo.getOverdueDays());
            backMoneyModel.setPaystatus(caseInfo.getPayStatus());
            backMoneyModels.add(backMoneyModel);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("该用户没有回款信息", "")).body(backMoneyModels);
    }

    @GetMapping("/getPayProof")
    @ApiOperation(value = "查看凭证", notes = "查看凭证")
    public ResponseEntity<List<UploadFile>> getPayProof(@RequestParam @ApiParam(value = "还款审批ID") String casePayId) {
        log.debug("REST request to get payment proof");
        try {
            List<UploadFile> uploadFiles = caseInfoService.getRepaymentVoucher(casePayId);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("下载成功", "UploadFile")).body(uploadFiles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("下载失败", "uploadFile", e.getMessage())).body(null);
        }
    }
}
