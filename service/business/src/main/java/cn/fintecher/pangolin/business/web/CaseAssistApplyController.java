package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.ApplyAssistModel;
import cn.fintecher.pangolin.business.model.AssistApplyApproveModel;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.business.service.UserService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.message.SendReminderMessage;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @Author : sunyanping
 * @Description : 案件协催申请操作
 * @Date : 2017/7/17.
 */
@RestController
@RequestMapping("/api/caseAssistApplyController")
@Api(value = "CaseAssistApplyController", description = "案件协催申请操作")
public class CaseAssistApplyController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(CaseAssistApplyController.class);

    @Inject
    private CaseInfoRepository caseInfoRepository;
    @Inject
    private CaseAssistApplyRepository caseAssistApplyRepository;
    @Inject
    private RestTemplate restTemplate;
    @Inject
    private UserRepository userRepository;
    @Inject
    private RoleRepository roleRepository;
    @Inject
    private CaseAssistRepository caseAssistRepository;
    @Inject
    private UserService userService;

    @GetMapping("/findAllApplyByCaseNumber/{caseNumber}")
    @ApiOperation(value = "查询某个案件的所有协催申请", notes = "查询某个案件的所有协催申请")
    public ResponseEntity<Page<CaseAssistApply>> findAllApplyByCaseNumber(@PathVariable("caseNumber") @ApiParam("案件编号") String caseNumber,
                                                                          @ApiIgnore Pageable pageable) {
        log.debug("Rest request get caseAssistApply by caseNumber");
        try {
            QCaseAssistApply qCaseAssistApply = QCaseAssistApply.caseAssistApply;
            BooleanExpression exp = qCaseAssistApply.caseNumber.eq(caseNumber);
            Page<CaseAssistApply> page = caseAssistApplyRepository.findAll(exp, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", "findAllApplyByCaseNumber", e.getMessage())).body(null);
        }
    }

    @GetMapping("/findAllTelPassedApply")
    @ApiOperation(value = "外访审批协催申请页面条件查询", notes = "外访审批协催申请页面条件查询")
    public ResponseEntity<Page<CaseAssistApply>> findAllTelPassedApply(@QuerydslPredicate Predicate predicate,
                                                                       @ApiIgnore Pageable pageable) {
        log.debug("Rest request get all tel passed apply");
        try {
            QCaseAssistApply qCaseAssistApply = QCaseAssistApply.caseAssistApply;
            // 查出所有电催审批通过的
            BooleanBuilder exp = new BooleanBuilder(predicate);
            exp.and(qCaseAssistApply.approvePhoneResult.eq(CaseAssistApply.ApproveResult.TEL_PASS.getValue()));
            Page<CaseAssistApply> page = caseAssistApplyRepository.findAll(exp, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", "findAllTelPassedApply", e.getMessage())).body(null);
        }
    }

    @GetMapping("/findAllApply")
    @ApiOperation(value = "电催审批协催申请页面条件查询", notes = "电催审批协催申请页面条件查询")
    public ResponseEntity<Page<CaseAssistApply>> findAllApply(@QuerydslPredicate Predicate predicate,
                                                              @ApiIgnore Pageable pageable) {
        log.debug("Rest request get all CaseAssistApply of tel passed");
        try {
            QCaseAssistApply qCaseAssistApply = QCaseAssistApply.caseAssistApply;
            BooleanBuilder exp = new BooleanBuilder(predicate);
            // 查出所有电催待审批的案件
            Page<CaseAssistApply> page = caseAssistApplyRepository.findAll(exp, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", "findAllTelPassedApply", e.getMessage())).body(null);
        }
    }

    @PostMapping("/assistApplyVisitApprove/{id}")
    @ApiOperation(value = "协催申请外访审批", notes = "协催申请外访审批")
    public ResponseEntity<CaseAssistApply> assistApplyVisitApprove(@RequestBody AssistApplyApproveModel approveModel,
                                                                   @PathVariable("id") @ApiParam("案件协催申请ID") String id,
                                                                   @RequestHeader(value = "X-UserToken") String token) throws Exception{
        log.debug("Rest request get all  CaseAssistApply");
        User user = getUserByToken(token);

        try {
            CaseAssistApply apply = caseAssistApplyRepository.findOne(id);
            if (Objects.isNull(apply)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseAssistApplyController", "assistApplyApprove", "申请不存在!")).body(null);
            }
            String caseId = apply.getCaseId();
            CaseInfo caseInfo = caseInfoRepository.findOne(caseId);
            //修改案件协催申请信息
            Integer approveResult = approveModel.getApproveResult();
            CaseAssist caseAssist = new CaseAssist();
            // 审批通过
            if (approveResult == CaseAssistApply.ApproveResult.VISIT_PASS.getValue()) {
                // 案件协催表增加记录
                caseAssist.setCaseId(caseInfo);
                caseAssist.setAssistWay(apply.getAssistWay()); //协催方式
                caseAssist.setAssistStatus(CaseInfo.AssistStatus.ASSIST_WAIT_ASSIGN.getValue()); //协催状态（协催待分配）
                caseAssist.setCaseFlowinTime(new Date()); //流入时间
                caseAssist.setLatelyCollector(caseInfo.getAssistCollector()); //上一个协催员
                caseAssist.setCurrentCollector(caseInfo.getCurrentCollector()); //当前催收员
                //修该案件中的案件协催状态为协催待分配
                caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_WAIT_ASSIGN.getValue());
                // TODO 提醒电催主管和申请人协催申请审批通过
                String title = "协催申请审批未通过!";
                String content = "案件["+apply.getCaseNumber()+"]申请的协催已审批通过!";
                String applyUserId = userRepository.findByUserName(apply.getApplyUserName()).getId();
                String telUserId = userRepository.findByUserName(apply.getApprovePhoneUser()).getId();
                sendAssistApproveReminder(title,content,applyUserId);
                sendAssistApproveReminder(title,content,telUserId);
            }
            // 审批拒绝
            if (approveResult == CaseAssistApply.ApproveResult.VISIT_REJECT.getValue()) {
                //修该案件中的案件协催状态为协催拒绝
                caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_REFUSED.getValue());
                // TODO 提醒电催主管很申请人协催申请审批未通过
                String title = "协催申请审批未通过!";
                String content = "案件["+apply.getCaseNumber()+"]申请的协催被外访拒绝!";
                String applyUserId = userRepository.findByUserName(apply.getApplyUserName()).getId();
                String telUserId = userRepository.findByUserName(apply.getApprovePhoneUser()).getId();
                sendAssistApproveReminder(title,content,applyUserId);
                sendAssistApproveReminder(title,content,telUserId);
            }
            // 修改申请表信息
            apply.setApproveStatus(CaseAssistApply.ApproveStatus.VISIT_COMPLETE.getValue()); //审批状态
            apply.setApproveOutResult(approveResult); //审批结果
            apply.setApproveOutMemo(approveModel.getApproveMemo()); //审批意见
            apply.setApproveOutUser(user.getUserName()); //审批人
            apply.setApproveOutName(user.getRealName()); //审批人姓名
            apply.setApproveOutDatetime(new Date()); //审批时间
            CaseAssistApply save = caseAssistApplyRepository.save(apply);
            caseAssistRepository.save(caseAssist);
            return ResponseEntity.ok().body(save);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("审批失败", "assistApplyVisitApprove", e.getMessage())).body(null);
        }

    }

    @PostMapping("/assistApplyTelApprove/{id}")
    @ApiOperation(value = "协催申请电催审批", notes = "协催申请电催审批")
    public ResponseEntity assistApplyTelApprove(@RequestBody AssistApplyApproveModel approveModel,
                                                @PathVariable("id") @ApiParam("案件协催申请ID") String id,
                                                @RequestHeader(value = "X-UserToken") String token) throws Exception {
        log.debug("Rest request to assistApplyTelApprove");
        User user = getUserByToken(token);
        try {
            CaseAssistApply apply = caseAssistApplyRepository.findOne(id);
            if (Objects.isNull(apply)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseAssistApplyController", "assistApplyTelApprove", "申请不存在!")).body(null);
            }
            // 审批状态为电催审批完成的不能再审批
            Integer approveStatus = apply.getApproveStatus();
            if (Objects.equals(approveStatus, CaseAssistApply.ApproveStatus.TEL_COMPLETE.getValue())) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseAssistApplyController", "assistApplyTelApprove", "该申请已经审批过，不能再审批!")).body(null);
            }
            String caseId = apply.getCaseId();
            CaseInfo caseInfo = caseInfoRepository.findOne(caseId);
            if (Objects.isNull(caseInfo)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseAssistApplyController", "assistApplyTelApprove", "案件不存在!")).body(null);
            }
            //修改案件协催申请信息
            Integer approveResult = approveModel.getApproveResult(); //审批结果
            // 审批通过
            if (approveResult == CaseAssistApply.ApproveResult.TEL_PASS.getValue()) {
                // TODO 提醒外访主管审批
                String title = "有协催申请需要审批!";
                String content = "电催组申请对案件["+apply.getCaseNumber()+"]进行协催，请及时审批!";
                List<User> allUser = userService.getAllUser(user.getCompanyCode(), 2, 0, 0);//公司Code 电催 启用 管理者
                allUser.forEach(u -> sendAssistApproveReminder(title,content,u.getId()));
            }
            // 审批拒绝
            if (approveResult == CaseAssistApply.ApproveResult.TEL_REJECT.getValue()) {
                //修该案件中的案件协催状态为协催拒绝
                caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_REFUSED.getValue());
                // 提醒申请人
                String title = "协催申请被拒绝!";
                String content = "你于["+apply.getApplyDate()+"]申请协催案件["+apply.getCaseNumber()+"]被电催主管["+user.getRealName()+"]拒绝!";
                String userId = userRepository.findByUserName(apply.getApplyUserName()).getId();
                sendAssistApproveReminder(title,content,userId);
            }
            // 修改申请表信息
            apply.setApproveStatus(CaseAssistApply.ApproveStatus.TEL_COMPLETE.getValue()); //审批状态
            apply.setApprovePhoneResult(approveResult); //审批结果
            apply.setApprovePhoneMemo(approveModel.getApproveMemo()); //审批意见
            apply.setApprovePhoneUser(user.getUserName()); //审批人
            apply.setApprovePhoneName(user.getRealName()); //审批人姓名
            apply.setApprovePhoneDatetime(new Date()); //审批时间
            CaseAssistApply save = caseAssistApplyRepository.save(apply);
            return ResponseEntity.ok().body(save);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("审批失败", "assistApplyTelApprove", e.getMessage())).body(null);
        }
    }

    //    @PostMapping("/assistApply/{id}")
