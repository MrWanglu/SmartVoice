package cn.fintecher.pangolin.business.job;


import cn.fintecher.pangolin.business.config.ConfigureQuartz;
import cn.fintecher.pangolin.business.repository.CompanyRepository;
import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.business.service.CaseAssistService;
import cn.fintecher.pangolin.business.service.CaseInfoService;
import cn.fintecher.pangolin.business.service.ReminderService;
import cn.fintecher.pangolin.business.service.UserService;
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
import org.springframework.web.client.RestClientException;
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

    @Autowired
    private CaseAssistService caseAssistService;

    @Autowired
    private UserService userService;
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        QSysParam qSysParam = QSysParam.sysParam;
        SysParam sysParam = sysParamRepository.findOne(qSysParam.code.eq(Constants.SYSPARAM_REMINDER_STATUS));
        try {
            if (Objects.equals("0", sysParam.getValue())) {
                sysParam.setValue("1");
                sysParamRepository.save(sysParam);
                logger.info("案件提醒批量发送中.......");
                JobDataMap jobDataMap = jobExecutionContext.getTrigger().getJobDataMap();
                String companyCode = jobDataMap.get("companyCode").toString();
                //案件即将强制流转提醒
                List<CaseInfo> forceTurnCaseList = caseInfoService.getForceTurnCase(companyCode);
                if (Objects.nonNull(forceTurnCaseList)) {
                    for (CaseInfo caseInfo : forceTurnCaseList) {
                        SendReminderMessage sendReminderMessage = new SendReminderMessage();
                        sendReminderMessage.setUserId(Objects.nonNull(caseInfo.getCurrentCollector()) ? caseInfo.getCurrentCollector().getId() : null);
                        sendReminderMessage.setType(ReminderType.FORCE_TURN);
                        sendReminderMessage.setTitle("案件强制流转提醒");
                        sendReminderMessage.setContent("您持有的案件 [" + caseInfo.getCaseNumber() + "] 即将强制流转,请及时留案");
                        sendReminderMessage.setMode(ReminderMode.POPUP);
                        ReminderService.sendReminder(sendReminderMessage);
                    }
                }

                //协催案件即将强制流转提醒
                List<CaseAssist> forceTurnAssistCaseList = caseAssistService.getForceTurnAssistCase(companyCode);
                if (Objects.nonNull(forceTurnAssistCaseList)) {
                    for (CaseAssist caseAssist : forceTurnAssistCaseList) {
                        SendReminderMessage sendReminderMessage = new SendReminderMessage();
                        sendReminderMessage.setUserId(Objects.nonNull(caseAssist.getAssistCollector()) ? caseAssist.getAssistCollector().getId() : null);
                        sendReminderMessage.setType(ReminderType.FORCE_TURN);
                        sendReminderMessage.setTitle("协催案件强制流转提醒");
                        sendReminderMessage.setContent("您参与协催的案件 [" + caseAssist.getCaseId().getCaseNumber() + "] 即将强制流转,请及时留案");
                        sendReminderMessage.setMode(ReminderMode.POPUP);
                        ReminderService.sendReminder(sendReminderMessage);
                    }
                }

                //持案天数逾期无进展提醒
                List<CaseInfo> nowhereCaseList = caseInfoService.getNowhereCase(companyCode);
                if (Objects.nonNull(nowhereCaseList)) {
                    for (CaseInfo caseInfo : nowhereCaseList) {
                        List<User> managers = userService.getManagerByUser(caseInfo.getCurrentCollector().getId());
                        SendReminderMessage sendReminderMessage = new SendReminderMessage();
                        sendReminderMessage.setUserId(managers.get(0).getId());
                        sendReminderMessage.setType(ReminderType.FLLOWUP);
                        sendReminderMessage.setTitle("案件跟进提醒");
                        sendReminderMessage.setContent("案件 [" + caseInfo.getCaseNumber() + "] 长时间无跟进记录,请及时处理");
                        sendReminderMessage.setMode(ReminderMode.POPUP);
                        if (managers.size() > 1) {
                            List<String> managerIds = new ArrayList<>();
                            for (int i = 1; i < managers.size(); i++) {
                                managerIds.add(managers.get(i).getId());
                            }
                            sendReminderMessage.setCcUserIds(managerIds.toArray(new String[managerIds.size()]));
                        }
                        ReminderService.sendReminder(sendReminderMessage);
                    }
                }

                //单次协催案件若干天无进展提醒
                List<CaseAssist> nowhereCaseAssist = caseInfoService.getNowhereCaseAssist(companyCode);
                if (Objects.nonNull(nowhereCaseAssist)) {
                    for (CaseAssist caseAssist : nowhereCaseAssist) {
                        SendReminderMessage sendReminderMessage = new SendReminderMessage();
                        sendReminderMessage.setUserId(caseAssist.getOperator().getId());
                        sendReminderMessage.setType(ReminderType.FLLOWUP);
                        sendReminderMessage.setTitle("协催案件跟进提醒");
                        sendReminderMessage.setContent("协催案件 [" + caseAssist.getCaseId().getCaseNumber() + "] 长时间无跟进记录,请及时处理");
                        sendReminderMessage.setMode(ReminderMode.POPUP);
                        ReminderService.sendReminder(sendReminderMessage);
                    }
                }

                restTemplate.execute("http://reminder-service/api/reminderTiming/sendReminderTiming", HttpMethod.GET, null, null);
            }
        } catch (RestClientException e) {
            logger.error(e.getMessage(),e);
        }finally {
            sysParam.setValue("0");
            sysParamRepository.save(sysParam);
        }
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
            logger.error(e.getMessage(),e);
        } catch (ParseException e) {
            logger.error(e.getMessage(),e);
        }
        return cronTriggerFactoryBeanList;
    }
}
