package cn.fintecher.pangolin.dataimp.web;

import cn.fintecher.pangolin.dataimp.entity.CaseStrategy;
import cn.fintecher.pangolin.dataimp.entity.QCaseStrategy;
import cn.fintecher.pangolin.dataimp.model.CaseInfoDisModel;
import cn.fintecher.pangolin.dataimp.repository.CaseStrategyRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.EntityUtil;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.util.ZWStringUtils;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.swagger.annotations.*;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.io.StringReader;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Created by luqiang on 2017/8/2.
 */
@RestController
@RequestMapping("/api/caseStrategyController")
@Api(value = "", description = "案件策略")
public class CaseStrategyController {
    @Autowired
    private CaseStrategyRepository caseStrategyRepository;
    @Autowired
    private Configuration freemarkerConfiguration;
    @Autowired
    private RestTemplate restTemplate;

    private final Logger logger = LoggerFactory.getLogger(CaseStrategy.class);
    private static final String ENTITY_TEMPLATE = "caseStrategy";
    private static final String ENTITY_NAME = "caseStrategy";

    @GetMapping("getCaseStrategy")
    @ApiOperation(value = "分配策略按条件分页查询", notes = "分配策略按条件分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity getCaseStrategy(@RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
                                           @QuerydslPredicate(root = CaseStrategy.class) Predicate predicate, @ApiIgnore Pageable pageable,
                                          @RequestHeader(value = "X-UserToken") String token) {
        try {
            ResponseEntity<User> userResponseEntity=null;
            try {
                userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
            }catch (Exception e){
                logger.error(e.getMessage(),e);
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user",ENTITY_NAME)).body(null);
            }
            User user=userResponseEntity.getBody();
            BooleanBuilder builder = new BooleanBuilder(predicate);
            if(Objects.isNull(user.getCompanyCode())){
                if(Objects.isNull(companyCode)){
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseStrategy", "caseStrategy", "请选择公司")).body(null);
                }
                builder.and(QTemplate.template.companyCode.eq(companyCode));
            }else{
                builder.and(QTemplate.template.companyCode.eq(user.getCompanyCode()));
            }
            Page<CaseStrategy> page = caseStrategyRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/caseStrategyController/getCaseStrategy");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "template", e.getMessage())).body(null);
        }
    }

