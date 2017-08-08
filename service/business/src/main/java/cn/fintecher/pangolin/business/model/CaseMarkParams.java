package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 案件打标参数
 * @Date : 15:57 2017/7/21
 */

@Data
public class CaseMarkParams {
    private List<String> caseIds; //案件ID集合
    private Integer colorNum; //打标颜色
}