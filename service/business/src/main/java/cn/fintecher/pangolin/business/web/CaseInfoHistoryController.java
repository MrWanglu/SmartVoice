package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.CaseInfoIdList;
import cn.fintecher.pangolin.business.repository.CaseInfoHistoryRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.CaseInfoHistory;
import cn.fintecher.pangolin.entity.QCaseInfo;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by ChenChang on 2017/5/23.
 */
@RestController
@RequestMapping("/api/caseInfoHistoryController")
@Api(value = "CaseInfoHistoryController", description = "结案案件操作")
public class CaseInfoHistoryController extends BaseController {

    private static final String ENTITY_NAME = "CaseInfoHistory";
    private final Logger log = LoggerFactory.getLogger(CaseInfoHistoryController.class);
    private final CaseInfoHistoryRepository caseInfoHistoryRepository;


    public CaseInfoHistoryController(CaseInfoHistoryRepository caseInfoHistoryRepository) {
        this.caseInfoHistoryRepository = caseInfoHistoryRepository;
    }

    @Autowired
    private CaseInfoRepository caseInfoRepository;

    /**
     * @Description : 查询结案案件
     */
    @GetMapping("/getAllCaseInfo")
    @ApiOperation(value = "分页查询结案案件", notes = "分页查询结案案件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseInfo>> getAllCaseInfo(@QuerydslPredicate(root = CaseInfo.class) Predicate predicate,
                                                         @ApiIgnore Pageable pageable,
                                                         @RequestHeader(value = "X-UserToken") String token,
                                                         @RequestParam(value = "companyCode", required = false) @ApiParam("公司Code码") String companyCode) {
        log.debug("REST request to getAllCaseInfo");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "getAllCaseInfo", e.getMessage())).body(null);
        }
        // 超级管理员
        if (Objects.isNull(user.getCompanyCode())) {
            if (Objects.nonNull(companyCode)) {
                user.setCompanyCode(companyCode);
            } else {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "请选择公司!")).body(null);
            }
        }
        try {
            QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
            BooleanBuilder builder = new BooleanBuilder(predicate);
            builder.and(qCaseInfo.companyCode.eq(user.getCompanyCode())); //公司
            builder.and(qCaseInfo.endType.eq(CaseInfo.EndType.REPAID.getValue())); //以结案
            if (Objects.equals(user.getManager(), User.MANAGER_TYPE.DATA_AUTH.getValue())) { //管理者
                builder.and(qCaseInfo.department.code.startsWith(user.getDepartment().getCode()));
            }
            if (Objects.equals(user.getManager(), User.MANAGER_TYPE.NO_DATA_AUTH.getValue())) { //不是管理者
                builder.and(qCaseInfo.currentCollector.eq(user).or(qCaseInfo.assistCollector.eq(user)));
            }
            Page<CaseInfo> page = caseInfoRepository.findAll(builder, pageable);
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("CaseInfoController", "getAllCaseInfo", "系统异常!")).body(null);
        }
    }

    /**
     * @Description : 删除案件放到caseInfoHistory
     */
    @PostMapping("/deleteCaseInfo")
    public ResponseEntity<List<CaseInfo>> deleteCaseInfo(@RequestBody CaseInfoIdList request) {
        if (request.getIds().size() == 0) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "请选择案件")).body(null);
        }
        List<CaseInfo> caseInfoList = caseInfoRepository.findAll(request.getIds());
        List<CaseInfo> caseInfoReturnList = new ArrayList<>();
        for (CaseInfo caseInfo : caseInfoList) {
            if(Objects.equals("24",CaseInfo.CollectionStatus.CASE_OVER.getValue())) {
                CaseInfoHistory caseInfoHistory = new CaseInfoHistory();
                BeanUtils.copyProperties(caseInfo, caseInfoHistory);
                caseInfoHistory.setCaseId(caseInfo.getId());
                caseInfoHistoryRepository.save(caseInfoHistory);
                caseInfoRepository.delete(caseInfo.getId());
            }else{
                caseInfoReturnList.add(caseInfo);
            }
        }
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(caseInfoReturnList);
    }
}
