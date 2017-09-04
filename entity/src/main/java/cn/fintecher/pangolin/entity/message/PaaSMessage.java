package cn.fintecher.pangolin.entity.message;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;

/**
 * @Author : gaobeibei
 * @Description : 创蓝短息发送实体
 * @Date : 2017/9/4.
 */
@Document
@Data
public class PaaSMessage {
    @Id
    private String id;
    private String phoneNumber;
    private String template;
    private Map<String, String> params;
    private Date sendTime;
    private String content;
    private String userId;
    private String companyCode;
}
