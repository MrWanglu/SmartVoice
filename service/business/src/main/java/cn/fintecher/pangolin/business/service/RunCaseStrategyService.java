package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.strategy.CaseStrategy;
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
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 策略分配中的平均分配计算
     *
     * @param caseInfos  案件数
     * @param dataList   用户数
     * @param disNumList 分配结果数
     */
    public void setDistributeNum(List<?> caseInfos, List<String> dataList, List<Integer> disNumList) {
        int i = caseInfos.size();
        int j = dataList.size();
        if (i % j == 0) {
            int g = i / j;
            for (int m = 1; m <= j; m++) {
                disNumList.add(g);
            }
        } else {
            int a = i / j;
            int b = i % j;
            for (int m = 0; m < j; m++) {
                if (m > b - 1) {
                    disNumList.add(a);
                } else {
                    disNumList.add(a + 1);
                }
            }
        }
    }
}
