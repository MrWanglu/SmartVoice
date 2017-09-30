package cn.fintecher.pangolin.report.model;

import lombok.Data;

/**
 * @author : huyanmin
 * @Description : 委外催收中查询的Params
 * @Date : 2017/9/25
 */

@Data
public class  QueryOutsourcePoolParams {

    /*公司标识码*/
    private String companyCode;
    /*案件批次号*/
    private String batchNumber;
    /*受托方名称*/
    private String outsName;
    /*委案日期*/
    private String outTime;
    /*委外到期日期*/
    private String overOutsourceTime;

}
