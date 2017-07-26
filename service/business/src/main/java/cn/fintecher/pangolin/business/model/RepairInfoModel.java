package cn.fintecher.pangolin.business.model;

import lombok.Data;

/**
 * @author : xiaqun
 * @Description : 修复信息模型
 * @Date : 14:46 2017/7/26
 */

@Data
public class RepairInfoModel {
    private String personalId; //客户信息ID
    private Integer relation; //关系
    private String name; //姓名
    private Integer phoneStatus; //电话状态
    private String phone; //电话号码
    private Integer socialType; //社交帐号类型
    private String socialValue; //社交帐号内容
}