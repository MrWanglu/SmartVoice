package cn.fintecher.pangolin.business.model;

import lombok.Data;

/**
 * @Author: PeiShouWen
 * @Description: 客户信息(短信参数）
 * @Date 11:34 2017/9/1
 */
@Data
public class PersonalParams {
    private String personalName;
    private String personalPhone;
    private String contId;
}
