package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.ResourceRepository;
import cn.fintecher.pangolin.business.repository.RoleRepository;
import cn.fintecher.pangolin.business.repository.UserRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.Status;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.net.URISyntaxException;
import java.util.ArrayList;
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
    public ResponseEntity<Page<Role>> getAllRolePage(@QuerydslPredicate(root = Principal.class) Predicate predicate,
                                                     @ApiIgnore Pageable pageable) throws URISyntaxException {
        logger.debug("REST request to get all Role");
        Page<Role> page = roleRepository.findAll(predicate, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/roleController/getAllRolePage");
        return new ResponseEntity<>(page, headers, HttpStatus.OK);
    }


    @PostMapping("/createRole")
    @ApiOperation(value = "增加角色", notes = "增加角色")
    public ResponseEntity<Role> createRole(@Validated @ApiParam("角色对象") @RequestBody Role role,
                                           @RequestHeader(value = "X-UserToken") String token) throws URISyntaxException {
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
        Iterator<Role> roles = roleRepository.findAll(qRole.name.eq(role.getName()).and(qRole.companyCode.eq(user.getCompanyCode()))).iterator();
        if (roles.hasNext()){
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The role name has been occupied", "该角色名已被占用")).body(null);
        }else {
            role.setOperator(user.getUserName());
            role.setOperateTime(ZWDateUtil.getNowDateTime());
            Role role1 = roleRepository.save(role);
            return ResponseEntity.ok().body(role1);
        }
    }


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
        if (exists){
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The role name has been occupied", "该角色名已被占用")).body(null);
        }else {
            //判断角色的状态
            if (Objects.equals(Status.Disable.getValue(), role.getStatus())) {
                QUser qUser = QUser.user;
                Iterable<User> users = userRepository.findAll(qUser.roles.any().id.eq(role.getId()));
                if (users.iterator().hasNext()){
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The role of related user, please delete the user", "该角色下有关联的用户，请先删除用户")).body(null);
                }else {
                    Role role1 = roleRepository.findOne(role.getId());
                    Iterable<Resource> resources = resourceRepository.findAll(QResource.resource.roles.contains(role1));
                    resources.forEach(e->{
                        e.getRoles().remove(role1);
                        resourceRepository.saveAndFlush(e);
                    });
                    Role roleNew = roleRepository.save(role);
                    return ResponseEntity.ok().body(roleNew);
                }
            } else {
                Role role1 = roleRepository.save(role);
                return ResponseEntity.ok().body(role1);
            }
        }
    }


    @GetMapping("/getRole")
    @ApiOperation(value = "查找角色通过id", notes = "查找角色通过id")
    public ResponseEntity<Role> getRole(@ApiParam(value = "角色id", required = true) @RequestParam(value = "id") String id) throws URISyntaxException {
        try {
        Role role = roleRepository.findOne(id);
        return ResponseEntity.ok().body(role);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("获取失败", ENTITY_NAME, e.getMessage())).body(null);
        }
    }

    @GetMapping("/getRoleRes")
    @ApiOperation(value = "角色查找资源", notes = "角色查找资源")
    public ResponseEntity<List<Resource>> getRoleRes(@ApiParam(value = "角色id", required = true) @RequestParam(value = "id") String id,
                                                     @QuerydslPredicate(root = Role.class) Predicate predicate) throws URISyntaxException {
        try {
        QResource qResource = QResource.resource;
        Iterator<Resource> resources = resourceRepository.findAll(qResource.roles.any().id.eq(id)).iterator();
        List<Resource> resourceList = new ArrayList<Resource>();
        while (resources.hasNext()) {
            resourceList.add(resources.next());
        }
        return ResponseEntity.ok().body(resourceList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createEntityCreationAlert("未查找到资源", ENTITY_NAME)).body(null);
        }
    }


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
    public ResponseEntity<Page<User>> roleFindUsers(@ApiParam(value = "角色id", required = true) @RequestParam String id,
                                                    @ApiIgnore Pageable pageable) throws URISyntaxException {

        Role role = roleRepository.findOne(id);
        if (Objects.nonNull(role)) {
            QUser qUser = QUser.user;
            Page<User> userPage = userRepository.findAll(qUser.roles.any().id.eq(id).and(qUser.companyCode.eq(role.getCompanyCode())), pageable);
            return ResponseEntity.ok().body(userPage);
        }
        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                "The role does not exist", "该角色不存在")).body(null);
    }

    @DeleteMapping("/deleteRole")
    @ApiOperation(value = "删除角色", notes = "删除角色")
    public ResponseEntity<Role> deleteRole(@ApiParam(value = "角色id", required = true) @RequestParam String id,
                                           @RequestHeader(value = "X-UserToken") String token) throws URISyntaxException {
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
        Iterable<User> userIterator = userRepository.findAll(qUser.roles.any().id.eq(id).and(qUser.companyCode.eq(role.getCompanyCode())));
        if (userIterator.iterator().hasNext()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "The role has an associated user, please delete the relationship with the user first", "该角色下有关联用户，请先删除与用户的关系")).body(null);
        }
        Iterable<Resource> resources = resourceRepository.findAll(QResource.resource.roles.contains(role));
        resources.forEach(e->{
            e.getRoles().remove(role);
            resourceRepository.saveAndFlush(e);
        });
        roleRepository.delete(id);
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/findAllRole")
    @ApiOperation(value = "查询所有角色分页", notes = "查询所有角色分页")
    public ResponseEntity<Page<Role>> getAllRole(@ApiIgnore Pageable pageable) throws URISyntaxException {
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
