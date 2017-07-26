package cn.fintecher.pangolin.service.reminder.model;

import cn.fintecher.pangolin.entity.ReminderType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Created by ChenChang on 2017/3/17.
 */
@Document
@Data
public class ReminderMessage implements Serializable {
    public enum ReadStatus {
        Read, UnRead
    }

    @Id
    private String id;
    private ReminderType type;
    private String userId;
    private String title;
    private String content;
    private ReadStatus state;
    private Date createTime;
    private Map<String, Object> params;
}
