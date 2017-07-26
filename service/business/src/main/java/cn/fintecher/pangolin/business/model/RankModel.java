package cn.fintecher.pangolin.business.model;

import lombok.Data;

/**
 * @author : gaobeibei
 * @Description : APP首页信息展示排行榜模型
 * @Date : 16:01 2017/7/25
 */
@Data
public class RankModel {
    private Integer rank; //排名
    private String userId; //用户ID
    private String userName; //用户名称
    private String score; //数据
}
