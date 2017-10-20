package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.UpdatePassword;
import cn.fintecher.pangolin.business.model.UserDeviceReset;
import cn.fintecher.pangolin.business.model.UserLoginResponse;
import cn.fintecher.pangolin.business.repository.SysParamRepository;
import cn.fintecher.pangolin.business.repository.UserDeviceRepository;
import cn.fintecher.pangolin.business.repository.UserRepository;
import cn.fintecher.pangolin.business.service.UserService;
import cn.fintecher.pangolin.business.session.SessionStore;
import cn.fintecher.pangolin.business.utils.GetClientIp;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.MD5;
import cn.fintecher.pangolin.entity.util.Status;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.util.ZWStringUtils;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

/**
 * @Author: PeiShouWen
 * @Description:
 * @Date 10:39 2017/7/17
 */
@RestController
@RequestMapping("/api/login")
@Api(value = "登录相关", description = "登陆相关")
public class LoginController extends BaseController {
    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private static final String ENTITY_NAME = "login";
    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    private SysParamRepository sysParamRepository;

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    /**
     * 无MD5加密用户登录 开发使用
     */
    @GetMapping("/getUserByToken")
    @ApiOperation(value = "通过token获取用户信息", notes = "通过token获取用户信息")
    public ResponseEntity<User> getUserToken(@RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = super.getUserByToken(token);
            if (Objects.isNull(user)) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The user does not exist", "该用户不存在")).body(null);
            }
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("登录成功", ENTITY_NAME)).body(user);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The user does not exist", "该用户不存在")).body(null);
        }
    }

    /**
     * 无MD5加密用户登录 开发使用
     */
    @PostMapping("/noUseMD5Login")
    @ApiOperation(value = "用户登陆测试", notes = "用户登陆测试")
    public ResponseEntity noUseMd5Login(@RequestBody UserLoginRequest loginRequest, HttpServletRequest request) {
        loginRequest.setPassword(MD5.MD5Encode(loginRequest.getPassword()));
        return login(loginRequest, request);
    }

    /**
     * 根据ip获取MAC地址
     */
    private String getLocalMac(InetAddress ia) throws SocketException {
        // TODO Auto-generated method stub
        //获取网卡，获取地址
        byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
        if (Objects.isNull(mac) || mac.length == 0) {
            return "";
        }
        System.out.println("mac数组长度：" + mac.length);
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < mac.length; i++) {
            if (i != 0) {
                sb.append("-");
            }
            //字节转换为整数
            int temp = mac[i] & 0xff;
            String str = Integer.toHexString(temp);
            System.out.println("每8位:" + str);
            if (str.length() == 1) {
                sb.append("0" + str);
            } else {
                sb.append(str);
            }
        }
        return sb.toString().toUpperCase();
    }

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
            Set<UserDevice> userDevices = user.getUserDevices();
            for (UserDevice userDevice : userDevices) {
                if (Objects.equals(loginRequest.getUsdeType(), userDevice.getType())) {
                    if (Objects.equals(userDevice.getStatus(), Status.Enable.getValue())) {
                        if (Objects.equals(userDevice.getValidate(), Status.Disable.getValue())) {
                            break;
                        }
                    } else {
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "device is disabled", "设备已处于停用状态！")).body(null);
                    }
                }
                if (Objects.equals(loginRequest.getUsdeType(), userDevice.getType())) {
                    // 启用设备
                    if (Objects.equals(userDevice.getStatus(), Status.Enable.getValue())) {
                        // 是否开启验证设备
                        if (Objects.equals(userDevice.getValidate(), Status.Enable.getValue())) {
                            if (Objects.equals(loginRequest.getUsdeType(), Status.Enable.getValue())) {
                                String ip = GetClientIp.getIp(request);
                                if (ZWStringUtils.isEmpty(userDevice.getCode())) {
                                    String mac = "";
                                    try {
                                        mac = getLocalMac(InetAddress.getLocalHost());
                                    } catch (UnknownHostException e) {
                                        logger.error(e.getMessage(), e);
                                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Failed to getMac", "获取MAC地址失败！")).body(null);
                                    } catch (SocketException e) {
                                        logger.error(e.getMessage(), e);
                                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Failed to getMac", "获取MAC地址失败！")).body(null);
                                    }
                                    // 判断用户请求的设备状态(PC端：0，移动端：1)
                                    loginRequest.setUsdeCode(ip);
                                    userDevice.setCode(ip);
                                    userDevice.setMac(ZWStringUtils.isEmpty(mac) ? null : mac);
                                } else {
                                    try {
                                        if (!Objects.equals(ZWStringUtils.isEmpty(getLocalMac(InetAddress.getLocalHost())) ? null : getLocalMac(InetAddress.getLocalHost()), userDevice.getMac())) {
                                            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "This login and the last login address are not consistent", "本次登录和上次登录地址不一致！")).body(null);
                                        }
                                    } catch (SocketException e) {
                                        logger.error(e.getMessage(), e);
                                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Failed to getMac", "获取MAC地址失败！")).body(null);
                                    } catch (UnknownHostException e) {
                                        logger.error(e.getMessage(), e);
                                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Failed to getMac", "获取MAC地址失败！")).body(null);
                                    }
                                }
                            } else {
                                if (ZWStringUtils.isEmpty(userDevice.getCode())) {
                                    userDevice.setCode(loginRequest.getUsdeCode());
                                } else {
                                    if (!Objects.equals(loginRequest.getUsdeCode(), userDevice.getCode())) {
                                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "This login and the last login address are not consistent", "本次登录和上次登录地址不一致！")).body(null);
                                    }
                                }
                            }
                        }
                    }
                }
                userDeviceRepository.save(userDevice);
            }
            userRepository.save(user);
            //用户设定修改密码的时间限制
            if (Objects.isNull(user.getPasswordInvalidTime())) {
                user.setPasswordInvalidTime(ZWDateUtil.getNowDateTime());
                userRepository.save(user);
                response.setReset(false);
            } else {
                //登录的密码设定的时间限制
                QSysParam qSysParam = QSysParam.sysParam;
                SysParam sysParams = null;
                try {
                    if (Objects.isNull(user.getCompanyCode())) {
                        sysParams = sysParamRepository.findOne(qSysParam.code.eq(Constants.USER_OVERDAY_CODE).and(qSysParam.type.eq(Constants.USER_OVERDAY_TYPE)).and(qSysParam.companyCode.isNull()));
                    } else {
                        sysParams = sysParamRepository.findOne(qSysParam.code.eq(Constants.USER_OVERDAY_CODE).and(qSysParam.type.eq(Constants.USER_OVERDAY_TYPE)).and(qSysParam.companyCode.eq(user.getCompanyCode())));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "Abnormal days overdue parameters", "过期天数参数异常")).body(null);
                }
                if (Objects.nonNull(sysParams)) {
                    if (Objects.equals(Status.Enable.getValue(), sysParams.getStatus())) {
                        int overDay = Integer.parseInt(sysParams.getValue());
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
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("登录成功", ENTITY_NAME)).body(response);
        } else {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "wrong password", "密码错误")).body(null);
        }
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
        User user1 = userService.save(user);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("修改密码成功", "")).body(user1);

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
        //重置密码的设置
        QSysParam qSysParam = QSysParam.sysParam;
        SysParam sysParamsPassword = null;
        try {
            sysParamsPassword = sysParamRepository.findOne(qSysParam.code.eq(Constants.USER_RESET_PASSWORD_CODE).and(qSysParam.type.eq(Constants.USER_RESET_PASSWORD_TYPE)).and(qSysParam.companyCode.eq(userOld.getCompanyCode())));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "To reset the password parameter abnormalities", "重置密码参数异常")).body(null);
        }
        if (Objects.nonNull(sysParamsPassword) && Objects.equals(Status.Enable.getValue(), sysParamsPassword.getStatus())) {
            userOld.setPassword(passwordEncoder.encode(MD5.MD5Encode(sysParamsPassword.getValue())));
        } else {
            userOld.setPassword(passwordEncoder.encode(Constants.LOGIN_RET_PASSWORD));
        }
        userOld.setPasswordInvalidTime(ZWDateUtil.getNowDateTime());
        User userReturn = userService.save(userOld);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("重置密码成功", "")).body(userReturn);
    }

    /**
     * @Description :启用/禁用设备
     */
    @RequestMapping(value = "/disableDevice", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "启用/禁用设备", notes = "启用/禁用设备")
    public ResponseEntity<User> disableDevice(@Validated @RequestBody @ApiParam("启用/禁用设备") UserDeviceReset request,
                                              @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User not login", "用户未登录")).body(null);
        }
        if (Objects.isNull(user)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User not login", "请登录后再修改")).body(null);
        }
        userService.resetDeviceStatus(request);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("禁用设备操作成功", "operator successfully")).body(user);
    }

    /**
     * @Description : 启用/停用设备锁
     */
    @RequestMapping(value = "/enableDeviceKey", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "启用/停用设备锁", notes = "启用/停用设备锁")
    public ResponseEntity<User> enableDeviceKey(@Validated @RequestBody @ApiParam("启用/停用设备锁") UserDeviceReset request,
                                                @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User not login", "用户未登录")).body(null);
        }
        if (Objects.isNull(user)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User not login", "请登录后再修改")).body(null);
        }
        userService.resetDeviceValidate(request);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("启用设备锁操作成功", "operator successfully")).body(user);
    }


    /**
     * @Description : 重置设备
     */
    @RequestMapping(value = "/resetDevice", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "重置设备", notes = "重置设备")
    public ResponseEntity<User> resetDevice(@Validated @RequestBody @ApiParam("重置设备") UserDeviceReset request,
                                            @RequestHeader(value = "X-UserToken") String token) {
        User user;
        try {
            user = getUserByToken(token);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User not login", "用户未登录")).body(null);
        }
        if (Objects.isNull(user)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "User not login", "请登录后再修改")).body(null);
        }
        userService.resetDeviceCode(request);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("重置设备操作成功", "operator successfully")).body(user);
    }
}