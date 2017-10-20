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
    /*委案日期开始*/
    private String outTimeStart;
    /*委案日期结束*/
    private String outTimeEnd;
    /*委外结案日期开始*/
    private String overOutsourceTimeStart;
    /*委外结案日期结束*/
    private String overOutsourceTimeEnd;
    /*页码数*/
    private Integer page;
    /*每一页显示的数量*/
    private Integer size;

}
