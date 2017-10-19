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
                    if (Objects.isNull(caseStrategy.getDepartments()) || caseStrategy.getDepartments().isEmpty() || (caseStrategy.getDepartments().size() == 1 && caseStrategy.getDepartments().get(0) == null)) {
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请选择要指定的机构")).body(null);
                    }
                    cs.setDepartments(caseStrategy.getDepartments());
                } else if (Objects.equals(assignType, CaseStrategy.AssignType.COLLECTOR.getValue())) {//催收员
                    cs.setAssignType(CaseStrategy.AssignType.COLLECTOR.getValue());
                    if (Objects.isNull(caseStrategy.getUsers()) || caseStrategy.getUsers().isEmpty() || (caseStrategy.getUsers().size() == 1 && caseStrategy.getUsers().get(0) == null)) {
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
                    if (Objects.isNull(caseStrategy.getOutsource()) || caseStrategy.getOutsource().isEmpty() || (caseStrategy.getOutsource().size() == 1 && caseStrategy.getOutsource().get(0) == null)) {
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请选择要指定的委外方")).body(null);
                    }
                    cs.setOutsource(caseStrategy.getOutsource());
                } else {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "请选择策略指定的对象")).body(null);
                }
            } else {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "未匹配到要新增的策略类型")).body(null);
            }
            if (Objects.nonNull(caseStrategy.getId()) && StringUtils.isNotBlank(caseStrategy.getId())) { // 修改
                cs.setId(caseStrategy.getId());
                // 检查名称重复
                Query query = new Query();
                query.addCriteria(Criteria.where("id").ne(cs.getId()));
                query.addCriteria(Criteria.where("name").is(cs.getName()));
                query.addCriteria(Criteria.where("companyCode").is(user.getCompanyCode()));
                List<CaseStrategy> caseStrategies = mongoTemplate.find(query, CaseStrategy.class);
                if (!caseStrategies.isEmpty()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "策略名称已存在!")).body(null);
                }
            } else { // 新增
                // 检查重复名字
                Query query = new Query();
                query.addCriteria(Criteria.where("name").is(cs.getName()));
                query.addCriteria(Criteria.where("companyCode").is(user.getCompanyCode()));
                List<CaseStrategy> caseStrategies = mongoTemplate.find(query, CaseStrategy.class);
                if (!caseStrategies.isEmpty()) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "策略名称已存在!")).body(null);
                }
            }
            caseStrategyRepository.save(cs);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("新增成功", "")).body(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "新增失败!")).body(null);
        }
    }

    @GetMapping("/findCaseStrategy")
    @ApiOperation(value = "检查策略名称是否重复", notes = "检查策略名称是否重复")
    public ResponseEntity findCaseStrategy(@RequestParam @ApiParam("操作者的Token") String name,
                                           @RequestParam(required = false) @ApiParam("策略ID") String id,
                                           @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        try {
            ResponseEntity<User> userResult = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
            User user = userResult.getBody();
            Query query = new Query();
            query.addCriteria(Criteria.where("companyCode").is(user.getCompanyCode()));
            query.addCriteria(Criteria.where("name").is(name));
            if (StringUtils.isNotBlank(id)) {
                query.addCriteria(Criteria.where("id").ne(id));
            }
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
