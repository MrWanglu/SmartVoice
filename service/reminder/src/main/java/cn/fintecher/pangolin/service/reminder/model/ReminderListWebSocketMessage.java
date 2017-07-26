package cn.fintecher.pangolin.service.reminder.model;


import cn.fintecher.pangolin.web.WebSocketMessage;

import java.util.List;

/**
 * Created by ChenChang on 2017/4/5.
 */
public class ReminderListWebSocketMessage extends WebSocketMessage<List<ReminderMessage>> {
    public ReminderListWebSocketMessage() {
        super("ReminderList");
    }

}
