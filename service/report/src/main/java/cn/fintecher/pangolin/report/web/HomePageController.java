package cn.fintecher.pangolin.report.web;

import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.report.model.*;
import cn.fintecher.pangolin.report.service.HomePageService;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author : sunyanping
 * @Description : 首页
 * @Date : 2017/7/31.
 */
@RestController
@RequestMapping("/api/homePageController")
@Api(value = "HomePageController", description = "首页")
public class HomePageController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(HomePageController.class);

    @Autowired
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
            return ResponseEntity.ok().body(homePageResult);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController","getHomePageInformation","系统异常!")).body(null);
        }
    }

    @GetMapping(value = "/getHomePageCollectedPage")
    @ApiOperation(value = "统计首页周月完成数据",notes = "统计首页周月完成数据")
    public ResponseEntity getHomePageCollectedPage(@RequestHeader(value = "X-UserToken") String token){
        log.debug("REST request to get getHomePageCollectedPage : {}",token);
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController", "getHomePageInformation", e.getMessage())).body(null);
        }
        try {
            CollectPage CollectPage = homePageService.getCollectedWeekOrMonthPage(user);
            return ResponseEntity.ok().body(CollectPage);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController","getHomePageInformation","系统异常!")).body(null);
        }
    }

    @GetMapping(value = "/getHomePagePreviewTotalFollow")
    @ApiOperation(value = "统计首页跟催量总览",notes = "统计首页跟催量总览")
    public ResponseEntity getHomePagePreviewTotalFollow(@RequestHeader(value = "X-UserToken") String token){
        log.debug("REST request to get getHomePageCollectedPage : {}",token);
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController", "getHomePageInformation", e.getMessage())).body(null);
        }
        try {
            PreviewTotalFollowModel previewTotalFollowModel = homePageService.getPreviewTotal(user);
            return ResponseEntity.ok().body(previewTotalFollowModel);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController","getHomePageInformation","系统异常!")).body(null);
        }
    }

    @GetMapping(value = "/getHomePageCaseFollowedPreview")
    @ApiOperation(value = "统计首页案件状况总览",notes = "统计首页案件状况总览")
    public ResponseEntity getHomePageCaseFollowedPreview(@RequestHeader(value = "X-UserToken") String token){
        log.debug("REST request to get getHomePageCollectedPage : {}",token);
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController", "getHomePageInformation", e.getMessage())).body(null);
        }
        try {

            CaseStatusTotalPreview caseStatusTotalPreview = homePageService.getPreviewCaseStatus(user);
            return ResponseEntity.ok().body(caseStatusTotalPreview);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController","getHomePageInformation","系统异常!")).body(null);
        }
    }

    @GetMapping(value = "/getHomePageCollectedCaseBackRank")
    @ApiOperation(value = "统计首页案件状况总览",notes = "统计首页案件状况总览")
    public ResponseEntity getHomePageCollectedCaseBackRank(@RequestHeader(value = "X-UserToken") String token){
        log.debug("REST request to get getHomePageCollectedPage : {}",token);
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController", "getHomePageInformation", e.getMessage())).body(null);
        }
        try {

            CaseInfoRank caseInfoRank = homePageService.getCollectedCaseBackRank(user);
            return ResponseEntity.ok().body(caseInfoRank);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController","getHomePageInformation","系统异常!")).body(null);
        }
    }

    @GetMapping(value = "/getHomePageCollectedFollowedRank")
    @ApiOperation(value = "统计首页案件状况总览",notes = "统计首页案件状况总览")
    public ResponseEntity getHomePageCollectedFollowedRank(@RequestHeader(value = "X-UserToken") String token){
        log.debug("REST request to get getHomePageCollectedPage : {}",token);
        User user = null;
        try {
            user = getUserByToken(token);
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController", "getHomePageInformation", e.getMessage())).body(null);
        }
        try {

            CaseInfoRank caseInfoRank = homePageService.getCollectedFollowedRank(user);
            return ResponseEntity.ok().body(caseInfoRank);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController","getHomePageInformation","系统异常!")).body(null);
        }
    }



}
