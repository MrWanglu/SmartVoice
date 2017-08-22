package cn.fintecher.pangolin.report.model;

import lombok.Data;

/**
 * @author : xiaqun
 * @Description : 催收员业绩排名报表参数
 * @Date : 19:24 2017/8/21
 */

@Data
public class PerformanceRankingParams {
    private String realName; //用户姓名
    private Integer type; //报表类型 0-实时 1-历史
    private String companyCode; //公司code码
    private String date; //查询日期
    private String code; //部门code码
}