    @ResponseBody
    @PostMapping("/queryCaseInfoByCondition")
    @ApiOperation(value = "预览案件生成规则", notes = "预览案件生成规则")
    public ResponseEntity queryCaseInfoByCondition(@RequestBody CaseStrategy caseStrategy) throws IOException, TemplateException {
        if (Objects.isNull(caseStrategy)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "no message", "没有分配策略信息")).body(null);
        }
        if (ZWStringUtils.isEmpty(caseStrategy.getStrategyJson())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "no message", "没有分配策略信息")).body(null);
        }
        try {
            StringBuilder sb = new StringBuilder();
            analysisRule(caseStrategy.getStrategyJson(), sb);
            caseStrategy.setStrategyText(sb.toString());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "解析分配策略失败")).body(null);
        }
        caseStrategy.setId(UUID.randomUUID().toString());
        List<CaseInfoDistributed> caseInfoLsit = runCaseRun(caseStrategy, true);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "预览成功")).body(caseInfoLsit);
    }

    @ApiModelProperty
    @PostMapping("/addCaseStrategy")
    @ApiOperation(value = "生成案件分配策略", notes = "生成案件分配策略")
    public ResponseEntity addCaseStrategy(@RequestBody CaseStrategy caseStrategy, @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) throws IOException, TemplateException {
        caseStrategy = (CaseStrategy) EntityUtil.emptyValueToNull(caseStrategy);
        if (Objects.isNull(caseStrategy)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "no message", "没有分配策略信息")).body(null);
        }
        if (ZWStringUtils.isEmpty(caseStrategy.getStrategyJson())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "no message", "没有分配策略信息")).body(null);
        }
        //获取用户
        ResponseEntity<User> userResult = null;
        try {
            userResult = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "system error", "系统异常")).body(null);
        }
        if (!userResult.hasBody()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "no login", "用户没有登录")).body(null);
        }
        try {
            StringBuilder sb = new StringBuilder();
            analysisRule(caseStrategy.getStrategyJson(), sb);
            caseStrategy.setStrategyText(sb.toString());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

        }
        User user = userResult.getBody();
        if(Objects.isNull(user.getCompanyCode())){//如果是超级管理员，code码为空
            caseStrategy.setCompanyCode(null);
        }else{
            caseStrategy.setCompanyCode(user.getCompanyCode());
        }
        caseStrategy.setCreateTime(ZWDateUtil.getNowDateTime());
        caseStrategy.setCreator(userResult.getBody().getRealName());
        CaseStrategy caseStrategyNew = caseStrategyRepository.save(caseStrategy);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, " successfully", "解析成功")).body(caseStrategyNew);
    }

    @ApiModelProperty
    @PostMapping("/smartDistribute")
    @ApiOperation(value = "策略分配案件", notes = "策略分配案件")
    public ResponseEntity smartDistribute(@RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) throws Exception {
        List<CaseStrategy> caseStrategies = caseStrategyRepository.findAll(new Sort(Sort.Direction.ASC, "priority"));
        if (caseStrategies.isEmpty()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "no strategy", "没有查询到的分配策略")).body("");
        }
        //案件列表
        List<CaseInfo> caseInfoObjList = new ArrayList<>();
        //接收案件列表信息
        List<String> dataList = null;
        //计算平均分配案件数
        // List<Integer> disNumList = new ArrayList<>();
        for (CaseStrategy caseStrategy : caseStrategies) {
            //得到符合分配策略的案件 caseInfos
            List<CaseInfoDistributed> caseInfos = runCaseRun(caseStrategy, false);
            if (Objects.isNull(caseInfos) || caseInfos.isEmpty()) {
                continue;
            } else {
                //走案件分配流程
                ResponseEntity<User> userResult = null;
                userResult = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
                if (!userResult.hasBody()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "failure", "用户未登录")).body(null);
                }
                User user = userResult.getBody();
                //流转记录列表
                List<CaseTurnRecord> caseTurnRecordList = new ArrayList<>();
                List<CaseRepair> caseRepairList = new ArrayList<>();
                List<String> caseIds = new ArrayList<>();
                //符合策略的案件id列表
                for (CaseInfoDistributed caseInfoDistributed : caseInfos) {
                    caseIds.add(caseInfoDistributed.getId());//案件id集合
                }
                //每个机构或人分配案件的数量
                // List<Integer> caseInfoDisModelList = caseInfoDisModel.getCaseNumList();
                //已经分配的案件数量
                int alreadyCaseNum = 0;
                //接收案件列表信息
                List<String> deptOrUserList = null;
                if (caseStrategy.getAssignType().equals(CaseInfoDisModel.DisType.DEPART_WAY.getValue())) {
                    //所要分配 机构id
                    deptOrUserList = caseStrategy.getDepartments();
                } else if (caseStrategy.getAssignType().equals(CaseInfoDisModel.DisType.USER_WAY.getValue())) {
                    //得到所有用户ID
                    deptOrUserList = caseStrategy.getUsers();
                }
                for (int i = 0; i < (deptOrUserList != null ? deptOrUserList.size() : 0); i++) {
                    //如果按机构分配则是机构的ID，如果是按用户分配则是用户ID
                    String deptOrUserid = deptOrUserList.get(i);
                    Department department = null;
                    User targetUser = null;
                    if (caseStrategy.getAssignType().equals(CaseInfoDisModel.DisType.DEPART_WAY.getValue())) {
                        ResponseEntity<Department> departmentResponseEntity = restTemplate.getForEntity(Constants.ORG_SERVICE_URL.concat("getDepartmentById").concat("?deptId=").concat(deptOrUserid), Department.class);
                        department = departmentResponseEntity.getBody();
                    } else if (caseStrategy.getAssignType().equals(CaseInfoDisModel.DisType.USER_WAY.getValue())) {
                        ResponseEntity<User> userResponseEntity = restTemplate.getForEntity("http://business-service/api/userResource/findUserById?id=" + deptOrUserid, User.class);

                        targetUser = userResponseEntity.getBody();
                    }
                    //计算平均分配案件数
                    List<Integer> disNumList = new ArrayList<>();
                    setDistributeNum(caseInfos, deptOrUserList, disNumList);
                    Integer disNum = disNumList.get(i);//每个人分配的案件数
                    for (int j = 0; j < disNum; j++) {
                        CaseInfoDistributed caseInfoDistributed = caseInfos.get(alreadyCaseNum);
                        if (Objects.nonNull(caseInfoDistributed)) {
                            CaseInfo caseInfo = new CaseInfo();
                            BeanUtils.copyProperties(caseInfoDistributed, caseInfo);
                            if (Objects.nonNull(department)) {
                                caseInfo.setDepartment(department); //部门
                                if (Objects.equals(department.getType(), Department.Type.TELEPHONE_COLLECTION.getValue())) {
                                    caseInfo.setCollectionType(CaseInfo.CollectionType.TEL.getValue());
                                } else if (Objects.equals(department.getType(), Department.Type.OUTBOUND_COLLECTION.getValue())) {
                                    caseInfo.setCollectionType(CaseInfo.CollectionType.VISIT.getValue());
                                } else if (Objects.equals(department.getType(), Department.Type.JUDICIAL_COLLECTION.getValue())) {
                                    caseInfo.setCollectionType(CaseInfo.CollectionType.JUDICIAL.getValue());
                                } else if (Objects.equals(department.getType(), Department.Type.OUTSOURCING_COLLECTION.getValue())) {
                                    caseInfo.setCollectionType(CaseInfo.CollectionType.outside.getValue());
                                } else if (Objects.equals(department.getType(), Department.Type.REMIND_COLLECTION.getValue())) {
                                    caseInfo.setCollectionType(CaseInfo.CollectionType.remind.getValue());
                                }
                                caseInfo.setCaseFollowInTime(null); //案件流入时间
                                caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue()); //催收状态-待分配
                            }
                            if (Objects.nonNull(targetUser)) {
                                caseInfo.setDepartment(targetUser.getDepartment());
                                caseInfo.setCurrentCollector(targetUser);
                                if (Objects.equals(targetUser.getType(), User.Type.TEL.getValue())) {
                                    caseInfo.setCollectionType(CaseInfo.CollectionType.TEL.getValue());
                                } else if (Objects.equals(targetUser.getType(), User.Type.VISIT.getValue())) {
                                    caseInfo.setCollectionType(CaseInfo.CollectionType.VISIT.getValue());
                                } else if (Objects.equals(targetUser.getType(), User.Type.JUD.getValue())) {
                                    caseInfo.setCollectionType(CaseInfo.CollectionType.JUDICIAL.getValue());
                                } else if (Objects.equals(targetUser.getType(), User.Type.OUT.getValue())) {
                                    caseInfo.setCollectionType(CaseInfo.CollectionType.outside.getValue());
                                } else if (Objects.equals(targetUser.getType(), User.Type.REMINDER.getValue())) {
                                    caseInfo.setCollectionType(CaseInfo.CollectionType.remind.getValue());
                                }
                                caseInfo.setCaseFollowInTime(ZWDateUtil.getNowDateTime());
                                caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.WAITCOLLECTION.getValue()); //催收状态-待催收
                            }
                            caseInfo.setAssistFlag(CaseInfo.AssistFlag.NO_ASSIST.getValue());
                            //案件剩余天数(结案日期-当前日期)
                            caseInfo.setLeftDays(ZWDateUtil.getBetween(ZWDateUtil.getNowDate(), caseInfo.getCloseDate(), ChronoUnit.DAYS));
                            //案件类型
                            caseInfo.setCaseType(CaseInfo.CaseType.DISTRIBUTE.getValue());
                            caseInfo.setOperator(user);
                            caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime());
                            //案件流转记录
                            CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
                            BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
                            caseTurnRecord.setId(null); //主键置空
                            caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
                            caseTurnRecord.setDepartId(caseInfo.getDepartment().getId()); //部门ID

                            if (Objects.nonNull(caseInfo.getCurrentCollector())) {
                                caseTurnRecord.setReceiveDeptName(caseInfo.getCurrentCollector().getDepartment().getName()); //接收部门名称
                                caseTurnRecord.setReceiveUserRealName(caseInfo.getCurrentCollector().getRealName()); //接受人名称
                            } else {
                                caseTurnRecord.setReceiveDeptName(caseInfo.getDepartment().getName());
                            }
                            caseTurnRecord.setCirculationType(3); //流转类型 3-正常流转
                            caseTurnRecord.setOperatorUserName(user.getUserName()); //操作员
                            caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                            caseTurnRecordList.add(caseTurnRecord);

                            //进入案件修复池
                            CaseRepair caseRepair = new CaseRepair();
                            caseRepair.setCaseId(caseInfo);
                            caseRepair.setRepairStatus(CaseRepair.CaseRepairStatus.REPAIRING.getValue());
                            caseRepair.setOperatorTime(ZWDateUtil.getNowDateTime());
                            caseRepair.setCompanyCode(user.getCompanyCode());
                            caseRepairList.add(caseRepair);
                            caseInfo.setId(null);
                            //案件列表
                            caseInfoObjList.add(caseInfo);
                        }
                        alreadyCaseNum = alreadyCaseNum + 1;
                    }
                }
                //保存案件信息
                //  restTemplate.postForEntity(Constants.BUSINESS_SERVICE_URL.concat("saveCaseInfo"), caseInfoObjList, CaseInfo.class);
                //保存流转记录
                restTemplate.postForEntity(Constants.BUSINESS_SERVICE_URL.concat("saveCaseInfoRecord"), caseTurnRecordList, CaseTurnRecord.class);
                //保存修复信息
                restTemplate.postForEntity(Constants.BUSINESS_SERVICE_URL.concat("saveRepair"), caseRepairList, CaseRepair.class);
                //删除待分配案件
                for (String id : caseIds) {
                    restTemplate.delete(Constants.BUSINESS_SERVICE_URL.concat("deleteCaseInfoDistributed").concat("?id=").concat(id));
                }
            }
        }
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, " successfully", "分配成功")).body(caseInfoObjList);
    }

    @ApiModelProperty
    @GetMapping("/findCaseStrategy")
    @ApiOperation(value = "检查策略名称是否重复", notes = "检查策略名称是否重复")
    public ResponseEntity findCaseStrategy(@RequestParam String name) {
        if (ZWStringUtils.isEmpty(name)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "no name", "没有输入策略名称")).body(null);
        }
        try {
            CaseStrategy caseStrategy = caseStrategyRepository.findOne(QCaseStrategy.caseStrategy.name.eq(name));
           if(Objects.nonNull(caseStrategy)){
               return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "exist", "该策略名称已存在")).body(null);
           }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "failure", "检查策略名称失败")).body(null);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("操作成功","caseInfo")).body(null);
    }

    @ApiModelProperty
    @GetMapping("/deleteCaseStrategy")
    @ApiOperation(value = "删除分配策略", notes = "删除分配策略")
    public ResponseEntity queryCaseInfoByCondition(@RequestParam String ruleId) {
        if (ZWStringUtils.isEmpty(ruleId)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "failure", "没有分配规则ID")).body(null);
        }
        try {
            caseStrategyRepository.delete(ruleId);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "failure", "删除分配策略规则失败")).body(null);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, " successfully", "删除成功")).body(null);
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
    public List<CaseInfoDistributed> runCaseRun(CaseStrategy caseStrategy, boolean flag) throws IOException, TemplateException {
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
        ResponseEntity<List<CaseInfoDistributed>> resp = restTemplate.exchange(Constants.BUSINESS_SERVICE_URL.concat("getAllCaseInfo"),
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
