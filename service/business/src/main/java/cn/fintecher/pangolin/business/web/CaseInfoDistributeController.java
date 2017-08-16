package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.AccCaseInfoDisModel;
import cn.fintecher.pangolin.business.model.UserInfoModel;
import cn.fintecher.pangolin.business.repository.CaseInfoDistributedRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.UserRepository;
import cn.fintecher.pangolin.business.service.CaseInfoDistributedService;
import cn.fintecher.pangolin.business.service.CaseInfoService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author: PeiShouWen
 * @Description: 案件分配
 * @Date 15:50 2017/8/7
 */
@RestController
@RequestMapping(value = "/api/caseInfoDistributeController")
@Api(value = "案件分配", description = "案件分配")
public class CaseInfoDistributeController extends BaseController {

    private static final String ENTITY_NAME = "caseInfoDistributeController";
    Logger logger=LoggerFactory.getLogger(CaseInfoDistributeController.class);

    @Autowired
    CaseInfoService caseInfoService;
    @Inject
    CaseInfoDistributedRepository caseInfoDistributedRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    CaseInfoRepository caseInfoRepository;
    @Inject
    CaseInfoDistributedService caseInfoDistributedService;

    @RequestMapping(value = "/distributeCeaseInfo", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    @ApiOperation(value = "案件分配(机构时传入机构的ID)", notes = "案件分配")
    public ResponseEntity distributeCeaseInfo(@RequestBody AccCaseInfoDisModel accCaseInfoDisModel,
                                              @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        try {
            User user=getUserByToken(token);
            caseInfoDistributedService.distributeCeaseInfo(accCaseInfoDisModel, user);
            return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功",ENTITY_NAME)).body(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            String msg= Objects.isNull(e.getMessage()) ? "系统异常" : e.getMessage();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,"error",msg)).body(null);
        }

    }

    @GetMapping("/findCaseInfoDistribute")
    @ApiOperation(value = "案件分配页面（多条件查询）", notes = "案件分配页面（多条件查询）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseInfoDistributed>> findCaseInfoDistribute(@QuerydslPredicate(root = CaseInfoDistributed.class) Predicate predicate,
                                                                            @ApiIgnore Pageable pageable,
                                                                            @RequestHeader(value = "X-UserToken") String token) {
        logger.debug("REST request to findCaseInfoDistribute");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("CaseInfoDistributeController", "findCaseInfoDistribute", e.getMessage()))
                    .body(null);
        }
        try {
            QCaseInfoDistributed qd = QCaseInfoDistributed.caseInfoDistributed;
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(qd.companyCode.eq(user.getCompanyCode()));
            Page<CaseInfoDistributed> page = caseInfoDistributedRepository.findAll(builder, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("CaseInfoDistributeController", "findCaseInfoDistribute", "系统异常!"))
                    .body(null);
        }
    }

    @PostMapping("/getUserInfoByUserId")
    @ApiOperation(value = "查找用户的案件数", notes = "查找用户的案件数")
    public ResponseEntity getAccReceivePoolByUserId(@ApiParam(value = "用户userId组", required = true) @RequestBody UserInfoModel userIds) {
        try {
            List<UserInfoModel> list = new ArrayList<>();
            List<String> userIds1 = userIds.getUserIds();
            for (String userId : userIds1) {
                UserInfoModel userInfo = new UserInfoModel();
                User user = userRepository.findOne(userId);
                userInfo.setUserId(user.getId());
                userInfo.setUserName(user.getUserName());
                userInfo.setCollector(user.getRealName());
                Integer caseCountOnUser = caseInfoDistributedRepository.getCaseCountOnUser(user.getId());
                userInfo.setCaseCount(caseCountOnUser);
                list.add(userInfo);
            }
            return ResponseEntity.ok().body(list);
        } catch (Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("CaseInfoDistributeController", "getAccReceivePoolByUserId", "系统异常!"))
                    .body(null);
        }
    }

    @GetMapping("/getCaseCountOnDept")
    @ApiOperation(value = "查找部门下的案件数", notes = "查找部门下的案件数")
    public ResponseEntity getCaseCountOnDept(@RequestParam("deptCode") @ApiParam("部门Code") String deptCode,
                                             @RequestHeader(value = "X-UserToken") String token) {
        logger.debug("REST request to getCaseCountOnDept");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("CaseInfoDistributeController", "getCaseCountOnDept", e.getMessage()))
                    .body(null);
        }
        try {
            QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(qCaseInfo.companyCode.eq(user.getCompanyCode()));
            builder.and(qCaseInfo.collectionStatus.notIn(CaseInfo.CollectionStatus.CASE_OVER.getValue()));
            Long count = caseInfoRepository.count(builder);
            return ResponseEntity.ok().body(count);
        } catch (Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("CaseInfoDistributeController", "getCaseCountOnDept", "系统异常!"))
                    .body(null);
        }
    }

    @GetMapping("/getCaseInfoDistributedDetails")
    @ApiOperation(value = "案件详情查询操作", notes = "案件详情查询操作")
    public ResponseEntity<CaseInfoDistributed> getCaseInfoDistributedDetails(@RequestParam("id") String id){
        CaseInfoDistributed caseInfoDistributed= caseInfoDistributedRepository.findOne(id);
        return ResponseEntity.ok().body(caseInfoDistributed);
    }
}
