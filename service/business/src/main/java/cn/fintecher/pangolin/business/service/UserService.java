package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.UserDeviceReset;
import cn.fintecher.pangolin.business.repository.DepartmentRepository;
import cn.fintecher.pangolin.business.repository.UserRepository;
import cn.fintecher.pangolin.business.session.SessionStore;
import cn.fintecher.pangolin.entity.Department;
import cn.fintecher.pangolin.entity.QUser;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.UserDevice;
import cn.fintecher.pangolin.entity.util.Constants;
import com.querydsl.core.BooleanBuilder;
import org.apache.commons.collections4.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.*;

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
    public List<User> getAllUser(String companyCode, Integer type, Integer state, Integer manager) {
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

    /**
     * @Description : 通过token查询用户
     */
    public User getUserByToken(String token) throws Exception {
        HttpSession session = SessionStore.getInstance().getSession(token);
        if (session == null) {
            throw new Exception(Constants.SYS_EXCEPTION_NOSESSION);
        }
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        return user;
    }

    /**
     * 禁用设备
     *
     * @param request
     */
    public void resetDeviceStatus(UserDeviceReset request) {
        Set<User> users = new HashSet<>();
        for (String id : request.getUserIds()) {
            User user = userRepository.findOne(id);
            Set<UserDevice> userDevices = new HashSet<>();
            userDevices.addAll(user.getUserDevices());
            for (UserDevice ud : userDevices) {
                if (Objects.equals(ud.getType(), request.getUsdeType())) {
                    ud.setStatus(request.getUsdeStatus());
                }
            }
            user.getUserDevices().clear();
            user.getUserDevices().addAll(userDevices);
            users.add(user);
        }
        userRepository.save(users);
        userRepository.flush();
    }

    /**
     * 启动设备锁
     *
     * @param request
     */
    public void resetDeviceValidate(UserDeviceReset request) {
        Set<User> users = new HashSet<>();
        for (String id : request.getUserIds()) {
            User user = userRepository.findOne(id);
            Set<UserDevice> userDevices = new HashSet<>();
            userDevices.addAll(user.getUserDevices());
            if (!userDevices.isEmpty()) {
                for (UserDevice ud : userDevices) {
                    if (Objects.equals(ud.getType(), request.getUsdeType())) {
                        ud.setValidate(request.getValidate());
                    }
                }
            }
            user.getUserDevices().clear();
            user.getUserDevices().addAll(userDevices);
            users.add(user);
        }
        userRepository.save(users);
        userRepository.flush();
    }

    /**
     * 重置设备
     *
     * @param request
     */
    public void resetDeviceCode(UserDeviceReset request) {
        Set<User> users = new HashSet<>();
        for (String id : request.getUserIds()) {
            User user = userRepository.findOne(id);
            Set<UserDevice> userDevices = new HashSet<>();
            userDevices.addAll(user.getUserDevices());
            if (!userDevices.isEmpty()) {
                Iterator<UserDevice> iterator = userDevices.iterator();
                while (iterator.hasNext()) {
                    UserDevice ud = iterator.next();
                    if (Objects.equals(ud.getType(), request.getUsdeType())) {
                        ud.setCode(null);
                    }
                }
            }
            user.getUserDevices().clear();
            user.getUserDevices().addAll(userDevices);
            users.add(user);
        }
        userRepository.save(users);
        userRepository.flush();
    }

    @Cacheable(value = "userCache", key = "'petstore:user:'+#user.userName", unless = "#result==null")
    public User save(User user) {
        return userRepository.save(user);
    }

    @Cacheable(value = "userCache", key = "'petstore:user:all'", unless = "#result==null")
    public Iterable<User> findAll() {
        return userRepository.findAll();
    }
}