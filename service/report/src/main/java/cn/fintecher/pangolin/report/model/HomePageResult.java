package cn.fintecher.pangolin.report.model;

import lombok.Data;

/**
 * @Author : sunyanping
 * @Description : 首页
 * @Date : 2017/7/31.
 */
@Data
public class HomePageResult<T> {
    private Integer type;
    private T data;
}
