package cn.fintecher.pangolin.dataimp.web;

import cn.fintecher.pangolin.dataimp.entity.QUserVideo;
import cn.fintecher.pangolin.dataimp.entity.UserVideo;
import cn.fintecher.pangolin.dataimp.repository.UserVideoRepository;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.VideoSwitchUtil;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.util.ZWStringUtils;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.omg.CORBA.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;
import sun.swing.FilePane;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Created by qijigui on 2017-11-08.
 */
@RestController
@RequestMapping("/api/userVideoController")
@Api(value = "CaseStrategyController", description = "催收员录音")
public class UserVideoController {

    private final Logger logger = LoggerFactory.getLogger(UserVideoController.class);

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    UserVideoRepository userVideoRepository;

    @GetMapping("/getAllVideos")
    @ApiOperation(value = "获取所有的用户录音", notes = "获取所有的用户录音")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    public ResponseEntity<Page<UserVideo>> getAllVideos(@QuerydslPredicate(root = UserVideo.class) Predicate predicate,
                                                        @ApiIgnore Pageable pageable,
                                                        @RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token,
                                                        @RequestParam(value = "companyCode", required = false) @ApiParam("公司Code") String companyCode) {
        try {
            ResponseEntity<User> userResponseEntity;
            try {
                userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return ResponseEntity.badRequest().headers(HeaderUtil.createAlert("获取用户信息失败", "")).body(null);
            }
            User user = userResponseEntity.getBody();
            BooleanBuilder builder = new BooleanBuilder(predicate);
            QUserVideo qUserVideo = QUserVideo.userVideo;
            if (Objects.isNull(user.getCompanyCode())) {
                if (StringUtils.isNotBlank(companyCode)) {
                    builder.and(qUserVideo.companyCode.eq(companyCode));
                }
            } else {
                builder.and(qUserVideo.companyCode.eq(user.getCompanyCode()));
            }
            Page<UserVideo> userVideoPage = userVideoRepository.findAll(predicate, pageable);
            return ResponseEntity.ok().body(userVideoPage);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "获取催收员录音失败")).body(null);
        }
    }

    @GetMapping("/addUserVideo")
    @ApiOperation(value = "增加催收员录音", notes = "增加催收员录音")
    public void addUserVideo(@RequestHeader(value = "X-UserToken") @ApiParam("操作者的Token") String token) {
        try {
            ResponseEntity<User> userResponseEntity = null;
            try {
                userResponseEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            User user = userResponseEntity.getBody();
            UserVideo userVideo = new UserVideo();
            userVideo.setCompanyCode("0001");
            userVideo.setDeptCode(user.getDepartment().getCode());
            userVideo.setDeptName(user.getDepartment().getName());
            userVideo.setOperatorTime(ZWDateUtil.getNowDateTime());
            userVideo.setUserName(user.getUserName());
            userVideo.setUserRealName(user.getRealName());
            userVideo.setVideoUrl("http://192.168.3.10:9000/file-service/api/fileUploadController/view/5a03b7b1acd2c423902ee206");
            userVideo.setVideoLength("00:20:00");
            userVideoRepository.save(userVideo);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