//    @ApiOperation(value = "申请协催", notes = "申请协催")
    public ResponseEntity assistApply(@PathVariable("id") @ApiParam("案件ID") String id,
                                      @RequestBody ApplyAssistModel applyModel,
                                      @RequestHeader(value = "X-UserToken") String token) {
        // token验证
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseAssistApplyController", "idexists", e.getMessage())).body(null);
        }

        CaseInfo caseInfo = caseInfoRepository.findOne(id);
        if (Objects.isNull(caseInfo)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseAssistApplyController", "idexists", "案件不存在!")).body(null);
        }
        // 添加一条协催申请
        try {
            CaseAssistApply apply = new CaseAssistApply();
            BeanUtils.copyProperties(caseInfo, apply);
            apply.setCaseId(caseInfo.getId()); // 案件ID
            apply.setPersonalName(caseInfo.getPersonalInfo().getName()); // 客户姓名
            apply.setPersonalId(caseInfo.getPersonalInfo().getId()); // 客户信息ID
            apply.setDepartId(caseInfo.getDepartment().getId()); // 部门ID
            apply.setPrincipalId(caseInfo.getPrincipalId().getId()); // 委托方ID
            apply.setPrincipalName(caseInfo.getPrincipalId().getName()); // 委托方名称
            apply.setAreaId(caseInfo.getArea().getId()); // 省份编号
            apply.setAreaName(caseInfo.getArea().getAreaName()); // 省份名称
            apply.setApplyUserName(user.getUserName()); // 申请人
            apply.setApplyRealName(user.getRealName()); // 申请人姓名
            apply.setApplyDeptName(user.getDepartment().getName()); // 申请人部门名称
            apply.setApplyReason(applyModel.getApplyReason()); // 申请原因
            apply.setApplyDate(new Date()); // 申请时间
//        apply.setApplyInvalidTime(""); // 申请失效时间
            apply.setAssistWay(applyModel.getAssistWay()); // 协催方式
            apply.setProductSeries(caseInfo.getProduct().getProductSeries().getId()); // 产品系列ID
            apply.setProductSeries(caseInfo.getProduct().getId()); // 产品ID
            apply.setProductSeriesName(caseInfo.getProduct().getProductSeries().getSeriesName()); // 产品系列名称
            apply.setProductName(caseInfo.getProduct().getProdcutName()); // 产品名称
            apply.setApproveStatus(CaseAssistApply.ApproveStatus.TEL_APPROVAL.getValue()); // 审批状态
            apply.setCompanyCode(caseInfo.getCompanyCode()); // 公司Code码

            // 修改CaseInfo协催状态
            caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_APPROVEING.getValue());

            caseAssistApplyRepository.save(apply);
            caseInfoRepository.save(caseInfo);

            return ResponseEntity.ok().body(null);
        } catch (BeansException e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().body("系统错误!");
        }
    }

    private void sendAssistApproveReminder(String title,String content,String userId) {
        SendReminderMessage sendReminderMessage = new SendReminderMessage();
        sendReminderMessage.setTitle(title);
        sendReminderMessage.setContent(content);
        sendReminderMessage.setType(ReminderType.ASSIST_APPROVE);
        sendReminderMessage.setCreateTime(new Date());
        sendReminderMessage.setUserId(userId);
        restTemplate.postForLocation("http://reminder-service/api/reminderMessages",sendReminderMessage);
    }
}
