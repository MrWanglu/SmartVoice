package cn.fintecher.pangolin.service.reminder.model;


import cn.fintecher.pangolin.entity.message.ProgressMessage;
import cn.fintecher.pangolin.web.WebSocketMessage;

/**
 * Created by ChenChang on 2017/4/5.
 */
public class ProgressWebSocketMessage extends WebSocketMessage<ProgressMessage> {
    public ProgressWebSocketMessage() {
        super("Progress");
    }


}
