package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.PaymentModel;
import cn.fintecher.pangolin.business.model.PaymentParams;
import cn.fintecher.pangolin.business.repository.CasePayApplyRepository;
import cn.fintecher.pangolin.business.service.PaymentService;
import cn.fintecher.pangolin.entity.CasePayApply;
import cn.fintecher.pangolin.entity.QCasePayApply;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.apache.commons.collections4.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
 * @author : xiaqun
 * @Description : 还款审批
 * @Date : 9:06 2017/7/29
 */

@RestController
@RequestMapping("/api/PaymentController")
@Api(value = "PaymentController", description = "还款审批")
public class PaymentController extends BaseController {
    final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private static final String ENTITY_CASE_PAY_APPLY = "CasePayApply";

    @Inject
    CasePayApplyRepository casePayApplyRepository;

    @Inject
    PaymentService paymentService;

    /**
     * @Description 多条件查询减免审批记录
     */
    @GetMapping("/getAllDerate")
    @ApiOperation(value = "多条件查询减免审批记录", notes = "多条件查询减免审批记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CasePayApply>> getAllDerate(@QuerydslPredicate(root = CasePayApply.class) Predicate predicate,
                                                           @ApiIgnore Pageable pageable,
                                                           @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
                                                           @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get all derate record");
        List<Integer> list = new ArrayList<>();
        list.add(CasePayApply.ApproveStatus.DERATE_TO_AUDIT.getValue()); //减免待审核
        list.add(CasePayApply.ApproveStatus.DERATE_AUDIT_REJECT.getValue()); //减免审核驳回
        try {
            User tokenUser = getUserByToken(token);
            BooleanBuilder builder = new BooleanBuilder(predicate);
            if (Objects.isNull(tokenUser.getCompanyCode())) {//超级管理员默认查所有记录
                if (Objects.nonNull(companyCode)) {
                    builder.and(QCasePayApply.casePayApply.companyCode.eq(companyCode));
                }
            } else {
                builder.and(QCasePayApply.casePayApply.companyCode.eq(tokenUser.getCompanyCode()));
            }
            builder.and(QCasePayApply.casePayApply.approveStatus.in(list)); //只查限定状态的记录
            builder.and(QCasePayApply.casePayApply.approveStatus.ne(CasePayApply.ApproveStatus.REVOKE.getValue())); //不查撤回的记录
            Page<CasePayApply> page = casePayApplyRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/PaymentController/getAllDerate");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASE_PAY_APPLY, "casePayApply", "查询失败")).body(null);
        }
    }

    /**
     * @Description 多条件查询还款记录
     */
    @GetMapping("/getAllPayment")
    @ApiOperation(value = "多条件查询还款审批记录", notes = "多条件查询还款审批记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CasePayApply>> getAllPayment(@QuerydslPredicate(root = CasePayApply.class) Predicate predicate,
                                                            @ApiIgnore Pageable pageable,
                                                            @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
                                                            @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get all payment record");
        List<Integer> list = new ArrayList<>();
        list.add(CasePayApply.ApproveStatus.PAY_TO_AUDIT.getValue()); //还款待审核
        list.add(CasePayApply.ApproveStatus.AUDIT_REJECT.getValue()); //审核拒绝
        list.add(CasePayApply.ApproveStatus.AUDIT_AGREE.getValue()); //审核通过
        try {
            User tokenUser = getUserByToken(token);
            BooleanBuilder builder = new BooleanBuilder(predicate);
            if (Objects.isNull(tokenUser.getCompanyCode())) {//超级管理员默认查所有记录
                if (Objects.nonNull(companyCode)) {
                    builder.and(QCasePayApply.casePayApply.companyCode.eq(companyCode));
                }
            } else {
                builder.and(QCasePayApply.casePayApply.companyCode.eq(tokenUser.getCompanyCode()));
            }
            builder.and(QCasePayApply.casePayApply.approveStatus.in(list)); //只查限定状态的记录
            builder.and(QCasePayApply.casePayApply.approveStatus.ne(CasePayApply.ApproveStatus.REVOKE.getValue())); //不查撤回状态记录
            builder.and(QCasePayApply.casePayApply.deptCode.startsWith(tokenUser.getDepartment().getCode())); //添加部门过滤条件
            Page<CasePayApply> page = casePayApplyRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/PaymentController/getAllPayment");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASE_PAY_APPLY, "casePayApply", "查询失败")).body(null);
        }
    }

