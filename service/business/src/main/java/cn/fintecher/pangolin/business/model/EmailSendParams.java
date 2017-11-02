package cn.fintecher.pangolin.business.model;

import lombok.Data;

/**
 * @author : xiaqun
 * @Description : 电子邮件页面群发参数
 * @Date : 13:52 2017/3/22
 */

@Data
public class EmailSendParams {
    private String custId; //客户ID
    private String custName; //客户姓名
    private String email; //客户邮箱
    private String cupoId;// 案件id
}