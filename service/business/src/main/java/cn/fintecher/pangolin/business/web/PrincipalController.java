package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.CaseInfoDistributedRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoExceptionRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.PrincipalRepository;
import cn.fintecher.pangolin.business.service.BatchSeqService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.EntityUtil;
import cn.fintecher.pangolin.entity.util.LabelValue;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 11:46 2017/7/14
 */
@RestController
@RequestMapping("/api/principalController")
@Api(value = "PrincipalController", description = "委托方操作")
public class PrincipalController extends BaseController {

    private final Logger logger = LoggerFactory.getLogger(PrincipalController.class);
    private static final String ENTITY_NAME = "PrincipalController";
    @Autowired
    private PrincipalRepository principalRepository;
    @Autowired
    private BatchSeqService batchSeqService;
    @Autowired
    private CaseInfoRepository caseInfoRepository;
    @Autowired
    private CaseInfoDistributedRepository caseInfoDistributedRepository;
    @Autowired
    private CaseInfoExceptionRepository caseInfoExceptionRepository;

    @GetMapping("/getPrincipalPageList")
    @ApiOperation(value = "获取委托方分页查询", notes = "获取委托方分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<Principal>> getPrincipalPageList(@QuerydslPredicate(root = Principal.class) Predicate predicate,
                                                                @ApiIgnore Pageable pageable,
                                                                @RequestHeader(value = "X-UserToken") String token,
                                                                @RequestParam(value = "companyCode",required = false) String companyCode) {
        logger.debug("REST request to getPrincipalPageList");
        User user ;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage())).body(null);
        }
        try {
            QPrincipal qPrincipal = QPrincipal.principal;
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(qPrincipal.flag.eq(Principal.deleteStatus.START.getDeleteCode())); //查询未删除的
            if (Objects.isNull(user.getCompanyCode())) {
                if (StringUtils.isNotBlank(companyCode)) {
                    builder.and(qPrincipal.companyCode.eq(companyCode));
                }
            } else {
                builder.and(qPrincipal.companyCode.eq(user.getCompanyCode()));
            }
            Page<Principal> page = principalRepository.findAll(builder, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "", Constants.ERROR_MESSAGE)).body(null);
        }

    }

    @GetMapping("/getPrincipalList")
    @ApiOperation(value = "获取所有委托方信息", notes = "获取所有委托方信息")
    public ResponseEntity<List<Principal>> getPrincipalPageList(@RequestParam(required = false) String companyCode,
                                                                @RequestHeader(value = "X-UserToken") String token) {
        logger.debug("REST request to get all Principal");
        User user ;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage())).body(null);
        }
        QPrincipal qPrincipal = QPrincipal.principal;
        BooleanBuilder builder = new BooleanBuilder();
        if (Objects.isNull(user.getCompanyCode())) {
            if (StringUtils.isNotBlank(companyCode)) {
                builder.and(qPrincipal.companyCode.eq(companyCode));
            }
        } else {
            builder.and(qPrincipal.companyCode.eq(user.getCompanyCode()));
        }
        builder.and(qPrincipal.flag.eq(Principal.deleteStatus.START.getDeleteCode()));
        Iterator<Principal> principalIterator = principalRepository.findAll(builder).iterator();
        List<Principal> principalList = IteratorUtils.toList(principalIterator);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功", "ENTITY_NAME")).body(principalList);
    }

    /**
     * @Description : 删除委托方
     */
    @DeleteMapping("/deletePrincipal")
    @ApiOperation(value = "删除委托方", notes = "删除委托方")
    public ResponseEntity<Principal> deletePrincipal(@RequestParam String id,
                                                     @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
        Iterator<CaseInfo> caseInfoIterator = caseInfoRepository.findAll(qCaseInfo.principalId.id.eq(id)).iterator();
        if (caseInfoIterator.hasNext()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "The client's association case is not allowed to be deleted", "该委托方有关联案件不允许删除")).body(null);
        }
        QCaseInfoDistributed qCaseInfoDistributed = QCaseInfoDistributed.caseInfoDistributed;
        Iterator<CaseInfoDistributed> caseInfoDistributedIterator = caseInfoDistributedRepository.findAll(qCaseInfoDistributed.principalId.id.eq(id)).iterator();
        if (caseInfoDistributedIterator.hasNext()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "The client's association case is not allowed to be deleted", "该委托方有关联分配的案件不允许删除")).body(null);
        }

        Principal principal4 = principalRepository.findOne(id);
        QCaseInfoException qCaseInfoException = QCaseInfoException.caseInfoException;
        Iterator<CaseInfoException> CaseInfoExceptionIterator = caseInfoExceptionRepository.findAll(qCaseInfoException.prinCode.eq(principal4.getCode())).iterator();
        if (CaseInfoExceptionIterator.hasNext()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "The client's association case is not allowed to be deleted", "该委托方有关联异常案件不允许删除")).body(null);
        }
        Principal principal = principalRepository.findOne(id);
        principal.setFlag(Principal.deleteStatus.BLOCK.getDeleteCode());
        Principal principal1 = principalRepository.save(principal);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(null);
    }

    /**
     * @Description : 新增/修改委托方信息
     */
    @PostMapping("/createPrincipal")
    @ApiOperation(value = "新增/修改委托方信息", notes = "新增/修改委托方信息")
    public ResponseEntity<Principal> createOutsource(@Validated @ApiParam("委托方对象") @RequestBody Principal request,
                                                     @RequestHeader(value = "X-UserToken") String token) {
        request = (Principal) EntityUtil.emptyValueToNull(request);
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        if (Objects.isNull(request.getId())) {
            //验证委外方是否重名
            QPrincipal qPrincipal = QPrincipal.principal;
            Iterator<Principal> principalIterator = principalRepository.findAll(qPrincipal.name.eq(request.getName()).and(qPrincipal.flag.eq(Principal.deleteStatus.START.getDeleteCode())).and(qPrincipal.companyCode.eq(request.getCompanyCode()))).iterator();
            if (principalIterator.hasNext()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                        "The outsourcename is not allowed to be used", "该名称已被占用，请重新输入")).body(null);
            }
            LabelValue labelValue = batchSeqService.nextSeq(Constants.PRIN_SEQ, Outsource.principalStatus.PRINCODE_DIGIT.getPrincipalCode());
            if (Objects.isNull(labelValue)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                        "The client code for failure", "委托方编号获取失败")).body(null);
            }
            String code = labelValue.getValue();
            String letter;
            switch (request.getType()) {
                case 155:
                    letter = "P";
                    break;
                case 156:
                    letter = "B";
                    break;
                case 157:
                    letter = "I";
                    break;
                default:
                    letter = "O";
                    break;
            }
            String subCode = code.substring(1);
            //委外方编码
            request.setCode(letter + subCode);
            //启用状态0
            request.setFlag(Outsource.deleteStatus.START.getDeleteCode());
            request.setOperatorTime(ZWDateUtil.getNowDateTime()); //创建时间
            request.setUser(user);
            Principal principalReturn = principalRepository.save(request);
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(principalReturn);
        } else {
            Principal principal = principalRepository.findOne(request.getId());
            //验证委外方是否重名
            QPrincipal qPrincipal = QPrincipal.principal;
            Iterator<Principal> principalIterator = principalRepository.findAll(qPrincipal.name.eq(request.getName()).and(qPrincipal.flag.eq(Outsource.deleteStatus.START.getDeleteCode())).and(qPrincipal.id.ne(request.getId()))).iterator();
            if (principalIterator.hasNext()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                        "The outsourcename is not allowed to be used", "该名字不允许被使用")).body(null);
            }
            BeanUtils.copyProperties(request, principal);
            Principal principalReturn = principalRepository.save(principal);
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(principalReturn);
        }
    }
}
