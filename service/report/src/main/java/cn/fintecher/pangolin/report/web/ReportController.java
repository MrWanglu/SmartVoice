package cn.fintecher.pangolin.report.web;

import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.report.model.*;
import cn.fintecher.pangolin.report.service.ReportService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author : xiaqun
 * @Description : 报表服务接口
 * @Date : 13:35 2017/8/2
 */

@RestController
@RequestMapping("/api/ReportController")
@Api(value = "ReportController", description = "报表服务接口")
public class ReportController extends BaseController {
    private final Logger log = LoggerFactory.getLogger(ReportController.class);

    private static final String ENTITY_BACKMONEY_REPORT = "BackMoneyReport";

    private static final String ENTITY_PERFORMANCE_REPORT = "PerformanceReport";

    private static final String ENTITY_DAILY_PROCESS_REPORT = "DailyProcessReport";

    private static final String ENTITY_DAILY_RESULT_REPORT = "DailyResultReport";

    private static final String ENTITY_PERFORMANCE_RANKING_REPORT = "PerformanceRankingReport";

    @Inject
    ReportService reportService;

    /**
     * @Description 催收员回款报表
     */
    @GetMapping("/getBackMoneyReport")
    @ApiOperation(value = "催收员回款报表", notes = "催收员回款报表")
    public ResponseEntity<List<BackMoneyModel>> getBackMoneyReport(GeneralParams generalParams,
                                                                   @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get back money report");
        try {
            User tokenUser = getUserByToken(token);
            List<BackMoneyModel> backMoneyModels = reportService.getBackMoneyReport(generalParams, tokenUser);
            if (Objects.equals(backMoneyModels, null)) {
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("报表为空", ENTITY_BACKMONEY_REPORT)).body(new ArrayList<>());
            } else {
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", ENTITY_BACKMONEY_REPORT)).body(backMoneyModels);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_BACKMONEY_REPORT, "backMoneyReport", "查询失败")).body(null);
        }
    }

    /**
     * @Description 催收员业绩进展报表
     */
    @GetMapping("/getPerformanceReport")
    @ApiOperation(value = "催收员业绩进展报表", notes = "催收员业绩进展报表")
    public ResponseEntity<List<PerformanceModel>> getPerformanceReport(PerformanceParams performanceParams,
                                                                       @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get performance report");
        try {
            User tokenUser = getUserByToken(token);
            List<PerformanceModel> performanceModels = reportService.getPerformanceReport(performanceParams, tokenUser);
            if (Objects.equals(performanceModels, null)) {
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("报表为空", ENTITY_PERFORMANCE_REPORT)).body(new ArrayList<>());
            } else {
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", ENTITY_PERFORMANCE_REPORT)).body(performanceModels);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_PERFORMANCE_REPORT, "performanceReport", "查询失败")).body(null);
        }
    }

    /**
     * @Description 催收员每日催收过程报表
     */
    @GetMapping("/getDailyProcessReport")
    @ApiOperation(value = "催收员每日催收过程报表", notes = "催收员每日催收过程报表")
    public ResponseEntity<List<DailyProcessModel>> getDailyProcessReport(GeneralParams generalParams,
                                                                         @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get daily process report");
        try {
            User tokenUser = getUserByToken(token);
            List<DailyProcessModel> dailyProcessModels = reportService.getDailyProcessReport(generalParams, tokenUser);
            if (Objects.isNull(dailyProcessModels)) {
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("报表为空", ENTITY_DAILY_PROCESS_REPORT)).body(new ArrayList<>());
            } else {
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", ENTITY_DAILY_PROCESS_REPORT)).body(dailyProcessModels);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_DAILY_PROCESS_REPORT, "dailyProcessReport", "查询失败")).body(null);
        }
    }

