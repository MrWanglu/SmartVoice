package cn.fintecher.pangolin.report.model;

import lombok.Data;

/**
 * @author : xianqun
 * @Description : 催收员业绩进展报表参数
 * @Date : 14:09 2017/8/3
 */

@Data
public class PerformanceParams {
    private String code; //部门code码
    private String userName; //用户名
    private String companyCode; //公司code码
}