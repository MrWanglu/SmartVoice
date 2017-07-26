package cn.fintecher.pangolin.entity.message;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * Created by ChenChang on 2017/3/23.
 */
@Data
public class SendSMSMessage {
    private String id;
    private String phoneNumber;
    private String template;
    private Map<String, String> params;
    private Date sendTime;

}
