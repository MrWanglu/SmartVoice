package cn.fintecher.pangolin.report.service;

import cn.fintecher.pangolin.entity.Resource;
import cn.fintecher.pangolin.entity.Role;
import cn.fintecher.pangolin.report.mapper.ResourceRightMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author:peishouwen
 * @Desc:
 * @Date:Create in 14:54 2017/11/15
 */
@Service("resourceRightService")
public class ResourceRightService {
    @Autowired
    ResourceRightMapper resourceRightMapper;

    /**
     * 获取所有资源
     * @return
     */
  public   List<Resource> getAllResource(){
        return resourceRightMapper.getAllResource();
    }

    /**
     * 获取某个角色的资源
     * @param role
     * @return
     */
   public List<Resource> getResourceRolesList(Role role){
        return resourceRightMapper.getResourceRolesList(role);
    }
}
