package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.ScoreFormula;
import cn.fintecher.pangolin.business.model.ScoreRule;
import cn.fintecher.pangolin.business.model.ScoreRules;
import cn.fintecher.pangolin.entity.strategy.CaseStrategy;
import cn.fintecher.pangolin.entity.util.Constants;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by sunyanping on 2017/9/30.
 */
@Service
public class RunCaseStrategyService {

    Logger logger = LoggerFactory.getLogger(RunCaseStrategyService.class);

    @Inject
    private Configuration freemarkerConfiguration;

    @Inject
    private RestTemplate restTemplate;

    /**
     * @param caseStrategy
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public KieSession runCaseRule(List<?> checkedList, CaseStrategy caseStrategy, String ruleName) {
        try {
            freemarker.template.Template template = freemarkerConfiguration.getTemplate(ruleName, "UTF-8");
            Map<String, String> map = new HashMap<>();
            map.put("id", caseStrategy.getId());
            map.put("strategyText", caseStrategy.getStrategyText());
            String rule = FreeMarkerTemplateUtils.processTemplateIntoString(template, caseStrategy);
            logger.debug("案件策略公式为：【" + rule + "】");
            //通过工厂获取KieServices,KieServices是一个线程安全的单身人士，担任由Kie提供的其他服务的枢纽
            KieServices kieServices = KieServices.Factory.get();
            // 创建一个新的KieFileSystem，用于以编程方式定义构成KieModule的资源
            KieFileSystem kfs = kieServices.newKieFileSystem();
            //在指定的路径中将给定的资源添加到此KieFileSystem
            kfs.write("src/main/resources/simple.drl",
                    kieServices.getResources().newReaderResource(new StringReader(rule)));
            //KieBuilder是KieModule中包含的资源的构建器
            KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();
            //返回建筑过程的结果
            Results results = kieBuilder.getResults();
            if (results.hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
                System.out.println(results.getMessages());
                throw new IllegalStateException("### errors ###");
            }
            KieContainer kieContainer =
                    kieServices.newKieContainer(kieBuilder.getKieModule().getReleaseId());
            KieSession kieSession = kieContainer.newKieSession();
            //在插入事实或启动流程之前设置全局变量
            kieSession.setGlobal("checkedList", checkedList);
            return kieSession;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("策略解析失败!");
        }
    }

    public String analysisRule(String jsonObject, StringBuilder stringBuilder) {
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
                    stringBuilder.append(jsonObj.get("relation"));
                    stringBuilder.append("(");
                    analysisRule(jsonObj.getJSONArray("children").toString(), stringBuilder);
                    stringBuilder.append(")");
                }
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            throw new RuntimeException("策略解析失败");
        }
    }


    public KieSession createSorceRule(String comanyCode, Integer strategyType) {
        List<ScoreRule> rules = null;
        try {
            ResponseEntity<ScoreRules> responseEntity = restTemplate.getForEntity(Constants.SCOREL_SERVICE_URL.concat("getScoreRules").concat("?comanyCode=").concat(comanyCode).concat("&strategyType=").concat(strategyType.toString()), ScoreRules.class);
            ScoreRules scoreRules = responseEntity.getBody();
            rules = scoreRules.getScoreRules();

        } catch (Exception e) {
            throw new IllegalStateException("获取策略失败");
        }
        if (Objects.equals(rules.size(), 0)) {
            throw new RuntimeException("请先设置评分策略");
        }
        try {
            freemarker.template.Template scoreFormulaTemplate = freemarkerConfiguration.getTemplate("scoreFormula.ftl", "UTF-8");
            freemarker.template.Template scoreRuleTemplate = freemarkerConfiguration.getTemplate("scoreRule.ftl", "UTF-8");
            StringBuilder sb = new StringBuilder();
            if (Objects.nonNull(rules)) {
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
                logger.error(results.getMessages().toString());
                throw new IllegalStateException("策略生成错误");
            }
            KieContainer kieContainer =
                    kieServices.newKieContainer(kieBuilder.getKieModule().getReleaseId());
            KieSession kieSession = kieContainer.newKieSession();
            return kieSession;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("策略生成失败");
        }
    }
}
