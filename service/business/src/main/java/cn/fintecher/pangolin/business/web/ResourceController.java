package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.ResAddRole;
import cn.fintecher.pangolin.business.repository.ResourceRepository;
import cn.fintecher.pangolin.business.repository.RoleRepository;
import cn.fintecher.pangolin.business.service.DataDictService;
import cn.fintecher.pangolin.business.service.ResourceService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-05-19:35
 */
@RestController
@RequestMapping("/api/resourceController")
@Api(value = "资源管理", description = "资源管理")
public class ResourceController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(ResourceController.class);
    private static final String ENTITY_NAME = "Resource";
    @Autowired
    private ResourceRepository resourceRepository;
    @Autowired
    DataDictService dataDictService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ResourceService resourceService;

    /**
     * @Description : 资源的level属性
     */
    @GetMapping("/getResourceLevel")
    @ApiOperation(value = "资源的level属性", notes = "资源的level属性")
    public ResponseEntity<List<DataDict>> getResourceLevel() {
        List<DataDict> dataDictList = dataDictService.getDataDictByTypeCode("0029");
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(dataDictList);

    }

    /**
     * @Description : 资源的type属性
     */
    @GetMapping("/getResourceType")
    @ApiOperation(value = "资源的type属性", notes = "资源的type属性")
    public ResponseEntity<List<DataDict>> getResourceType() {
        List<DataDict> dataDictList = dataDictService.getDataDictByTypeCode("0030");
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(dataDictList);
    }

    /**
     * @Description : 资源的filetype属性
     */
    @GetMapping("/getResourceFileType")
    @ApiOperation(value = "资源的filetype属性", notes = "资源的filetype属性")
    public ResponseEntity<List<DataDict>> getResourceFileType() {
        List<DataDict> dataDictList = dataDictService.getDataDictByTypeCode("0031");
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(dataDictList);
    }

    /**
     * @Description : 资源的flag属性
     */
    @GetMapping("/getResourceStatus")
    @ApiOperation(value = "资源的flag属性", notes = "资源的flag属性")
    public ResponseEntity<List<DataDict>> getResourceStatus() {
        List<DataDict> dataDictList = dataDictService.getDataDictByTypeCode("0032");
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(dataDictList);
    }

    /**
     * @Description : 新增资源方法
     */
    @PostMapping("/createResource")
    @ApiOperation(value = "增加资源", notes = "增加资源")
    public ResponseEntity<Resource> createResource(@Validated @ApiParam("资源") @RequestBody Resource resource,
                                                   @RequestHeader(value = "X-UserToken") String token) {
        logger.debug("REST request to save resource : {}", resource);
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        if (resource.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "idexists", "新增不应该含有ID")).body(null);
        }
        if (!(Objects.equals(user.getId(), Constants.ADMINISTRATOR_ID))) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "Can't add without permission", "没有权限不能添加")).body(null);
        }
        Resource result = resourceService.save(resource);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(result);
    }

    /**
     * @Description : 修改资源
     */
    @PostMapping("/updateResource")
    @ApiOperation(value = "修改资源", notes = "修改资源")
    public ResponseEntity<Resource> updateCompany(@Validated @ApiParam("资源对象") @RequestBody Resource resource,
                                                  @RequestHeader(value = "X-UserToken") String token) {
        logger.debug("REST request to update resource : {}", resource);
        if (resource.getId() == null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "idexists", "修改应该含有ID")).body(null);
        }
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        if (!(Objects.equals(user.getId(), Constants.ADMINISTRATOR_ID))) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "Can't add without permission", "没有权限不能添加")).body(null);
        }
        Resource result = resourceService.save(resource);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(result);
    }

    /**
     * @Description : 资源唯一值
     */
    @GetMapping(value = "/resourceHashCode")
    @ApiOperation(value = "资源唯一值", notes = "资源唯一值")
    public ResponseEntity<Map<String, String>> getAllResourceHashCode() {
        List<Resource> list = resourceRepository.findAll();
        String code = String.valueOf(list.hashCode());
        Map<String, String> map = new HashMap<String, String>();
        map.put("resourceHashCode", code);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("登录成功", ENTITY_NAME)).body(map);
    }

    /**
     * @Description : 获取所有资源
     */
    @GetMapping(value = "/getAllResource")
    @ApiOperation(value = "获取所有资源", notes = "获取所有资源")
    public ResponseEntity<List<Resource>> getAllResource() {
        List<Resource> list = resourceService.findAll();
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(list);
    }

    /**
     * @Description : 资源添加角色
     */
    @PostMapping("/resourceAddRole")
    @ApiOperation(value = "资源添加角色", notes = "资源添加角色")
    public ResponseEntity<Role> roleAddResource(@ApiParam("资源id集合") @RequestBody ResAddRole request) {
        Role role = roleRepository.findOne(request.getRoleId());
        if (Objects.isNull(role)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "Role does not exist", "角色不存在")).body(null);
        }
        roleRepository.deleteResoByRoleId(request.getRoleId());
        List<Resource> resources = resourceRepository.findAll(request.getResoIds());
        for (Resource resource : resources) {
            resource.getRoles().add(role);
            resourceService.save(resource);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(null);
    }

    /**
     * @Description : 查询资源列表
     */
    @PostMapping("/queryResource")
    @ApiOperation(value = "查询资源列表", notes = "查询资源列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<Resource>> queryResource(@RequestParam(required = false) String sysName,
                                                        @RequestParam(required = false) String name,
                                                        @RequestParam(required = false) String code,
                                                        @RequestParam(required = false) Integer level,
                                                        @RequestParam(required = false) Integer state,
                                                        @RequestParam(required = false) Integer type,
                                                        @RequestParam(required = false) Integer fileType,
                                                        @ApiIgnore Pageable pageable,
                                                        @RequestHeader(value = "X-UserToken") String token) {
        logger.debug("REST request to query Resource : {}");
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        QResource qResource = QResource.resource;
        BooleanBuilder builder = new BooleanBuilder();
        if (Objects.nonNull(sysName)) {
            builder.and(qResource.sysName.like(sysName.concat("%")));
        }
        if (Objects.nonNull(name)) {
            builder.and(qResource.name.like(name.concat("%")));
        }
        if (Objects.nonNull(code)) {
            builder.and(qResource.sysName.like(sysName.concat("%")));
        }
        if (Objects.nonNull(level)) {
            builder.and(qResource.level.eq(level));
        }
        if (Objects.nonNull(state)) {
            builder.and(qResource.status.eq(state));
        }
        if (Objects.nonNull(type)) {
            builder.and(qResource.type.eq(type));
        }
        if (Objects.nonNull(fileType)) {
            builder.and(qResource.fileType.eq(fileType));
        }
        Page<Resource> page = resourceRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(page);
    }
}