    /**
     * @Description 还款信息展示
     */
    @GetMapping("/getPaymentInfo")
    @ApiOperation(value = "还款信息展示", notes = "还款信息展示")
    public ResponseEntity<PaymentModel> getPaymentInfo(@RequestParam @ApiParam(value = "还款审批记录ID", required = true) String casePayApplyId) {
        log.debug("REST request to get payment information by {casePayApplyId}", casePayApplyId);
        try {
            PaymentModel paymentModel = paymentService.getPaymentInfo(casePayApplyId);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("还款信息展示成功", ENTITY_CASE_PAY_APPLY)).body(paymentModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASE_PAY_APPLY, "casePayApply", "还款信息展示失败")).body(null);
        }
    }

    /**
     * @Description 还款审批
     */
    @PutMapping("/approvalPayment")
    @ApiOperation(value = "还款审批", notes = "还款审批")
    public ResponseEntity<Void> approvalPayment(@RequestBody PaymentParams paymentParams,
                                                @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get approval payment");
        try {
            User tokenUser = getUserByToken(token);
            paymentService.approvalPayment(paymentParams, tokenUser);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("还款审批成功", ENTITY_CASE_PAY_APPLY)).body(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASE_PAY_APPLY, "casePayApply", "还款审批失败")).body(null);
        }
    }

    /**
     * @Description 导出还款记录
     */
    @GetMapping("/exportCasePayApply")
    @ApiOperation(value = "导出还款记录", notes = "导出还款记录")
    public ResponseEntity<String> exportCasePayApply(@QuerydslPredicate(root = CasePayApply.class) Predicate predicate,
                                                     @RequestHeader(value = "X-UserToken") String token,
                                                     @RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode) {
        log.debug("REST request to export casePayApply");
        List<Integer> list = new ArrayList<>();
        list.add(CasePayApply.ApproveStatus.PAY_TO_AUDIT.getValue()); //还款待审核
        list.add(CasePayApply.ApproveStatus.AUDIT_REJECT.getValue()); //审核拒绝
        list.add(CasePayApply.ApproveStatus.AUDIT_AGREE.getValue()); //审核通过
        try {
            User tokenUser = getUserByToken(token);
            BooleanBuilder builder = new BooleanBuilder(predicate);
            if (Objects.isNull(tokenUser.getCompanyCode())) { //超级管理员
                if (Objects.nonNull(companyCode)) {
                    builder.and(QCasePayApply.casePayApply.companyCode.eq(companyCode)); //只查登陆人公司的记录
                }
            } else { //不是超级管理员
                builder.and(QCasePayApply.casePayApply.companyCode.eq(tokenUser.getCompanyCode())); //只查登陆人公司的记录
            }
            builder.and(QCasePayApply.casePayApply.approveStatus.in(list)); //只查限定状态的记录
            builder.and(QCasePayApply.casePayApply.approveStatus.ne(CasePayApply.ApproveStatus.REVOKE.getValue())); //不查撤回的记录
            Iterator<CasePayApply> casePayApplies = casePayApplyRepository.findAll(builder, new Sort(Sort.Direction.DESC, "applyDate")).iterator();
            List<CasePayApply> casePayApplyList = IteratorUtils.toList(casePayApplies);
            String url = paymentService.exportCasePayApply(casePayApplyList);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("导出成功", ENTITY_CASE_PAY_APPLY)).body(url);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASE_PAY_APPLY, "casePayApply", e.getMessage())).body(null);
        }
    }
}