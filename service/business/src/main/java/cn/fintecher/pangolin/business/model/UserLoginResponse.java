package cn.fintecher.pangolin.business.model;


import cn.fintecher.pangolin.entity.User;
import lombok.Data;

/**
 * Created by ChenChang on 2017/3/7.
 */
@Data
public class UserLoginResponse {
    User user;
    String token;
    boolean reset; //true是需要修改密码
    String regDay;
}
