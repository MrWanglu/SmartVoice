package cn.fintecher.pangolin.dataimp.web;

import cn.fintecher.pangolin.dataimp.entity.ScoreFormula;
import cn.fintecher.pangolin.dataimp.entity.ScoreRule;
import cn.fintecher.pangolin.dataimp.model.JsonObj;
import cn.fintecher.pangolin.dataimp.model.ScoreRuleModel;
import cn.fintecher.pangolin.dataimp.model.ScoreRules;
import cn.fintecher.pangolin.dataimp.repository.ScoreRuleRepository;
import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.Personal;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.IdcardUtils;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by luqiang on 2017/8/10.
 */
@RestController
@RequestMapping("/api/scoreStrategyController")
@Api(value = "案件评分", description = "案件评分")
public class ScoreStrategyController {
    @Autowired
    private ScoreRuleRepository scoreRuleRepository;
    @Autowired
    private Configuration freemarkerConfiguration;
    @Autowired
    private RestTemplate restTemplate;
    private final Logger logger = LoggerFactory.getLogger(ScoreStrategyController.class);

    @GetMapping("/query")
    @ApiOperation(value = "查询所有规则属性", notes = "查询所有规则属性")
    public ResponseEntity query(@QuerydslPredicate(root = ScoreRule.class) Predicate predicate, @ApiIgnore Pageable pageable){
        try {
            BooleanBuilder builder = new BooleanBuilder(predicate);
            Page<ScoreRule> page = scoreRuleRepository.findAll(builder, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/scoreStrategyController/query");
            return new ResponseEntity<>(page, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("scoreStrategy", "", e.getMessage())).body(null);
        }
    }
    @PostMapping("/saveScoreStrategy")
    @ApiOperation(value = "新增评分记录", notes = "新增评分记录")
    public ResponseEntity saveScoreStrategy(@RequestBody JsonObj jsonStr){
        try {
            scoreRuleRepository.deleteAll();//保存之前删除已有数据
            List<ScoreRule> sorceRules = new ArrayList<>();//属性集合
            String str = jsonStr.getJsonStr();//取json字符串
            JSONArray jsonArray = JSONArray.parseArray(str);//解析
            for (int i = 0; i < jsonArray.size(); i++) {
                ScoreRule scoreRule = new ScoreRule();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                scoreRule.setName(jsonObject.getString("name"));
                scoreRule.setWeight(jsonObject.getDouble("weight"));
                JSONArray formulas = jsonObject.getJSONArray("formulas");
                List<ScoreFormula> formulaList = new ArrayList<>();
                for (int j = 0; j < formulas.size(); j++) {
                    JSONObject obj = formulas.getJSONObject(j);
                    ScoreFormula scoreFormula = new ScoreFormula();
                    scoreFormula.setName(obj.getString("name"));
                    scoreFormula.setStrategyJson(obj.getString("strategyJson"));
                    StringBuilder sb = new StringBuilder("");
                    scoreFormula.setStrategy(analysisRule(scoreFormula.getStrategyJson(), sb, scoreRule.getName()));
                    scoreFormula.setScore(obj.getBigDecimal("score"));
                    formulaList.add(scoreFormula);
                }
                scoreRule.setFormulas(formulaList);
                sorceRules.add(scoreRule);
            }
            sorceRules = scoreRuleRepository.save(sorceRules);
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert("scoreStregy", "operate successfully", "新增评分记录成功")).body(sorceRules);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("scoreStregy","no message","新增失败")).body(null);
        }
    }
    private String analysisRule(String strategyJson, StringBuilder sb, String variable) {
        if (StringUtils.isNotBlank(strategyJson)) {
            JSONArray jsonArray = JSONArray.parseArray(strategyJson);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                sb.append(variable);
                sb.append(jsonObject.getString("operator"));
                sb.append(jsonObject.getDouble("value"));
                sb.append("&&");
            }
            return sb.toString().substring(0, sb.toString().length() - 2);
        }
        return null;
    }
    @GetMapping("/updateAllScoreStrategyManual")
    @ApiOperation(value = "更新案件评分(手动)", notes = "更新案件评分(手动)")
    public ResponseEntity updateAllScoreStrategyManual() throws IOException {

        try {
            StopWatch watch1 = new StopWatch();
            watch1.start();
            KieSession kieSession= null;
            try {
                kieSession = createSorceRule();
            } catch (TemplateException e) {
                e.printStackTrace();
            }
            ParameterizedTypeReference<Iterable<CaseInfo>> responseType = new ParameterizedTypeReference<Iterable<CaseInfo>>() {
                               };
                ResponseEntity<Iterable<CaseInfo>> resp = restTemplate.exchange(Constants.BUSINESS_SERVICE_URL.concat("getCaseInfoList"),
                        HttpMethod.GET, null, responseType);
            Iterable<CaseInfo> caseInfos = resp.getBody();
            List<CaseInfo> accCaseInfoList = new ArrayList<>();
            List<CaseInfo> caseInfoList = new ArrayList<>();
            caseInfos.forEach(single ->caseInfoList.add(single));
            if (caseInfoList.size() > 0) {
                for(CaseInfo caseInfo : caseInfoList){
                    ScoreRuleModel scoreRuleModel = new ScoreRuleModel();
                    int age = IdcardUtils.getAgeByIdCard(caseInfo.getPersonalInfo().getIdCard());
                    scoreRuleModel.setAge(age);
                    scoreRuleModel.setOverDueAmount(caseInfo.getOverdueAmount().doubleValue());
                    scoreRuleModel.setOverDueDays(caseInfo.getOverdueDays());
                    scoreRuleModel.setProId(caseInfo.getArea().getId());//省份id
                    ResponseEntity<Personal> personalResponseEntity = restTemplate.getForEntity(Constants.PERSONAL_SERVICE_URL.concat("getPerson").concat("?id=").concat(caseInfo.getPersonalInfo().getId()),Personal.class);
                  Personal personal = personalResponseEntity.getBody();
                    if (Objects.nonNull(personal) && Objects.nonNull(personal.getPersonalJobs())) {
                        scoreRuleModel.setIsWork(1);
                    } else {
                        scoreRuleModel.setIsWork(0);
                    }
                    kieSession.insert(scoreRuleModel);//插入
                    kieSession.fireAllRules();//执行规则
                    caseInfo.setScore(new BigDecimal(scoreRuleModel.getCupoScore()));
                    accCaseInfoList.add(caseInfo);
                }
                kieSession.dispose();
                restTemplate.postForEntity(Constants.BUSINESS_SERVICE_URL.concat("saveCaseScore"), accCaseInfoList, CaseInfo.class);
                watch1.stop();
                //log.info("耗时："+watch1.getTotalTimeMillis());
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("评分完成", "success")).body(null);
            }
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseinfo","fai","案件为空")).body(null);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("caseInfo", "fialue", e.getMessage())).body(null);
        }
    }
    /**
     * 动态生成规则
     * @return
     * @throws IOException
     * @throws
     */
    private  KieSession createSorceRule() throws IOException, TemplateException {
        Template scoreFormulaTemplate = freemarkerConfiguration.getTemplate("scoreFormula.ftl", "UTF-8");
        Template scoreRuleTemplate = freemarkerConfiguration.getTemplate("scoreRule.ftl", "UTF-8");
        List<ScoreRule> rules=null;
        ScoreRules scoreRules = getAllScoreRule();
        if(Objects.isNull(scoreRules)){
           return null;
        }
        rules=scoreRules.getScoreRules();
        StringBuilder sb = new StringBuilder();
        if(Objects.nonNull(rules)){
            for (ScoreRule rule : rules) {
                for (int i = 0; i < rule.getFormulas().size(); i++) {
                    ScoreFormula scoreFormula = rule.getFormulas().get(i);
                    Map<String, String> map = new HashMap<>();
                    map.put("id", rule.getId());
                    map.put("index", String.valueOf(i));
                    map.put("strategy", scoreFormula.getStrategy());
                    map.put("score", String.valueOf(scoreFormula.getScore()));
                    map.put("weight", String.valueOf(rule.getWeight()));
                    sb.append(FreeMarkerTemplateUtils.processTemplateIntoString(scoreFormulaTemplate, map));
                }
            }
        }
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kfs = kieServices.newKieFileSystem();
        Map<String, String> map = new HashMap<>();
        map.put("allRules", sb.toString());
        String text = FreeMarkerTemplateUtils.processTemplateIntoString(scoreRuleTemplate, map);
        kfs.write("src/main/resources/simple.drl",
                kieServices.getResources().newReaderResource(new StringReader(text)));
        KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();
        Results results = kieBuilder.getResults();
        if (results.hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
            System.out.println(results.getMessages());
            throw new IllegalStateException("### errors ###");
        }
        KieContainer kieContainer =
                kieServices.newKieContainer(kieBuilder.getKieModule().getReleaseId());
        KieSession kieSession = kieContainer.newKieSession();
        return  kieSession;
    }
    public ScoreRules getAllScoreRule(){
        List<ScoreRule> allList = scoreRuleRepository.findAll();
        ScoreRules result=new ScoreRules();
        result.setScoreRules(allList);
        return  result;
    }
}
