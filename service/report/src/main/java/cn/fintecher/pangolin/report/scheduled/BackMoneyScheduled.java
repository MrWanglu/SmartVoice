package cn.fintecher.pangolin.report.scheduled;

import cn.fintecher.pangolin.report.entity.BackMoneyReport;
import cn.fintecher.pangolin.report.mapper.BackMoneyReportMapper;
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
 * @Description : 回款金额调度
 * @Date : 10:41 2017/8/2
 */

@Component
@EnableScheduling
@Transactional
@Lazy(value = false)
public class BackMoneyScheduled {
    private final Logger log = LoggerFactory.getLogger(BackMoneyScheduled.class);

    @Inject
    BackMoneyReportMapper backMoneyReportMapper;

    @Scheduled(cron = "0 0 0 * * ?")
    public void saveBackMoneyReport() {
        log.debug("定时调度 生成回款报表{}", ZWDateUtil.getNowDateTime());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date date = cal.getTime();
        List<BackMoneyReport> backMoneyReports = backMoneyReportMapper.saveHistoryReport(date);
        for (BackMoneyReport backMoneyReport : backMoneyReports) {
            backMoneyReport.setOperatorDate(ZWDateUtil.getNowDateTime()); //操作时间
            backMoneyReportMapper.insert(backMoneyReport);
        }
    }
}