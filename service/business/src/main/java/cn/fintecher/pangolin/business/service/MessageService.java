package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.repository.PersonalContactRepository;
import cn.fintecher.pangolin.business.repository.SendMessageRecordRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by Administrator on 2017/9/1.
 */
@Service("messageService")
public class MessageService {
    @Inject
    PersonalContactRepository personalContactRepository;
    @Inject
    SendMessageRecordRepository sendMessageRecordRepository;

    public SendMessageRecord saveMessage(CaseInfo caseInfo, Personal personal, Template template, String id, User user, Integer way) {
        SendMessageRecord sendMessageRecord = new SendMessageRecord();
        sendMessageRecord.setPersonalId(personal.getId());
        sendMessageRecord.setPersonalName(personal.getName());
        sendMessageRecord.setTempelateType(template.getTemplateType());
        sendMessageRecord.setTempelateName(template.getTemplateName());
        sendMessageRecord.setTempelateCode(template.getTemplateCode());
        sendMessageRecord.setTempelateId(template.getId());
        sendMessageRecord.setCompanyCode(user.getCompanyCode());
        sendMessageRecord.setOperatorUserName(user.getUserName());
        sendMessageRecord.setOperatorRealName(user.getRealName());
        sendMessageRecord.setOperatorDate(ZWDateUtil.getNowDateTime());
        sendMessageRecord.setMessageContent(template.getMessageContent());
        sendMessageRecord.setSendWay(way);
        sendMessageRecord.setCaseId(caseInfo.getId());
        PersonalContact personalContact = personalContactRepository.getOne(id);
        sendMessageRecord.setTarget(personalContact.getRelation());
        sendMessageRecord.setTargetName(personalContact.getName());
        sendMessageRecord.setPersonalContactId(id);
        sendMessageRecord.setMessageType(SendMessageRecord.MessageType.SMS.getValue());
        SendMessageRecord result = sendMessageRecordRepository.save(sendMessageRecord);
        return result;
    }
}