    /**
     * @Description 催收员每日催收结果报表
     */
    @GetMapping("/getDailyResultReport")
    @ApiOperation(value = "催收员每日催收结果报表", notes = "催收员每日催收结果报表")
    public ResponseEntity<List<DailyResultModel>> getDailyResultReport(GeneralParams generalParams,
                                                                       @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get daily result report");
        try {
            User tokenUser = getUserByToken(token);
            List<DailyResultModel> dailyResultModels = reportService.getDailyResultReport(generalParams, tokenUser);
            if (Objects.isNull(dailyResultModels)) {
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("报表为空", ENTITY_DAILY_RESULT_REPORT)).body(new ArrayList<>());
            } else {
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", ENTITY_DAILY_RESULT_REPORT)).body(dailyResultModels);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_DAILY_RESULT_REPORT, "dailyResultReport", "查询失败")).body(null);
        }
    }

    /**
     * @Description 催收员业绩排名报表
     */
    @GetMapping("/getPerformanceRankingReport")
    @ApiOperation(value = "催收员业绩排名报表", notes = "催收员业绩排名报表")
    public ResponseEntity<List<CollectorPerformanceModel>> getPerformanceRankingReport(PerformanceRankingParams performanceRankingParams,
                                                                                       @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get performance ranking report");
        try {
            User tokenUser = getUserByToken(token);
            List<CollectorPerformanceModel> collectorPerformanceModels = reportService.getPerformanceRankingReport(performanceRankingParams, tokenUser);
            if (Objects.isNull(collectorPerformanceModels)) {
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("报表为空", ENTITY_PERFORMANCE_RANKING_REPORT)).body(new ArrayList<>());
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", ENTITY_PERFORMANCE_RANKING_REPORT)).body(collectorPerformanceModels);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_PERFORMANCE_RANKING_REPORT, "performanceRankingReport", "查询失败")).body(null);
        }
    }

    /**
     * @Description 催收员业绩排名小组汇总报表
     */
    @GetMapping("/getSummaryReport")
    @ApiOperation(value = "催收员业绩报名小组汇总报表", notes = "催收员业绩报名小组汇总报表")
    public ResponseEntity<List<PerformanceSummaryModel>> getSummaryReport(PerformanceRankingParams performanceRankingParams,
                                                                          @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get summary report");
        try {
            User tokenUser = getUserByToken(token);
            List<PerformanceSummaryModel> performanceSummaryModels = reportService.getSummaryReport(performanceRankingParams, tokenUser);
            if (Objects.isNull(performanceSummaryModels)) {
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("报表为空", ENTITY_PERFORMANCE_RANKING_REPORT)).body(new ArrayList<>());
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", ENTITY_PERFORMANCE_RANKING_REPORT)).body(performanceSummaryModels);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_PERFORMANCE_RANKING_REPORT, "performanceRankingReport", "查询失败")).body(null);
        }
    }

