package cn.fintecher.pangolin.service.reminder.service.impl;

import cn.fintecher.pangolin.service.reminder.model.ReminderMessage;
import cn.fintecher.pangolin.service.reminder.repository.ReminderMessageRepository;
import cn.fintecher.pangolin.service.reminder.service.ReminderMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by ChenChang on 2017/3/20.
 */
@Service("reminderMessageService")
public class ReminderMessageServiceImpl implements ReminderMessageService {
    private final ReminderMessageRepository reminderMessageRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

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
    public Page<ReminderMessage> findByUser(String userId, Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
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
}
