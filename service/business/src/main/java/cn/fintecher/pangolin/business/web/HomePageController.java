package cn.fintecher.pangolin.business.web;

import cn.fintecher.pangolin.business.model.HomePageResult;
import cn.fintecher.pangolin.business.service.HomePageService;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * @Author : sunyanping
 * @Description : 管理员/催收员首页
 * @Date : 2017/7/31.
 */
@RestController
@RequestMapping("/api/homePageController")
@Api(value = "HomePageController", description = "管理员首页")
public class HomePageController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(HomePageController.class);

    @Inject
    private HomePageService homePageService;

    @GetMapping(value = "/getHomePageInformation")
    @ApiOperation(value = "统计首页数据",notes = "统计首页数据")
    public ResponseEntity getHomePageInformation(@RequestHeader(value = "X-UserToken") String token){
        log.debug("REST request to get getHomePageInformation : {}",token);
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController", "getHomePageInformation", e.getMessage())).body(null);
        }
        try {
            HomePageResult homePageResult = homePageService.getHomePageInformation(user);
            return ResponseEntity.ok().body(homePageResult.getData());
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController","getHomePageInformation","系统异常!")).body(null);
        }
    }
}