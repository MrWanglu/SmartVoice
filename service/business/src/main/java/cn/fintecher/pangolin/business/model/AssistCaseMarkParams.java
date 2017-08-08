package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * @author : sunyanping
 * @Description : 案件打标参数
 * @Date : 15:57 2017/7/21
 */

@Data
public class AssistCaseMarkParams {
    private List<String> assistIds; //协催案件ID集合
    private Integer markId; //打标标记
}