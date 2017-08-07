package cn.fintecher.pangolin.common.model;

import lombok.Data;

/**
 * 反馈信息操作
 * Created by gaobeibei on 2017/8/7.
 */
@Data
public class RequestFeedback {
    private String id;
    private String feedbackName;
    private String feedbackDescription;
    private String feedbackExtra;

}
