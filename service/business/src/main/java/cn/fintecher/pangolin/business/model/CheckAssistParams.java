package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * @author : xiaqun
 * @Description : 检查是否是协催参数
 * @Date : 9:51 2017/7/31
 */

@Data
public class CheckAssistParams {
    private List<String> list;
}