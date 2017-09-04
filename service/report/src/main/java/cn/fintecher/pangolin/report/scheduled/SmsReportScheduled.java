package cn.fintecher.pangolin.report.scheduled;

import cn.fintecher.pangolin.report.entity.SmsReport;
import cn.fintecher.pangolin.report.mapper.SmsReportMapper;
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
 * @Description : 短信发送统计调度
 * @Date : 10:19 2017/9/4
 */

@Component
@EnableScheduling
@Transactional
@Lazy(value = false)
public class SmsReportScheduled {
    private final Logger log = LoggerFactory.getLogger(SmsReportScheduled.class);

    @Inject
    SmsReportMapper smsReportMapper;

    @Scheduled(cron = "0 30 23 * * ?")
    void saveSmsReport() {
        log.debug("定时调度 生成短信发送统计报表{}", ZWDateUtil.getNowDateTime());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 0);
        Date date = cal.getTime();
        List<SmsReport> smsReports = smsReportMapper.saveHistoryReport(date);
        for (SmsReport smsReport : smsReports) {
            smsReport.setOperatorDate(ZWDateUtil.getNowDateTime());
            smsReportMapper.insert(smsReport);
        }
    }
}