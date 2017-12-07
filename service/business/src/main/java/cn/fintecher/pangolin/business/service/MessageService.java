package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.CapaPersonals;
import cn.fintecher.pangolin.business.model.PersonalParams;
import cn.fintecher.pangolin.business.model.SMSMessageParams;
import cn.fintecher.pangolin.business.repository.PersonalContactRepository;
import cn.fintecher.pangolin.business.repository.SendMessageRecordRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/9/1.
 */
@Service("messageService")
public class MessageService {
    @Inject
    PersonalContactRepository personalContactRepository;
    @Inject
    SendMessageRecordRepository sendMessageRecordRepository;

    public SendMessageRecord saveMessage(CaseInfo caseInfo, Personal personal, Template template, String id, User user, Integer way, Integer flag, String messageContent) {
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
        sendMessageRecord.setMessageContent(messageContent);
        sendMessageRecord.setSendWay(way);
        sendMessageRecord.setFlag(flag);
        sendMessageRecord.setCaseId(caseInfo.getId());
        PersonalContact personalContact = personalContactRepository.getOne(id);
        sendMessageRecord.setTarget(personalContact.getRelation());
        sendMessageRecord.setTargetName(personalContact.getName());
        sendMessageRecord.setPersonalContactId(id);
        sendMessageRecord.setMessageType(SendMessageRecord.MessageType.SMS.getValue());
        SendMessageRecord result = sendMessageRecordRepository.save(sendMessageRecord);
        return result;
    }

    public List<PersonalParams> parseErrorList(List<CapaPersonals> personals) {
        List<PersonalParams> result = new ArrayList<>();
        for (CapaPersonals params : personals) {
            List<String> ids = params.getConcatIds();
            List<String> names = params.getConcatNames();
            List<String> phones = params.getConcatPhones();
            for (int i = 0; i < params.getConcatIds().size(); i++) {
                PersonalParams personalParams = new PersonalParams();
                personalParams.setContId(ids.get(i));
                personalParams.setPersonalName(names.get(i));
                personalParams.setPersonalPhone(phones.get(i));
                result.add(personalParams);
            }
        }
        return result;
    }
}
