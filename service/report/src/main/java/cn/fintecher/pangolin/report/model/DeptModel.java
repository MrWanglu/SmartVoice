package cn.fintecher.pangolin.report.model;

import lombok.Data;

/**
 * @author : xiaqun
 * @Description : 用户部门code码和名称模型
 * @Date : 12:05 2017/8/2
 */

@Data
public class DeptModel {
    private String code; //部门code码
    private String name; //部门名称
    private Integer flag; //是否存在父部门ID 0-不存在 1-存在
}