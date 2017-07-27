package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.repository.DepartmentRepository;
import cn.fintecher.pangolin.entity.Department;
import cn.fintecher.pangolin.entity.QDepartment;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-27-9:50
 */
@Service("departmentService")
public class DepartmentService {
    final Logger log = LoggerFactory.getLogger(DepartmentService.class);
    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * @Description :查询用户的子部门
     */
    public List<Department> querySonDepartment(User user) {
        //超级管理员
        if (Objects.equals(Constants.ADMINISTRATOR_ID, user.getId())) {
            List<Department> departmentList = new ArrayList<>();
            QDepartment qDepartment = QDepartment.department;
            Iterator<Department> departments = departmentRepository.findAll(qDepartment.code.like(user.getDepartment().getCode().concat("%")).and(qDepartment.id.ne(user.getDepartment().getId()))).iterator();
            while (departments.hasNext()) {
                departmentList.add(departments.next());
            }
            return departmentList;
        }
        //普通管理员的方法
        List<Department> departmentList = new ArrayList<>();
        QDepartment qDepartment = QDepartment.department;
        Iterator<Department> departments = departmentRepository.findAll(qDepartment.code.like(user.getDepartment().getCode().concat("%")).and(qDepartment.id.ne(user.getDepartment().getId())).and(qDepartment.companyCode.eq(user.getCompanyCode()))).iterator();
        while (departments.hasNext()) {
            departmentList.add(departments.next());
        }
        return departmentList;
    }
}
