package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.CaseInfoSameModel;
import cn.fintecher.pangolin.business.repository.CaseInfoDistributedRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoExceptionRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.apache.commons.collections4.IterableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author : sunyanping
 * @Description : 异常案件操作
 * @Date : 2017/8/9.
 */
@RestController
@RequestMapping(value = "/api/caseInfoExceptionController")
@Api(value = "CaseInfoExceptionController", description = "异常案件操作")
public class CaseInfoExceptionController extends BaseController{

    private final Logger logger= LoggerFactory.getLogger(CaseInfoExceptionController.class);

    @Inject
    private CaseInfoExceptionRepository caseInfoExceptionRepository;
    @Inject
    private CaseInfoRepository caseInfoRepository;
    @Inject
    private CaseInfoDistributedRepository caseInfoDistributedRepository;

    @GetMapping("/findCaseInfoException")
    @ApiOperation(value = "异常案件页面（多条件查询）", notes = "异常案件页面（多条件查询）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseInfoException>> findCaseInfoException(@QuerydslPredicate(root = CaseInfoException.class) Predicate predicate,
                                                                          @ApiIgnore Pageable pageable,
                                                                          @RequestHeader(value = "X-UserToken") String token) {
        logger.debug("REST request to findCaseInfoException");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("CaseInfoExceptionController", "findCaseInfoException", e.getMessage()))
                    .body(null);
        }
        try {
            QCaseInfoException qd = QCaseInfoException.caseInfoException;
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(qd.companyCode.eq(user.getCompanyCode()));
            Page<CaseInfoException> page = caseInfoExceptionRepository.findAll(builder, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("CaseInfoExceptionController", "findCaseInfoException", "系统异常!"))
                    .body(null);
        }
    }

    @DeleteMapping("/deleteCaseInfoException")
    @ApiOperation(value = "删除异常案件", notes = "删除异常案件")
    public ResponseEntity<Page<CaseInfoException>> deleteCaseInfoException(@RequestParam(value = "cieId") @ApiParam("异常案件ID") String cieId,
                                                                           @RequestHeader(value = "X-UserToken") String token) {
        logger.debug("REST request to deleteCaseInfoException");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("CaseInfoExceptionController", "findCaseInfoException", e.getMessage()))
                    .body(null);
        }
        try {
            caseInfoExceptionRepository.delete(cieId);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("删除成功!","")).body(null);
        } catch (Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("CaseInfoExceptionController", "deleteCaseInfoException", "系统异常!"))
                    .body(null);
        }
    }

    @GetMapping("/findSameCaseInfoByCie")
    @ApiOperation(value = "查询相同案件", notes = "查询相同案件")
    public ResponseEntity<List<CaseInfoSameModel>> findSameCaseInfoByCie(@RequestParam(value = "cieId") @ApiParam("异常案件ID") String cieId,
                                                                         @RequestHeader(value = "X-UserToken") String token) {
        logger.debug("REST request to findSameCaseInfoByCie");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("CaseInfoExceptionController", "findSameCaseInfoByCie", e.getMessage()))
                    .body(null);
        }
        try {
            CaseInfoException caseInfoException = caseInfoExceptionRepository.findOne(cieId);
            // 相同案件的条件
            String companyCode = caseInfoException.getCompanyCode(); //公司
            String productName = caseInfoException.getProductName(); //产品名称
            String idCard = caseInfoException.getIdCard(); //客户身份证号
            String personalName = caseInfoException.getPersonalName();//客户名称
            // 在CaseInfo找相同的案件
            QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(qCaseInfo.companyCode.eq(companyCode));
            builder.and(qCaseInfo.product.prodcutName.eq(productName));
            builder.and(qCaseInfo.personalInfo.idCard.eq(idCard));
            builder.and(qCaseInfo.personalInfo.name.eq(personalName));
            Iterable<CaseInfo> all = caseInfoRepository.findAll(builder);
            List<CaseInfo> caseInfos = IterableUtils.toList(all);

            // 在CaseInfoDistributed找相同案件
            QCaseInfoDistributed qCaseInfoDistributed = QCaseInfoDistributed.caseInfoDistributed;
            BooleanBuilder exp = new BooleanBuilder();
            exp.and(qCaseInfoDistributed.companyCode.eq(companyCode));
            exp.and(qCaseInfoDistributed.product.prodcutName.eq(productName));
            exp.and(qCaseInfoDistributed.personalInfo.idCard.eq(idCard));
            exp.and(qCaseInfoDistributed.personalInfo.name.eq(personalName));
            Iterable<CaseInfoDistributed> all1 = caseInfoDistributedRepository.findAll(exp);
            List<CaseInfoDistributed> caseInfoDistributeds = IterableUtils.toList(all1);

            List<CaseInfoSameModel> caseInfoSameModels = new ArrayList<>();
            for (CaseInfo caseInfo : caseInfos) {
                CaseInfoSameModel caseInfoSameModel = new CaseInfoSameModel();
                BeanUtils.copyProperties(caseInfo, caseInfoSameModel);
                caseInfoSameModel.setCollectorName(caseInfo.getCurrentCollector().getRealName());
                caseInfoSameModel.setDepartment(caseInfo.getDepartment().getName());
                caseInfoSameModel.setPersonalName(caseInfo.getPersonalInfo().getName());
                caseInfoSameModel.setProductName(caseInfo.getProduct().getProdcutName());
                caseInfoSameModels.add(caseInfoSameModel);
            }
            for (CaseInfoDistributed caseInfoDistributed : caseInfoDistributeds) {
                CaseInfoSameModel caseInfoSameModel = new CaseInfoSameModel();
                BeanUtils.copyProperties(caseInfoDistributed, caseInfoSameModel);
                caseInfoSameModel.setCollectorName(null);
                caseInfoSameModel.setDepartment(caseInfoDistributed.getDepartment().getName());
                caseInfoSameModel.setPersonalName(caseInfoDistributed.getPersonalInfo().getName());
                caseInfoSameModel.setProductName(caseInfoDistributed.getProduct().getProdcutName());
                caseInfoSameModels.add(caseInfoSameModel);
            }
            return ResponseEntity.ok().body(caseInfoSameModels);
        } catch (Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("CaseInfoExceptionController", "findSameCaseInfoByCie", "系统异常!"))
                    .body(null);
        }
    }

    @PutMapping("/updateSameCaseInfoByCie")
    @ApiOperation(value = "更新", notes = "更新")
    public ResponseEntity<List<CaseInfoSameModel>> updateSameCaseInfoByCie(@RequestParam(value = "caseNumber") @ApiParam("案件编号") String caseNumber,
                                                                           @RequestParam(value = "cieId") @ApiParam("异常案件ID") String cieId,
                                                                           @RequestHeader(value = "X-UserToken") String token) {
        logger.debug("REST request to updateSameCaseInfoByCie");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("CaseInfoExceptionController", "updateSameCaseInfoByCie", e.getMessage()))
                    .body(null);
        }
        try {
            return null;
        } catch (Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert("CaseInfoExceptionController", "updateSameCaseInfoByCie", "系统异常!"))
                    .body(null);
        }
    }


}
