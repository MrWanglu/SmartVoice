package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.ResourceRepository;
import cn.fintecher.pangolin.business.repository.RoleRepository;
import cn.fintecher.pangolin.business.repository.UserRepository;
import cn.fintecher.pangolin.entity.Resource;
import cn.fintecher.pangolin.entity.Role;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.Status;
import cn.fintecher.pangolin.util.ZWDateUtil;
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

import java.util.*;

/**
 * @Author: PeiShouWen
 * @Description:角色信息管理
 * @Date 14:51 2017/7/14
 */
@RestController
@RequestMapping("/api/roleController")
@Api(value = "角色资源管理", description = "角色资源管理")
public class RoleController extends BaseController {
    private static final String ENTITY_NAME = "Role";
    private final Logger logger = LoggerFactory.getLogger(RoleController.class);
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ResourceRepository resourceRepository;
    @Autowired
    private UserRepository userRepository;

    /**
     * @Description : 查询所有角色
     */
    @GetMapping(value = "/query")
    @ResponseBody
    @ApiOperation(value = "查询所有角色分页", notes = "查询所有角色分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<Role>> query(@RequestParam(required = false) String realName,
                                            @RequestParam(required = false) Integer state,
                                            @ApiIgnore Pageable pageable,
                                            @RequestHeader(value = "X-UserToken") String token) {
        logger.debug("REST request to get all Role");
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        QRole qRole = QRole.role;
        BooleanBuilder builder = new BooleanBuilder();
        if (Objects.nonNull(realName)) {
            builder.and(qRole.name.like(realName.concat("%")));
        }
        if (Objects.nonNull(state)) {
            builder.and(qRole.status.eq(state));
        }
        if (Objects.nonNull(user.getCompanyCode())) {
            builder.and(qRole.companyCode.eq(user.getCompanyCode()));
        }
        Page<Role> page = roleRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(page);
    }

    /**
     * @Description : 新建角色
     */
    @PostMapping("/createRole")
    @ApiOperation(value = "新建角色", notes = "新建角色")
    public ResponseEntity<Role> createRole(@Validated @ApiParam("角色对象") @RequestBody Role role,
                                           @RequestHeader(value = "X-UserToken") String token) {
        logger.debug("REST request to save caseInfo : {}", role);
        if (role.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "idexists", "新增不应该含有ID")).body(null);
        }
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        //增加角色的code需要传入
        QRole qRole = QRole.role;
        Iterator<Role> roles = roleRepository.findAll(qRole.name.eq(role.getName()).and(qRole.companyCode.eq(role.getCompanyCode()))).iterator();
        if (roles.hasNext()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The role name has been occupied", "该角色名已被占用")).body(roles.next());
        }
        role.setOperator(user.getUserName());
        role.setOperateTime(ZWDateUtil.getNowDateTime());
        Role role1 = roleRepository.save(role);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(role1);
    }

    /**
     * @Description : 更新角色
     */
    @PostMapping("/updateRole")
    @ApiOperation(value = "更新角色", notes = "更新角色")
    public ResponseEntity<Role> updateRole(@Validated @ApiParam("需更新的角色对象") @RequestBody Role role,
                                           @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        if (Objects.equals(Constants.ADMIN_ROLE_ID, role.getId())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The role can not be modified", "该角色不能被修改")).body(null);
        }
        //判断角色的名称是否重复
        QRole qRole = QRole.role;
        Iterator<Role> roles = roleRepository.findAll(qRole.id.ne(role.getId()).and(qRole.name.eq(role.getName())).and(qRole.companyCode.eq(role.getCompanyCode()))).iterator();
        if (roles.hasNext()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The role name has been occupied", "该角色名已被占用")).body(roles.next());
        }
        //判断角色的状态
        if (Objects.equals(Status.Disable.getValue(), role.getStatus())) {
            QUser qUser = QUser.user;
            List<User> userList = new ArrayList<>();
            Iterator<User> users = userRepository.findAll(qUser.roles.any().id.eq(role.getId())).iterator();
            while (users.hasNext()) {
                userList.add(users.next());
            }
            if (0 != userList.size()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The role of related user, please delete the user", "该角色关联" + userList.size() + "个用户，请先删除用户")).body(null);
            }
            role.setResources(new HashSet<>());
            Role role1 = roleRepository.save(role);
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(role1);
        } else {
            Role role1 = roleRepository.save(role);
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(role1);
        }
    }

    /**
     * @Description : 查找角色通过id
     */

    @GetMapping("/getRoleById")
    @ApiOperation(value = "查找角色通过id", notes = "查找角色通过id")
    public ResponseEntity<Role> getRoleById(@RequestParam(required = false) String id) {
        Role role = roleRepository.findOne(id);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(role);
    }

    /**
     * @Description : 角色查找资源
     */

    @GetMapping("/getResoByRole")
    @ApiOperation(value = "角色查找资源", notes = "角色查找资源")
    public ResponseEntity<List<Resource>> getResoByRole(@RequestParam String id,
                                                        @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        QResource qResource = QResource.resource;
        List<Resource> resourceList = new ArrayList<>();
        Iterator<Resource> resources = resourceRepository.findAll(qResource.roles.any().id.eq(id)).iterator();
        while (resources.hasNext()) {
            resourceList.add(resources.next());
        }
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(resourceList);
    }

    /**
     * @Description : 删除角色
     */
    @DeleteMapping("/deleteRole")
    @ApiOperation(value = "删除角色", notes = "删除角色")
    public ResponseEntity<Role> deleteRole(@RequestParam String id,
                                           @RequestHeader(value = "X-UserToken") String token) {
        logger.debug("REST request to save Role : {}", id);
        User userToken;
        try {
            userToken = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        if (Objects.equals(Constants.ADMINISTRATOR_ID, id)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "The superadministrator is not allowed to delete", "超级管理员角色不允许删除")).body(null);
        }
        Role role = roleRepository.findOne(id);
        QUser qUser = QUser.user;
        List<User> userList = new ArrayList<>();
        Iterator<User> users = userRepository.findAll(qUser.roles.any().id.eq(id).and(qUser.companyCode.eq(role.getCompanyCode()))).iterator();
        while (users.hasNext()) {
            userList.add(users.next());
        }
        if (0 != userList.size()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "The role has an associated user, please delete the relationship with the user first", "该角色关联" + userList.size() + "个用户，请先删除与用户的关系")).body(null);
        }
        role.setResources(new HashSet<>());
        Role role1 = roleRepository.save(role);
        roleRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(role1);
    }

    /**
     * @Description : 查询所有角色不分页
     */
    @GetMapping("/getRoleNoPage")
    @ApiOperation(value = "查询所有角色不分页", notes = "查询所有角色不分页")
    public ResponseEntity<List<Role>> getRoleNoPage(@RequestHeader(value = "X-UserToken") String token) {
        logger.debug("REST request to get all of Role");
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        QRole qRole = QRole.role;
        List<Role> roleList = new ArrayList<>();
        BooleanBuilder builder = new BooleanBuilder();
        if (Objects.nonNull(user.getCompanyCode())) {
            builder.and(qRole.companyCode.eq(user.getCompanyCode()));
        }
        Iterator<Role> roles = roleRepository.findAll(builder).iterator();
        while (roles.hasNext()) {
            roleList.add(roles.next());
        }
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(roleList);
    }
}
