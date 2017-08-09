package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.DurationResult;
import cn.fintecher.pangolin.business.repository.UserLoginLogRepository;
import cn.fintecher.pangolin.entity.QUserLoginLog;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.UserLoginLog;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.IterableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * @Author : sunyanping
 * @Description : 用户登录日志
 * @Date : 2017/8/9.
 */
@RestController
@RequestMapping("/api/userLoginLogController")
@Api(value = "UserLoginLogController", description = "用户登录日志")
public class UserLoginLogController extends BaseController{

    private final Logger log = LoggerFactory.getLogger(UserController.class);

    @Inject
    private UserLoginLogRepository userLoginLogRepository;

    @GetMapping(value = "/getOnlineUserDurationTimeDay")
    @ApiOperation(value = "获取用户今日登陆总时长(秒)", notes = "获取用户今日登陆总时长")
    public ResponseEntity<DurationResult> getOnlineUserDurationTimeDay(@RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get getOnlineUserDurationTimeDay : {}", token);
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("UserLoginLogController", "getOnlineUserDurationTimeDay", e.getMessage())).body(null);
        }
        Long totalTime = 0L;
        Date startDay = ZWDateUtil.getNightTime(-1);
        Date endDay = ZWDateUtil.getNightTime(0);
        try {
            QUserLoginLog qUserLoginLog = QUserLoginLog.userLoginLog;
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(qUserLoginLog.userId.eq(user.getId()));
            builder.and(qUserLoginLog.loginTime.between(startDay, endDay));
            builder.and(qUserLoginLog.logoutTime.between(startDay, endDay));
            Iterable<UserLoginLog> all = userLoginLogRepository.findAll(builder);
            List<UserLoginLog> userList = IterableUtils.toList(all);
            if (!userList.isEmpty()) {
                for (UserLoginLog u : userList) {
                    totalTime += u.getDuration();
                }
            }

            QUserLoginLog qUserLoginLog1 = QUserLoginLog.userLoginLog;
            BooleanBuilder builder1 = new BooleanBuilder();
            builder1.and(qUserLoginLog1.userId.eq(user.getId()));
            builder1.and(qUserLoginLog1.loginTime.between(startDay, endDay));
            builder1.and(qUserLoginLog1.logoutTime.isNull());
            Iterable<UserLoginLog> all1 = userLoginLogRepository.findAll(builder1);
            List<UserLoginLog> userLoginLogs = IterableUtils.toList(all1);
            if (!userLoginLogs.isEmpty()) {
                if (userLoginLogs.get(0) != null) {
                    totalTime += (new Date().getTime() - userLoginLogs.get(0).getLoginTime().getTime()) / 1000;
                }
            }
            DurationResult durationResult = new DurationResult();
            durationResult.setUserId(user.getId());
            durationResult.setDuration(totalTime);
            durationResult.setOffLineDuration(getOffLineDuration(totalTime));
            return ResponseEntity.ok().body(durationResult);
        } catch (Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("UserLoginLogController", "getOnlineUserDurationTimeDay", "系统异常!")).body(null);
        }
    }

    /**
     * 获取离线时长
     * @param duration
     * @return
     */
    public Long getOffLineDuration(Long duration) {
        long nowTimeMillis = System.currentTimeMillis() / 1000;
        Date date = ZWDateUtil.getNightTime(-1);
        long firstDayTimeMillis = date.getTime() / 1000;
        return nowTimeMillis - firstDayTimeMillis - duration;
    }
}
