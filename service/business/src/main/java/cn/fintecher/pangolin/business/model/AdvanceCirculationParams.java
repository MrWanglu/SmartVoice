package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 申请提前流转参数
 * @Date : 9:44 2017/8/16
 */

@Data
public class AdvanceCirculationParams {
    private List<String> caseIds; //案件ID列表
    private Integer type; //案件类型 0-电催 1-外访
}