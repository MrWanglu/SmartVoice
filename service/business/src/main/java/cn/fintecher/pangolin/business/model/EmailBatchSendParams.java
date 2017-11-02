package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : xiaqun
 * @Description : 电子邮件群发参数封装对象
 * @Date : 13:37 2017/3/24
 */

@Data
public class EmailBatchSendParams {
    private List<String> emailBatchSendList = new ArrayList<>(); //案件数组ID

    private List<EmailSendParams> emailSendParamList = new ArrayList<>();

    private String mereContent;

    private String tesmId;

    private Integer mereStyle;

}