package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.config.ConfigureQuartz;
import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.entity.QSysParam;
import cn.fintecher.pangolin.entity.SysParam;
import cn.fintecher.pangolin.entity.util.Constants;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @Author: PeiShouWen
 * @Description: 调度服务类
 * @Date 15:29 2017/8/10
 */
@Service("jobTaskService")
public class JobTaskService {

    Logger logger= LoggerFactory.getLogger(JobTaskService.class);

    @Autowired
    SchedulerFactoryBean schedFactory;

    @Autowired
    SysParamRepository sysParamRepository;

    /**
     * 更新CRON任务调度时间
      *
     */
    public void updateJobTask(String cron,String companyCode,String sysParamCode, String triggerName,String triggerGroup,String triggerDesc,
                              String jobName,String jobGroup,String jobDesc,Class objClass,String beanName) throws Exception {
        StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();
        JobDetail jobDetail=scheduler.getJobDetail(JobKey.jobKey(jobName,jobGroup));
        if(Objects.nonNull(jobDetail)){
            scheduler.deleteJob(jobDetail.getKey());
        }else{
            jobDetail = ConfigureQuartz.createJobDetail(objClass, jobGroup,jobName, jobDesc);
        }
        JobDataMap jobDataMap=new JobDataMap();
        jobDataMap.put("companyCode",companyCode);
        jobDataMap.put("sysParamCode",sysParamCode);
        CronTriggerFactoryBean cronTriggerFactoryBean = ConfigureQuartz.createCronTrigger(triggerGroup,triggerName, beanName, triggerDesc, jobDetail, cron,jobDataMap);
        cronTriggerFactoryBean.afterPropertiesSet();
        //加入调度器
        schedFactory.getScheduler().scheduleJob(jobDetail, cronTriggerFactoryBean.getObject());
    }

    /**
     * 检查调度是否正在执行
     * @param companyCode
     * @param sysParamCode
     * @return
     */
    public boolean checkJobIsRunning(String companyCode,String sysParamCode){
        QSysParam qSysParam=QSysParam.sysParam;
        SysParam sysParam=sysParamRepository.findOne(qSysParam.companyCode.eq(companyCode)
                                                        .and(qSysParam.code.eq(sysParamCode)));
        if(Objects.nonNull(sysParam)){
            if(sysParam.getValue().equals(Constants.BatchStatus.RUNING.getValue())){
                return true;
            }
        }
        return false;
    }
}


