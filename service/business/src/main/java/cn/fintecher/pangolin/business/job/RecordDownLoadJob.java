package cn.fintecher.pangolin.business.job;

import cn.fintecher.pangolin.business.config.ConfigureQuartz;
import cn.fintecher.pangolin.business.repository.CompanyRepository;
import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.entity.Company;
import cn.fintecher.pangolin.entity.QSysParam;
import cn.fintecher.pangolin.entity.SysParam;
import cn.fintecher.pangolin.entity.util.Constants;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author: PeiShouWen
 * @Description: 录音下载批量
 * @Date 13:55 2017/8/11
 */
@Service("recordDownLoadJob")
@DisallowConcurrentExecution
public class RecordDownLoadJob implements Job{

    Logger logger= LoggerFactory.getLogger(RecordDownLoadJob.class);

    @Autowired
    SchedulerFactoryBean schedFactory;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    SysParamRepository sysParamRepository;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("录音下载批量.......");
    }

    @Bean
    public List<CronTriggerFactoryBean> CreateCronTrigger() {
        List<CronTriggerFactoryBean> cronTriggerFactoryBeanList = new ArrayList<>();
        try{
            //获取公司码
            List<Company> companyList= companyRepository.findAll();
            for (Company company : companyList) {
                QSysParam qSysParam = QSysParam.sysParam;
                SysParam sysParam = sysParamRepository.findOne(qSysParam.companyCode.eq(company.getCode())
                        .and(qSysParam.code.eq(Constants.SYSPARAM_RECORD))
                        .and(qSysParam.status.eq(SysParam.StatusEnum.Start.getValue())));
                if (Objects.nonNull(sysParam)) {
                        String cron=sysParam.getValue();
                        cron="0 ".concat("0").concat("/").concat(cron).concat(" * * * ?");
                        JobDetail jobDetail = ConfigureQuartz.createJobDetail(this.getClass(), Constants.RECORD_JOB_GROUP,
                                Constants.RECORD_JOB_NAME.concat("_").concat(company.getCode()),
                                Constants.RECORD_JOB_DESC.concat("_").concat(company.getChinaName()));
                        JobDataMap jobDataMap=new JobDataMap();
                        jobDataMap.put("companyCode",company.getCode());
                        jobDataMap.put("sysParamCode",Constants.SYSPARAM_RECORD);
                        CronTriggerFactoryBean cronTriggerFactoryBean = ConfigureQuartz.createCronTrigger(Constants.RECORD_TRIGGER_GROUP,
                                Constants.RECORD_TRIGGER_NAME.concat("_").concat(company.getCode()),
                                "RecordDownLoadJobBean".concat("_").concat(company.getCode()),
                                Constants.RECORD_TRIGGER_DESC, jobDetail, cron,jobDataMap);
                        cronTriggerFactoryBean.afterPropertiesSet();
                        schedFactory.getScheduler().deleteJob(jobDetail.getKey());
                        //加入调度器
                        schedFactory.getScheduler().scheduleJob(jobDetail, cronTriggerFactoryBean.getObject());
                        cronTriggerFactoryBeanList.add(cronTriggerFactoryBean);
                }
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return cronTriggerFactoryBeanList;
    }
}
