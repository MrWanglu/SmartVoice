package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

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
    private List<Map<Integer,String>> phoneList; //电话集合
    private List<Map<Integer,String>> socialList; //社交帐号集合
}