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
    private List<String> caseIds;
}