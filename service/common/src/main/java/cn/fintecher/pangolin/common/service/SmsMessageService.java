package cn.fintecher.pangolin.common.service;


import cn.fintecher.pangolin.entity.message.SendSMSMessage;

/**
 * Created by Administrator on 2017/3/24.
 */
public interface SmsMessageService {

    /**
     * 发送短信
     *
     * @param
     * @return 返回发送结果
     */
    void sendMessage(SendSMSMessage message);

}


