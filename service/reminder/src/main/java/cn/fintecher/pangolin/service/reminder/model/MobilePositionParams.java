package cn.fintecher.pangolin.service.reminder.model;

import lombok.Data;

import java.util.Date;

/**
 * @author : xiaqun
 * @Description : 移动定位多条件查询参数
 * @Date : 11:59 2017/5/31
 */
@Data
public class MobilePositionParams {
    private String name; //催收员姓名
    private String depCode; //部门
    private String startDate; //起始时间
    private String endDate; //终止时间
}