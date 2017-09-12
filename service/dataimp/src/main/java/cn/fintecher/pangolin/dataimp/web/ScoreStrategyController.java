package cn.fintecher.pangolin.dataimp.web;

import cn.fintecher.pangolin.dataimp.entity.ScoreFormula;
import cn.fintecher.pangolin.dataimp.entity.ScoreRule;
import cn.fintecher.pangolin.dataimp.model.JsonObj;
import cn.fintecher.pangolin.dataimp.repository.ScoreRuleRepository;
import cn.fintecher.pangolin.entity.QTemplate;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.EntityUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import freemarker.template.Configuration;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;
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
    private Configuration freemarkerConfiguration;
    @Autowired
    private RestTemplate restTemplate;
    private final Logger logger = LoggerFactory.getLogger(ScoreStrategyController.class);

    @GetMapping("/query")
    @ApiOperation(value = "查询所有规则属性", notes = "查询所有规则属性")
    public ResponseEntity query(@RequestParam(required = false) @ApiParam(value = "公司code码") String companyCode,
                                @QuerydslPredicate(root = ScoreRule.class) Predicate predicate,
                                @RequestHeader(value = "X-UserToken") String token,@ApiIgnore Pageable pageable){
        try {
            ResponseEntity<User> userResponseEntity=null;
            try {
                userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
            }catch (Exception e){
                logger.error(e.getMessage(),e);
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(e.getMessage(), "user","请登录")).body(null);
            }
            User user=userResponseEntity.getBody();
            BooleanBuilder builder = new BooleanBuilder(predicate);
            if(Objects.isNull(user.getCompanyCode())){
                if(Objects.isNull(companyCode)){
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("scoreStrategy", "scoreStrategy", "请选择公司")).body(null);
                }
                builder.and(QTemplate.template.companyCode.eq(companyCode));
            }else{
                builder.and(QTemplate.template.companyCode.eq(user.getCompanyCode()));
            }
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
    public ResponseEntity saveScoreStrategy(@RequestBody JsonObj jsonStr, @RequestHeader(value = "X-UserToken") String token){
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
            scoreRuleRepository.deleteAll();//保存之前删除已有数据
            List<ScoreRule> sorceRules = new ArrayList<>();//属性集合
            String str = jsonStr.getJsonStr();//取json字符串
            JSONArray jsonArray = JSONArray.parseArray(str);//解析
            for (int i = 0; i < jsonArray.size(); i++) {
                ScoreRule scoreRule = new ScoreRule();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                scoreRule.setName(jsonObject.getString("name"));
                scoreRule.setWeight(jsonObject.getDouble("weight"));
                if(Objects.isNull(user.getCompanyCode())){//如果是超级管理员，code码为空
                    scoreRule.setCompanyCode(null);
                }else{
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
    @DeleteMapping("/deleteScoreStrategy")
    @ApiOperation(value = "删除评分记录", notes = "删除评分记录")
    public ResponseEntity deleteScoreStrategy(@RequestParam String id){
        scoreRuleRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert("scoreStregy", "operate successfully", "删除评分记录成功")).body(null);
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
