package cn.fintecher.pangolin.business.model;

import lombok.Data;

/**
 * @author : sunyanping
 * @Description : 案件打标参数
 * @Date : 15:57 2017/7/21
 */

@Data
public class AssistCaseMarkParams {
    private String assistId; //协催案件ID
    private String markId; //打标标记
}