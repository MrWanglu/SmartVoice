package cn.fintecher.pangolin.business.model;

import lombok.Data;

import java.util.List;

/**
 * Created by ChenChang on 2017/3/13.
 */
@Data
public class ManyUserAddRoleRequest {
    List<String> userIds;
    List<String> roleIds;
}
