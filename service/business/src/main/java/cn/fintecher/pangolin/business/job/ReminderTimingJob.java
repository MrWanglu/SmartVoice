package cn.fintecher.pangolin.business.job;


import cn.fintecher.pangolin.business.config.ConfigureQuartz;
import cn.fintecher.pangolin.business.repository.CompanyRepository;
import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.business.service.CaseInfoService;
import cn.fintecher.pangolin.business.service.ReminderService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.message.SendReminderMessage;
import cn.fintecher.pangolin.entity.util.Constants;
import org.apache.commons.lang.StringUtils;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service("reminderTimingJob")
@DisallowConcurrentExecution
public class ReminderTimingJob implements Job{

    private final Logger logger = LoggerFactory.getLogger(ReminderTimingJob.class);

    @Autowired
    private SchedulerFactoryBean schedFactory;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private SysParamRepository sysParamRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CaseInfoService caseInfoService;

    @Autowired
    private ReminderService ReminderService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //案件即将强制流转提醒
        List<CaseInfo> caseInfoList = caseInfoService.getAllForceTurnCase();
        if(Objects.nonNull(caseInfoList)){
            for(CaseInfo caseInfo : caseInfoList){
                SendReminderMessage sendReminderMessage = new SendReminderMessage();
                sendReminderMessage.setUserId(caseInfo.getCurrentCollector().getId());
                sendReminderMessage.setType(ReminderType.FORCE_TURN);
                sendReminderMessage.setTitle("案件强制流转提醒");
                sendReminderMessage.setContent("您持有的案件 ["+caseInfo.getCaseNumber()+"] 即将强制流转,请及时处理");
                sendReminderMessage.setMode(ReminderMode.POPUP);
                ReminderService.sendReminder(sendReminderMessage);
            }
        }
        restTemplate.execute("http://reminder-service/api/reminderTiming/sendReminderTiming", HttpMethod.GET,null,null);
    }

    @Bean(name = "createReminderTimingJob")
    public List<CronTriggerFactoryBean> CreateReminderTimingJob() {
        List<CronTriggerFactoryBean> cronTriggerFactoryBeanList = new ArrayList<>();
        try {
            //获取公司码
            List<Company> companyList = companyRepository.findAll();
            for (Company company : companyList) {
                QSysParam qSysParam = QSysParam.sysParam;
                SysParam sysParam = sysParamRepository.findOne(qSysParam.companyCode.eq(company.getCode())
                        .and(qSysParam.code.eq(Constants.SYSPARAM_REMINDER))
                        .and(qSysParam.status.eq(SysParam.StatusEnum.Start.getValue())));
                if (Objects.nonNull(sysParam)) {
                    String cronString = sysParam.getValue();
                    if (StringUtils.isNotBlank(cronString) && StringUtils.length(cronString) == 6) {
                        String hours = cronString.substring(0, 2);
                        String mis = cronString.substring(2, 4);
                        String second = cronString.substring(4, 6);
                        cronString = second.concat(" ").concat(mis).concat(" ").concat(hours).concat(" * * ?");
                        JobDetail jobDetail = ConfigureQuartz.createJobDetail(ReminderTimingJob.class, Constants.REMINDER_JOB_GROUP,
                                Constants.REMINDER_JOB_NAME.concat("_").concat(company.getCode()), Constants.REMINDER_JOB_DESC.concat("_").concat(company.getCode()));
                        JobDataMap jobDataMap = new JobDataMap();
                        jobDataMap.put("companyCode", company.getCode());
                        jobDataMap.put("sysParamCode", Constants.SYSPARAM_REMINDER_STATUS);
                        CronTriggerFactoryBean cronTriggerFactoryBean =ConfigureQuartz.createCronTrigger(Constants.REMINDER_TRIGGER_GROUP.concat("_").concat(company.getCode()),
                                Constants.REMINDER_TRIGGER_NAME.concat("_").concat(company.getCode()),
                                "reminderTimingJobBean".concat("_").concat(company.getCode()),
                                Constants.REMINDER_TRIGGER_DESC.concat("_").concat(company.getCode()),
                                jobDetail,cronString,jobDataMap);
                        cronTriggerFactoryBean.afterPropertiesSet();
                        schedFactory.getScheduler().deleteJob(jobDetail.getKey());
                        schedFactory.getScheduler().scheduleJob(jobDetail,cronTriggerFactoryBean.getObject());
                        cronTriggerFactoryBeanList.add(cronTriggerFactoryBean);
                    }
                }
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cronTriggerFactoryBeanList;
    }

}
