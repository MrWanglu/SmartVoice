package cn.fintecher.pangolin.business.session;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 13:29 2017/7/17
 */
public class SessionStore {
    private static SessionStore instance = new SessionStore();
    private Map<String, HttpSession> appUserSessions;

    private SessionStore() {
        appUserSessions = new HashMap<>();
    }

    public static SessionStore getInstance() {
        return instance;
    }

    public HttpSession getSession(String id) {
        return appUserSessions.get(id);
    }

    public void addUser(String id, HttpSession session) {
        appUserSessions.put(id, session);
    }

    public void removeUser(String id) {
        appUserSessions.remove(id);
    }
}
