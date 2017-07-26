package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.ReDistributionParams;
import cn.fintecher.pangolin.business.repository.CaseAssistRepository;
import cn.fintecher.pangolin.business.repository.UserRepository;
import cn.fintecher.pangolin.business.service.CaseInfoService;
import cn.fintecher.pangolin.entity.CaseAssist;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.inject.Inject;
import java.util.Objects;

/**
 * @Author : sunyanping
 * @Description : 案件协催
 * @Date : 2017/7/20.
 */
@RestController
@RequestMapping("/api/caseAssistController")
@Api(value = "CaseAssistController", description = "案件协催")
public class CaseAssistController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(CaseAssistApplyController.class);

    @Inject
    private CaseInfoService caseInfoService;
    @Inject
    private CaseAssistRepository caseAssistRepository;
    @Inject
    private UserRepository userRepository;


    @PutMapping("/assignCaseAssist")
    @ApiOperation(value = "协催案件分配", notes = "协催案件分配")
    public ResponseEntity assignCaseAssist(@RequestParam("caseAssistId") @ApiParam("协催案件ID") String caseAssistId,
                                           @RequestParam("assistorId") @ApiParam("要分配的协催员ID") String assistorId,
                                           @RequestHeader(value = "X-UserToken") String token) {
        log.debug("Rest request to assignCaseAssist");
        // token验证
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(null, "idexists", e.getMessage())).body(null);
        }

        CaseAssist caseAssist = caseAssistRepository.findOne(caseAssistId);

        User assistor = userRepository.findOne(assistorId);

        //更改协催案件信息
//        caseAssist.setDepartId(assistor.getDepartment().getId()); //协催部门ID
//        caseAssist.setCompanyCode(assistor.getCompanyCode()); //协催公司码
//        caseAssist.setAssistCollector(assistor); //协催员
        //更改案件信息
//        CaseInfo caseInfo = caseAssist.getCaseId();
//        caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_WAIT_ACC.getValue()); //协催状态
//        caseInfo.setAssistCollector(assistor); //协催员
//        caseAssist.setCaseId(caseInfo);

        // 协催分配
        ReDistributionParams reDistributionParams = new ReDistributionParams();
        reDistributionParams.setCaseId(caseAssist.getCaseId().getId());//案件ID
        reDistributionParams.setUserName(assistor.getUserName()); //协催员用户名
        reDistributionParams.setIsAssist(true);
        caseInfoService.reDistribution(reDistributionParams, user);
//        caseAssistRepository.save(caseAssist);
        return ResponseEntity.ok().body("分配成功!");
    }

    @PutMapping("/receiveCaseAssist/{id}")
    @ApiOperation(value = "协催案件抢单", notes = "协催案件抢单")
    public ResponseEntity receiveCaseAssist(@PathVariable("id") @ApiParam("协催案件ID") String id,
                                            @RequestHeader(value = "X-UserToken") String token) {
        log.debug("Rest request to receiveCaseAssist");
        // token验证
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseAssistController", "receiveCaseAssist", e.getMessage())).body(null);
        }

        CaseAssist caseAssist = caseAssistRepository.findOne(id);
        if (Objects.isNull(caseAssist)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseAssistController", "receiveCaseAssist", "该协催案件不存在!")).body(null);
        }

        // 若案件状态为 协催待分配 则可以抢单
        if (Objects.equals(caseAssist.getAssistStatus(), CaseInfo.AssistStatus.ASSIST_WAIT_ASSIGN)) {
            synchronized (this) {
                CaseAssist caseAssist1 = caseAssistRepository.findOne(id);
                if (Objects.equals(caseAssist1.getAssistStatus(), CaseInfo.AssistStatus.ASSIST_WAIT_ASSIGN)) {
                    //更改协催案件信息
                    caseAssist.setDepartId(user.getDepartment().getId()); //协催部门ID
                    caseAssist.setCompanyCode(user.getCompanyCode()); //协催公司码
                    caseAssist.setAssistCollector(user); //协催员
                    //更改案件信息
                    CaseInfo caseInfo = caseAssist.getCaseId();
                    caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_WAIT_ACC.getValue()); //协催状态
                    caseInfo.setAssistCollector(user); //协催员
                    caseAssist.setCaseId(caseInfo);
                    caseAssistRepository.save(caseAssist);
                } else {
                    return ResponseEntity.ok().body("抢单失败!");
                }
            }
            return ResponseEntity.ok().body("抢单成功!");
        }
        return ResponseEntity.ok().body("已被抢单!");
    }

    @GetMapping("/findAllCaseAssist")
    @ApiOperation(value = "获取所有协催案件", notes = "获取所有协催案件")
    public ResponseEntity<Page<CaseAssist>> findAllCaseAssist(@QuerydslPredicate Predicate predicate,
                                                              @ApiIgnore Pageable pageable) {
        log.debug("Rest request to findAllCaseAssist");
        BooleanBuilder exp = new BooleanBuilder(predicate);
        Page<CaseAssist> page = caseAssistRepository.findAll(exp, pageable);
        return ResponseEntity.ok().body(page);
    }
}
