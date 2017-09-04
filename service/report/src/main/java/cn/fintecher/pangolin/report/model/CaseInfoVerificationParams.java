package cn.fintecher.pangolin.report.model;

import lombok.Data;

import java.util.Date;

/**
 * Created by yuanyanting on 2017/9/1.
 */
@Data
public class CaseInfoVerificationParams {

    private String startTime; // 开始时间
    private String endTime; // 结束时间
    private Integer page; // 第几页
    private Integer size; // 每页多少条记录
    private String companyCode; // 公司code

}
