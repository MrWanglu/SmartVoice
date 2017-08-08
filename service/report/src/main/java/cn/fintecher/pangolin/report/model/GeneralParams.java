package cn.fintecher.pangolin.report.model;

import lombok.Data;

import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 通用报表参数
 * @Date : 9:00 2017/8/2
 */

@Data
public class GeneralParams {
    private Date startDate; //起始时间
    private Date endDate; //终止时间
    private String realName; //用户名
    private String code; //部门code码
    private Integer type; //报表类型 0-实时 1历史
}