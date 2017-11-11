package cn.fintecher.pangolin.report.model;


import lombok.Data;

/*
* Auther: huangrui
* Date: 2017年11月11日
* Desc: 某个季度的跟进记录
* */
@Data
public class GroupMonthFollowRecord {
    //当前月份
    private String currentMonth;
    //类型 0:外呼 1：催记
    private Integer wayType;
    //当前月份某种类型的记录数
    private Integer typeCount;
}
