package cn.fintecher.pangolin.entity;

import lombok.Data;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 10:38 2017/7/17
 */
@Data
public class UserLoginRequest {
    String username;
    String password;
    Integer usdeType;
    String usdeCode;
}
