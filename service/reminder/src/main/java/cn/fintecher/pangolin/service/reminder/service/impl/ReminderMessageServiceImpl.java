package cn.fintecher.pangolin.service.reminder.service.impl;

import cn.fintecher.pangolin.service.reminder.model.AppMsg;
import cn.fintecher.pangolin.service.reminder.model.ReminderMessage;
import cn.fintecher.pangolin.service.reminder.model.ReminderWebSocketMessage;
import cn.fintecher.pangolin.service.reminder.repository.AppMsgRepository;
import cn.fintecher.pangolin.service.reminder.repository.ReminderMessageRepository;
import cn.fintecher.pangolin.service.reminder.service.AppMsgService;
import cn.fintecher.pangolin.service.reminder.service.ReminderMessageService;
import cn.fintecher.pangolin.service.reminder.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by ChenChang on 2017/3/20.
 */
@Service("reminderMessageService")
public class ReminderMessageServiceImpl implements ReminderMessageService {
    private final Logger log = LoggerFactory.getLogger(ReminderMessageServiceImpl.class);

    private final ReminderMessageRepository reminderMessageRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ReminderMessageService reminderMessageService;
    @Autowired
    private UserService userService;
    @Autowired
    private AppMsgService appMsgService;
    @Autowired
    private AppMsgRepository appMsgRepository;

    @Autowired
    public ReminderMessageServiceImpl(ReminderMessageRepository reminderMessageRepository) {
        this.reminderMessageRepository = reminderMessageRepository;
    }

    @Override
    public List<ReminderMessage> findByUser(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "createTime")));
        List<ReminderMessage> list = mongoTemplate.find(query, ReminderMessage.class);
        return list;
    }

    @Override
    public Page<ReminderMessage> findByUser(String userId, Pageable pageable, ReminderMessage.ReadStatus readStatus) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        if (Objects.nonNull(readStatus)) {
            query.addCriteria(Criteria.where("state").is(readStatus));
        }
        query.with(pageable);
        long count = mongoTemplate.count(query, ReminderMessage.class);
        Page<ReminderMessage> page = new PageImpl<>(mongoTemplate.find(query, ReminderMessage.class), pageable, count);
        return page;
    }


    @Override
    public Page<ReminderMessage> findByUser(String userId, Map<String, Object> params, Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        for (String key : params.keySet()) {
            query.addCriteria(Criteria.where(key).is(params.get(key)));
        }
        query.with(pageable);
        long count = mongoTemplate.count(query, ReminderMessage.class);
        Page<ReminderMessage> page = new PageImpl<>(mongoTemplate.find(query, ReminderMessage.class), pageable, count);
        return page;
    }

    @Override
    public Long countUnRead(String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("state").is(ReminderMessage.ReadStatus.UnRead));
        long count = mongoTemplate.count(query, ReminderMessage.class);
        return count;
    }

    @Override
    public ReminderMessage sendMessage(ReminderMessage message) {
        message.setState(ReminderMessage.ReadStatus.UnRead);
        message.setCreateTime(new Date());
        ReminderMessage result = reminderMessageRepository.save(message);
        ReminderWebSocketMessage reminderWebSocketMessage = new ReminderWebSocketMessage();
        reminderWebSocketMessage.setData(result);
        if (Objects.nonNull(result.getUserId())) {
            userService.sendMessage(result.getUserId(), reminderWebSocketMessage);
            Long count = reminderMessageService.countUnRead(result.getUserId());
            AppMsg request = new AppMsg();
            BeanUtils.copyProperties(result, request);
            request.setId(null);
            request.setAppMsgUnRead(Long.valueOf(count).intValue());
            request.setContent(result.getContent());
            appMsgRepository.save(request);
            try {
                appMsgService.sendPush(request);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                log.error("消息推送失败");
            }
        }
        return result;
    }
}
