package cn.fintecher.pangolin.service.reminder.model;


import cn.fintecher.pangolin.web.WebSocketMessage;

/**
 * Created by ChenChang on 2017/4/5.
 */
public class ListResultMessage extends WebSocketMessage<ListResult> {
    public ListResultMessage() {
        super("ListResult");
    }


}
