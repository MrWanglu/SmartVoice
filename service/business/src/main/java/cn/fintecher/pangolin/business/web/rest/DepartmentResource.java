package cn.fintecher.pangolin.business.web.rest;

import cn.fintecher.pangolin.business.repository.DepartmentRepository;
import cn.fintecher.pangolin.business.service.DepartmentService;
import cn.fintecher.pangolin.entity.Department;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Created by luqiang on 2017/8/10.
 */
@RestController
@RequestMapping("/api/departmentResource")
@Api(value = "", description = "组织机构简单查询")
public class DepartmentResource {

    private final Logger log = LoggerFactory.getLogger(DepartmentResource.class);
    @Inject
    private DepartmentService departmentService;
    @Inject
    private DepartmentRepository departmentRepository;

    @GetMapping("/getDepartmentById")
    @ApiOperation(value = "查找机构通过id", notes = "查找机构通过id")
    public ResponseEntity<Department> getDepartmentById(@RequestParam(value = "deptId") String deptId) {
        log.debug("REST request to get Department : {}", deptId);
        Department department = departmentRepository.findOne(deptId);
        return Optional.ofNullable(department)
                .map(result -> new ResponseEntity<>(
                        result,
                        HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