    /**
     * @Description 催收员业绩报名汇总报表
     */
    @GetMapping("/getGroupLeaderReport")
    @ApiOperation(value = "催收员业绩报名汇总报表", notes = "催收员业绩报名汇总报表")
    public ResponseEntity<List<GroupLeaderModel>> getGroupLeaderReport(PerformanceRankingParams performanceRankingParams,
                                                                       @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get group leader report");
        try {
            User tokenUser = getUserByToken(token);
            List<GroupLeaderModel> groupLeaderModels = reportService.getGroupLeaderReport(performanceRankingParams, tokenUser);
            if (Objects.isNull(groupLeaderModels)) {
                return ResponseEntity.ok().headers(HeaderUtil.createAlert("报表为空", ENTITY_PERFORMANCE_RANKING_REPORT)).body(new ArrayList<>());
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", ENTITY_PERFORMANCE_RANKING_REPORT)).body(groupLeaderModels);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_PERFORMANCE_RANKING_REPORT, "performanceRankingReport", "查询失败")).body(null);
        }
    }

    /**
     * @Description 导出催收员回款报表
     */
    @GetMapping("/exportBackMoneyReport")
    @ApiOperation(value = "导出催收员回款报表", notes = "导出催收员回款报表")
    public ResponseEntity<String> exportBackMoneyReport(GeneralParams generalParams,
                                                        @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to export back money report");
        try {
            User tokenUser = getUserByToken(token);
            String url = reportService.exportBackMoneyReport(generalParams, tokenUser);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("导出成功", ENTITY_BACKMONEY_REPORT)).body(url);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_BACKMONEY_REPORT, "backMoneyReport", "导出失败")).body(null);
        }
    }

    /**
     * @Description 导出催收员业绩进展报表
     */
    @GetMapping("/exportPerformanceReport")
    @ApiOperation(value = "导出催收员业绩进展报表", notes = "导出催收员业绩进展报表")
    public ResponseEntity<String> exportPerformanceReport(PerformanceParams performanceParams,
                                                          @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to export performance report");
        try {
            User tokenUser = getUserByToken(token);
            String url = reportService.exportPerformanceReport(performanceParams, tokenUser);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("导出成功", ENTITY_PERFORMANCE_REPORT)).body(url);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_PERFORMANCE_REPORT, "performanceReport", "导出失败")).body(null);
        }
    }

    /**
     * @Description 导出催收员每日催收过程报表
     */
    @GetMapping("/exportDailyProcessReport")
    @ApiOperation(value = "导出催收员每日催收过程报表", notes = "导出催收员每日催收过程报表")
    public ResponseEntity<String> exportDailyProcessReport(GeneralParams generalParams,
                                                           @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to export daily process report");
        try {
            User tokenUser = getUserByToken(token);
            String url = reportService.exportDailyProcessReport(generalParams, tokenUser);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("导出成功", ENTITY_DAILY_PROCESS_REPORT)).body(url);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_DAILY_PROCESS_REPORT, "dailyProcessReport", "导出失败")).body(null);
        }
    }

    /**
     * @Description 导出催收员每日催收结果报表
     */
    @GetMapping("/exportDailyResultReport")
    @ApiOperation(value = "导出催收员每日催收结果报表", notes = "导出催收员每日催收结果报表")
    public ResponseEntity<String> exportDailyResultReport(GeneralParams generalParams,
                                                          @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to export daily result report");
        try {
            User tokenUser = getUserByToken(token);
            String url = reportService.exportDailyResultReport(generalParams, tokenUser);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("导出成功", ENTITY_DAILY_RESULT_REPORT)).body(url);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_DAILY_RESULT_REPORT, "dailyResultReport", "导出失败")).body(null);
        }
    }

    /**
     * @Description 导出催收员业绩排名报表
     */
    @GetMapping("/exportPerformanceRankingReport")
    @ApiOperation(value = "导出催收员业绩排名报表", notes = "导出催收员业绩排名报表")
    public ResponseEntity<String> exportPerformanceRankingReport(PerformanceRankingParams performanceRankingParams,
                                                                 @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to export performance ranking report");
        try {
            User tokenUser = getUserByToken(token);
            String url = reportService.exportPerformanceRankingReport(performanceRankingParams, tokenUser);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("导出成功", ENTITY_PERFORMANCE_RANKING_REPORT)).body(url);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_PERFORMANCE_RANKING_REPORT, "performanceRankingReport", "导出失败")).body(null);
        }
    }

    /**
     * @Description 导出催收员业绩排名小组汇总报表
     */
    @GetMapping("/exportSummaryReport")
    @ApiOperation(value = "导出催收员业绩排名小组汇总报表", notes = "导出催收员业绩排名小组汇总报表")
    public ResponseEntity<String> exportSummaryReport(PerformanceRankingParams performanceRankingParams,
                                                      @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to export summary report");
        try {
            User tokenUser = getUserByToken(token);
            String url = reportService.exportSummaryReport(performanceRankingParams, tokenUser);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("导出成功", ENTITY_PERFORMANCE_RANKING_REPORT)).body(url);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_PERFORMANCE_RANKING_REPORT, "performanceRankingReport", "导出失败")).body(null);
        }
    }
}