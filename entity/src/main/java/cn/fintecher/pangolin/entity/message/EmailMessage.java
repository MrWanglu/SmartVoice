package cn.fintecher.pangolin.entity.message;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @Author: jwdstef
 * @Description:
 * @Date 2017/3/30
 */
@Data
public class EmailMessage implements Serializable{
    private String id;
    private String sendTo;
    private String title;
    private Map<String, Object> model;
    private String templateContent;
    private Date sendTime;
}
