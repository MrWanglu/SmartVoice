package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.AssistingStatisticsModel;
import cn.fintecher.pangolin.business.model.CollectionCaseModel;
import cn.fintecher.pangolin.business.repository.DepartmentRepository;
import cn.fintecher.pangolin.business.repository.UserRepository;
import cn.fintecher.pangolin.business.service.CaseAssistService;
import cn.fintecher.pangolin.business.service.CaseInfoService;
import cn.fintecher.pangolin.business.service.DataDictService;
import cn.fintecher.pangolin.business.service.UserService;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.ShortUUID;
import cn.fintecher.pangolin.entity.util.Status;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.apache.commons.collections4.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Created by ChenChang on 2017/5/23.
 */
@RestController
@RequestMapping("/api/departmentController")
@Api(value = "部门信息管理", description = "部门信息管理")
public class DepartmentController extends BaseController {
    private final Logger log = LoggerFactory.getLogger(DepartmentController.class);
    private static final String ENTITY_NAME = "Department";
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    DataDictService dataDictService;
    @Autowired
    CaseInfoService caseInfoService;
    @Autowired
    CaseAssistService caseAssistService;

    /**
     * @Description : 组织机构的type属性
     */
    @GetMapping("/getDepartmentType")
    @ApiOperation(value = "组织机构的type属性", notes = "组织机构的type属性")
    public ResponseEntity<List<DataDict>> getDepartmentType() {
        List<DataDict> dataDictList = dataDictService.getDataDictByTypeCode("0001");
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(dataDictList);
    }

    /**
     * @Description : 组织机构的level属性
     */
    @ApiOperation(value = "组织机构的level属性", notes = "组织机构的level属性")
    @GetMapping("/getDepartmentLevel")
    public ResponseEntity<List<DataDict>> getDepartmentLevel() {
        List<DataDict> dataDictList = dataDictService.getDataDictByTypeCode("0002");
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(dataDictList);
    }

