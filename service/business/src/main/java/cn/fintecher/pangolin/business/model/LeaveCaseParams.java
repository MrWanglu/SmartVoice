package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 留案操作参数
 * @Date : 14:58 2017/8/15
 */

@Data
public class LeaveCaseParams {
    private Integer type; //案件类型 0-电催 1-外访
    private List<String> caseIds; //案件ID集合
    private String companyCode; //公司code码
}