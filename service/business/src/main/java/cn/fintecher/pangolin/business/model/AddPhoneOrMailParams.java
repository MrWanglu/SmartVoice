package cn.fintecher.pangolin.business.model;

import lombok.Data;

/**
 * @author : xiaqun
 * @Description : 新增客户关系人电话或邮箱参数
 * @Date : 16:44 2017/8/8
 */

@Data
public class AddPhoneOrMailParams {
    private String id; //关系人ID
    private String phone; //新电话号码
    private String email; //新邮箱地址
}