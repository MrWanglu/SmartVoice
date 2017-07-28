package cn.fintecher.pangolin.entity.message;

import cn.fintecher.pangolin.entity.ReminderType;
import lombok.Data;

import java.util.Date;

/**
 * Created by ChenChang on 2017/3/17.
 */
@Data
public class ReminderMessage{
    public enum ReadStatus{
      Read,UnRead
    }
    private String id;
    private ReminderType type;
    private String userId;
    private String title;
    private String content;
    private ReadStatus state;
    private Date createTime;
    private String cupoId; //案件ID
    private String cupoCaseNum; //案件编号
    private String custId; //客户ID
    private Integer cupoRecType; //催收类型

}
