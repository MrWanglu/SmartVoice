package cn.fintecher.pangolin.report.model;

import lombok.Data;

/**
 * Created by qijigui on 2017-11-10.
 */

@Data
public class CaseDateModel {

    // 案件池中所有的案件的最小年份
    private String minCaseYear;

    // 案件池中所有的案件的最小年份 月份
    private String minCaseYearMonth;
}


