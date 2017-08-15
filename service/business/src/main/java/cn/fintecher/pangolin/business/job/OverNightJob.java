package cn.fintecher.pangolin.business.job;

import cn.fintecher.pangolin.business.config.ConfigureQuartz;
import cn.fintecher.pangolin.business.repository.CompanyRepository;
import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.business.service.JobTaskService;
import cn.fintecher.pangolin.business.service.OverNightBatchService;
import cn.fintecher.pangolin.entity.Company;
import cn.fintecher.pangolin.entity.QSysParam;
import cn.fintecher.pangolin.entity.SysParam;
import cn.fintecher.pangolin.entity.util.Constants;
import org.apache.commons.lang.StringUtils;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author: PeiShouWen
 * @Description: pangolin 夜间批量
 * @Date 11:00 2017/8/9
 */
@Service("overNightJob")
@DisallowConcurrentExecution
public class OverNightJob implements Job {

    Logger logger= LoggerFactory.getLogger(OverNightJob.class);
    @Autowired
     SchedulerFactoryBean schedFactory;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    SysParamRepository sysParamRepository;
    @Autowired
    JobTaskService jobTaskService;

    @Autowired
    OverNightBatchService overNightBatchService;


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getTrigger().getJobDataMap();
        StopWatch watch = new StopWatch();
        watch.start();
        try{
            logger.info("开始晚间批量_{} ",jobDataMap.get("sysParamCode"));
            if(jobTaskService.checkJobIsRunning(jobDataMap.getString("companyCode"),jobDataMap.getString("sysParamCode"))){
                logger.info("晚间批量正在执行_{}",jobDataMap.get("sysParamCode"));
            }else{
                //批量状态修改为正在执行
                jobTaskService.updateSysparam(jobDataMap.getString("companyCode"),jobDataMap.getString("sysParamCode"),Constants.BatchStatus.RUNING.getValue());
                //批量步骤
                SysParam sysParam=jobTaskService.getSysparam(jobDataMap.getString("companyCode"),Constants.SYSPARAM_OVERNIGHT_STEP);
                String step=sysParam.getValue();
                switch (step){
                    case "" :
                        step="0";
                        break;
                    case "0":
                        step="1";
                        overNightBatchService.doOverNightOne(jobDataMap,step);
                        break;
                    default:
                        break;
                }
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }finally {
            //批量状态修改为未执行
            jobTaskService.updateSysparam(jobDataMap.getString("companyCode"),jobDataMap.getString("sysParamCode"),Constants.BatchStatus.STOP.getValue());
            watch.stop();
            logger.info("结束晚间批量 {} ,耗时: {} 毫秒",jobDataMap.get("sysParamCode"),watch.getTotalTimeMillis());
        }
    }

    @Bean
    public  List<CronTriggerFactoryBean> CreateCronTrigger() {
        List<CronTriggerFactoryBean> cronTriggerFactoryBeanList = new ArrayList<>();
        try{
            //获取公司码
            List<Company> companyList= companyRepository.findAll();
            for (Company company : companyList) {
                QSysParam qSysParam = QSysParam.sysParam;
                SysParam sysParam = sysParamRepository.findOne(qSysParam.companyCode.eq(company.getCode())
                        .and(qSysParam.code.eq(Constants.SYSPARAM_OVERNIGHT))
                        .and(qSysParam.status.eq(SysParam.StatusEnum.Start.getValue())));
                if (Objects.nonNull(sysParam)) {
                    String cronStr=sysParam.getValue();
                    //时间长度必须为6位
                    if(StringUtils.isNotBlank(cronStr) && StringUtils.length(cronStr)==6){
                        String hours=cronStr.substring(0,2);
                        String mis=cronStr.substring(2,4);
                        String second=cronStr.substring(4,6);
                        cronStr=second.concat(" ").concat(mis).concat(" ").concat(hours).concat(" * * ?");
                        JobDetail jobDetail = ConfigureQuartz.createJobDetail(this.getClass(), Constants.OVERNIGHT_JOB_GROUP,
                                Constants.OVERNIGHT_JOB_NAME.concat("_").concat(company.getCode()),
                                Constants.OVERNIGHT_JOB_DESC.concat("_").concat(company.getCode()));
                        JobDataMap jobDataMap=new JobDataMap();
                        jobDataMap.put("companyCode",company.getCode());
                        jobDataMap.put("sysParamCode",Constants.SYSPARAM_OVERNIGHT_STATUS);
                        CronTriggerFactoryBean cronTriggerFactoryBean = ConfigureQuartz.createCronTrigger(Constants.OVERNIGHT_TRIGGER_GROUP,
                                Constants.OVERNIGHT_TRIGGER_NAME.concat("_").concat(company.getCode()),
                                "overNightJobBean".concat("_").concat(company.getCode()),
                                Constants.OVERNIGHT_TRIGGER_DESC.concat("_").concat(company.getCode()), jobDetail, cronStr,jobDataMap);
                        cronTriggerFactoryBean.afterPropertiesSet();
                        schedFactory.getScheduler().deleteJob(jobDetail.getKey());
                        //加入调度器
                        schedFactory.getScheduler().scheduleJob(jobDetail, cronTriggerFactoryBean.getObject());
                        cronTriggerFactoryBeanList.add(cronTriggerFactoryBean);
                    }
                }
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return cronTriggerFactoryBeanList;
    }



}