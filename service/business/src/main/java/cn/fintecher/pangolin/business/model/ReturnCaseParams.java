package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 退案参数
 * @Date : 16:44 2017/9/19
 */

@Data
public class ReturnCaseParams {
    List<String> caseIds; //案件ID集合
    String reason; //退案原因
}