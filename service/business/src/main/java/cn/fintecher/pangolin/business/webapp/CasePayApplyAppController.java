package cn.fintecher.pangolin.business.webapp;

import cn.fintecher.pangolin.business.repository.CasePayApplyRepository;
import cn.fintecher.pangolin.business.web.BaseController;
import cn.fintecher.pangolin.entity.CasePayApply;
import cn.fintecher.pangolin.entity.QCasePayApply;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import javax.inject.Inject;
import java.net.URISyntaxException;

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
    @GetMapping("/getCasePaymentRecordForApp")
    @ApiOperation(value = "根据案件ID获取还款记录", notes = "根据案件ID获取还款记录")
    public ResponseEntity<Page<CasePayApply>> getPaymentRecord(@RequestParam @ApiParam(value = "案件ID", required = true) String caseId,
                                                               @ApiIgnore Pageable pageable) throws URISyntaxException {
        try {
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(QCasePayApply.casePayApply.caseId.eq(caseId));
            Page<CasePayApply> page = casePayApplyRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/casePayApplyAppController/getCasePaymentRecord");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("查询失败", "casePayApply", e.getMessage())).body(null);
        }
    }
}
