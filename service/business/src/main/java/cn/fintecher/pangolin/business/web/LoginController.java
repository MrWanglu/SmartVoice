package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.UpdatePassword;
import cn.fintecher.pangolin.business.model.UserLoginResponse;
import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.business.repository.UserRepository;
import cn.fintecher.pangolin.business.session.SessionStore;
import cn.fintecher.pangolin.entity.QSysParam;
import cn.fintecher.pangolin.entity.SysParam;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.UserLoginRequest;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.Status;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 10:39 2017/7/17
 */
@RestController
@RequestMapping("/api/login")
@Api(value = "登录相关", description = "登陆相关")
public class LoginController extends BaseController {
    private static final String ENTITY_NAME = "login";
    @Autowired
    UserRepository userRepository;
    @Autowired
    private SysParamRepository sysParamRepository;

    /**
     * @Description : 用户登录返回部门和角色
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户登陆", notes = "用户登陆")
    public ResponseEntity login(@RequestBody UserLoginRequest loginRequest, HttpServletRequest request) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        User user = userRepository.findByUserName(loginRequest.getUsername());
        if (Objects.isNull(user)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The user does not exist", "该用户不存在")).body(null);
        }
        //登录用户状态
        if (Objects.equals(Status.Disable.getValue(), user.getStatus())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The user has been disabled. Please contact your administrato", "该用户已被停用，请联系管理员")).body(null);
        }
        //登录用户设备锁
        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            //用户登录返回
            HttpSession session = request.getSession();
            session.setAttribute(Constants.SESSION_USER, user);
            UserLoginResponse response = new UserLoginResponse();
            response.setUser(user);
            response.setToken(session.getId());
            //用户设定修改密码的时间限制
            if (Objects.isNull(user.getPasswordInvalidTime())) {
                user.setPasswordInvalidTime(ZWDateUtil.getNowDateTime());
                userRepository.save(user);
                response.setReset(false);
            } else {
                //登录的密码设定的时间限制
                QSysParam qSysParam = QSysParam.sysParam;
                Iterable<SysParam> sysParams;
                if (Objects.isNull(user.getCompanyCode())) {
                    sysParams = sysParamRepository.findAll(qSysParam.code.eq(Constants.USER_OVERDAY_CODE).and(qSysParam.type.eq(Constants.USER_OVERDAY_TYPE)).and(qSysParam.companyCode.isNull()));
                } else {
                    sysParams = sysParamRepository.findAll(qSysParam.code.eq(Constants.USER_OVERDAY_CODE).and(qSysParam.type.eq(Constants.USER_OVERDAY_TYPE)).and(qSysParam.companyCode.eq(user.getCompanyCode())));
                }

                if (sysParams.iterator().hasNext()) {
                    if (Objects.equals(Status.Enable.getValue(), sysParams.iterator().next().getStatus())) {
                        int overDay = Integer.parseInt(sysParams.iterator().next().getValue());
                        Date nowDate = ZWDateUtil.getNowDate();
                        Calendar calendar1 = Calendar.getInstance();
                        Calendar calendar2 = Calendar.getInstance();
                        calendar1.setTime(user.getPasswordInvalidTime()); //用户修改密码的时间
                        calendar2.setTime(nowDate); // 当前时间
                        int dayDiff = (calendar2.getTime().getYear() - calendar1.getTime().getYear()) * 12 + calendar2.getTime().getMonth() - calendar1.getTime().getMonth() + calendar2.getTime().getDate() - calendar1.getTime().getDate();
                        if (overDay >= dayDiff) {
                            response.setReset(false);
                        } else {
                            response.setReset(true);
                        }
                    } else {
                        response.setReset(false);
                    }
                } else {
                    response.setReset(false);
                }
            }
            SessionStore.getInstance().addUser(session.getId(), session);
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "login successfully", "登录成功")).body(response);
        } else {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "wrong password", "密码错误")).body(null);
        }
    }

    /**
     * @Description : 注销用户
     */
    @RequestMapping(value = "/logout", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "注销", notes = "注销")
    public ResponseEntity logout(@RequestParam String token, HttpServletRequest request) {
        request.getSession().removeAttribute(Constants.SESSION_USER);
        SessionStore.getInstance().removeUser(request.getSession().getId());
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Cancellation of success", "注销成功")).body(request);
    }

    /**
     * @Description : 修改密码
     */
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "修改密码", notes = "修改密码")
    public ResponseEntity<User> updatePassword(@Validated @RequestBody @ApiParam("修改的用户密码") UpdatePassword request,
                                               @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        //密码加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (Objects.isNull(request.getOldPassword())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Please enter the original password", "请输入原始密码")).body(null);
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Enter the original password mistake", "输入的原始密码错误")).body(null);
        }
        if (Objects.equals(request.getNewPassword(), request.getOldPassword())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Enter a new password is the same as the original password, please enter again", "输入的新密码和原始密码相同，请重新输入")).body(null);
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordInvalidTime(ZWDateUtil.getNowDateTime());
        User user1 = userRepository.save(user);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Change the password successfully", "修改密码成功")).body(user1);

    }

    /**
     * @Description : 重置密码
     */
    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "重置密码", notes = "重置密码")
    public ResponseEntity<User> resetPassword(@Validated @RequestBody @ApiParam("重置密码") UpdatePassword request,
                                              @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User is not login", "用户未登录")).body(null);
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        User userOld = userRepository.findOne(request.getUserId());
        QSysParam qSysParam = QSysParam.sysParam;
        Iterable<SysParam> sysParams;
        if (Objects.isNull(user.getCompanyCode())) {
            sysParams = sysParamRepository.findAll(qSysParam.code.eq(Constants.USER_RESET_PASSWORD_CODE).and(qSysParam.type.eq(Constants.USER_RESET_PASSWORD_TYPE)).and(qSysParam.companyCode.isNull()));
        } else {
            sysParams = sysParamRepository.findAll(qSysParam.code.eq(Constants.USER_RESET_PASSWORD_CODE).and(qSysParam.type.eq(Constants.USER_RESET_PASSWORD_TYPE)).and(qSysParam.companyCode.eq(userOld.getCompanyCode())));
        }
        if (Objects.nonNull(sysParams) && Objects.equals(Status.Enable.getValue(), sysParams.iterator().next().getStatus())) {
            user.setPassword(passwordEncoder.encode(sysParams.iterator().next().getValue()));
        } else {
            userOld.setPassword(passwordEncoder.encode(Constants.LOGIN_RET_PASSWORD));
        }
        userOld.setPasswordInvalidTime(ZWDateUtil.getNowDateTime());
        User userReturn = userRepository.save(userOld);
        return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Password reset successfully", "重置密码成功")).body(userReturn);
    }
}