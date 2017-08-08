package cn.fintecher.pangolin.report.scheduled;

import cn.fintecher.pangolin.report.entity.BackMoneyReport;
import cn.fintecher.pangolin.report.mapper.BackMoneyReportMapper;
import cn.fintecher.pangolin.report.model.DeptModel;
import cn.fintecher.pangolin.report.model.NoBackMoneyModel;
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
import java.util.Objects;

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
        DeptModel dept;
        for (BackMoneyReport backMoneyReport : backMoneyReports) {
            dept = backMoneyReportMapper.getDept(backMoneyReport.getUserName()); //获取本部门信息
            backMoneyReport.setDeptCode(dept.getCode()); //本部门code码
            backMoneyReport.setDeptName(dept.getName()); //本部门名称
            if (Objects.equals(dept.getFlag(), 1)) { //存在父部门
                dept = backMoneyReportMapper.getParentDept(backMoneyReport.getUserName()); //获得父部门信息
                backMoneyReport.setParentDeptCode(dept.getCode()); //父部门code码
                backMoneyReport.setParentDeptName(dept.getName()); //父部门名称
            }
            backMoneyReport.setBackDate(date); //回款时间
            backMoneyReport.setOperatorDate(ZWDateUtil.getNowDateTime()); //操作时间
            backMoneyReportMapper.insert(backMoneyReport);
        }

        //处理有案件却没有回款的用户
        List<NoBackMoneyModel> noBackMoneyModels = backMoneyReportMapper.getUserNames(null, null, null, null);
        if (!noBackMoneyModels.isEmpty()) { //存在有案件却没有回款的用户
            BackMoneyReport backMoneyReport;
            for (NoBackMoneyModel noBackMoneyModel : noBackMoneyModels) {
                backMoneyReport = new BackMoneyReport();
                backMoneyReport.setUserName(noBackMoneyModel.getUserName()); //用户名
                backMoneyReport.setRealName(noBackMoneyModel.getRealName()); //用户姓名
                backMoneyReport.setOperatorDate(ZWDateUtil.getNowDateTime()); //操作时间
                dept = backMoneyReportMapper.getDept(noBackMoneyModel.getUserName()); //获取本部门信息
                backMoneyReport.setDeptCode(dept.getCode()); //本部门code码
                backMoneyReport.setDeptName(dept.getName()); //本部门名称
                if (Objects.equals(dept.getFlag(), 1)) { //存在父部门
                    dept = backMoneyReportMapper.getParentDept(noBackMoneyModel.getUserName()); //获得父部门信息
                    backMoneyReport.setParentDeptCode(dept.getCode()); //父部门code码
                    backMoneyReport.setParentDeptName(dept.getName()); //父部门名称
                }
                backMoneyReportMapper.insert(backMoneyReport);
            }
        }
    }
}