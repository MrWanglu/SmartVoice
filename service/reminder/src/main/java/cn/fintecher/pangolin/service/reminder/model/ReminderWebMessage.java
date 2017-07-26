package cn.fintecher.pangolin.service.reminder.model;


import org.springframework.data.domain.PageImpl;

/**
 * Created by Administrator on 2017/4/24.
 */
public class ReminderWebMessage {
    private Long unReadeCount;

    private PageImpl<ReminderMessage> messageList;

    public Long getUnReadeCount() {
        return unReadeCount;
    }

    public void setUnReadeCount(Long unReadeCount) {
        this.unReadeCount = unReadeCount;
    }

    public PageImpl<ReminderMessage> getMessageList() {
        return messageList;
    }

    public void setMessageList(PageImpl<ReminderMessage> messageList) {
        this.messageList = messageList;
    }
}
