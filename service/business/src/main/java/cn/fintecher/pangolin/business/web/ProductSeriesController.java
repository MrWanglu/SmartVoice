package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.QCaseInfo;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author : sunyanping
 * @Description : 产品系列操作
 * @Date : 2017/7/31.
 */
@RestController
@RequestMapping("/api/productSeriesController")
@Api(value = "ProductSeriesController", description = "产品系列操作")
public class ProductSeriesController extends BaseController{

    private final Logger log = LoggerFactory.getLogger(ProductSeriesController.class);

    @Inject
    private CaseInfoRepository caseInfoRepository;

    @GetMapping("/getProductSeriesName")
    @ApiOperation(value = "获取所有的产品名称",notes = "获取所有的产品名称")
    public ResponseEntity<Set<String>> getProductSeriesName(@RequestHeader(value = "X-UserToken") String token,
                                                            @RequestParam(value = "companyCode",required = false) @ApiParam("公司Code") String companyCode) {
        log.debug("REST request to getProductSeriesName");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("ProductSeriesController", "getProductSeriesName", e.getMessage())).body(null);
        }
        try {
            QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(qCaseInfo.companyCode.eq(user.getCompanyCode()));
            builder.and(qCaseInfo.product.isNotNull());
            builder.and(qCaseInfo.product.productSeries.isNotNull());
            Iterable<CaseInfo> caseInfos = caseInfoRepository.findAll(builder);
            Set<String> set = new HashSet<>();
            for (CaseInfo caseInfo : caseInfos) {
                set.add(caseInfo.getProduct().getProductSeries().getSeriesName());
            }
            return ResponseEntity.ok().body(set);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("ProductSeriesController", "getProductSeriesName", "系统异常!")).body(null);
        }
    }

}
