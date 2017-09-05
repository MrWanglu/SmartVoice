package cn.fintecher.pangolin.common.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Created by ChenChang on 2017/3/23.
 */
@Document
@Data
public class SMSMessage implements Serializable{
    @Id
    private String id;
    private String phoneNumber;
    private String template;
    private Map<String, String> params;
    private Date sendTime;
    private String userId;
    private String companyCode;
}
