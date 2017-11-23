package cn.fintecher.pangolin.common.web;

import cn.fintecher.pangolin.common.client.UserClient;
import cn.fintecher.pangolin.common.model.AppVersion;
import cn.fintecher.pangolin.common.model.AppVersionSaveCondition;
import cn.fintecher.pangolin.common.model.QAppVersion;
import cn.fintecher.pangolin.common.respository.AppVersionRepository;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.EntityUtil;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.util.ZWStringUtils;
import cn.fintecher.pangolin.web.HeaderUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by  hukaijia.
 * Description:
 * Date: 2017-07-05-20:11
 */
@RestController
@RequestMapping("/api/appVersionController")
@Api(value = "APP版本控制", description = "APP版本控制")
public class AppVersionController {
    private final Logger logger = LoggerFactory.getLogger(AppVersionController.class);
    private static final String ENTITY_NAME = "AppVersion";
    @Autowired
    private AppVersionRepository appVersionRepository;
    @Autowired
    private UserClient userClient;
    @Autowired
    private RestTemplate restTemplate;

    @PostMapping(value = "/createAppVersion")
    @ApiOperation(value = "添加app版本", notes = "添加app版本")
    public ResponseEntity<List<AppVersion>> createAppVersion(@Validated @RequestBody AppVersionSaveCondition condition,
                                                             @RequestHeader(value = "X-UserToken") String token) {
        condition = (AppVersionSaveCondition) EntityUtil.emptyValueToNull(condition);
        User user = userClient.getUserByToken(token).getBody();
        String regex = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";
        if (!condition.getMobileVersion().matches(regex)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The version number is not in conformity with the rules", "版本号不符合规则")).body(null);
        }
        for (String os : condition.getOs()) {
            QAppVersion qAppVersion = QAppVersion.appVersion;
            Iterable<AppVersion> appVersions = appVersionRepository.findAll(qAppVersion.mobileVersion.eq(condition.getMobileVersion()).and(qAppVersion.os.eq(os)));
            if (appVersions.iterator().hasNext()) {
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "This version has been in existence", "该版本已存在")).body(null);
            }
        }
        List<AppVersion> appVersionList = new ArrayList<AppVersion>();
        for (String os : condition.getOs()) {
            AppVersion appVersion = new AppVersion();
            BeanUtils.copyProperties(condition, appVersion);
            appVersion.setOs(os);
            appVersion.setCreator(user.getUserName());
            appVersion.setCreateTime(ZWDateUtil.getNowDateTime());
            appVersion.setPublishTime(ZWDateUtil.getNowDateTime());
            appVersion.setCompanyCode(user.getCompanyCode());
            AppVersion appVersion1 = appVersionRepository.save(appVersion);
            appVersionList.add(appVersion1);
        }
        return ResponseEntity.ok().body(appVersionList);
    }

    @GetMapping(value = "/deleteAppVersion")
    @ApiOperation(value = "删除app版本", notes = "删除app版本")
    public ResponseEntity<Void> deleteAppVersion(@RequestParam String id) {
        try {
            appVersionRepository.delete(id);
            return ResponseEntity.ok().body(null);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "删除失败")).body(null);
        }
    }

    @GetMapping("/queryAppVersion")
    @ApiOperation(value = "分页查询app版本控制", notes = "分页查询app版本控制")
    public ResponseEntity<Page<AppVersion>> queryAppVersion(Pageable pageable,
                                                            @RequestHeader(value = "X-UserToken") String token,
                                                            @RequestParam(value = "companyCode", required = false) @ApiParam("公司Code码") String companyCode,
                                                            @RequestParam(value = "os", required = false) String os,
                                                            @RequestParam(value = "pubDateBegin", required = false) String pubDateBegin,
                                                            @RequestParam(value = "pubDateEnd", required = false) String pubDateEnd,
                                                            @RequestParam(value = "mobileVersion", required = false) String mobileVersion) {

        User user;
        try {


            BooleanBuilder builder = new BooleanBuilder();
            try {
                ResponseEntity<User> userEntity = restTemplate.getForEntity(Constants.USERTOKEN_SERVICE_URL.concat(token), User.class);
                user = userEntity.getBody();
                if (Objects.nonNull(user.getCompanyCode())) {
                    builder.and(QAppVersion.appVersion.companyCode.eq(user.getCompanyCode()));
                }
                logger.debug("REST request to query company : {}");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "AppVersion", "系统异常!")).body(null);
            }
            if (ZWStringUtils.isNotEmpty(os)) {
                builder.and(QAppVersion.appVersion.os.eq(os));
            }
            if (ZWStringUtils.isNotEmpty(mobileVersion)) {
                builder.and(QAppVersion.appVersion.mobileVersion.eq(mobileVersion));
            }
            if (ZWStringUtils.isNotEmpty(pubDateBegin)) {
                String beginDate = pubDateBegin + " 00:00:00";
                String endDate = pubDateEnd + " 23:59:59";
                builder.and(QAppVersion.appVersion.publishTime.goe(ZWDateUtil.getUtilDate(beginDate, "yyyy-MM-dd HH:mm:ss")));
                builder.and(QAppVersion.appVersion.publishTime.loe(ZWDateUtil.getUtilDate(endDate, "yyyy-MM-dd HH:mm:ss")));
            }
            Page<AppVersion> page = appVersionRepository.findAll(builder, pageable);
            return ResponseEntity.ok().headers(HeaderUtil.createAlert("查询成功", ENTITY_NAME)).body(page);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("", "", "查询失败")).body(null);
        }
    }


    @GetMapping(value = "/publishAppVersion")
    @ApiOperation(value = "发布新版本", notes = "发布新版本")
    public ResponseEntity<AppVersion> checkUpdate(@RequestParam String os, @RequestParam String version) {
        String regex = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";
        if (!version.matches(regex)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The version number abnormality", "版本号异常")).body(null);
        }
        QAppVersion qAppVersion = QAppVersion.appVersion;
        Iterable<AppVersion> appVersions = appVersionRepository.findAll(qAppVersion.os.eq(os), new Sort(Sort.Direction.DESC, "publishTime"));
        if (appVersions.iterator().hasNext()) {
            if (Objects.equals(appVersions.iterator().next().getMobileVersion(), version)) {
                return ResponseEntity.ok().body(null);
            } else {
                return ResponseEntity.ok().body(appVersions.iterator().next());
            }
        }
        return ResponseEntity.ok().body(null);
    }

}