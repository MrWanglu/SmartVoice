package cn.fintecher.pangolin.common.service;


import cn.fintecher.pangolin.entity.message.EmailMessage;

/**
 * @Author: sunyanping
 * @Description: 邮件发送接口
 * @Date 2017/2/28
 */
public interface EmailService {
    void sendMail(EmailMessage emailMessage);

}
