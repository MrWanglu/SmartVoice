package cn.fintecher.pangolin.service.reminder.service;

import cn.fintecher.pangolin.service.reminder.model.ReminderMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Created by ChenChang on 2017/3/20.
 */
public interface ReminderMessageService {
    List<ReminderMessage> findByUser(String userId);

    Page<ReminderMessage> findByUser(String userId, Pageable pageable);

    Page<ReminderMessage> findByUser(String userId, Map<String, Object> params, Pageable pageable);

    Long countUnRead(String userId);
}
