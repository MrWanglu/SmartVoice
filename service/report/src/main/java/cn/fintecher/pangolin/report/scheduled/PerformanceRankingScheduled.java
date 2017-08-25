package cn.fintecher.pangolin.report.scheduled;

import cn.fintecher.pangolin.report.entity.PerformanceRankingReport;
import cn.fintecher.pangolin.report.mapper.PerformanceRankingReportMapper;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author : xiaqun
 * @Description : 催收员业绩排名报表调度
 * @Date : 15:08 2017/8/21
 */

@Component
@EnableScheduling
@Transactional
@Lazy(value = false)
public class PerformanceRankingScheduled {
    private final Logger log = LoggerFactory.getLogger(PerformanceRankingScheduled.class);

    @Inject
    PerformanceRankingReportMapper performanceRankingReportMapper;

    @Scheduled(cron = "0 0 0 * * ?")
    void savePerformanceRanking() {
        log.debug("定时调度 生成催收员业绩排名报表{}", ZWDateUtil.getNowDateTime());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 0);
        cal.set(Calendar.DATE, 1);
        Date startDate = cal.getTime();
        cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date endDate = cal.getTime();
        List<PerformanceRankingReport> performanceRankingReports = new ArrayList<>();
        List<String> companyCodeList = performanceRankingReportMapper.getCompanyCode();
        for (String companyCode : companyCodeList) {
            List<PerformanceRankingReport> performanceRankingReportList = performanceRankingReportMapper.saveHistoryReport(startDate, endDate, endDate, companyCode);
            performanceRankingReports.addAll(performanceRankingReportList);
        }
        for (PerformanceRankingReport performanceRankingReport : performanceRankingReports) {
            performanceRankingReport.setNowDate(endDate);
            performanceRankingReport.setOperatorDate(ZWDateUtil.getNowDateTime()); //操作时间
            performanceRankingReportMapper.insert(performanceRankingReport);
        }
    }
}