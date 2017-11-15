package cn.fintecher.pangolin.report.web;
import cn.fintecher.pangolin.entity.Resource;
import cn.fintecher.pangolin.entity.Role;
import cn.fintecher.pangolin.report.service.ResourceRightService;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * @Author:peishouwen
 * @Desc: 资源加载控制层
 * @Date:Create in 11:44 2017/11/15
 */
@RestController
@RequestMapping("/api/ResourceRightController")
@Api(description = "资源查询")
public class ResourceRightController extends BaseController{

    private final Logger log = LoggerFactory.getLogger(ResourceRightController.class);
    @Autowired
    ResourceRightService resourceRightService;
    @GetMapping("/getAllResource")
    @ApiOperation(value = "获取所有资源权限", notes = "获取所有资源权限")
    public ResponseEntity<List<Resource>> getAllResource(@RequestHeader(value = "X-UserToken") String token) {
        try {
            getUserByToken(token);
        } catch ( Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("ResourceRightController", "getAllResource", e.getMessage())).body(null);
        }
        List<Resource> resources=resourceRightService.getAllResource();
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功","")).body(resources);
    }

    @GetMapping("/getResourceRolesList")
    @ApiOperation(value = "获取某个角色的资源权限", notes = "获取某个角色的资源权限")
    public ResponseEntity<List<Resource>> getResourceRolesList(@RequestHeader(value = "X-UserToken") String token,
                                                         @RequestParam(required = false) @ApiParam(value = "角色ID") String id,
                                                               @RequestParam(required = false) @ApiParam(value = "公司码") String companyCode) {
        try {
            getUserByToken(token);
        } catch ( Exception e) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("ResourceRightController", "getAllResource", e.getMessage())).body(null);
        }
        Role role=new Role();
        role.setId(id);
        role.setCompanyCode(companyCode);
        List<Resource> resources=resourceRightService.getResourceRolesList(role);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功","")).body(resources);
    }
}
