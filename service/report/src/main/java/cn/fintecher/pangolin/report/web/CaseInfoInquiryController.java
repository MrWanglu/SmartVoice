package cn.fintecher.pangolin.report.web;

import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.report.mapper.CaseInfoMapper;
import cn.fintecher.pangolin.report.model.CaseAssistModel;
import cn.fintecher.pangolin.report.model.CaseInfoConditionParams;
import cn.fintecher.pangolin.report.model.CaseInfoModel;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

/**
 * @author : xiaqun
 * @Description : 案件多条件查询
 * @Date : 9:38 2017/11/1
 */

@RestController
@RequestMapping("/api/CaseInfoInquiryController")
@Api(description = "案件多条件查询")
public class CaseInfoInquiryController extends BaseController {
    private final Logger log = LoggerFactory.getLogger(ReportController.class);

    private final static String ENTITY_CASE_INFO = "CaseInfo";

    private final static String ENTITY_CASE_ASSIST = "CaseAssist";

    @Inject
    CaseInfoMapper caseInfoMapper;

    /**
     * @Description 多条件查询电催，外访案件
     */
    @GetMapping("/getCaseInfoByCondition")
    @ApiOperation(value = "多条件查询电催，外访案件", notes = "多条件查询电催，外访案件")
    public ResponseEntity<Page<CaseInfoModel>> getCaseInfoByCondition(CaseInfoConditionParams caseInfoConditionParams,
                                                                      @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get case info by condition");
        try {
            User tokenUser = getUserByToken(token);
            String sort = "";
            String newSort = "";
            if (Objects.nonNull(caseInfoConditionParams.getSort())) {
                sort = caseInfoConditionParams.getSort();
                newSort = sort.replace(",", " ");
            }
            if (sort.contains("followupBack") ||
                    sort.contains("caseNumber") ||
                    sort.contains("overdueAmount") ||
                    sort.contains("overdueDays") ||
                    sort.contains("batchNumber") ||
                    sort.contains("followupTime") ||
                    sort.contains("followupBack")) {
                newSort = "a.".concat(newSort);
            }
            if (sort.contains("idCard")) {
                String str = newSort.replace("idCard", "id_card");
                newSort = "b.".concat(str);
            }
            PageHelper.startPage(caseInfoConditionParams.getPage(), caseInfoConditionParams.getSize());
            List<CaseInfoModel> caseInfoModels = caseInfoMapper.getCaseInfoByCondition(StringUtils.trim(caseInfoConditionParams.getPersonalName()),
                    StringUtils.trim(caseInfoConditionParams.getMobileNo()),
                    caseInfoConditionParams.getDeptCode(),
                    StringUtils.trim(caseInfoConditionParams.getCollectorName()),
                    caseInfoConditionParams.getOverdueMaxAmt(),
                    caseInfoConditionParams.getOverdueMinAmt(),
                    caseInfoConditionParams.getPayStatus(),
                    caseInfoConditionParams.getOverMaxDay(),
                    caseInfoConditionParams.getOverMinDay(),
                    StringUtils.trim(caseInfoConditionParams.getBatchNumber()),
                    caseInfoConditionParams.getPrincipalId(),
                    StringUtils.trim(caseInfoConditionParams.getIdCard()),
                    caseInfoConditionParams.getFollowupBack(),
                    caseInfoConditionParams.getAssistWay(),
                    caseInfoConditionParams.getCaseMark(),
                    caseInfoConditionParams.getCollectionType(),
                    caseInfoConditionParams.getSort() == null ? null : newSort,
                    tokenUser.getDepartment().getCode(),
                    caseInfoConditionParams.getCollectionStatusList(),
                    caseInfoConditionParams.getCollectionStatus(),
                    caseInfoConditionParams.getParentAreaId(),
                    caseInfoConditionParams.getAreaId());
            PageInfo<CaseInfoModel> pageInfo = new PageInfo<>(caseInfoModels);
            Pageable pageable = new PageRequest(caseInfoConditionParams.getPage(), caseInfoConditionParams.getSize());
            Page<CaseInfoModel> page = new PageImpl<>(caseInfoModels, pageable, pageInfo.getTotal());
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", ENTITY_CASE_INFO)).body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASE_INFO, "caseInfo", "查询失败")).body(null);
        }
    }

    /**
     * @Description 多条件查询协催案件
     */
    @GetMapping("/getCaseAssistByCondition")
    @ApiOperation(value = "多条件查询协催案件", notes = "多条件查询协催案件")
    public ResponseEntity<Page<CaseAssistModel>> getCaseAssistByCondition(CaseInfoConditionParams caseInfoConditionParams,
                                                                          @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get case assist by condition");
        try {
            User tokenUser = getUserByToken(token);
            String sort = "";
            String newSort = "";
            if (Objects.nonNull(caseInfoConditionParams.getSort())) {
                sort = caseInfoConditionParams.getSort();
                newSort = sort.replace(",", " ");
            }
            if (sort.contains("operatorTime") ||
                    sort.contains("assistStatus") ||
                    sort.contains("caseFlowinTime") ||
                    sort.contains("leaveCaseFlag")) {
                newSort = "a.".concat(newSort);
            }
            if (sort.contains("caseNumber")) {
                String str = newSort.replace("caseNumber", "case_number");
                newSort = "b.".concat(str);
            }
            if (sort.contains("overdueAmount")) {
                String str = newSort.replace("overdueAmount", "overdue_amount");
                newSort = "b.".concat(str);
            }
            PageHelper.startPage(caseInfoConditionParams.getPage(), caseInfoConditionParams.getSize());
            List<CaseAssistModel> caseAssistModels = caseInfoMapper.getCaseAssistByCondition(StringUtils.trim(caseInfoConditionParams.getPersonalName()),
                    StringUtils.trim(caseInfoConditionParams.getMobileNo()),
                    caseInfoConditionParams.getOverdueMaxAmt(),
                    caseInfoConditionParams.getOverdueMinAmt(),
                    caseInfoConditionParams.getAssistStatusList(),
                    tokenUser.getDepartment().getCode(), newSort);
            PageInfo<CaseAssistModel> pageInfo = new PageInfo<>(caseAssistModels);
            Pageable pageable = new PageRequest(caseInfoConditionParams.getPage(), caseInfoConditionParams.getSize());
            Page<CaseAssistModel> page = new PageImpl<>(caseAssistModels, pageable, pageInfo.getTotal());
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", ENTITY_CASE_ASSIST)).body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_CASE_ASSIST, "caseAssist", "查询失败")).body(null);
        }
    }
}