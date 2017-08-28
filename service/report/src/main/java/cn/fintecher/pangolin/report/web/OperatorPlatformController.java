package cn.fintecher.pangolin.report.web;

import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.report.mapper.CupoPageMapper;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Author : sunyanping
 * @Description : 操作台相关
 * @Date : 2017/8/7.
 */
@RestController
@RequestMapping("/api/operatorPlatformController")
@Api(value = "OperatorPlatformController", description = "操作台相关")
public class OperatorPlatformController extends BaseController{

    private final Logger log = LoggerFactory.getLogger(HomePageController.class);

    @Inject
    private CupoPageMapper cupoPageMapper;

    @GetMapping(value = "/getOperatorPlatformMessage")
    @ApiOperation(value = "查询工作台信息", notes = "任务一览表与数据统计的数据")
    public ResponseEntity<Map<String, String>> getOperatorPlatformMessage(@RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get OperatorPlatform message : {}", token);
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("OperatorPlatformController", "getOperatorPlatformMessage", e.getMessage())).body(null);
        }
        try {
            //当前催收总案件数
            Integer caseInfoCount = cupoPageMapper.getCaseInfoCount(user.getId());
            //今日流入案件数
            Integer caseInputCount = cupoPageMapper.getFlowInCaseToday(user.getId());
            //今日结清案件数
            Integer finishCaseToday = cupoPageMapper.getFinishCaseToday(user.getId());
            //今日流出案件数
            Integer flowOutCaseToday = cupoPageMapper.getFlowOutCaseToday(user.getId());
            //催收员案件总数
            Integer caseInfoAllCount = cupoPageMapper.getCaseInfoAllCount(user.getId());
            //催收员回款总额
            BigDecimal moneySumResult1 = cupoPageMapper.getMoneySumResult(user.getUserName());
            BigDecimal moneySumResult = Objects.isNull(moneySumResult1) ? new BigDecimal("0.00") : moneySumResult1;
            //催收员本月累计回款
            BigDecimal monthMoneyResult1 = cupoPageMapper.getMonthMoneyResult(user.getUserName());
            BigDecimal monthMoneyResult = Objects.isNull(monthMoneyResult1) ? new BigDecimal("0.00") : monthMoneyResult1;
            //催收员今日累计回款
            BigDecimal dayMoneyResult1 = cupoPageMapper.getDayMoneyResult(user.getUserName());
            BigDecimal dayMoneyResult = Objects.isNull(dayMoneyResult1) ? new BigDecimal("0.00") : dayMoneyResult1;
            //催收员本月累计催收次数
            Integer monthFollowCount = cupoPageMapper.getMonthFollowCount(user.getId());
            //催收员今日累计催收次数
            Integer dayFollowCount = cupoPageMapper.getDayFollowCount(user.getId());
            //金额余两位小数处理
            moneySumResult = moneySumResult.setScale(2, BigDecimal.ROUND_HALF_UP);
            monthMoneyResult = monthMoneyResult.setScale(2, BigDecimal.ROUND_HALF_UP);
            dayMoneyResult = dayMoneyResult.setScale(2, BigDecimal.ROUND_HALF_UP);

            //开始包装结果
            Map<String, String> result = new HashMap<>();
            result.put("currentCaseSum", caseInfoCount.toString()); //当前催收总案件数
            result.put("flowInCaseToday", caseInputCount.toString()); //今日流入案件数
            result.put("finishCaseToday", finishCaseToday.toString()); //今日结清案件数
            result.put("flowOutCaseToday", flowOutCaseToday.toString()); //今日流出案件数

            result.put("caseSum", caseInfoAllCount.toString()); //催收员案件总数
            result.put("moneySumResult", moneySumResult.toString()); //催收员回款总额

            result.put("monthMoneyResult", monthMoneyResult.toString()); //催收员本月累计回款
            result.put("dayMoneyResult", dayMoneyResult.toString()); //催收员今日累计回款
            result.put("monthFollowCount", monthFollowCount.toString()); //催收员本月累计催收次数
            result.put("dayFollowCount", dayFollowCount.toString()); //催收员今日累计催收次数

            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("OperatorPlatformController", "getOperatorPlatformMessage", "系统异常!")).body(null);
        }
    }
}
