package cn.fintecher.pangolin.service.reminder.service;

import cn.fintecher.pangolin.service.reminder.model.AppMsg;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-05-02-11:57
 */
public interface AppMsgService {


    /**
     * @auther hukaijia
     * @createtime : 2017/2/24 15:24
     * @function :
     */
    void sendPush(AppMsg request);
}
