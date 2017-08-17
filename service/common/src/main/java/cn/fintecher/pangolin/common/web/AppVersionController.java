package cn.fintecher.pangolin.common.web;

import cn.fintecher.pangolin.common.client.UserClient;
import cn.fintecher.pangolin.common.model.AppVersion;
import cn.fintecher.pangolin.common.model.AppVersionSaveCondition;
import cn.fintecher.pangolin.common.model.QAppVersion;
import cn.fintecher.pangolin.common.respository.AppVersionRepository;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWDateUtil;
import cn.fintecher.pangolin.web.HeaderUtil;
import cn.fintecher.pangolin.web.PaginationUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.net.URISyntaxException;
import java.util.ArrayList;
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

    @PostMapping(value = "/createAppVersion")
    @ApiOperation(value = "添加app版本", notes = "添加app版本")
    public ResponseEntity<List<AppVersion>> createAppVersion(@Validated @RequestBody AppVersionSaveCondition condition,
                                                             @RequestHeader(value = "X-UserToken") String token) {
        User user = userClient.getUserByToken(token).getBody();

        if (!(Objects.equals(user.getId(), Constants.ADMINISTRATOR_ID))) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "Can't add without permission", "没有权限不能添加")).body(null);
        }
        String regex = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";
        if (!condition.getAppVersion().matches(regex)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The version number is not in conformity with the rules", "版本号不符合规则")).body(null);
        }
        for (String os : condition.getOs()) {
            QAppVersion qAppVersion = QAppVersion.appVersion1;
            Iterable<AppVersion> appVersions = appVersionRepository.findAll(qAppVersion.appVersion.eq(condition.getAppVersion()).and(qAppVersion.os.eq(os)));
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
            AppVersion appVersion1 = appVersionRepository.save(appVersion);
            appVersionList.add(appVersion1);
        }
        return ResponseEntity.ok().body(appVersionList);
    }

    @GetMapping(value = "/deleteAppVersion")
    @ApiOperation(value = "删除app版本", notes = "删除app版本")
    public ResponseEntity<Void> deleteAppVersion(@RequestParam String id,
                                                 @RequestHeader(value = "X-UserToken") String token) {
        User user = userClient.getUserByToken(token).getBody();
        if (!(Objects.equals(user.getId(), Constants.ADMINISTRATOR_ID))) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "Can't add without permission", "没有权限不能添加")).body(null);
        }
        appVersionRepository.delete(id);
        return ResponseEntity.ok().body(null);
    }

    @PostMapping("/queryAppVersion")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", dataType = "int", paramType = "query",
                    value = "页数 (0..N)"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query",
                    value = "每页大小."),
            @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                    value = "依据什么排序: 属性名(,asc|desc). ")
    })
    @ApiOperation(value = "分页查询app版本控制", notes = "分页查询app版本控制")
    public ResponseEntity<Page<AppVersion>> queryAppVersion(@QuerydslPredicate(root = AppVersion.class) Predicate predicate,
                                                            @ApiIgnore Pageable pageable,
                                                            @RequestHeader(value = "X-UserToken") String token) throws URISyntaxException {
        logger.debug("REST request to query company : {}");
        BooleanBuilder builder = new BooleanBuilder(predicate);
        Page<AppVersion> page = appVersionRepository.findAll(builder, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/appVersionController");
        return new ResponseEntity<>(page, headers, HttpStatus.OK);
    }

    @GetMapping(value = "publishAppVersion")
    @ApiOperation(value = "发布新版本", notes = "发布新版本")
    public ResponseEntity<AppVersion> checkUpdate(@RequestParam String os, @RequestParam String version) {
        String regex = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$";
        if (!version.matches(regex)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "The version number abnormality", "版本号异常")).body(null);
        }
        QAppVersion qAppVersion = QAppVersion.appVersion1;
        Iterable<AppVersion> appVersions = appVersionRepository.findAll(qAppVersion.os.eq(os), new Sort(Sort.Direction.DESC, "publishTime"));
        if (appVersions.iterator().hasNext()) {
            if (Objects.equals(appVersions.iterator().next().getAppVersion(), version)) {
                return ResponseEntity.ok().body(null);
            } else {
                return ResponseEntity.ok().body(appVersions.iterator().next());
            }
        }
        return ResponseEntity.ok().body(null);
    }
}