package cn.fintecher.pangolin.service.reminder.model;

/**
 * Created by ChenChang on 2017/3/17.
 */
public class LoginMessage {
    private String token;

    public LoginMessage() {
    }

    public LoginMessage(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
