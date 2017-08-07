package cn.fintecher.pangolin.service.reminder.service;

import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.UserLoginLog;
import cn.fintecher.pangolin.service.reminder.client.UserClient;
import cn.fintecher.pangolin.service.reminder.client.UserLoginLogClient;
import cn.fintecher.pangolin.service.reminder.model.ReminderListWebSocketMessage;
import cn.fintecher.pangolin.service.reminder.model.ReminderMessage;
import cn.fintecher.pangolin.web.WebSocketMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.util.*;

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
    @Autowired
    UserLoginLogClient userLoginLogClient;

    public String loginUser(String token, Session session) throws JsonProcessingException {
        ResponseEntity<User> userResult = userClient.getUserByToken(token);
        if (!userResult.hasBody()) {
            return "";
        }
        User user = userResult.getBody();
        users.put(user.getId(), session);
        session.getUserProperties().put("userId", user.getId());
        ObjectMapper mapper = new ObjectMapper();
        List<ReminderMessage> list = reminderMessageService.findByUser(user.getId());
        ReminderListWebSocketMessage reminderListWebSocketMessage = new ReminderListWebSocketMessage();
        reminderListWebSocketMessage.setData(list);
        userLoginLogClient.save(createLoginLog(session, user));
        return mapper.writeValueAsString(reminderListWebSocketMessage);
    }

    private UserLoginLog createLoginLog(Session session, User user) {
        UserLoginLog userLoginLog = new UserLoginLog();
        userLoginLog.setId(session.getId());
        userLoginLog.setLoginTime(new Date());
        userLoginLog.setUserId(user.getId());
        userLoginLog.setUserName(user.getUserName());
        return userLoginLog;
    }

    public void removeUser(Session session) {
        users.get((String) session.getUserProperties().get("userId")).remove(session);
        ResponseEntity<UserLoginLog> entity = userLoginLogClient.get(session.getId());
        if (entity.getStatusCode().is2xxSuccessful()) {
            if (entity.hasBody()) {
                UserLoginLog userLoginLog = entity.getBody();
                userLoginLog.setLogoutTime(new Date());
                userLoginLog.setDuration(Long.valueOf(Seconds.secondsBetween(new DateTime(userLoginLog.getLoginTime()), new DateTime(userLoginLog.getLogoutTime())).getSeconds()));
                userLoginLogClient.save(userLoginLog);
            }
        }
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

    public Integer getOnlineUsersOnCompany(String token) {
        List<String> userIds = new ArrayList<>();
        Iterator<Map.Entry<String, Session>> iterator = users.entries().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Session> next = iterator.next();
            String key = next.getKey(); //userId
            userIds.add(key);
        }
        ResponseEntity<Integer> usersOnCompany = userClient.getUsersOnCompany(userIds, token);
        Integer num = usersOnCompany.getBody();
        return num;
    }
}
