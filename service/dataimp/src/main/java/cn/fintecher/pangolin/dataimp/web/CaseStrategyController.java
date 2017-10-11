package cn.fintecher.pangolin.dataimp.web;

import cn.fintecher.pangolin.dataimp.repository.CaseStrategyRepository;
import cn.fintecher.pangolin.entity.CaseInfoDistributed;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.strategy.CaseStrategy;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWStringUtils;
import cn.fintecher.pangolin.web.HeaderUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by luqiang on 2017/8/2.
 */
@RestController
@RequestMapping("/api/caseStrategyController")
@Api(value = "CaseStrategyController", description = "案件策略")
public class CaseStrategyController {

    private final Logger logger = LoggerFactory.getLogger(CaseStrategy.class);
    private static final String ENTITY_NAME = "caseStrategy";

    @Autowired
    private CaseStrategyRepository caseStrategyRepository;
    @Autowired
    private Configuration freemarkerConfiguration;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MongoTemplate mongoTemplate;


    @GetMapping("/getCaseStrategy")
    @ApiOperation(value = "分配策略按条件分页查询", notes = "分配策略按条件分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<CaseStrategy>> getCaseStrategy(@RequestParam(required = false) @ApiParam("公司code码") String companyCode,
                                                              @RequestParam(required = false) @ApiParam("策略名称") String name,
                                                              @RequestParam(required = false) @ApiParam("策略类型") Integer strategyType,
                                                              @ApiIgnore Pageable pageable,
                                                              @RequestHeader(value = "X-UserToken") String token) {
        try {
            ResponseEntity<User> userResponseEntity;
            try {
                userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user", ENTITY_NAME)).body(null);
            }
            User user = userResponseEntity.getBody();
            Query query = new Query();
            if (Objects.isNull(user.getCompanyCode())) {
                if (StringUtils.isNotBlank(companyCode)) {
                    query.addCriteria(Criteria.where("companyCode").is(companyCode));
                }
            } else {
                query.addCriteria(Criteria.where("companyCode").is(user.getCompanyCode()));
            }
            if (StringUtils.isNotBlank(name)) {
                Pattern pattern = Pattern.compile("^" + name + ".*$", Pattern.CASE_INSENSITIVE);
                query.addCriteria(Criteria.where("name").regex(pattern));
            }
            if (Objects.nonNull(strategyType)) {
                query.addCriteria(Criteria.where("strategyType").is(strategyType));
            }
            int total = (int) mongoTemplate.count(query, CaseStrategy.class);
            query.with(pageable);
            List<CaseStrategy> list = mongoTemplate.find(query, CaseStrategy.class);
            return ResponseEntity.ok().body(new PageImpl<>(list, pageable, total));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "查询失败")).body(null);
        }
    }

    @PostMapping("/queryCaseInfoByCondition")
    @ApiOperation(value = "预览案件生成规则", notes = "预览案件生成规则")
    public ResponseEntity queryCaseInfoByCondition(@RequestBody CaseStrategy caseStrategy, @RequestHeader(value = "X-UserToken") String token) throws IOException, TemplateException {
        if (Objects.isNull(caseStrategy)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "no message", "没有分配策略信息")).body(null);
        }
        if (ZWStringUtils.isEmpty(caseStrategy.getStrategyJson())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "no message", "没有分配策略信息")).body(null);
        }
        ResponseEntity<User> userResponseEntity = null;
        try {
            userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user", ENTITY_NAME)).body(null);
        }
        User user = userResponseEntity.getBody();
        String companyCode = user.getCompanyCode();
        try {
            StringBuilder sb = new StringBuilder();
            analysisRule(caseStrategy.getStrategyJson(), sb);
            caseStrategy.setStrategyText(sb.toString());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "解析分配策略失败")).body(null);
        }
        caseStrategy.setId(UUID.randomUUID().toString());
        List<CaseInfoDistributed> caseInfoLsit = runCaseRun(caseStrategy, true, companyCode);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("预览成功", "")).body(caseInfoLsit);
    }

    @PostMapping("/addCaseStrategy")
    @ApiOperation(value = "新增/修改策略", notes = "新增/修改策略")
    public ResponseEntity addCaseStrategy(@RequestBody CaseStrategy caseStrategy,
                                          @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {

        ResponseEntity<User> userResponseEntity = null;
        try {
            userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "user", "请登录!")).body(null);
        }
        User user = userResponseEntity.getBody();
        if (Objects.isNull(user.getCompanyCode())) {
            if (StringUtils.isBlank(caseStrategy.getCompanyCode())) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请选择公司")).body(null);
            }
            user.setCompanyCode(caseStrategy.getCompanyCode());
        }
        Integer strategyType = caseStrategy.getStrategyType();
        StringBuilder sb = new StringBuilder();
        try {
            analysisRule(caseStrategy.getStrategyJson(), sb);
        } catch (Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", e.getMessage())).body(null);
        }
        try {
            String strategyText = sb.toString();
            CaseStrategy cs = new CaseStrategy();
            if (Objects.nonNull(caseStrategy.getId()) && StringUtils.isNotBlank(caseStrategy.getId())) {
                cs.setId(caseStrategy.getId());
            }
            cs.setCreateTime(new Date());
            cs.setCreator(user.getRealName());
            cs.setCreatorId(user.getId());
            cs.setCompanyCode(user.getCompanyCode());
            cs.setName(caseStrategy.getName());
            cs.setStrategyJson(caseStrategy.getStrategyJson());
            cs.setStrategyText(strategyText);
            cs.setPriority(caseStrategy.getPriority());
            if (Objects.isNull(strategyType)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请选择要新增的策略类型")).body(null);
            } else if (Objects.equals(strategyType, CaseStrategy.StrategyType.IMPORT.getValue())) {//导入分配策略
                cs.setStrategyType(CaseStrategy.StrategyType.IMPORT.getValue());
                if (Objects.equals(caseStrategy.getAssignType(), CaseStrategy.AssignType.INNER_POOL.getValue())) { //内催池
                    cs.setAssignType(CaseStrategy.AssignType.INNER_POOL.getValue());
                } else if (Objects.equals(caseStrategy.getAssignType(), CaseStrategy.AssignType.OUTER_POOL.getValue())) {//委外池
                    cs.setAssignType(CaseStrategy.AssignType.OUTER_POOL.getValue());
                } else {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请选择要指定的对象")).body(null);
                }
                cs.setAssignType(caseStrategy.getAssignType());
            } else if (Objects.equals(strategyType, CaseStrategy.StrategyType.INNER.getValue())) {//内催分配策略
                cs.setStrategyType(CaseStrategy.StrategyType.INNER.getValue());
                Integer assignType = caseStrategy.getAssignType();
                if (Objects.equals(assignType, CaseStrategy.AssignType.DEPART.getValue())) {//机构
                    cs.setAssignType(CaseStrategy.AssignType.DEPART.getValue());
                    cs.setDepartments(caseStrategy.getDepartments());
                } else if (Objects.equals(assignType, CaseStrategy.AssignType.COLLECTOR.getValue())) {//催收员
                    cs.setAssignType(CaseStrategy.AssignType.COLLECTOR.getValue());
                    if (caseStrategy.getUsers().isEmpty()) {
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请选择要指定的催收员")).body(null);
                    }
                    cs.setUsers(caseStrategy.getUsers());
                } else {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请选择策略指定的对象")).body(null);
                }
            } else if (Objects.equals(strategyType, CaseStrategy.StrategyType.OUTS.getValue())) {//委外分配策略
                cs.setStrategyType(CaseStrategy.StrategyType.OUTS.getValue());
                if (Objects.equals(caseStrategy.getAssignType(), CaseStrategy.AssignType.OUTER.getValue())) {
                    cs.setAssignType(CaseStrategy.AssignType.OUTER.getValue());
                    if (caseStrategy.getOutsource().isEmpty()) {
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "")).body(null);
                    }
                    cs.setOutsource(caseStrategy.getOutsource());
                } else {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请选择策略指定的对象")).body(null);
                }
            } else {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "未匹配到要新增的策略类型")).body(null);
            }
            caseStrategyRepository.save(cs);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("新增成功", "")).body(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "新增失败!")).body(null);
        }
    }

