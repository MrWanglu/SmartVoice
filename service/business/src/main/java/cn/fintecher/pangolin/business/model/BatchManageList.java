package cn.fintecher.pangolin.business.model;

import lombok.Data;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-08-23-17:50
 */
@Data
public class BatchManageList {
    private String taskName;
    private String taskGroup;
    private String taskDescription;
    private String taskClassName;
    private String triggerName;
    private String triggerGroup;
    private String nextExecutionTime;
    private String expression;
    private String timeZone;
}
