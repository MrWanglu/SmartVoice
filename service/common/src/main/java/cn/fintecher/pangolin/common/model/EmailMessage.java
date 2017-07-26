package cn.fintecher.pangolin.common.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;

/**
 * @Author: jwdstef
 * @Description:
 * @Date 2017/3/30
 */
@Document
@Data
public class EmailMessage {
    @Id
    private String id;
    private String sendTo;
    private String title;
    private Map<String, Object> model;
    private String templateContent;
    private Date sendTime;
}
