package cn.fintecher.pangolin.report.scheduled;

import cn.fintecher.pangolin.report.entity.DailyProcessReport;
import cn.fintecher.pangolin.report.mapper.DailyProcessReportMapper;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author : xiaqun
 * @Description : 催收员每日催收过程调度
 * @Date : 19:37 2017/8/3
 */

@Component
@EnableScheduling
@Transactional
@Lazy(value = false)
public class DailyProcessScheduled {
    private final Logger log = LoggerFactory.getLogger(DailyProcessScheduled.class);

    @Inject
    DailyProcessReportMapper dailyProcessReportMapper;

    @Scheduled(cron = "0 30 23 * * ?")
    public void saveDailyProcessReport() {
        log.debug("定时调度 生成催收员每日催收过程报表{}", ZWDateUtil.getNowDateTime());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 0);
        Date date = cal.getTime();
        List<DailyProcessReport> dailyProcessReports = dailyProcessReportMapper.saveHistoryReport(date);
        for (DailyProcessReport dailyProcessReport : dailyProcessReports) {
            dailyProcessReport.setNowDate(date);
            dailyProcessReport.setOperatorDate(ZWDateUtil.getNowDateTime());
            dailyProcessReportMapper.insert(dailyProcessReport);
        }
    }
}