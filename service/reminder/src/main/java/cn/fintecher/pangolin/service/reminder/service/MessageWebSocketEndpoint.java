package cn.fintecher.pangolin.service.reminder.service;

import cn.fintecher.pangolin.service.reminder.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;


/**
 * Created by ChenChang on 2017/3/22.
 */
@ServerEndpoint(value = "/webSocketMessage")
public class MessageWebSocketEndpoint {
    private final Logger logger = LoggerFactory.getLogger(MessageWebSocketEndpoint.class);

    @OnMessage
    public void handleMessage(Session session, String message) throws IOException {
        UserService userService = SpringContextUtil.getApplicationContext().getBean(UserService.class);
        String userUnReadList = userService.loginUser(message, session);
        logger.info("Received: {}", userUnReadList);
        session.getBasicRemote()
                .sendText(userUnReadList);
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        session.getBasicRemote().sendText("onOpen");
    }

    @OnError
    public void onError(Throwable t) {
        t.printStackTrace();
    }

    @OnClose
    public void onClose(Session session) {
        UserService userService = SpringContextUtil.getApplicationContext().getBean(UserService.class);
        userService.removeUser(session);
    }
}
