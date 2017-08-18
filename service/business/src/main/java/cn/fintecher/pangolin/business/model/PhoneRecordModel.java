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
    private String targetName; //跟进对象
    private String operatorName; //操作人
    private String url; //录音地址
    private Date date; //跟进时间
}