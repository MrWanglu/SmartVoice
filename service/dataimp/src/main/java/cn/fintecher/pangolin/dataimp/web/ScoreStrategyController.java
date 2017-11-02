package cn.fintecher.pangolin.dataimp.web;

import cn.fintecher.pangolin.dataimp.model.JsonObj;
import cn.fintecher.pangolin.dataimp.repository.ScoreRuleRepository;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.strategy.ScoreFormula;
import cn.fintecher.pangolin.entity.strategy.ScoreRule;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.EntityUtil;
import cn.fintecher.pangolin.util.ZWStringUtils;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private RestTemplate restTemplate;
    @Autowired
    MongoTemplate mongoTemplate;

    private final Logger logger = LoggerFactory.getLogger(ScoreStrategyController.class);

    @GetMapping("/query")
    @ApiOperation(value = "查询所有规则属性", notes = "查询所有规则属性")
    public ResponseEntity query(@RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
                                @RequestParam @ApiParam(value = "策略类型") Integer strategyType,
                                @RequestHeader(value = "X-UserToken") String token, @ApiIgnore Pageable pageable) {
        try {
            ResponseEntity<User> userResponseEntity = null;
            try {
                userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user", "请登录")).body(null);
            }
            User user = userResponseEntity.getBody();
            Query query = new Query();
            if (Objects.isNull(user.getCompanyCode())) {
                if (Objects.nonNull(companyCode)) {
                    query.addCriteria(Criteria.where("companyCode").is(companyCode));
                }
            } else {
                query.addCriteria(Criteria.where("companyCode").is(user.getCompanyCode()));
            }
            //根据不同的策略类型去查询
            if(Objects.nonNull(strategyType)){
                query.addCriteria(Criteria.where("strategyType").is(strategyType));
            }

            int total = (int) mongoTemplate.count(query, ScoreRule.class);
            query.with(pageable);
            List<ScoreRule> list = mongoTemplate.find(query, ScoreRule.class);
            return ResponseEntity.ok().body(new PageImpl<>(list, pageable, total));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("scoreStrategy", "", e.getMessage())).body(null);
        }
    }

    @PostMapping("/saveScoreStrategy")
    @ApiOperation(value = "新增评分记录", notes = "新增评分记录")
    public ResponseEntity saveScoreStrategy(@RequestBody JsonObj jsonStr, @RequestHeader(value = "X-UserToken") String token) {
        try {
            ResponseEntity<User> userResult = null;
            try {
                userResult = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "system error", "系统异常")).body(null);
            }
            if (!userResult.hasBody()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "no login", "用户没有登录")).body(null);
            }
            User user = userResult.getBody();
            jsonStr = (JsonObj) EntityUtil.emptyValueToNull(jsonStr);
            Query query = new Query();
            if(Objects.nonNull(jsonStr.getStrategyType())){
                query.addCriteria(Criteria.where("strategyType").is(jsonStr.getStrategyType()));
            }
            query.addCriteria(Criteria.where("companyCode").is(user.getCompanyCode()));
            //查找需要保存所对应的策略类型对象集合
            List<ScoreRule> scoreRules = mongoTemplate.find(query, ScoreRule.class);
            ///保存之前删除所对应的策略类型对象集合
            scoreRuleRepository.delete(scoreRules);
            List<ScoreRule> sorceRules = new ArrayList<>();//属性集合
            String str = jsonStr.getJsonStr();//取json字符串
            JSONArray jsonArray = JSONArray.parseArray(str);//解析
            for (int i = 0; i < jsonArray.size(); i++) {
                ScoreRule scoreRule = new ScoreRule();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                scoreRule.setName(jsonObject.getString("name"));
                scoreRule.setWeight(jsonObject.getDouble("weight"));
                scoreRule.setStrategyType(jsonStr.getStrategyType()); //策略类型
                if (Objects.isNull(user.getCompanyCode())) {//如果是超级管理员，code码为空
                    scoreRule.setCompanyCode(null);
                } else {
                    scoreRule.setCompanyCode(user.getCompanyCode());
                }
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
                    if(Objects.isNull(scoreFormula.getScore())){
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("scoreStregy", "no message", "请设置分值")).body(null);
                    }
                    if(scoreFormula.getScore().equals(BigDecimal.ZERO)){
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("scoreStregy", "no message", "分值不能为0")).body(null);
                    }
                    formulaList.add(scoreFormula);
                }
                scoreRule.setFormulas(formulaList);
                sorceRules.add(scoreRule);
            }
            sorceRules = scoreRuleRepository.save(sorceRules);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("新增评分记录成功", "")).body(sorceRules);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("scoreStregy", "no message", "新增失败")).body(null);
        }
    }

    @DeleteMapping("/deleteScoreStrategy")
    @ApiOperation(value = "删除评分记录", notes = "删除评分记录")
    public ResponseEntity deleteScoreStrategy(@RequestParam String id) {
        //传ID的时候表示确实要删除，没有传值得时候表示只是前台界面的删除。
        if (ZWStringUtils.isNotEmpty(id)) {
            try {
                scoreRuleRepository.delete(id);
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error(ex.getMessage(), ex);
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("scoreStregy", "operate failed", "刪除的信息不存在")).body(null);
            }
        }
        return ResponseEntity.ok().body(null);
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
}
