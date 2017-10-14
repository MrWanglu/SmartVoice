package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.CaseInfoExceptionIdList;
import cn.fintecher.pangolin.business.model.CaseUpdateParams;
import cn.fintecher.pangolin.business.model.ItemsModel;
import cn.fintecher.pangolin.business.model.RepeatCaseModel;
import cn.fintecher.pangolin.business.repository.CaseInfoDistributedRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoExceptionRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.service.CaseInfoExceptionService;
import cn.fintecher.pangolin.business.service.ExportItemService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author : DuChao
 * @Description : 异常案件操作
 * @Date : 2017/7/20.
 */

@RestController
@RequestMapping("/api/CaseInfoExceptionController")
@Api(value = "CaseInfoExceptionController", description = "异常案件操作")
public class CaseInfoExceptionController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(CaseInfoController.class);
    private static final String ENTITY_NAME = "caseInfoException";
    private static final String CASE_INFO_ENTITY = "caseInfo";
    @Autowired
    private CaseInfoExceptionService caseInfoExceptionService;
    @Autowired
    private CaseInfoExceptionRepository caseInfoExceptionRepository;
    @Autowired
    ExportItemService exportItemService;
    @Autowired
    CaseInfoRepository caseInfoRepository;
    @Autowired
    CaseInfoDistributedRepository caseInfoDistributedRepository;

    /**
     * 获取所有异常案件
     *
     * @return CaseInfoExceptionList
     */
    @GetMapping("/findAllCaseInfoException")
    @ApiOperation(value = "获取所有异常案件", notes = "获取所有异常案件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseInfoException>> getAllCaseInfoException(@QuerydslPredicate(root = CaseInfoException.class) Predicate predicate,
                                                                           @ApiIgnore Pageable pageable,
                                                                           @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token,
                                                                           @RequestParam(value = "companyCode", required = false) @ApiParam("公司Code") String companyCode) {
        log.debug("REST request to get all CaseInfoExceptions");
        try {
            User user = getUserByToken(token);
            QCaseInfoException qCaseInfoException = QCaseInfoException.caseInfoException;
            BooleanBuilder builder = new BooleanBuilder(predicate);
            if (Objects.isNull(user.getCompanyCode())) {
                if (StringUtils.isNotBlank(companyCode)) {
                    builder.and(qCaseInfoException.companyCode.eq(companyCode));
                }
            } else {
                builder.and(qCaseInfoException.companyCode.eq(user.getCompanyCode()));
            }
            Page<CaseInfoException> page = caseInfoExceptionRepository.findAll(predicate, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "查询失败!")).body(null);
        }
    }

    /**
     * 新增案件
     *
     * @param caseInfoExceptionId
     * @param token
     * @return
     * @throws URISyntaxException
     */
    @GetMapping("/addCaseInfoException")
    @ApiOperation(value = "新增案件", notes = "新增案件")
    public ResponseEntity<CaseInfoDistributed> addCaseInfo(@RequestParam @ApiParam(value = "异常案件id") String caseInfoExceptionId,
                                                           @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) throws Exception {
        log.debug("REST request to add case to CaseInfoDistributed");
        CaseInfoDistributed caseInfoDistributed = caseInfoExceptionService.addCaseInfoDistributed(caseInfoExceptionId, getUserByToken(token));
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert(CASE_INFO_ENTITY, caseInfoDistributed.getId())).body(caseInfoDistributed);
    }

    /**
     * 更新案件
     *
     * @param caseUpdateParams
     * @param token
     * @return
     * @throws URISyntaxException
     */
    @PostMapping("/updateCaseInfoException")
    @ApiOperation(value = "更新案件", notes = "更新案件")
    public ResponseEntity<CaseInfo> updateCaseInfoException(@RequestBody CaseUpdateParams caseUpdateParams,
                                                            @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) throws Exception {
        log.debug("REST request to update CaseInfo");
        CaseInfoException caseInfoException = caseInfoExceptionRepository.findOne(caseUpdateParams.getCaseInfoExceptionId());
        if (Objects.isNull(caseInfoException)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Exception Case", "异常案件已经被更新")).body(null);
        }
        List<CaseInfo> caseInfoList = caseInfoExceptionService.updateCaseInfoException(caseUpdateParams.getCaseInfoExceptionId(), caseUpdateParams.getCaseInfoIds(), getUserByToken(token));
        if (caseInfoList.isEmpty()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "caseNotFound", "不存在相同案件，无法更新")).body(null);
        }
        return ResponseEntity.ok().body(null);
    }

    /**
     * 删除异常池案件
     *
     * @param caseInfoExceptionId
     * @return
     */
    @DeleteMapping("/deleteCaseInfoException")
    @ApiOperation(value = "删除异常池案件", notes = "删除异常池案件")
    public ResponseEntity<Void> deleteCaseInfoException(@RequestParam @ApiParam(value = "异常案件id") String caseInfoExceptionId) {
        try {
            log.debug("REST request to delete caseInfoException : {}", caseInfoExceptionId);
            caseInfoExceptionService.deleteCaseInfoException(caseInfoExceptionId);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "delete error", "案件删除失败，请检查案件是否已被删除")).body(null);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, caseInfoExceptionId)).build();
    }

    @GetMapping("/checkExceptionCase")
    @ApiOperation(value = "检查异常案件", notes = "检查异常案件")
    public ResponseEntity<CaseInfoException> checkExceptionCase(@RequestParam(value = "companyCode", required = false) String companyCode,
                                                                @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        log.debug("REST request to checkExceptionCase");
        try {
            User user = getUserByToken(token);
            if (Objects.isNull(user.getCompanyCode())) {
                if (StringUtils.isNotBlank(companyCode)) {
                    user.setCompanyCode(companyCode);
                }
            }
            if (caseInfoExceptionService.checkCaseExceptionExist(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "异常池有异常案件，请先处理!")).body(null);
            }
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "系统异常!")).body(null);
        }
    }

    @DeleteMapping("/batchDeleteCaseInfoException")
    @ApiOperation(value = "批量删除异常池案件", notes = "批量删除异常池案件")
    public ResponseEntity<Void> batchDeleteCaseInfoException(@RequestBody @ApiParam(value = "异常案件ids") CaseInfoExceptionIdList caseInfoExceptionIdList) {
        try {
            log.debug("REST request to delete caseInfoException : {}", caseInfoExceptionIdList);
            if (Objects.isNull(caseInfoExceptionIdList.getIds()) || caseInfoExceptionIdList.getIds().isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请选择要删除的案件!")).body(null);
            }
            List<CaseInfoException> all = caseInfoExceptionRepository.findAll(caseInfoExceptionIdList.getIds());
            caseInfoExceptionRepository.deleteInBatch(all);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("删除成功!", "")).body(null);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "delete error", "案件删除失败，请检查案件是否已被删除")).body(null);
        }
    }

    @PostMapping("/updateExceptionCase")
    @ApiOperation(value = "更新案件", notes = "更新案件")
    public ResponseEntity<CaseInfo> updateExceptionCase(@RequestBody CaseUpdateParams caseUpdateParams,
                                                            @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) throws Exception {
        try {
            log.debug("REST request to update CaseInfo");
            User user = getUserByToken(token);
            ItemsModel itemsModel = exportItemService.getExportItems(user, ExportItem.Category.CASEUPDATE.getValue());
            if (itemsModel.getPersonalItems().isEmpty() && itemsModel.getJobItems().isEmpty() && itemsModel.getConnectItems().isEmpty()
                    && itemsModel.getCaseItems().isEmpty() && itemsModel.getBankItems().isEmpty() && itemsModel.getFollowItems().isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请先设置导出项")).body(null);
            }
            CaseInfoException caseInfoException = caseInfoExceptionRepository.findOne(caseUpdateParams.getCaseInfoExceptionId());
            if (Objects.isNull(caseInfoException)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Exception Case", "异常案件已经被更新")).body(null);
            }
            String assigned = caseInfoException.getAssignedRepeat();
            assigned = assigned.substring(1, assigned.length() - 1);
            String[] assigneds = assigned.split(",");
            List<String> assignedList = Arrays.asList(assigneds);
            String distribute = caseInfoException.getDistributeRepeat();
            distribute = distribute.substring(1, distribute.length() - 1);
            String[] distributes = distribute.split(",");
            List<String> distributeList = Arrays.asList(distributes);
            for (String id : caseUpdateParams.getCaseInfoIds()) {
                if (assignedList.contains(id)) {
                    CaseInfo caseInfo = caseInfoExceptionService.updateExceptionCase(caseUpdateParams.getCaseInfoExceptionId(), id, user, itemsModel);
                    if (Objects.isNull(caseInfo)) {
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "不存在相同案件，无法更新")).body(null);
                    }
                } else if (distributeList.contains(id)) {
                    CaseInfoDistributed caseInfoDistributed = caseInfoExceptionService.updateCaseDistributeException(caseUpdateParams.getCaseInfoExceptionId(), id, user, itemsModel);
                    if (Objects.isNull(caseInfoDistributed)) {
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "不存在相同案件，无法更新")).body(null);
                    }
                }
            }
            return ResponseEntity.ok().body(null);
        }catch (Exception e){
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "更新失败")).body(null);
        }
    }

    @GetMapping("/findRepeatCaseInfo")
    @ApiOperation(value = "获取重复案件", notes = "获取重复案件")
    public ResponseEntity<Page<CaseInfo>> findRepeatCaseInfo(@QuerydslPredicate(root = CaseInfoException.class) Predicate predicate,
                                                                       @ApiIgnore Pageable pageable,
                                                                       @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token,
                                                                       @RequestParam(value = "caseInfoExceptionId", required = true) @ApiParam("异常案件ID") String caseInfoExceptionId) {
        try {
            CaseInfoException caseInfoException = caseInfoExceptionRepository.findOne(caseInfoExceptionId);
            String assigned = caseInfoException.getAssignedRepeat();
            assigned = assigned.substring(1, assigned.length() - 1);
            String[] assigneds = assigned.split(",");
            Page<CaseInfo> page = caseInfoRepository.findAll(QCaseInfo.caseInfo.id.in(assigneds),pageable);
            return ResponseEntity.ok().body(page);
        }catch(Exception e){
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "查询失败")).body(null);
        }
    }

    /**
     * 获取所有重复案件
     *
     * @return CaseInfoExceptionList
     */
    @GetMapping("/findAllRepeatCaseInfo")
    @ApiOperation(value = "获取所有重复案件", notes = "获取所有重复案件")
    public ResponseEntity<Page<RepeatCaseModel>> findAllRepeatCaseInfo(@QuerydslPredicate(root = CaseInfoException.class) Predicate predicate,
                                                                               @ApiIgnore Pageable pageable,
                                                                               @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token,
                                                                               @RequestParam(value = "caseInfoExceptionId", required = true) @ApiParam("异常案件ID") String caseInfoExceptionId,
                                                                               @RequestParam(value = "companyCode", required = false) @ApiParam("公司Code") String companyCode) {
        try {
            CaseInfoException caseInfoException = caseInfoExceptionRepository.findOne(caseInfoExceptionId);
            String assigned = caseInfoException.getAssignedRepeat();
            assigned = assigned.substring(1, assigned.length() - 1);
            String[] assigneds = assigned.split(",");
            List<RepeatCaseModel> repeatCaseModels = new ArrayList<>();
            List<CaseInfo> caseInfoList = IterableUtils.toList(caseInfoRepository.findAll(QCaseInfo.caseInfo.id.in(assigneds)));
            for (CaseInfo caseInfo : caseInfoList) {
                RepeatCaseModel model = new RepeatCaseModel();
                BeanUtils.copyProperties(caseInfo, model);
                model.setRepeatType(1);
                repeatCaseModels.add(model);
            }
            String distribute = caseInfoException.getDistributeRepeat();
            distribute = distribute.substring(1, distribute.length() - 1);
            String[] distributes = distribute.split(",");
            List<CaseInfoDistributed> distributedList = IterableUtils.toList(caseInfoDistributedRepository.findAll(QCaseInfoDistributed.caseInfoDistributed.id.in(distributes)));
            for (CaseInfoDistributed caseInfoDistributed : distributedList) {
                RepeatCaseModel model = new RepeatCaseModel();
                BeanUtils.copyProperties(caseInfoDistributed, model);
                model.setRepeatType(0);
                repeatCaseModels.add(model);
            }
            Page<RepeatCaseModel> page = new PageImpl<RepeatCaseModel>(repeatCaseModels, pageable, repeatCaseModels.size());
            return ResponseEntity.ok().body(page);
        }catch(Exception e){
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "查询失败")).body(null);
        }
    }
}
