package cn.fintecher.pangolin.report.scheduled;

import cn.fintecher.pangolin.report.entity.DailyResultReport;
import cn.fintecher.pangolin.report.mapper.DailyResultReportMapper;
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
 * @Description : 催收员每日催收结果报表调度
 * @Date : 13:32 2017/8/4
 */

@Component
@EnableScheduling
@Transactional
@Lazy(value = false)
public class DailyResultScheduled {
    private final Logger log = LoggerFactory.getLogger(DailyResultScheduled.class);

    @Inject
    DailyResultReportMapper dailyResultReportMapper;

    @Scheduled(cron = "0 0 0 * * ?")
    public void saveDailyProcessReport() {
        log.debug("定时调度 生成催收员每日催收结果报表{}", ZWDateUtil.getNowDateTime());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date date = cal.getTime();
        List<DailyResultReport> dailyResultReports = dailyResultReportMapper.saveHistoryReport(date);
        for (DailyResultReport dailyResultReport : dailyResultReports) {
            dailyResultReport.setNowDate(date);
            dailyResultReport.setOperatorDate(ZWDateUtil.getNowDateTime());
            dailyResultReportMapper.insert(dailyResultReport);
        }
    }
}