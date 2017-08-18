package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 电话录音对象模型
 * @Date : 14:49 2017/8/18
 */

@Data
public class PhoneRecordModel {
    private String url;
    private Date date;
}