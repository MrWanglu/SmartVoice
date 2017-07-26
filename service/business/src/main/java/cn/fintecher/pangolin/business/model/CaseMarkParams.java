package cn.fintecher.pangolin.business.model;

import lombok.Data;

/**
 * @author : xiaqun
 * @Description : 案件打标参数
 * @Date : 15:57 2017/7/21
 */

@Data
public class CaseMarkParams {
    private String caseId; //案件ID
    private Integer colorNum; //打标颜色
}