package cn.fintecher.pangolin.dataimp.web.rest;

import cn.fintecher.pangolin.dataimp.entity.ScoreRule;
import cn.fintecher.pangolin.dataimp.model.ScoreRules;
import cn.fintecher.pangolin.dataimp.repository.ScoreRuleRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @GetMapping("/getScoreRules")
    @ApiOperation(value = "获取案件评分规则", notes = "获取案件评分规则")
   public ResponseEntity<ScoreRules> getScoreRules(){
        List<ScoreRule> allList = scoreRuleRepository.findAll();
        ScoreRules result=new ScoreRules();
        result.setScoreRules(allList);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
