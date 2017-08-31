package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.message.SendReminderMessage;
import cn.fintecher.pangolin.entity.util.Constants;
import org.quartz.JobDataMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author duchao
 * @Description: 消息批量
 * @Date 15:46 2017/8/11
 */
@Service("remindTimingBatchService")
public class RemindTimingBatchService {
    Logger logger = LoggerFactory.getLogger(RemindTimingBatchService.class);

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

    @Autowired
    private SysParamRepository sysParamRepository;

    @Autowired
    private JobTaskService jobTaskService;

    public void doRemindTask(JobDataMap jobDataMap) {
        StopWatch watch = new StopWatch();
        String companyCode = jobDataMap.getString("companyCode");
        String sysParamCode = jobDataMap.getString("sysParamCode");
        try {
            if (jobTaskService.checkJobIsRunning(companyCode, sysParamCode)) {
                logger.info("案件提醒批量正在执行_{}", jobDataMap.get("companyCode"));
            } else {
                watch.start();
                jobTaskService.updateSysparam(companyCode, sysParamCode, Constants.BatchStatus.RUNING.getValue());
                logger.info("案件提醒批量发送中.......");
                getForceTurnCase(companyCode);
                getForceTurnAssistCase(companyCode);
                getNowhereCase(companyCode);
                getNowhereCaseAssist(companyCode);
                restTemplate.execute("http://reminder-service/api/reminderTiming/sendReminderTiming", HttpMethod.GET, null, null);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                jobTaskService.updateSysparam(companyCode, sysParamCode, Constants.BatchStatus.STOP.getValue());
            } catch (Exception e) {
                logger.error("案件提醒批量处理结束 {},状态修改失败 {}", companyCode, sysParamCode, e);
            }
            watch.stop();
            logger.info("案件提醒批量处理结束 {} , 耗时 {} 毫秒", companyCode, watch.getTotalTimeMillis());
        }
    }

    /**
     * 案件强制流转提醒
     *
     * @param companyCode
     */
    protected void getForceTurnCase(String companyCode) {
        try {
            List<CaseInfo> forceTurnCaseList = caseInfoService.getForceTurnCase(companyCode);
            if (Objects.nonNull(forceTurnCaseList)) {
                for (CaseInfo caseInfo : forceTurnCaseList) {
                    SendReminderMessage sendReminderMessage = new SendReminderMessage();
                    sendReminderMessage.setUserId(Objects.nonNull(caseInfo.getCurrentCollector()) ? caseInfo.getCurrentCollector().getId() : null);
                    sendReminderMessage.setType(ReminderType.FORCE_TURN);
                    sendReminderMessage.setTitle("客户 [" + caseInfo.getPersonalInfo().getName() + "] 的案件强制流转提醒");
                    sendReminderMessage.setContent("您持有客户 [" + caseInfo.getPersonalInfo().getName() + "] 的案件 [" + caseInfo.getCaseNumber() + "] 即将强制流转,请及时留案");
                    sendReminderMessage.setMode(ReminderMode.POPUP);
                    ReminderService.sendReminder(sendReminderMessage);
                }
            }
        } catch (Exception e) {
            logger.error("案件强制流转批量提醒出错，请联系管理员...\n" + e.getMessage(), e);
        }
    }

    /**
     * 协催案件即将强制流转提醒
     *
     * @param companyCode
     */
    protected void getForceTurnAssistCase(String companyCode) {
        try {
            List<CaseAssist> forceTurnAssistCaseList = caseAssistService.getForceTurnAssistCase(companyCode);
            if (Objects.nonNull(forceTurnAssistCaseList)) {
                for (CaseAssist caseAssist : forceTurnAssistCaseList) {
                    SendReminderMessage sendReminderMessage = new SendReminderMessage();
                    sendReminderMessage.setUserId(Objects.nonNull(caseAssist.getAssistCollector()) ? caseAssist.getAssistCollector().getId() : null);
                    sendReminderMessage.setType(ReminderType.FORCE_TURN);
                    sendReminderMessage.setTitle("客户 [" + caseAssist.getCaseId().getPersonalInfo().getName() + "] 的协催案件强制流转提醒");
                    sendReminderMessage.setContent("您参与客户 [" + caseAssist.getCaseId().getPersonalInfo().getName() + "] 的协催案件 [" + caseAssist.getCaseId().getCaseNumber() + "] 即将强制流转,请及时留案");
                    sendReminderMessage.setMode(ReminderMode.POPUP);
                    ReminderService.sendReminder(sendReminderMessage);
                }
            }
        } catch (Exception e) {
            logger.error("协催案件强制流转批量提醒出错，请联系管理员...\n" + e.getMessage(), e);
        }
    }

    /**
     * 持案天数逾期无进展提醒
     *
     * @param companyCode
     */
    protected void getNowhereCase(String companyCode) {
        try {
            List<CaseInfo> nowhereCaseList = caseInfoService.getNowhereCase(companyCode);
            if (Objects.nonNull(nowhereCaseList)) {
                for (CaseInfo caseInfo : nowhereCaseList) {
                    List<User> managers = userService.getManagerByUser(caseInfo.getCurrentCollector().getId());
                    SendReminderMessage sendReminderMessage = new SendReminderMessage();
                    sendReminderMessage.setUserId(managers.get(0).getId());
                    sendReminderMessage.setType(ReminderType.FLLOWUP);
                    sendReminderMessage.setTitle("客户 [" + caseInfo.getPersonalInfo().getName() + "] 无跟进记录提醒");
                    sendReminderMessage.setContent("客户 [" + caseInfo.getPersonalInfo().getName() + "] 案件 [" + caseInfo.getCaseNumber() + "] 长期无跟进记录,请及时处理");
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
        } catch (Exception e) {
            logger.error("持案天数逾期无进展批量提醒出错，请联系管理员...\n" + e.getMessage(), e);
        }
    }

    /**
     * 单次协催案件若干天无进展提醒
     *
     * @param companyCode
     */
    protected void getNowhereCaseAssist(String companyCode) {
        try {
            List<CaseAssist> nowhereCaseAssist = caseInfoService.getNowhereCaseAssist(companyCode);
            if (Objects.nonNull(nowhereCaseAssist)) {
                for (CaseAssist caseAssist : nowhereCaseAssist) {
                    SendReminderMessage sendReminderMessage = new SendReminderMessage();
                    sendReminderMessage.setUserId(caseAssist.getOperator().getId());
                    sendReminderMessage.setType(ReminderType.FLLOWUP);
                    sendReminderMessage.setTitle("客户 [" + caseAssist.getCaseId().getPersonalInfo().getName() + "] 协催案件跟进提醒");
                    sendReminderMessage.setContent("客户 [" + caseAssist.getCaseId().getPersonalInfo().getName() + "] 协催案件 [" + caseAssist.getCaseId().getCaseNumber() + "] 长时间无跟进记录,请及时处理");
                    sendReminderMessage.setMode(ReminderMode.POPUP);
                    ReminderService.sendReminder(sendReminderMessage);
                }
            }
        } catch (Exception e) {
            logger.error("单次协催案件若干天无进展批量提醒出错，请联系管理员...\n" + e.getMessage(), e);
        }
    }
}
