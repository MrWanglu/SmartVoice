package cn.fintecher.pangolin.dataimp.web.rest;

import cn.fintecher.pangolin.entity.strategy.CaseStrategy;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 案件策略资源
 * Created by sunyanping on 2017/9/30.
 */
@ApiIgnore
@RestController
@RequestMapping("/api/caseStrategyResource")
@Api(value = "CaseStrategyResource", description = "案件策略资源")
public class CaseStrategyResource {

    private final Logger logger = LoggerFactory.getLogger(CaseStrategyResource.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/getCaseStrategy")
    @ApiOperation(notes = "获取案件分配策略", value = "获取案件分配策略")
    public ResponseEntity<List<CaseStrategy>> getCaseStrategy(@RequestParam(value = "companyCode") @ApiParam("公司Code") String companyCode,
                                                        @RequestParam(value = "strategyType") @ApiParam("策略类型") Integer strategyType) {
        logger.debug("Rest request to getCaseStrategy");
        Query query = new Query();
        query.addCriteria(Criteria.where("companyCode").is(companyCode));
        query.addCriteria(Criteria.where("strategyType").is(strategyType));
        List<CaseStrategy> caseStrategies = mongoTemplate.find(query, CaseStrategy.class);
        Collections.sort(caseStrategies, Comparator.comparingInt(CaseStrategy::getPriority));
        return ResponseEntity.ok().body(caseStrategies);
    }
}
