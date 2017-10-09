package cn.fintecher.pangolin.business.service;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.io.IOException;

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
//    public KieSession runCaseRun(List<?> checkedList, CaseStrategy caseStrategy) {
//        try {
//            freemarker.template.Template template = freemarkerConfiguration.getTemplate("caseInfo.ftl", "UTF-8");
//            Map<String, String> map = new HashMap<>();
//            map.put("id", caseStrategy.getId());
//            map.put("strategyText", caseStrategy.getStrategyText());
//            String rule = FreeMarkerTemplateUtils.processTemplateIntoString(template, caseStrategy);
//            logger.debug("案件策略公式为：【" + rule + "】");
//            KieServices kieServices = KieServices.Factory.get();
//            KieFileSystem kfs = kieServices.newKieFileSystem();
//            kfs.write("src/main/resources/simple.drl",
//                    kieServices.getResources().newReaderResource(new StringReader(rule)));
//            KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();
//
//            Results results = kieBuilder.getResults();
//            if (results.hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
//                System.out.println(results.getMessages());
//                throw new IllegalStateException("### errors ###");
//            }
//            KieContainer kieContainer =
//                    kieServices.newKieContainer(kieBuilder.getKieModule().getReleaseId());
//            KieSession kieSession = kieContainer.newKieSession();
//            kieSession.setGlobal("checkedList", checkedList);
//            return kieSession;
//        } catch (Exception e) {
//           logger.error(e.getMessage(), e);
//           throw new RuntimeException("策略解析失败!");
//        }
//    }
}
