package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.session.SessionStore;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.util.Constants;

import javax.servlet.http.HttpSession;

/**
 * @Author: PeiShouWen
 * @Description: 通用Controller
 * @Date 13:26 2017/7/17
 */

public class BaseController {

    public User getUserByToken(String token) throws Exception {
        HttpSession session = SessionStore.getInstance().getSession(token);
        if (session == null) {
            throw new Exception(Constants.SYS_EXCEPTION_NOSESSION);
        }
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        return user;
    }
}
