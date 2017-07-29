package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.ResourceRepository;
import cn.fintecher.pangolin.business.repository.RoleRepository;
import cn.fintecher.pangolin.business.repository.UserRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.Status;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.*;
import org.apache.commons.collections4.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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
     * @Description : 带条件的分页查询
     */
    @GetMapping(value = "/getAllRolePage")
    @ResponseBody
    @ApiOperation(value = "带条件的分页查询", notes = "带条件的分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<Role>> getAllRolePage(@RequestParam(required = false) String companyCode,
                                                     @RequestParam(required = false) String name,
                                                     @RequestParam(required = false) Integer status,
                                                     @RequestParam(required = false) String operator,
                                                     @ApiIgnore Pageable pageable) {
        logger.debug("REST request to get all Role");
        QRole qRole = QRole.role;
        BooleanBuilder builder = new BooleanBuilder();
        if (Objects.nonNull(companyCode)) {
            builder.and(qRole.companyCode.eq(companyCode));
        }
        if (Objects.nonNull(name)) {
            builder.and(qRole.name.like(name.concat("%")));
        }
        if (Objects.nonNull(status)) {
            builder.and(qRole.status.eq(status));
        }
        if (Objects.nonNull(operator)) {
            builder.and(qRole.operator.like(operator.concat("%")));
        }
        Page<Role> page = roleRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "operate successfully", "操作成功")).body(page);
    }

    /**
     * @Description : 增加角色
     */
    @PostMapping("/createRole")
    @ApiOperation(value = "增加角色", notes = "增加角色")
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
        boolean exist = roleRepository.exists(qRole.name.eq(role.getName()).and(qRole.companyCode.eq(role.getCompanyCode())));
        if (exist) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The role name has been occupied", "该角色名已被占用")).body(null);
        } else {
            role.setOperator(user.getUserName());
            role.setOperateTime(ZWDateUtil.getNowDateTime());
            Role role1 = roleRepository.save(role);
            return ResponseEntity.ok().body(role1);
        }
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
        //更新角色的公司code码需要前端传
        //判断角色的名称是否重复
        QRole qRole = QRole.role;
        boolean exists = roleRepository.exists(qRole.id.ne(role.getId()).and(qRole.name.eq(role.getName())).and(qRole.companyCode.eq(user.getCompanyCode())));
        if (exists) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The role name has been occupied", "该角色名已被占用")).body(null);
        } else {
            //判断角色的状态
            if (Objects.equals(Status.Disable.getValue(), role.getStatus())) {
                QUser qUser = QUser.user;
                if (userRepository.exists(qUser.roles.any().id.eq(role.getId()))) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The role of related user, please delete the user", "该角色下有关联的用户，请先删除用户")).body(null);
                } else {
                    //解除角色与资源的关系
                    roleRepository.deleteResoByRoleId(role.getId());
                    Role roleNew = roleRepository.save(role);
                    return ResponseEntity.ok().body(roleNew);
                }
            } else {
                Role role1 = roleRepository.save(role);
                return ResponseEntity.ok().body(role1);
            }
        }
    }

    /**
     * @Description : 查找角色通过id
     */
    @GetMapping("/getRole")
    @ApiOperation(value = "查找角色通过id", notes = "查找角色通过id")
    public ResponseEntity<Role> getRole(@ApiParam(value = "角色id", required = true) @RequestParam(value = "id") String id) {
        try {
            Role role = roleRepository.findOne(id);
            return ResponseEntity.ok().body(role);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取失败", ENTITY_NAME, e.getMessage())).body(null);
        }
    }

    /**
     * @Description : 角色查找资源
     */
    @GetMapping("/getRoleRes")
    @ApiOperation(value = "角色查找资源", notes = "角色查找资源")
    public ResponseEntity<List<Resource>> getRoleRes(@RequestParam(required = false) String id,
                                                     @RequestParam(required = false) String companyCode) {
        try {
            QResource qResource = QResource.resource;
            BooleanBuilder builder = new BooleanBuilder();
            if (Objects.nonNull(id)) {
                builder.and(qResource.roles.any().id.eq(id));
            }
            if (Objects.nonNull(companyCode)) {
                builder.and(qResource.roles.any().companyCode.eq(companyCode));
            }
            Iterator<Resource> resources = resourceRepository.findAll(builder).iterator();
            List<Resource> resourceList = IteratorUtils.toList(resources);
            return ResponseEntity.ok().body(resourceList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createEntityCreationAlert("未查找到资源", ENTITY_NAME)).body(null);
        }
    }

    /**
     * @Description : 角色查找用户分页
     */
    @GetMapping("/roleFindUsers")
    @ApiOperation(value = "角色查找用户分页", notes = "角色查找用户分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<User>> roleFindUsers(@RequestParam String id,
                                                    @RequestParam(required = false) String name,
                                                    @RequestParam(required = false) Integer status,
                                                    @RequestParam(required = false) String companyCode,
                                                    @ApiIgnore Pageable pageable) {

        Role role = roleRepository.findOne(id);
        if (Objects.nonNull(role)) {
            QUser qUser = QUser.user;
            BooleanBuilder builder = new BooleanBuilder();
            if (Objects.nonNull(id)) {
                builder.and(qUser.roles.any().id.eq(id));
            }
            if (Objects.nonNull(name)) {
                builder.and(qUser.roles.any().name.like(name.concat("%")));
            }
            if (Objects.nonNull(status)) {
                builder.and(qUser.roles.any().status.eq(status));
            }
            if (Objects.nonNull(companyCode)) {
                builder.and(qUser.roles.any().companyCode.eq(companyCode));
            }
            Page<User> page = userRepository.findAll(builder, pageable);
            return ResponseEntity.ok().body(page);
        }
        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                "The role does not exist", "该角色不存在")).body(null);
    }

    /**
     * @Description : 删除角色
     */
    @DeleteMapping("/deleteRole")
    @ApiOperation(value = "删除角色", notes = "删除角色")
    public ResponseEntity<Role> deleteRole(@ApiParam(value = "角色id", required = true) @RequestParam String id,
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
        boolean exist = userRepository.exists(qUser.roles.any().id.eq(id).and(qUser.companyCode.eq(role.getCompanyCode())));
        if (exist) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "The role has an associated user, please delete the relationship with the user first", "该角色下有关联用户，请先删除与用户的关系")).body(null);
        }
        //解除角色与资源的关系
        roleRepository.deleteResoByRoleId(role.getId());
        Role roleNew = roleRepository.save(role);
        roleRepository.delete(id);
        return ResponseEntity.ok().body(null);
    }

    /**
     * @Description : 查询所有角色分页
     */
    @GetMapping("/findAllRole")
    @ApiOperation(value = "查询所有角色分页", notes = "查询所有角色分页")
    public ResponseEntity<Page<Role>> getAllRole(@ApiIgnore Pageable pageable) {
        logger.debug("REST request to get all of Role");
        try {
            Page<Role> page = roleRepository.findAll(pageable);
            return new ResponseEntity<>(page, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createEntityCreationAlert("查找失败", ENTITY_NAME)).body(null);
        }
    }
}
