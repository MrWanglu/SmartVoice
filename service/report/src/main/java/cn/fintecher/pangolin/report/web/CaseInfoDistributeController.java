package cn.fintecher.pangolin.report.web;

import cn.fintecher.pangolin.entity.CaseInfoDistributed;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.report.mapper.CaseInfoDistributeMapper;
import cn.fintecher.pangolin.report.model.CaseInfoDistributeQueryParams;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

/**
 * 待分配案件查询操作
 * Created by sunyanping on 2017/11/6.
 */

@RestController
@RequestMapping("/api/caseInfoDistributeController")
@Api(description = "待分配案件查询操作")
public class CaseInfoDistributeController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(CaseInfoDistributeController.class);

    @Inject
    private CaseInfoDistributeMapper caseInfoDistributeMapper;

    @PostMapping(value = "/findCaseInfoDistribute")
    @ApiOperation(notes = "多条件查询待分配案件", value = "多条件查询待分配案件")
    public ResponseEntity<Page<CaseInfoDistributed>> findCaseInfoDistribute(@RequestBody CaseInfoDistributeQueryParams params,
                                                 @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        logger.debug("REST request to findCaseInfoDistribute");
        try {
            User user = getUserByToken(token);
            if (Objects.nonNull(user.getCompanyCode())) {
                params.setCompanyCode(user.getCompanyCode());
            }
            PageHelper.startPage(params.getPage() + 1, params.getSize());
            List<CaseInfoDistributed> caseInfoDistributes = caseInfoDistributeMapper.findCaseInfoDistribute(params);
            PageInfo<CaseInfoDistributed> pageInfo = new PageInfo<>(caseInfoDistributes);
            Pageable pageable = new PageRequest(params.getPage(), params.getSize());
            Page<CaseInfoDistributed> page = new PageImpl<>(caseInfoDistributes, pageable, pageInfo.getTotal());
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "查询失败")).body(null);
        }
    }
}
