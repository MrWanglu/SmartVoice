package cn.fintecher.pangolin.report.model;

import lombok.Data;

/**
 * Created by Administrator on 2017/9/25.
 */
@Data
public class CollectingCaseParams {
    private String batchNumber;
    private String principalId;
    private String delegationDateStart;
    private String delegationDateEnd;
    private String closeDateStart;
    private String closeDateEnd;
    private String deptCode;
    private String companyCode;
    /*页码数*/
    private Integer page;
    /*每一页显示的数量*/
    private Integer size;
}