//    @ApiModelProperty
//    @PostMapping("/smartDistribute")
//    @ApiOperation(value = "策略分配案件", notes = "策略分配案件")
//    public ResponseEntity smartDistribute(@RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) throws Exception {
//
//        ResponseEntity<User> userResult = null;
//        userResult = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
//        if (!userResult.hasBody()) {
//            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "failure", "用户未登录")).body(null);
//        }
//        User user = userResult.getBody();
//        String cmpanyCode = user.getCompanyCode();
//        ParameterizedTypeReference<List<CaseInfoException>> responseType = new ParameterizedTypeReference<List<CaseInfoException>>() {
//        };
//        ResponseEntity<List<CaseInfoException>> resp = restTemplate.exchange(Constants.BUSINESS_SERVICE_URL.concat("getAllExceptionCaseInfo").concat("?companyCode=").concat(cmpanyCode),
//                HttpMethod.GET, null, responseType);
//        List<CaseInfoException> caseInfoExceptions = resp.getBody();
//        if (caseInfoExceptions.size() > 0) {
//            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "failure", "有异常案件未处理请先处理")).body(null);
//        }
//        Iterable<CaseStrategy> caseStrategies = caseStrategyRepository.findAll(QCaseStrategy.caseStrategy.companyCode.eq(user.getCompanyCode()), new Sort(Sort.Direction.ASC, "priority"));
//
//        if (Objects.isNull(caseStrategies) || !caseStrategies.iterator().hasNext()) {
//            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "no strategy", "暂无分配策略")).body("");
//        }
//        //案件列表
//        List<CaseInfo> caseInfoObjList = new ArrayList<>();
//        //接收案件列表信息
//        List<String> dataList = null;
//        //计算平均分配案件数
//        // List<Integer> disNumList = new ArrayList<>();
//        for (CaseStrategy caseStrategy : caseStrategies) {
//            String companyCode = user.getCompanyCode();
//            //得到符合分配策略的案件 caseInfos
//            List<CaseInfoDistributed> caseInfos = runCaseRun(caseStrategy, false, companyCode);
//            if (Objects.isNull(caseInfos) || caseInfos.isEmpty()) {
//                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "not in line with the strategy of the case", "没有符合策略的案件")).body("");
//            } else {
//                //走案件分配流程
//
//                //流转记录列表
//                List<CaseTurnRecord> caseTurnRecordList = new ArrayList<>();
//                List<CaseRepair> caseRepairList = new ArrayList<>();
//                List<String> caseIds = new ArrayList<>();
//                //符合策略的案件id列表
//                for (CaseInfoDistributed caseInfoDistributed : caseInfos) {
//                    caseIds.add(caseInfoDistributed.getId());//案件id集合
//                }
//                //每个机构或人分配案件的数量
//                // List<Integer> caseInfoDisModelList = caseInfoDisModel.getCaseNumList();
//                //已经分配的案件数量
//                int alreadyCaseNum = 0;
//                //接收案件列表信息
//                List<String> deptOrUserList = null;
//                if (caseStrategy.getAssignType().equals(CaseInfoDisModel.DisType.DEPART_WAY.getValue())) {
//                    //所要分配 机构id
//                    deptOrUserList = caseStrategy.getDepartments();
//                } else if (caseStrategy.getAssignType().equals(CaseInfoDisModel.DisType.USER_WAY.getValue())) {
//                    //得到所有用户ID
//                    deptOrUserList = caseStrategy.getUsers();
//                }
//                for (int i = 0; i < (deptOrUserList != null ? deptOrUserList.size() : 0); i++) {
//                    //如果按机构分配则是机构的ID，如果是按用户分配则是用户ID
//                    String deptOrUserid = deptOrUserList.get(i);
//                    Department department = null;
//                    User targetUser = null;
//                    if (caseStrategy.getAssignType().equals(CaseInfoDisModel.DisType.DEPART_WAY.getValue())) {
//                        ResponseEntity<Department> departmentResponseEntity = restTemplate.getForEntity(Constants.ORG_SERVICE_URL.concat("getDepartmentById").concat("?deptId=").concat(deptOrUserid), Department.class);
//                        department = departmentResponseEntity.getBody();
//                    } else if (caseStrategy.getAssignType().equals(CaseInfoDisModel.DisType.USER_WAY.getValue())) {
//                        ResponseEntity<User> userResponseEntity = restTemplate.getForEntity("http://business-service/api/userResource/findUserById?id=" + deptOrUserid, User.class);
//
//                        targetUser = userResponseEntity.getBody();
//                    }
//                    //计算平均分配案件数
//                    List<Integer> disNumList = new ArrayList<>();
//                    setDistributeNum(caseInfos, deptOrUserList, disNumList);
//                    Integer disNum = disNumList.get(i);//每个人分配的案件数
//                    for (int j = 0; j < disNum; j++) {
//                        CaseInfoDistributed caseInfoDistributed = caseInfos.get(alreadyCaseNum);
//                        if (Objects.nonNull(caseInfoDistributed)) {
//                            CaseInfo caseInfo = new CaseInfo();
//                            BeanUtils.copyProperties(caseInfoDistributed, caseInfo);
//                            if (Objects.nonNull(department)) {
//                                caseInfo.setDepartment(department); //部门
//                                if (Objects.equals(department.getType(), Department.Type.TELEPHONE_COLLECTION.getValue())) {
//                                    caseInfo.setCollectionType(CaseInfo.CollectionType.TEL.getValue());
//                                } else if (Objects.equals(department.getType(), Department.Type.OUTBOUND_COLLECTION.getValue())) {
//                                    caseInfo.setCollectionType(CaseInfo.CollectionType.VISIT.getValue());
//                                } else if (Objects.equals(department.getType(), Department.Type.JUDICIAL_COLLECTION.getValue())) {
//                                    caseInfo.setCollectionType(CaseInfo.CollectionType.JUDICIAL.getValue());
//                                } else if (Objects.equals(department.getType(), Department.Type.OUTSOURCING_COLLECTION.getValue())) {
//                                    caseInfo.setCollectionType(CaseInfo.CollectionType.outside.getValue());
//                                } else if (Objects.equals(department.getType(), Department.Type.REMIND_COLLECTION.getValue())) {
//                                    caseInfo.setCollectionType(CaseInfo.CollectionType.remind.getValue());
//                                }
//                                caseInfo.setCaseFollowInTime(ZWDateUtil.getNowDateTime()); //案件流入时间
//                                caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue()); //催收状态-待分配
//                            }
//                            if (Objects.nonNull(targetUser)) {
//                                caseInfo.setDepartment(targetUser.getDepartment());
//                                caseInfo.setCurrentCollector(targetUser);
//                                if (Objects.equals(targetUser.getType(), User.Type.TEL.getValue())) {
//                                    caseInfo.setCollectionType(CaseInfo.CollectionType.TEL.getValue());
//                                } else if (Objects.equals(targetUser.getType(), User.Type.VISIT.getValue())) {
//                                    caseInfo.setCollectionType(CaseInfo.CollectionType.VISIT.getValue());
//                                } else if (Objects.equals(targetUser.getType(), User.Type.JUD.getValue())) {
//                                    caseInfo.setCollectionType(CaseInfo.CollectionType.JUDICIAL.getValue());
//                                } else if (Objects.equals(targetUser.getType(), User.Type.OUT.getValue())) {
//                                    caseInfo.setCollectionType(CaseInfo.CollectionType.outside.getValue());
//                                } else if (Objects.equals(targetUser.getType(), User.Type.REMINDER.getValue())) {
//                                    caseInfo.setCollectionType(CaseInfo.CollectionType.remind.getValue());
//                                }
//                                caseInfo.setCaseFollowInTime(ZWDateUtil.getNowDateTime());
//                                caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.WAITCOLLECTION.getValue()); //催收状态-待催收
//                            }
//                            caseInfo.setLeaveCaseFlag(CaseInfo.leaveCaseFlagEnum.NO_LEAVE.getValue()); //留案标识默认-非留案
//                            caseInfo.setAssistFlag(CaseInfo.AssistFlag.NO_ASSIST.getValue());
//                            //案件剩余天数(结案日期-当前日期)
//                            caseInfo.setLeftDays(ZWDateUtil.getBetween(ZWDateUtil.getNowDate(), caseInfo.getCloseDate(), ChronoUnit.DAYS));
//                            //案件类型
//                            caseInfo.setCaseType(CaseInfo.CaseType.DISTRIBUTE.getValue());
//                            caseInfo.setOperator(user);
//                            caseInfo.setCaseMark(CaseInfo.Color.NO_COLOR.getValue());//打标标记
//                            caseInfo.setFollowUpNum(0);//流转次数
//                            caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime());
//                            //案件流转记录
//                            CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
//                            BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
//                            caseTurnRecord.setId(null); //主键置空
//                            caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
//                            caseTurnRecord.setDepartId(caseInfo.getDepartment().getId()); //部门ID
//
//                            if (Objects.nonNull(caseInfo.getCurrentCollector())) {
//                                caseTurnRecord.setReceiveDeptName(caseInfo.getCurrentCollector().getDepartment().getName()); //接收部门名称
//                                caseTurnRecord.setReceiveUserRealName(caseInfo.getCurrentCollector().getRealName()); //接受人名称
//                                caseTurnRecord.setReceiveUserId(caseInfo.getCurrentCollector().getId()); //接收人ID
//                            } else {
//                                caseTurnRecord.setReceiveDeptName(caseInfo.getDepartment().getName());
//                            }
//                            caseTurnRecord.setCirculationType(3); //流转类型 3-正常流转
//                            caseTurnRecord.setOperatorUserName(user.getUserName()); //操作员
//                            caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
//                            caseTurnRecordList.add(caseTurnRecord);
//
//                            //进入案件修复池
//                            CaseRepair caseRepair = new CaseRepair();
//                            caseRepair.setCaseId(caseInfo);
//                            caseRepair.setRepairStatus(CaseRepair.CaseRepairStatus.REPAIRING.getValue());
//                            caseRepair.setOperatorTime(ZWDateUtil.getNowDateTime());
//                            caseRepair.setCompanyCode(user.getCompanyCode());
//                            caseRepairList.add(caseRepair);
//                            caseInfo.setId(null);
//                            //案件列表
//                            caseInfoObjList.add(caseInfo);
//                        }
//                        alreadyCaseNum = alreadyCaseNum + 1;
//                    }
//                }
//                //保存案件信息
//                //  restTemplate.postForEntity(Constants.BUSINESS_SERVICE_URL.concat("saveCaseInfo"), caseInfoObjList, CaseInfo.class);
//                //保存流转记录
//                restTemplate.postForEntity(Constants.BUSINESS_SERVICE_URL.concat("saveCaseInfoRecord"), caseTurnRecordList, CaseTurnRecord.class);
//                //保存修复信息
//                restTemplate.postForEntity(Constants.BUSINESS_SERVICE_URL.concat("saveRepair"), caseRepairList, CaseRepair.class);
//                //删除待分配案件
//                for (String id : caseIds) {
//                    restTemplate.delete(Constants.BUSINESS_SERVICE_URL.concat("deleteCaseInfoDistributed").concat("?id=").concat(id));
//                }
//            }
//        }
//        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, " successfully", "分配成功")).body(caseInfoObjList);
//    }

    @GetMapping("/findCaseStrategy")
    @ApiOperation(value = "检查策略名称是否重复", notes = "检查策略名称是否重复")
    public ResponseEntity findCaseStrategy(@RequestParam @ApiParam("操作者的Token") String name,
                                           @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        try {
            ResponseEntity<User> userResult = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
            User user = userResult.getBody();
            Query query = new Query();
            query.addCriteria(Criteria.where("companyCode").is(user.getCompanyCode()));
            query.addCriteria(Criteria.where("name").is(name));
            long count = mongoTemplate.count(query, CaseStrategy.class);
            if (count != 0) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "策略名称已存在")).body(null);
            }
            return ResponseEntity.ok().body(null);
        } catch (RestClientException e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "检查策略异常")).body(null);
        }
    }

    @GetMapping("/deleteCaseStrategy")
    @ApiOperation(value = "删除分配策略", notes = "删除分配策略")
    public ResponseEntity deleteCaseStrategy(@RequestParam @ApiParam("策略ID") String ruleId) {
        try {
            caseStrategyRepository.delete(ruleId);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("删除成功", "")).body(null);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "删除失败")).body(null);
        }
    }

    private String analysisRule(String jsonObject, StringBuilder stringBuilder) {
        String result = "";
        if (Objects.isNull(jsonObject) || jsonObject.isEmpty()) {
            return null;
        }
        try {
            JSONArray jsonArray = new JSONArray(jsonObject);
            int iSize = jsonArray.length();
            for (int i = 0; i < iSize; i++) {
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                if (jsonObj.getBoolean("leaf")) {
                    stringBuilder.append(jsonObj.get("relation"));
                    stringBuilder.append(jsonObj.get("variable"));
                    stringBuilder.append(jsonObj.get("symbol"));
                    stringBuilder.append("\"");
                    stringBuilder.append(jsonObj.get("value"));
                    stringBuilder.append("\"");
                } else {
                    stringBuilder.append("(");
                    analysisRule(jsonObj.getJSONArray("children").toString(), stringBuilder);
                    stringBuilder.append(")");
                    if (jsonObj.has("relation")) {
                        stringBuilder.append(jsonObj.get("relation"));
                    }
                }
            }
            // stringBuilder.delete(0, 1);
            return stringBuilder.toString();
        } catch (Exception e) {
            //  e.printStackTrace();
            return null;
        }
    }

    /**
     * @param caseStrategy
     * @param flag         用来区分是预览用还是分配用
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public List<CaseInfoDistributed> runCaseRun(CaseStrategy caseStrategy, boolean flag, String companyCode) throws IOException, TemplateException {
        List<CaseInfoDistributed> checkedList = new ArrayList<>();
        Template template = freemarkerConfiguration.getTemplate("caseInfo.ftl", "UTF-8");
        Map<String, String> map = new HashMap<>();
        map.put("id", caseStrategy.getId());
        map.put("strategyText", caseStrategy.getStrategyText());
        String rule = FreeMarkerTemplateUtils.processTemplateIntoString(template, caseStrategy);
        logger.debug("案件策略公式为：【" + rule + "】");
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kfs = kieServices.newKieFileSystem();
        kfs.write("src/main/resources/simple.drl",
                kieServices.getResources().newReaderResource(new StringReader(rule)));
        KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();

        Results results = kieBuilder.getResults();
        if (results.hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
            System.out.println(results.getMessages());
            throw new IllegalStateException("### errors ###");
        }
        KieContainer kieContainer =
                kieServices.newKieContainer(kieBuilder.getKieModule().getReleaseId());
        KieSession kieSession = kieContainer.newKieSession();
        kieSession.setGlobal("checkedList", checkedList);
        List<CaseInfoDistributed> caseInfoList = null;
        ParameterizedTypeReference<List<CaseInfoDistributed>> responseType = new ParameterizedTypeReference<List<CaseInfoDistributed>>() {
        };
        ResponseEntity<List<CaseInfoDistributed>> resp = restTemplate.exchange(Constants.BUSINESS_SERVICE_URL.concat("getAllCaseInfo").concat("?companyCode=").concat(companyCode),
                HttpMethod.GET, null, responseType);
        caseInfoList = resp.getBody();
        for (CaseInfoDistributed caseInfoDistributed : caseInfoList) {
            kieSession.insert(caseInfoDistributed);//插入
            kieSession.fireAllRules();//执行规则
            if (checkedList.size() > 9 && flag) {
                break;
            }
        }
        kieSession.dispose();

        return checkedList;
    }

    public void setDistributeNum(List<CaseInfoDistributed> caseInfoModelsTemp, List<String> dataList, List<Integer> disNumList) {
        if (caseInfoModelsTemp.size() > dataList.size()) {
            int number = caseInfoModelsTemp.size() / dataList.size();
            for (int i = 0; i < dataList.size() - 1; i++) {
                disNumList.add(number);
            }
            disNumList.add(number + caseInfoModelsTemp.size() % dataList.size());
        } else {
            for (int i = 0; i < dataList.size(); i++) {
                if (i < caseInfoModelsTemp.size()) {//如果人数暂时小于案件数
                    disNumList.add(1);
                } else {
                    disNumList.add(0);
                }
                int num = dataList.size() - caseInfoModelsTemp.size();//分不到案件的人数
                //disNumList.add(1);
            }
        }
    }

}
