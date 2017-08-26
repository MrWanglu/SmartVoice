package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/8/26.
 */
@Data
public class ExportCaseNum {
    private List<String> caseNumberList;//案件编号集合
    private String companyCode; //公司Code
}
