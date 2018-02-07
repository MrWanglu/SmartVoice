package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.exception.GeneralException;
import cn.fintecher.pangolin.business.model.*;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.business.service.CaseInfoDistributedService;
import cn.fintecher.pangolin.business.service.CaseInfoService;
import cn.fintecher.pangolin.business.service.RunCaseStrategyService;
import cn.fintecher.pangolin.business.utils.ZWMathUtil;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.strategy.CaseStrategy;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    Logger logger = LoggerFactory.getLogger(CaseInfoDistributeController.class);

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
    @Inject
    PersonalContactRepository personalContactRepository;
    @Inject
    RunCaseStrategyService runCaseStrategyService;
    @Inject
    OutsourcePoolRepository outsourcePoolRepository;

    @RequestMapping(value = "/distributeCeaseInfo", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    @ApiOperation(value = "案件分配(机构时传入机构的ID)", notes = "案件分配")
    public ResponseEntity distributeCeaseInfo(@RequestBody AccCaseInfoDisModel accCaseInfoDisModel,
                                              @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        try {
            User user = getUserByToken(token);
            try {
                caseInfoDistributedService.distributeCeaseInfo(accCaseInfoDisModel, user);
            } catch (final Exception e) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage())).body(null);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert("操作成功", ENTITY_NAME)).body(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            String msg = Objects.isNull(e.getMessage()) ? "系统异常" : e.getMessage();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "error", msg)).body(null);
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
                                                                            @RequestHeader(value = "X-UserToken") String token,
                                                                            @RequestParam(value = "companyCode", required = false) String companyCode) {
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
            if (Objects.isNull(user.getCompanyCode())) {
                if (StringUtils.isNotBlank(companyCode)) {
                    builder.and(qd.companyCode.eq(companyCode));
                }
            } else {
                builder.and(qd.companyCode.eq(user.getCompanyCode()));
            }
            builder.and(qd.recoverRemark.eq(CaseInfo.RecoverRemark.NOT_RECOVERED.getValue()));
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
            builder.and(qCaseInfo.department.code.like(deptCode.concat("%")));
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
    public ResponseEntity<CaseInfoDistributed> getCaseInfoDistributedDetails(@RequestParam("id") String id) {
        CaseInfoDistributed caseInfoDistributed = caseInfoDistributedRepository.findOne(id);
        return ResponseEntity.ok().body(caseInfoDistributed);
    }

    @GetMapping("/batchAddPersonContacts")
    @ApiOperation(value = "根据备注解析联系人信息", notes = "根据备注解析联系人信息")
    public ResponseEntity<Void> batchAddPersonContacts(@RequestHeader(value = "X-UserToken") String token) {
        User user = null;
        try {
            user = getUserByToken(token);
            List<CaseInfoDistributed> caseInfoDistributeds = caseInfoDistributedRepository.findAll();
            if (Objects.isNull(caseInfoDistributeds) || caseInfoDistributeds.size() == 0) {
                return ResponseEntity.ok()
                        .headers(HeaderUtil.createAlert("", "")).body(null);
            }
            for (CaseInfoDistributed caseInfoDistributed : caseInfoDistributeds) {
                if (Objects.nonNull(caseInfoDistributed.getMemo())) {
                    char[] charArray = caseInfoDistributed.getMemo().toCharArray();
                    String phoneNumber = "";
                    for (char temp : charArray) {
                        if (((int) temp >= 48 && (int) temp <= 57) || (int) temp == 45) {
                            phoneNumber += temp;
                        } else {
                            if (!Objects.equals(phoneNumber, "")) {
                                setPersonalContacts(caseInfoDistributed.getPersonalInfo().getId(), phoneNumber, user);
                            }
                            phoneNumber = "";
                        }
                    }
                    if (!Objects.equals(phoneNumber, "")) {
                        setPersonalContacts(caseInfoDistributed.getPersonalInfo().getId(), phoneNumber, user);
                    }
                }
                //解析完了将memo 置为空。
                caseInfoDistributed.setMemo(null);
            }
            caseInfoDistributedRepository.save(caseInfoDistributeds);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("RepairCaseDistributeController", "error", e.getMessage())).body(null);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("", null)).body(null);
    }

    /**
     * 增加联系人信息
     *
     * @param custId
     * @param phoneNumber
     */
    private void setPersonalContacts(String custId, String phoneNumber, User user) {
        PersonalContact personalContact = new PersonalContact();
        personalContact.setPersonalId(custId);
        personalContact.setPhone(phoneNumber);
        personalContact.setInformed(0);
        personalContact.setPhoneStatus(Personal.PhoneStatus.NORMAL.getValue());
        personalContact.setSource(Constants.DataSource.IMPORT.getValue());
        personalContact.setOperator(user.getUserName());
        personalContact.setOperatorTime(ZWDateUtil.getNowDate());
        personalContact.setRelation(PersonalContact.relation.OTHER.getValue());
        personalContactRepository.save(personalContact);
    }

    @PostMapping("/manualAllocation")
    @ApiOperation(notes = "案件分配手动分案", value = "案件分配手动分案")
    public ResponseEntity manualAllocation(@RequestHeader(value = "X-UserToken") String token,
                                           @RequestBody ManualParams manualParams) {
        logger.debug("REST request to getCaseCountOnDept");
        try {
            User user = getUserByToken(token);
            caseInfoDistributedService.manualAllocation(manualParams, user);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("分配成功", "")).body(null);
        } catch (Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "分配失败")).body(null);
        }
    }

    @PostMapping("/allocationCount")
    @ApiOperation(notes = "案件分配手动分案统计", value = "案件分配手动分案统计")
    public ResponseEntity<AllocationCountModel> allocationCount(@RequestBody ManualParams manualParams) {
        try {
            AllocationCountModel model = caseInfoDistributedService.allocationCount(manualParams);
            return ResponseEntity.ok().body(model);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "手动分案统计失败")).body(null);
        }
    }

    @PostMapping("/strategyAllocation")
    @ApiOperation(notes = "案件导入分配策略分案", value = "案件导入分配策略分案")
    public ResponseEntity strategyAllocation(@RequestBody CaseInfoStrategyModel model,
                                             @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            caseInfoDistributedService.strategyAllocation(model, user);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("分配成功", "")).body(null);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage())).body(null);
        }
    }

    @PostMapping("/countStrategyAllocation")
    @ApiOperation(notes = "策略分配情况统计", value = "策略分配情况统计")
    public ResponseEntity<CaseInfoStrategyModel> countStrategyAllocation(@RequestBody CaseInfoIdList caseInfoIdList,
                                                                         @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            List<CaseInfoStrategyResultModel> modelList = caseInfoDistributedService.countStrategyAllocation(caseInfoIdList, user);
            CaseInfoStrategyModel model = new CaseInfoStrategyModel();
            model.setModelList(modelList);
            return ResponseEntity.ok().body(model);
        } catch (GeneralException ge) {
            logger.error(ge.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", ge.getMessage())).body(null);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "统计策略分配情况错误")).body(null);
        }
    }

    @PostMapping("/previewResult")
    @ApiOperation(value = "策略预览结果", notes = "策略预览结果")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity previewResult(@RequestBody PreviewParams previewParams,
                                        @RequestHeader(value = "X-UserToken") String token) {

        try {
            User user = getUserByToken(token);

            if (StringUtils.isBlank(previewParams.getJsonString())) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请先配置策略")).body(null);
            }
            if (Objects.isNull(previewParams.getType())) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请选择策略类型")).body(null);
            }
            Pageable pageable = new PageRequest(previewParams.getPage(), previewParams.getSize(), new Sort(Sort.Direction.DESC, "id"));
            CaseStrategy caseStrategy = null;
            try {
                caseStrategy = caseInfoDistributedService.previewResult(previewParams.getJsonString());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage())).body(null);
            }
            boolean areaIdFlag = false;
            boolean ppsFlag = false;
            if (StringUtils.isNotBlank(caseStrategy.getStrategyText())) {
                if (caseStrategy.getStrategyText().contains(Constants.STRATEGY_AREA_ID)) {
                    areaIdFlag = true;
                }
                if (caseStrategy.getStrategyText().contains(Constants.STRATEGY_PRODUCT_SERIES)) {
                    ppsFlag = true;
                }
            }
            List<StrategyPreviewModel> modelList = new ArrayList<>();
            if (Objects.equals(previewParams.getType(), CaseStrategy.StrategyType.INNER.getValue())) {// 案件导入策略分配
                List<CaseInfoDistributed> checkList = new ArrayList<>();
                QCaseInfoDistributed qCaseInfoDistributed = QCaseInfoDistributed.caseInfoDistributed;
                BooleanBuilder builder = new BooleanBuilder();
                if (Objects.isNull(user.getCompanyCode())) {
                    if (StringUtils.isNotBlank(previewParams.getCompanyCode())) {
                        builder.and(qCaseInfoDistributed.companyCode.eq(previewParams.getCompanyCode()));
                    }
                } else {
                    builder.and(qCaseInfoDistributed.companyCode.eq(user.getCompanyCode()));
                }
                if (StringUtils.isNotBlank(previewParams.getPersonalName())) {
                    builder.and(qCaseInfoDistributed.personalInfo.name.like("%" + previewParams.getPersonalName().trim() + "%"));
                }
                if (StringUtils.isNotBlank(previewParams.getPhone())) {
                    builder.and(qCaseInfoDistributed.personalInfo.mobileNo.eq(previewParams.getPhone()));
                }
                if (StringUtils.isNotBlank(previewParams.getIdCard())) {
                    builder.and(qCaseInfoDistributed.personalInfo.idCard.eq(previewParams.getIdCard()));
                }
                if (StringUtils.isNotBlank(previewParams.getBatchNumber())) {
                    builder.and(qCaseInfoDistributed.batchNumber.eq(previewParams.getBatchNumber()));
                }
                if (Objects.nonNull(previewParams.getStartAmount())) {
                    builder.and(qCaseInfoDistributed.overdueAmount.gt(previewParams.getStartAmount()));
                }
                if (Objects.nonNull(previewParams.getEndAmount())) {
                    builder.and(qCaseInfoDistributed.overdueAmount.lt(previewParams.getEndAmount()));
                }
                if (areaIdFlag == true) {
                    builder.and(qCaseInfoDistributed.area.isNotNull());
                }
                if (ppsFlag == true) {
                    builder.and(qCaseInfoDistributed.product.productSeries.isNotNull());
                }
                Iterable<CaseInfoDistributed> iterable = caseInfoDistributedRepository.findAll(builder);
                Iterator<CaseInfoDistributed> iterator = iterable.iterator();
                KieSession kieSession = null;
                try {
                    kieSession = runCaseStrategyService.runCaseRule(checkList, caseStrategy, Constants.CASE_INFO_DISTRIBUTE_RULE);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage())).body(null);
                }
                while (iterator.hasNext()) {
                    CaseInfoDistributed next = iterator.next();
                    kieSession.insert(next);//插入
                    kieSession.fireAllRules();//执行规则
                }
                kieSession.dispose();
                if (!checkList.isEmpty()) {
                    for (CaseInfoDistributed distributed : checkList) {
                        StrategyPreviewModel model = new StrategyPreviewModel();
                        BeanUtils.copyProperties(distributed, model);
                        model.setCity(Objects.isNull(distributed.getArea()) ? null : distributed.getArea().getAreaName());
                        model.setIdCard(Objects.isNull(distributed.getPersonalInfo()) ? null : distributed.getPersonalInfo().getIdCard());
                        model.setPersonalName(Objects.isNull(distributed.getPersonalInfo()) ? null : distributed.getPersonalInfo().getName());
                        model.setPhone(Objects.isNull(distributed.getPersonalInfo()) ? null : distributed.getPersonalInfo().getMobileNo());
                        model.setPrincipalName(Objects.isNull(distributed.getPrincipalId()) ? null : distributed.getPrincipalId().getName());
                        modelList.add(model);
                    }
                }
            } else if (Objects.equals(previewParams.getType(), CaseStrategy.StrategyType.INNER.getValue())) {// 内催策略分配
                List<CaseInfo> checkList = new ArrayList<>();
                QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
                BooleanBuilder builder = new BooleanBuilder();
                if (Objects.isNull(user.getCompanyCode())) {
                    if (StringUtils.isNotBlank(previewParams.getCompanyCode())) {
                        builder.and(qCaseInfo.companyCode.eq(previewParams.getCompanyCode()));
                    }
                } else {
                    builder.and(qCaseInfo.companyCode.eq(user.getCompanyCode()));
                }
                builder.and(qCaseInfo.casePoolType.eq(CaseInfo.CasePoolType.INNER.getValue()));
                builder.and(qCaseInfo.collectionStatus.eq(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue()));
                builder.and(qCaseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue()));
                if (StringUtils.isNotBlank(previewParams.getPersonalName())) {
                    builder.and(qCaseInfo.personalInfo.name.like("%" + previewParams.getPersonalName().trim() + "%"));
                }
                if (StringUtils.isNotBlank(previewParams.getPhone())) {
                    builder.and(qCaseInfo.personalInfo.mobileNo.eq(previewParams.getPhone()));
                }
                if (StringUtils.isNotBlank(previewParams.getIdCard())) {
                    builder.and(qCaseInfo.personalInfo.idCard.eq(previewParams.getIdCard()));
                }
                if (StringUtils.isNotBlank(previewParams.getBatchNumber())) {
                    builder.and(qCaseInfo.batchNumber.eq(previewParams.getBatchNumber()));
                }
                if (Objects.nonNull(previewParams.getStartAmount())) {
                    builder.and(qCaseInfo.overdueAmount.gt(previewParams.getStartAmount()));
                }
                if (Objects.nonNull(previewParams.getEndAmount())) {
                    builder.and(qCaseInfo.overdueAmount.lt(previewParams.getEndAmount()));
                }
                if (areaIdFlag == true) {
                    builder.and(qCaseInfo.area.isNotNull());
                }
                if (ppsFlag == true) {
                    builder.and(qCaseInfo.product.productSeries.isNotNull());
                }
                Iterable<CaseInfo> all = caseInfoRepository.findAll(builder);
                Iterator<CaseInfo> iterator = all.iterator();
                KieSession kieSession = null;
                try {
                    kieSession = runCaseStrategyService.runCaseRule(checkList, caseStrategy, Constants.CASE_INFO_RULE);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage())).body(null);
                }
                while (iterator.hasNext()) {
                    CaseInfo next = iterator.next();
                    kieSession.insert(next);
                    kieSession.fireAllRules();
                }
                kieSession.dispose();
                if (!checkList.isEmpty()) {
                    for (CaseInfo caseInfo : checkList) {
                        StrategyPreviewModel model = new StrategyPreviewModel();
                        BeanUtils.copyProperties(caseInfo, model);
                        model.setCity(Objects.isNull(caseInfo.getArea()) ? null : caseInfo.getArea().getAreaName());
                        model.setIdCard(Objects.isNull(caseInfo.getPersonalInfo()) ? null : caseInfo.getPersonalInfo().getIdCard());
                        model.setPersonalName(Objects.isNull(caseInfo.getPersonalInfo()) ? null : caseInfo.getPersonalInfo().getName());
                        model.setPhone(Objects.isNull(caseInfo.getPersonalInfo()) ? null : caseInfo.getPersonalInfo().getMobileNo());
                        model.setPrincipalName(Objects.isNull(caseInfo.getPrincipalId()) ? null : caseInfo.getPrincipalId().getName());
                        modelList.add(model);
                    }
                }
            } else if (Objects.equals(previewParams.getType(), CaseStrategy.StrategyType.OUTS.getValue())) {// 委外策略分配
                List<CaseInfo> checkList = new ArrayList<>();
                QOutsourcePool qOutsourcePool = QOutsourcePool.outsourcePool;
                BooleanBuilder builder = new BooleanBuilder();
                if (Objects.isNull(user.getCompanyCode())) {
                    if (StringUtils.isNotBlank(previewParams.getCompanyCode())) {
                        builder.and(qOutsourcePool.companyCode.eq(previewParams.getCompanyCode()));
                    }
                } else {
                    builder.and(qOutsourcePool.companyCode.eq(user.getCompanyCode()));
                }
                builder.and(qOutsourcePool.outStatus.eq(OutsourcePool.OutStatus.TO_OUTSIDE.getCode()));
                builder.and(qOutsourcePool.caseInfo.recoverRemark.eq(CaseInfo.RecoverRemark.NOT_RECOVERED.getValue()));
                if (StringUtils.isNotBlank(previewParams.getPersonalName())) {
                    builder.and(qOutsourcePool.caseInfo.personalInfo.name.like("%" + previewParams.getPersonalName().trim() + "%"));
                }
                if (StringUtils.isNotBlank(previewParams.getPhone())) {
                    builder.and(qOutsourcePool.caseInfo.personalInfo.mobileNo.eq(previewParams.getPhone()));
                }
                if (StringUtils.isNotBlank(previewParams.getIdCard())) {
                    builder.and(qOutsourcePool.caseInfo.personalInfo.idCard.eq(previewParams.getIdCard()));
                }
                if (StringUtils.isNotBlank(previewParams.getBatchNumber())) {
                    builder.and(qOutsourcePool.caseInfo.batchNumber.eq(previewParams.getBatchNumber()));
                }
                if (Objects.nonNull(previewParams.getStartAmount())) {
                    builder.and(qOutsourcePool.caseInfo.overdueAmount.gt(previewParams.getStartAmount()));
                }
                if (Objects.nonNull(previewParams.getEndAmount())) {
                    builder.and(qOutsourcePool.caseInfo.overdueAmount.lt(previewParams.getEndAmount()));
                }
                if (areaIdFlag == true) {
                    builder.and(qOutsourcePool.caseInfo.area.isNotNull());
                }
                Iterable<OutsourcePool> all = outsourcePoolRepository.findAll(builder);
                Iterator<OutsourcePool> iterator = all.iterator();
                if (ppsFlag == true) {
                    while (iterator.hasNext()) {
                        OutsourcePool next = iterator.next();
                        QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
                        CaseInfo one = caseInfoRepository.findOne(qCaseInfo.id.eq(next.getCaseInfo().getId()));
                        if (Objects.nonNull(one.getProduct())) {
                            if (Objects.isNull(one.getProduct().getProductSeries())) {
                                iterator.remove();
                            }
                        } else {
                            iterator.remove();
                        }
                    }
                }
                List<CaseInfo> caseInfoList = new ArrayList<>();
                Iterator<OutsourcePool> iterator1 = all.iterator();
                while (iterator1.hasNext()) {
                    OutsourcePool outsourcePool = iterator1.next();
                    caseInfoList.add(outsourcePool.getCaseInfo());
                }
                KieSession kieSession = null;
                try {
                    kieSession = runCaseStrategyService.runCaseRule(checkList, caseStrategy, Constants.CASE_INFO_RULE);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage())).body(null);
                }
                if (!caseInfoList.isEmpty()) {
                    for (CaseInfo caseInfo : caseInfoList) {
                        kieSession.insert(caseInfo);
                        kieSession.fireAllRules();
                    }
                }
                kieSession.dispose();
                if (!checkList.isEmpty()) {
                    for (CaseInfo caseInfo : checkList) {
                        StrategyPreviewModel model = new StrategyPreviewModel();
                        BeanUtils.copyProperties(caseInfo, model);
                        model.setCity(Objects.isNull(caseInfo.getArea()) ? null : caseInfo.getArea().getAreaName());
                        model.setIdCard(Objects.isNull(caseInfo.getPersonalInfo()) ? null : caseInfo.getPersonalInfo().getIdCard());
                        model.setPersonalName(Objects.isNull(caseInfo.getPersonalInfo()) ? null : caseInfo.getPersonalInfo().getName());
                        model.setPhone(Objects.isNull(caseInfo.getPersonalInfo()) ? null : caseInfo.getPersonalInfo().getMobileNo());
                        model.setPrincipalName(Objects.isNull(caseInfo.getPrincipalId()) ? null : caseInfo.getPrincipalId().getName());
                        modelList.add(model);
                    }
                }
            } else {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请选择策略类型再预览")).body(null);
            }
            List<StrategyPreviewModel> collect = modelList.stream().skip(pageable.getPageNumber() * pageable.getPageSize()).limit(pageable.getPageSize()).collect(Collectors.toList());
            Page<StrategyPreviewModel> page = new PageImpl<>(collect, pageable, modelList.size());
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "预览结果失败")).body(null);
        }

    }

    @GetMapping("/findRecoverDistribute")
    @ApiOperation(value = "查询待分配案件的回收案件", notes = "查询待分配案件的回收案件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseInfoDistributed>> findRecoverDistribute(@QuerydslPredicate(root = CaseInfoDistributed.class) Predicate predicate,
                                                                           @ApiIgnore Pageable pageable,
                                                                           @RequestHeader(value = "X-UserToken") String token,
                                                                           @RequestParam(value = "companyCode", required = false) String companyCode) {
        logger.debug("REST request to findRecoverDistribute");
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage()))
                    .body(null);
        }
        try {
            QCaseInfoDistributed qd = QCaseInfoDistributed.caseInfoDistributed;
            BooleanBuilder builder = new BooleanBuilder(predicate);
            if (Objects.isNull(user.getCompanyCode())) {
                if (StringUtils.isNotBlank(companyCode)) {
                    builder.and(qd.companyCode.eq(companyCode));
                }
            } else {
                builder.and(qd.companyCode.eq(user.getCompanyCode()));
            }
            builder.and(qd.recoverRemark.eq(CaseInfo.RecoverRemark.RECOVERED.getValue()));
            Page<CaseInfoDistributed> page = caseInfoDistributedRepository.findAll(builder, pageable);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "系统异常!"))
                    .body(null);
        }
    }

    @DeleteMapping("/deleteRecoverDistribute")
    @ApiOperation(value = "删除待分配案件回收案件", notes = "删除待分配案件回收案件")
    public ResponseEntity deleteRecoverDistribute(@RequestBody CaseInfoIdList ids) {
        logger.debug("REST request to deleteRecoverDistribute");
        try {
            if (Objects.isNull(ids.getIds()) || ids.getIds().isEmpty()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请选择要删除的案件")).body(null);
            }
            List<CaseInfoDistributed> all = caseInfoDistributedRepository.findAll(ids.getIds());
            caseInfoDistributedRepository.delete(all);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("删除成功", "")).body(null);
        } catch (Exception e) {
            logger.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "删除错误")).body(null);
        }
    }

    @PostMapping("/importCaseScore")
    @ApiOperation(value = "案件导入待分配案件评分", notes = "案件导入待分配案件评分")
    public ResponseEntity importCaseScore(@RequestBody CaseInfoIdList params,
                                          @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            if (Objects.isNull(user.getCompanyCode())) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "超级管理员不允许此项操作")).body(null);
            }
            KieSession kieSession = null;
            try {
                kieSession = runCaseStrategyService.createSorceRule(user.getCompanyCode(), CaseStrategy.StrategyType.INNER.getValue());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage())).body(null);
            }
            Iterator<CaseInfoDistributed> iterator;
            if (Objects.nonNull(params.getIds()) && !params.getIds().isEmpty()) {
                List<CaseInfoDistributed> all = caseInfoDistributedRepository.findAll(params.getIds());
                iterator = all.iterator();
            } else {
                QCaseInfoDistributed qCaseInfoDistributed = QCaseInfoDistributed.caseInfoDistributed;
                BooleanBuilder builder = new BooleanBuilder();
                // 未回收
                builder.and(qCaseInfoDistributed.recoverRemark.eq(CaseInfo.RecoverRemark.NOT_RECOVERED.getValue()));
                builder.and(qCaseInfoDistributed.companyCode.eq(user.getCompanyCode()));
                Iterable<CaseInfoDistributed> all = caseInfoDistributedRepository.findAll(builder);
                iterator = all.iterator();
            }

            List<CaseInfoDistributed> caseInfoDistributedList = new ArrayList<>();
            while (iterator.hasNext()) {
                CaseInfoDistributed next = iterator.next();
                ScoreRuleModel scoreRuleModel = new ScoreRuleModel();
                if (Objects.nonNull(next.getPersonalInfo())) {
                    scoreRuleModel.setAge(Objects.isNull(next.getPersonalInfo().getAge()) ? 0 : next.getPersonalInfo().getAge());
                    if (Objects.nonNull(next.getPersonalInfo().getPersonalJobs()) && !next.getPersonalInfo().getPersonalJobs().isEmpty()) {
                        scoreRuleModel.setIsWork(1);
                    } else {
                        scoreRuleModel.setIsWork(0);
                    }
                }
                scoreRuleModel.setOverDueAmount(next.getOverdueAmount().doubleValue());
                scoreRuleModel.setOverDueDays(next.getOverdueDays());
                if (Objects.nonNull(next.getArea())) {
                    if (Objects.nonNull(next.getArea().getParent())) {
                        scoreRuleModel.setProId(next.getArea().getParent().getId());
                    }
                } else {
                    scoreRuleModel.setProId(null);
                }
                kieSession.insert(scoreRuleModel);
                kieSession.fireAllRules();
                if (scoreRuleModel.getCupoScore() != 0) {
                    next.setScore(ZWMathUtil.DoubleToBigDecimal(scoreRuleModel.getCupoScore(), null, null));
                    caseInfoDistributedList.add(next);
                }
            }
            kieSession.dispose();
            caseInfoDistributedRepository.save(caseInfoDistributedList);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("评分成功", "")).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "评分失败")).body(null);
        }
    }


}
