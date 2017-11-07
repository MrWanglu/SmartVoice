package cn.fintecher.pangolin.service.reminder.service.impl;

import cn.fintecher.pangolin.entity.util.MD5;
import cn.fintecher.pangolin.service.reminder.model.AppMsg;
import cn.fintecher.pangolin.service.reminder.repository.AppMsgRepository;
import cn.fintecher.pangolin.service.reminder.service.AppMsgService;
import cn.jiguang.common.ClientConfig;
import cn.jiguang.common.ServiceHelper;
import cn.jiguang.common.connection.NativeHttpClient;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-05-02-11:58
 */
@Service("appMsgService")
public class AppMsgServiceImpl implements AppMsgService {
    Logger logger = LoggerFactory.getLogger(AppMsgServiceImpl.class);

    private final AppMsgRepository appMsgRepository;

    public AppMsgServiceImpl(AppMsgRepository appMsgRepository) {
        this.appMsgRepository = appMsgRepository;
    }

    protected static final String APP_KEY = "a06c080ce726b538738b18c9";
    protected static final String MASTER_SECRET = "0c1f4c5b6599df23fe1c3f42";

    /**
     * @auther hukaijia
     * @createtime : 2017/2/24 15:24
     * @function :
     */
    public void sendPush(AppMsg request) {
        ClientConfig clientConfig = ClientConfig.getInstance();
        final JPushClient jpushClient = new JPushClient(MASTER_SECRET, APP_KEY, null, clientConfig);
        String authCode = ServiceHelper.getBasicAuthorization(APP_KEY, MASTER_SECRET);
        NativeHttpClient httpClient = new NativeHttpClient(authCode, null, clientConfig);
        jpushClient.getPushClient().setHttpClient(httpClient);
        PushPayload payload;
        payload = buildPushObject_android_and_ios(request);
        try {
            PushResult result = jpushClient.sendPush(payload);
            logger.info("Got result - " + result);

        } catch (APIConnectionException e) {
            logger.error("Connection error. Should retry later. ", e);
            logger.error("Sendno: " + payload.getSendno());

        } catch (APIRequestException e) {
            logger.error("Error response from JPush server. Should review and fix it. ", e);
            logger.info("HTTP Status: " + e.getStatus());
            logger.info("Error Code: " + e.getErrorCode());
            logger.info("Error Message: " + e.getErrorMessage());
            logger.info("Msg ID: " + e.getMsgId());
            logger.error("Sendno: " + payload.getSendno());
        }

    }

    public PushPayload buildPushObject_android_and_ios(AppMsg request) {
        ObjectMapper mapper = new ObjectMapper();
        String value = "";
        try {
            value = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        logger.debug("AppMsg JsonValue is {}", value);
        return PushPayload.newBuilder()
                .setPlatform(Platform.android_ios())
                .setAudience(Audience.alias(MD5.MD5Encode(request.getUserId())))
                .setNotification(Notification.newBuilder()
                        /*.addPlatformNotification(AndroidNotification.newBuilder()
                                .setTitle(request.getTitle())
                                .setAlert(request.getContent())
                                .setBuilderId(2) //设置1是简单的一个文本，2是提醒
                                .addExtra("jsonObject", value)
                                .build())*/
                        .addPlatformNotification(IosNotification.newBuilder()
                                .setAlert(request.getTitle() + "\n" + request.getContent())
                                .incrBadge(1)
                                .setBadge(request.getAppMsgUnRead())   //未读消息数量
                                .addExtra("jsonObject", value)
                                .setSound("happy")
                                .build())
                        .build())
                .setMessage(Message.newBuilder()
                        .setTitle(request.getTitle())
                        .setMsgContent(request.getContent())
                        .addExtra("jsonObject", value)
                        .build())
                .setOptions(Options.newBuilder()
                        .setApnsProduction(true)
                        .build())
                .build();
    }
}
