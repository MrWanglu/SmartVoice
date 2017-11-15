package cn.fintecher.pangolin.report.mapper;

import cn.fintecher.pangolin.entity.Resource;
import cn.fintecher.pangolin.entity.Role;

import java.util.List;

/**
 * @Author:peishouwen
 * @Desc:
 * @Date:Create in 13:43 2017/11/15
 */
public interface ResourceRightMapper {
    /**
     * 获取所有的资源权限
     * @return
     */
    List<Resource> getAllResource();
    /**
     * 更新role 查询某个角色的资源权限
     * @param role
     * @return
     */
    List<Resource> getResourceRolesList(Role role);
}
