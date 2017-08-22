package cn.fintecher.pangolin.business.service.impl;

import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.service.ReminderService;
import cn.fintecher.pangolin.entity.message.SendReminderMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.util.List;

/**
 * @author : DuChao
 * @Description : 提醒接口实现
 * @Date : 2017/8/16.
 */
@Service("reminderService")
public class ReminderServiceImpl implements ReminderService{

    @Inject
    private RestTemplate restTemplate;
    @Inject
    private CaseInfoRepository caseInfoRepository;

    @Override
    public void sendReminder(SendReminderMessage sendReminderMessage) {
        restTemplate.postForLocation("http://reminder-service/api/reminderMessages/receiveMessage",sendReminderMessage);
    }

    @Override
    public void saveReminderTiming(SendReminderMessage sendReminderMessage){
        restTemplate.postForLocation("http://reminder-service/api/reminderTiming/saveReminderTiming",sendReminderMessage);
    }

    @Override
    public List<SendReminderMessage> getAllReminderMessage(){
        return null;
    }


    @Override
    public void leaveCaseReminder() {
/*        QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
        Iterable<CaseInfo> caseInfoIterable = caseInfoRepository.findAll(qCaseInfo.leaveCaseFlag.eq(1).
                        and(qCaseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue())));
        Iterator<CaseInfo> iterator = caseInfoIterable.iterator();
        while(iterator.hasNext()){
            CaseInfo caseInfo = iterator.next();
            String userId = caseInfo.getCurrentCollector().getId();
            String reminderTitle = "案件留案提醒";
            String reminderContent = "您有留案案件["+caseInfo.getCaseNumber()+"]未处理,请及时处理。";
            this.sendReminder(reminderTitle,reminderContent,userId,ReminderType.LEAVE_CASE,null);
        }*/    //待修改
    }

    @Override
    public void applyReminder() {

    }

}
