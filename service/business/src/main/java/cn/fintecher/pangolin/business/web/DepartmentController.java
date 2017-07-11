package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.repository.DepartmentRepository;
import cn.fintecher.pangolin.entity.Department;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * Created by ChenChang on 2017/5/23.
 */
@RestController
@RequestMapping("/api")
public class DepartmentController {

    private static final String ENTITY_NAME = "department";
    private final Logger log = LoggerFactory.getLogger(DepartmentController.class);
    private final DepartmentRepository departmentRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }


    @PostMapping("/department")
    public ResponseEntity<Department> createDepartment(@RequestBody Department department) throws URISyntaxException {
        log.debug("REST request to save department : {}", department);
        if (department.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "新增案件不应该含有ID")).body(null);
        }
        Department result = departmentRepository.save(department);
        return ResponseEntity.created(new URI("/api/department/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    @PutMapping("/department")
    public ResponseEntity<Department> updateDepartment(@RequestBody Department department) throws URISyntaxException {
        log.debug("REST request to update Department : {}", department);
        if (department.getId() == null) {
            return createDepartment(department);
        }
        Department result = departmentRepository.save(department);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, department.getId().toString()))
                .body(result);
    }

    @GetMapping("/department")
    public List<Department> getAllDepartment() {
        log.debug("REST request to get all Department");
        List<Department> departmentList = departmentRepository.findAll();
        return departmentList;
    }

    @GetMapping("/department/{id}")
    public ResponseEntity<Department> getDepartment(@PathVariable String id) {
        log.debug("REST request to get department : {}", id);
        Department department = departmentRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(department));
    }

    @DeleteMapping("/department/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable String id) {
        log.debug("REST request to delete department : {}", id);
        departmentRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id)).build();
    }

}