    /**
     * @Description : 增加部门
     */
    @PostMapping("/createDepartment")
    @ApiOperation(value = "增加部门", notes = "增加部门")
    public ResponseEntity<Department> createDepartment(@RequestBody Department department,
                                                       @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to save department : {}", department);
        if (department.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "新增部门不应该含有ID")).body(null);
        }
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        //判断公司的名称是否重复
        QDepartment qDepartment = QDepartment.department;
        boolean exist = departmentRepository.exists(qDepartment.name.eq(department.getName()).and(qDepartment.companyCode.eq(department.getCompanyCode())));
        if (exist) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The department name has been occupied", "该部门名称已被占用,请重新输入名称")).body(null);
        }
        if (Objects.equals(Status.Disable.getValue(), department.getParent().getStatus())) {
            department.setStatus(Status.Disable.getValue());
        }
        //administrator添加部门比较特殊  没有父部门  并且公司的code需要从前端传入
        if (Objects.isNull(department.getParent().getCode())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Department code cannot be empty", "父部门的code为空")).body(null);
        } else {
            department.setCode(department.getParent().getCode() + "_" + ShortUUID.generateShortUuid());
        }
        if (Objects.nonNull(department.getParent().getType())) {
            department.setType(department.getParent().getType());
        }
        if (Objects.isNull(department.getParent().getLevel())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Department level cannot be empty", "部门级别不能为空")).body(null);
        } else {
            department.setLevel(department.getParent().getLevel() + 1);
        }
        department.setOperator(user.getUserName());
        department.setOperateTime(ZWDateUtil.getNowDateTime());
        Department result = departmentRepository.save(department);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(result);
    }

    /**
     * @Description : 修改部门
     */
    @PostMapping("/updateDepartment")
    @ApiOperation(value = "修改部门", notes = "修改部门")
    public ResponseEntity<Department> updateDepartment(@RequestBody Department department, @RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to update Department : {}", department);
        if (department.getId() == null) {
            return createDepartment(department, token);
        }
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        Department dept = departmentRepository.findOne(department.getId());
        //判断公司的名称是否重复
        QDepartment qDepartment = QDepartment.department;
        boolean exist = departmentRepository.exists(qDepartment.name.eq(department.getName()).and(qDepartment.companyCode.eq(department.getCompanyCode())).and(qDepartment.id.ne(department.getId())));
        if (exist) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The department name has been occupied", "该部门名称已被占用,请重新输入名称")).body(null);
        }
        if (!(Objects.equals(department.getParent().getType(), department.getType())) && Objects.nonNull(department.getParent().getType())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The type is inconsistent", "父子机构类型不一致,不能修改")).body(null);
        }
        //status  状态 Eable(0)启用 Disable(1) 停用  机构的状态改变
        if (!(Objects.equals(department.getStatus(), dept.getStatus()))) {
            //状态由停用变为启用
            if (Objects.equals(Status.Enable.getValue(), department.getStatus())) {
                //找父是否为停用
                if (Objects.equals(Status.Enable.getValue(), department.getParent().getStatus())) {
                    Department deptNew = departmentRepository.save(department);
                    return ResponseEntity.ok().body(deptNew);
                } else {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The parent department for the disabled", "父部门为停用,请先修改父部门状态")).body(null);
                }
            }
            //状态由启用变为停用
            if (Objects.equals(Status.Disable.getValue(), department.getStatus())) {
                //机构关联的电催和外访的案件数
                CollectionCaseModel collectionCaseModel = caseInfoService.haveCollectionCase(dept);
                if (Objects.nonNull(collectionCaseModel)) {
                    int number = collectionCaseModel.getNum();
                    if (0 != number) {
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Department level cannot be empty", "该机构下关联" + number + "个未处理的案件，不能停用，请先处理完该机构下的案件")).body(null);
                    }
                }
                //机构下关联的协催正在催收的案件
                AssistingStatisticsModel assistingStatisticsMode = caseAssistService.getDepartmentCollectingAssist(dept);
                if (Objects.nonNull(assistingStatisticsMode)) {
                    int num = assistingStatisticsMode.getNum();
                    if (0 != num) {
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Department level cannot be empty", "该机构下关联" + num + "个未处理的协催案件，不能停用，请先处理完该机构下的案件")).body(null);
                    }
                }
                //首先的移除部门下面的用户
                QUser qUser = QUser.user;
                int usersNum = (int) userRepository.count(qUser.department.code.like(dept.getCode().concat("%")));
                if (usersNum > 0) {
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Under the department has a user cannot stop", "该部门下有" + usersNum + "个用户,不能停用,请先移出用户")).body(null);
                }
                //子机构状态
                Iterator<Department> departments = departmentRepository.findAll(qDepartment.code.like(department.getCode().concat("%")).and(qDepartment.companyCode.eq(department.getCompanyCode())).and(qDepartment.id.ne(department.getId()))).iterator();
                List<Department> departmentList = IteratorUtils.toList(departments);
                for (Department department1 : departmentList) {
                    if (Objects.equals(Status.Enable.getValue(), department1.getStatus())) {
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Setup status to stop using, please modify child institutions", "子机构状态为启用，请先修改子机构状态")).body(null);
                    }
                }
                Department dept1 = departmentRepository.save(department);
                return ResponseEntity.ok().body(dept1);
            }
        }
        Department result = departmentRepository.save(department);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(result);
    }

    /**
     * @Description :删除部门
     */
    @DeleteMapping("/deleteDepartment")
    @ApiOperation(value = "删除部门", notes = "删除部门")
    public ResponseEntity<Void> deleteDepartment(@RequestParam String id) {
        log.debug("REST request to delete department : {}", id);
        Department department = departmentRepository.findOne(id);
        if (Objects.isNull(department)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The agency does not exist", "该机构不存在")).body(null);
        }
        //机构关联的案件数
        CollectionCaseModel collectionCaseModel = caseInfoService.haveCollectionCase(department);
        if (Objects.nonNull(collectionCaseModel)) {
            int number = collectionCaseModel.getNum();
            if (0 != number) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Department level cannot be empty", "该机构下关联" + number + "个未处理的案件，不能删除，请先处理完该机构下的案件")).body(null);
            }
        }
        //机构下关联的协催正在催收的案件
        AssistingStatisticsModel assistingStatisticsMode = caseAssistService.getDepartmentCollectingAssist(department);
        if (Objects.nonNull(assistingStatisticsMode)) {
            int num = assistingStatisticsMode.getNum();
            if (0 != num) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Department level cannot be empty", "该机构下关联" + num + "个未处理的协催案件，不能停用，请先处理完该机构下的案件")).body(null);
            }
        }
        //首先的移除部门下面的用户
        QUser qUser = QUser.user;
        int usersNum = (int) userRepository.count(qUser.department.code.like(department.getCode().concat("%")).and(qUser.companyCode.eq(department.getCompanyCode())));
        if (usersNum > 0) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Under the department has a user cannot stop", "该部门下有" + usersNum + "个用户,不能停用,请先移出用户")).body(null);
        }
        if (usersNum > 0) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Under the department has a user cannot delete", "该部门下有" + usersNum + "个用户不能删除")).body(null);
        }
        //子机构数量
        QDepartment qDepartment = QDepartment.department;
        int deptNum = (int) departmentRepository.count(qDepartment.code.like(department.getCode().concat("%")).and(qDepartment.id.ne(department.getId())).and(qDepartment.companyCode.eq(department.getCompanyCode())));
        if (deptNum > 0) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The department is following agencies cannot be deleted", "该部门下子机构不能删除")).body(null);
        }
        departmentRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id)).build();
    }

    /**
     * @Description :查询用户所在的子部门
     */
    @GetMapping("/queryDepartment")
    @ApiOperation(value = "查询用户所在的子部门", notes = "查询用户所在的子部门")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<Department>> queryCaseInfo(@QuerydslPredicate(root = Department.class) Predicate predicate,
                                                          @ApiIgnore Pageable pageable,
                                                          @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        BooleanBuilder builder = new BooleanBuilder(predicate);
        if (Objects.nonNull(user.getCompanyCode())) {
            builder.and(QDepartment.department.companyCode.like(user.getCompanyCode().concat("%")));
        }
        Page<Department> page = departmentRepository.findAll(builder, pageable);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(page);
    }

    /**
     * @Description :查询部门通过id
     */
    @GetMapping("/department/{id}")
    @ApiOperation(value = "查询部门通过id", notes = "查询部门通过id")
    public ResponseEntity<Department> getDepartment(@PathVariable String id) {
        log.debug("REST request to get department : {}", id);
        Department department = departmentRepository.findOne(id);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(department);
    }

    /**
     * @Description :查询用户所属部门及子部门
     */
    @GetMapping(value = "/queryOwnDepartment")
    @ApiOperation(value = "查询用户所属部门及子部门", notes = "查询用户所属部门及子部门")
    public ResponseEntity<List<Department>> queryOwnDepartment(@RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        List<Department> departmentList = new ArrayList<>();
        QDepartment qDepartment = QDepartment.department;
        Iterator<Department> departments = departmentRepository.findAll(qDepartment.code.like(user.getDepartment().getCode().concat("%"))).iterator();
        while (departments.hasNext()) {
            departmentList.add(departments.next());
        }
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(departmentList);
    }

    /**
     * @Description :查询所属公司的部门
     */
    @GetMapping(value = "/queryAllDepartment")
    @ApiOperation(value = "查询所属公司的部门", notes = "查询所属公司的部门")
    public ResponseEntity<List<Department>> queryAllDepartment(@RequestParam(required = false) String companyCode,
                                                               @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        if (Objects.equals(Constants.ADMIN_USER_NAME, user.getUserName())) {
            if (Objects.isNull(companyCode)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "请输入公司code码")).body(null);
            } else {
                List<Department> departmentList = new ArrayList<>();
                QDepartment qDepartment = QDepartment.department;
                Iterator<Department> departments = departmentRepository.findAll(qDepartment.code.like(user.getDepartment().getCode().concat("%")).and(qDepartment.companyCode.eq(companyCode))).iterator();
                while (departments.hasNext()) {
                    departmentList.add(departments.next());
                }
                return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(departmentList);
            }
        }
        List<Department> departmentList = new ArrayList<>();
        QDepartment qDepartment = QDepartment.department;
        Iterator<Department> departments = departmentRepository.findAll(qDepartment.code.like(user.getDepartment().getCode().concat("%")).and(qDepartment.companyCode.eq(user.getCompanyCode()))).iterator();
        while (departments.hasNext()) {
            departmentList.add(departments.next());
        }
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(departmentList);
    }

    /**
     * @Description :移动组织机构
     */
    @ApiOperation(value = "移动组织机构", notes = "移动组织机构")
    @GetMapping("/moveDepartment")
    public ResponseEntity<Department> moveDepartment(@ApiParam(value = "要移动组织机构的id", required = true) @RequestParam String deptId,
                                                     @ApiParam(value = "移动到的组织机构的id", required = true) @RequestParam String parentDeptId) {
        if (Objects.isNull(deptId) || Objects.isNull(parentDeptId)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "参数不完整", "Incomplete parameter")).body(null);
        }
        //需要移动的机构
        Department dept = departmentRepository.findOne(deptId);
        //移动后的父机构
        Department deptParent = departmentRepository.findOne(parentDeptId);
        if (Objects.isNull(deptParent)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "There is no parent", "父机构不存在")).body(null);
        }
        if (Objects.equals(Status.Disable.getValue(), deptParent.getStatus())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The state of the parent department is disabled and cannot be moved", "父部门的状态为停用，不能移动")).body(null);
        }
        //如果子部门的类型和父部门一样 直接移动 部门的等级和code码需要变化
        if (Objects.equals(dept.getType(), deptParent.getType())) {
            dept.setLevel(deptParent.getLevel() + 1);
            dept.setCode(deptParent + "_" + ShortUUID.generateShortUuid());
            dept.setParent(deptParent);
            Department department = departmentRepository.save(dept);
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(department);
        }
        //如果子部门和父部门的类型不一致需要判断部门下的案件和用户
        //首先的移除部门下面的用户
        QUser qUser = QUser.user;
        int usersNum = (int) userRepository.count(qUser.department.code.like(dept.getCode().concat("%")).and(qUser.companyCode.eq(dept.getCompanyCode())));
        if (usersNum > 0) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Under the department has a user cannot stop", "该部门下有" + usersNum + "个用户,不能移动,请先移出用户")).body(null);
        }
        //子机构数量
        QDepartment qDepartment = QDepartment.department;
        int deptNum = (int) departmentRepository.count(qDepartment.code.like(dept.getCode().concat("%")).and(qDepartment.id.ne(dept.getId())).and(qUser.companyCode.eq(dept.getCompanyCode())));
        if (deptNum > 0) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The department is following agencies cannot be deleted", "该部门下子机构不能移动")).body(null);
        }
