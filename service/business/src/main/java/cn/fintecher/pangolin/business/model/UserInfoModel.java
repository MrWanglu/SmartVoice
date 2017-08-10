package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/8/9.
 */
@Data
public class UserInfoModel {
    private List<String> userIds;
    private String userId;
    private String userName;
    private String collector;
    private Integer caseCount;
}
