package cn.fintecher.pangolin.entity.message;

import cn.fintecher.pangolin.entity.ReminderMode;
import cn.fintecher.pangolin.entity.ReminderType;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/7/21.
 */
@Data
public class SendReminderMessage {
    private ReminderType type;
    private ReminderMode mode;
    private String userId;
    private String title;
    private String content;
    private Date createTime;
    private Map<String, Object> params;
    private String[] ccUserIds;
}

