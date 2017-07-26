package cn.fintecher.pangolin.service.reminder.service;

import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.service.reminder.client.UserClient;
import cn.fintecher.pangolin.service.reminder.model.ReminderListWebSocketMessage;
import cn.fintecher.pangolin.service.reminder.model.ReminderMessage;
import cn.fintecher.pangolin.web.WebSocketMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Created by ChenChang on 2017/3/20.
 */
@Component
public class UserService {

    Multimap<String, Session> users = ArrayListMultimap.create();

    @Autowired
    ReminderMessageService reminderMessageService;
    @Autowired
    UserClient userClient;

    public String loginUser(String token, Session session) throws JsonProcessingException {
        ResponseEntity<User> userResult = userClient.getUserByToken(token);
        User user = userResult.getBody();
        users.put(user.getId(), session);
        session.getUserProperties().put("userId", user.getId());
        ObjectMapper mapper = new ObjectMapper();
        List<ReminderMessage> list = reminderMessageService.findByUser(user.getId());
        ReminderListWebSocketMessage reminderListWebSocketMessage = new ReminderListWebSocketMessage();
        reminderListWebSocketMessage.setData(list);
        return mapper.writeValueAsString(reminderListWebSocketMessage);
    }


    public void removeUser(Session session) {
        users.get((String) session.getUserProperties().get("userId")).remove(session);
    }

    public Collection<Session> getUserSession(String userId) {
        return users.get(userId);
    }

    public void sendMessage(String userId, WebSocketMessage message) {
        try {
            Collection<Session> sessions = getUserSession(userId);
            if (Objects.nonNull(sessions)) {
                for (Session session : sessions) {
                    ObjectMapper mapper = new ObjectMapper();
                    String msg = mapper.writeValueAsString(message);
                    session.getBasicRemote().sendText(msg);
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Integer getOnlineUsers() {
        return users.size();
    }
}
