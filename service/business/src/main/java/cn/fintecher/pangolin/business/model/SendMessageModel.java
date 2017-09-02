package cn.fintecher.pangolin.business.model;

import cn.fintecher.pangolin.entity.Template;
import cn.fintecher.pangolin.entity.User;
import lombok.Data;

import java.util.List;

/**
 * Created by Administrator on 2017/9/1.
 */
@Data
public class SendMessageModel {
    private String caseNumber;
    private PersonModel personal;
    private Template template;
    private User user;
    private Integer sendWay;
}