//机构关联的案件数
        CollectionCaseModel collectionCaseModel = caseInfoService.haveCollectionCase(dept);
        if (Objects.nonNull(collectionCaseModel)) {
            int number = collectionCaseModel.getNum();
            if (0 != number) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Department level cannot be empty", "该机构下关联" + number + "个未处理的案件，不能移动，请先处理完该机构下的案件")).body(null);
            }
        }
        //机构下关联的协催正在催收的案件
        AssistingStatisticsModel assistingStatisticsMode = caseAssistService.getDepartmentCollectingAssist(dept);
        if (Objects.nonNull(assistingStatisticsMode)) {
            int num = assistingStatisticsMode.getNum();
            if (0 != num) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Department level cannot be empty", "该机构下关联" + num + "个未处理的协催案件，不能移动，请先处理完该机构下的案件")).body(null);
            }
        }
        dept.setType(deptParent.getType());
        dept.setLevel(deptParent.getLevel() + 1);
        dept.setCode(deptParent + "_" + ShortUUID.generateShortUuid());
        dept.setParent(deptParent);
        Department department = departmentRepository.save(dept);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(department);
    }

    /**
     * @Description :查询公司下的电催或者外访机构  1 电催  2 外访
     */
    @RequestMapping(value = "/querySubdivision", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    @ApiOperation(value = "查询公司下的电催或者外访机构", notes = "查询公司下的电催或者外访机构")
    public ResponseEntity<List<Department>> querySubdivision(@RequestParam String companyCode,
                                                             @RequestParam Integer type) {
        QDepartment qDepartment = QDepartment.department;
        BooleanBuilder builder = new BooleanBuilder();
        if (Objects.nonNull(companyCode)) {
            builder.and(qDepartment.companyCode.eq(companyCode));
        }
        if (Objects.nonNull(companyCode)) {
            builder.and(qDepartment.type.eq(type).or(qDepartment.type.isNull()));
        }
        Iterator<Department> departments = departmentRepository.findAll(builder).iterator();
        List<Department> departmentList = IteratorUtils.toList(departments);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "invented successfully", "获取成功")).body(departmentList);
    }
}
