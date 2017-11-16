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
            PageHelper.startPage(caseInfoConditionParams.getPage() + 1, caseInfoConditionParams.getSize());
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
                    caseInfoConditionParams.getSort() == null ? null : caseInfoConditionParams.getSort(),
                    tokenUser.getDepartment().getCode(),
                    caseInfoConditionParams.getCollectionStatusList(),
                    caseInfoConditionParams.getCollectionStatus(),
                    caseInfoConditionParams.getParentAreaId(),
                    caseInfoConditionParams.getAreaId(),
                    tokenUser.getType(),
                    tokenUser.getManager(),
                    tokenUser.getId(),
                    caseInfoConditionParams.getRealPayMaxAmt(),
                    caseInfoConditionParams.getRealPayMinAmt());
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
            PageHelper.startPage(caseInfoConditionParams.getPage() + 1, caseInfoConditionParams.getSize());
            List<CaseAssistModel> caseAssistModels = caseInfoMapper.getCaseAssistByCondition(StringUtils.trim(caseInfoConditionParams.getPersonalName()),
                    StringUtils.trim(caseInfoConditionParams.getMobileNo()),
                    caseInfoConditionParams.getOverdueMaxAmt(),
                    caseInfoConditionParams.getOverdueMinAmt(),
                    caseInfoConditionParams.getAssistStatusList(),
                    tokenUser.getDepartment().getCode(),
                    caseInfoConditionParams.getSort() == null ? null : caseInfoConditionParams.getSort(),
                    tokenUser.getManager(),
                    tokenUser.getId());
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