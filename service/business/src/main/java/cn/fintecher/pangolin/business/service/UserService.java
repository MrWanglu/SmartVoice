package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.repository.DepartmentRepository;
import cn.fintecher.pangolin.business.repository.UserRepository;
import cn.fintecher.pangolin.entity.Department;
import cn.fintecher.pangolin.entity.QUser;
import cn.fintecher.pangolin.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author : Administrator
 * @Description :
 * @Date : 17:43 2017/7/20
 */
@Service("userService")
public class UserService {
    final Logger log = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    public List<User> getAllUser(String id, Integer state) {
        Department department = departmentRepository.findOne(id);
        QUser qUser = QUser.user;
        Iterator<User> userList;
        if (Objects.isNull(state)) {
            userList = userRepository.findAll(qUser.department.code.like(department.getCode().concat("%")).and(qUser.companyCode.eq(department.getCompanyCode()))).iterator();
        } else {
            userList = userRepository.findAll(qUser.department.code.like(department.getCode().concat("%")).and(qUser.department.status.eq(state)).and(qUser.companyCode.eq(department.getCompanyCode()))).iterator();
        }
        List<User> userReturn = new ArrayList<User>();
        //转成list
        while (userList.hasNext()) {
            userReturn.add(userList.next());
        }
        return userReturn;
    }

}