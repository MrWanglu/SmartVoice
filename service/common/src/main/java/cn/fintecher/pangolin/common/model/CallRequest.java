package cn.fintecher.pangolin.common.model;

import lombok.Data;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-06-21-17:56
 */
@Data
public class CallRequest {
    private String caller;
    private String called;
    private String timeout;
    private String displayCaller;
    private String displayCalled;
}
