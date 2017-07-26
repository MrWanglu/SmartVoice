package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 用户所拥有的案件模型
 * @Date : 17:07 2017/7/20
 */

@Data
public class CollectionCaseModel {
    private Integer num; //该用户拥有的案件数
    private List<String> caseIds; //该用户拥有的案件ID集合
}