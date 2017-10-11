package cn.fintecher.pangolin.dataimp.web.rest;

import cn.fintecher.pangolin.dataimp.model.ScoreRules;
import cn.fintecher.pangolin.dataimp.repository.ScoreRuleRepository;
import cn.fintecher.pangolin.entity.strategy.ScoreRule;
import cn.fintecher.pangolin.util.ZWStringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zzl029 on 2017/8/25.
 */
@RestController
@RequestMapping("/api/scoreResource")
@Api(description = "获取案件评分规则")
public class ScoreResource {

    @Autowired
    private ScoreRuleRepository scoreRuleRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/getScoreRules")
    @ApiOperation(value = "获取案件评分规则", notes = "获取案件评分规则")
    public ResponseEntity<ScoreRules> getScoreRules(@RequestParam String comanyCode,
                                                    @RequestParam Integer strategyType) {

        Query query = new Query();
        if (ZWStringUtils.isNotEmpty(comanyCode)) {
            query.addCriteria(Criteria.where("companyCode").is(comanyCode));
        }
        query.addCriteria(Criteria.where("strategyType").is(strategyType));
        List<ScoreRule> scoreRules = mongoTemplate.find(query, ScoreRule.class);
        List<ScoreRule> caseInfoList1 = new ArrayList<>();
        scoreRules.forEach(single -> caseInfoList1.add(single));
        ScoreRules result = new ScoreRules();
        result.setScoreRules(caseInfoList1);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
