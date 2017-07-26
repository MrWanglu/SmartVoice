package cn.fintecher.pangolin.service.reminder.model;

import cn.fintecher.pangolin.entity.ReminderType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Created by ChenChang on 2017/3/20.
 *
 * @Modify by xiaqun on 2017/4/24
 */
@Data
@Document
public class ReminderCalendar implements Serializable {
    @Id
    private String id;
    private ReminderType type;
    private String title;
    private String content;
    private Date remindTime;
    private String userId;
    private Map<String, Object> params;
}
