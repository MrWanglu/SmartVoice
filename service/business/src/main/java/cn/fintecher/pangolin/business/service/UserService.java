package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.repository.DepartmentRepository;
import cn.fintecher.pangolin.business.repository.UserRepository;
import cn.fintecher.pangolin.entity.Department;
import cn.fintecher.pangolin.entity.QUser;
import cn.fintecher.pangolin.entity.User;
import com.querydsl.core.BooleanBuilder;
import org.apache.commons.collections4.IteratorUtils;
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

    /**
     * @Description : 得到特定公司部门下的用户 id 部门的id  state 用户的状态
     */
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

    /**
     * @Description : 得到外访的主管 type 用户类型   state 用户的状态   companyCode 公司code   manager 是否管理者  0 是  1 否
     */
    public List<User> getAllUser(String companyCode, Integer type, Integer state,Integer manager) {
        QUser qUser = QUser.user;
        BooleanBuilder builder = new BooleanBuilder();
        if (Objects.nonNull(companyCode)) {
            builder.and(qUser.companyCode.eq(companyCode));
        }
        if (Objects.nonNull(type)) {
            builder.and(qUser.type.eq(type));
        }
        if (Objects.nonNull(state)) {
            builder.and(qUser.status.eq(state));
        }
        if (Objects.nonNull(manager)) {
            builder.and(qUser.manager.eq(manager));
        }
        Iterator<User> userList = userRepository.findAll(builder).iterator();
        List<User> userReturn = IteratorUtils.toList(userList);
        return userReturn;
    }
}