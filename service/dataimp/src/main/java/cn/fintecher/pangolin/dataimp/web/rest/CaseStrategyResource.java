package cn.fintecher.pangolin.dataimp.web.rest;

import cn.fintecher.pangolin.dataimp.entity.CaseStrategy;
import cn.fintecher.pangolin.dataimp.entity.QCaseStrategy;
import cn.fintecher.pangolin.dataimp.repository.CaseStrategyRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Iterator;

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
    private CaseStrategyRepository caseStrategyRepository;

    @GetMapping("/getCaseStrategy")
    public ResponseEntity<CaseStrategy> getCaseStrategy(@RequestParam(value = "companyCode") @ApiParam("公司Code") String companyCode,
                                                        @RequestParam(value = "strategyTye") @ApiParam("策略类型") Integer strategyType) {
        logger.debug("Rest request to getCaseStrategy");
        QCaseStrategy qCaseStrategy = QCaseStrategy.caseStrategy;
        Iterable<CaseStrategy> all = caseStrategyRepository.findAll(qCaseStrategy.companyCode.eq(companyCode)
                .and(qCaseStrategy.strategyType.eq(strategyType)), new Sort(Sort.Direction.ASC,"priority"));
        Iterator<CaseStrategy> iterator = all.iterator();
        while (iterator.hasNext()) {
            CaseStrategy next = iterator.next();
            return ResponseEntity.ok().body(next);
        }
        return null;
    }

}
