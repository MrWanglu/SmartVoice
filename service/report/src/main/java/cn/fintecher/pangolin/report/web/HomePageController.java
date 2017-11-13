package cn.fintecher.pangolin.report.web;

import cn.fintecher.pangolin.entity.CaseInfo;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.report.mapper.AdminPageMapper;
import cn.fintecher.pangolin.report.model.CollectorRankingModel;
import cn.fintecher.pangolin.report.model.CollectorRankingParams;
import cn.fintecher.pangolin.report.model.HomePageResult;
import cn.fintecher.pangolin.report.model.*;
import cn.fintecher.pangolin.report.service.HomePageService;
import cn.fintecher.pangolin.web.HeaderUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
    private final String ENTITY_NAME = "HomePageController";

    @Autowired
    private HomePageService homePageService;
    @Inject
    private AdminPageMapper adminPageMapper;

    @GetMapping(value = "/getHomePageInformation")
    @ApiOperation(value = "统计首页数据", notes = "统计首页数据")
    public ResponseEntity getHomePageInformation(@RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get getHomePageInformation : {}", token);
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
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController", "getHomePageInformation", "系统异常!")).body(null);
        }
    }

    @GetMapping(value = "/getHomePageCollectedPage")
    @ApiOperation(value = "统计催收员首页周月完成数据", notes = "统计催收员首页周月完成数据")
    public ResponseEntity getHomePageCollectedPage(@RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get getHomePageCollectedPage : {}", token);
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
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController", "getHomePageInformation", "查询周月完成进度失败")).body(null);
        }
    }

    @GetMapping(value = "/getHomePagePreviewTotalFollow")
    @ApiOperation(value = "统计催收员首页跟催量总览", notes = "统计催收员首页跟催量总览")
    public ResponseEntity getHomePagePreviewTotalFollow(@RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get getHomePageCollectedPage : {}", token);
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
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController", "getHomePageInformation", "查询跟催量总览失败")).body(null);
        }
    }

    @GetMapping(value = "/getHomePageCaseFollowedPreview")
    @ApiOperation(value = "统计催收员首页案件状况总览", notes = "统计催收员首页案件状况总览")
    public ResponseEntity getHomePageCaseFollowedPreview(@RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get getHomePageCollectedPage : {}", token);
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
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController", "getHomePageInformation", "查询案件状况总览失败")).body(null);
        }
    }

    @GetMapping(value = "/getHomePageCollectedCaseBackRank")
    @ApiOperation(value = "统计催收员首页回款金额排名", notes = "统计催收员首页回款金额排名")
    public ResponseEntity getHomePageCollectedCaseBackRank(@RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get getHomePageCollectedPage : {}", token);
        User user = null;
        String depCode = null;
        try {
            user = getUserByToken(token);
            depCode = user.getDepartment().getCode();
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController", "getHomePageInformation", e.getMessage())).body(null);
        }
        try {
            CaseInfoRank caseInfoRank = homePageService.getCollectedCaseBackRank(user, depCode);
            return ResponseEntity.ok().body(caseInfoRank);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController", "getHomePageInformation", "查询回款金额排名失败")).body(null);
        }
    }

    @GetMapping(value = "/getHomePageCollectedFollowedRank")
    @ApiOperation(value = "统计催收员首页跟催量排名", notes = "统计催收员首页跟催量排名")
    public ResponseEntity getHomePageCollectedFollowedRank(@RequestHeader(value = "X-UserToken") String token) {
        log.debug("REST request to get getHomePageCollectedPage : {}", token);
        User user = null;
        String depName = null;
        try {
            user = getUserByToken(token);
            depName = user.getDepartment().getName();
        } catch (final Exception e) {
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController", "getHomePageInformation", e.getMessage())).body(null);
        }
        try {
            CaseInfoRank caseInfoRank = homePageService.getCollectedFollowedRank(user, depName);
            return ResponseEntity.ok().body(caseInfoRank);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("HomePageController", "getHomePageInformation", "查询跟催量排名失败")).body(null);
        }
    }


    @GetMapping("/collectorRanking")
    @ApiOperation(value = "管理员首页催收员排行榜", notes = "管理员首页催收员排行榜")
    public ResponseEntity<Page<CollectorRankingModel>> collectorRanking(CollectorRankingParams params,
                                                                        @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            String code = user.getDepartment().getCode();
            params.setDeptCode(code);
            List<CollectorRankingModel> collectorRankingModels = adminPageMapper.collectorRanking(params);
            Page<CollectorRankingModel> page = new PageImpl(collectorRankingModels);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "催收员排行榜统计错误!")).body(null);
        }
    }

    @GetMapping("/quickAccessCaseInfo")
    @ApiOperation(value = "催收员首页快速催收", notes = "催收员首页快速催收")
    public ResponseEntity<CaseInfoModel> quickAccessCaseInfo(@RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            CaseInfoConditionParams caseInfoConditionParams = new CaseInfoConditionParams();
            //1代表催收员是电催部门
            if (user.getType() == 1) {
                caseInfoConditionParams.setCollectionType(CaseInfo.CollectionType.TEL.getValue().toString());//电催
            } else if (user.getType() == 2) { //2代表催收员是外访部门
                caseInfoConditionParams.setCollectionType(CaseInfo.CollectionType.VISIT.getValue().toString());//外访
            } else {
                caseInfoConditionParams.setCollectionType(CaseInfo.CollectionType.COMPLEX.getValue().toString());//综合
            }
            //待催收状态
            caseInfoConditionParams.setCollectionStatusList(CaseInfo.CollectionStatus.WAITCOLLECTION.getValue().toString());
            CaseInfoModel caseInfoModel = homePageService.quickAccessCaseInfo(user, caseInfoConditionParams);
            return ResponseEntity.ok().body(caseInfoModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "快速催收失败")).body(null);
        }
    }

    @GetMapping("/outsourceRanking")
    @ApiOperation(value = "管理员首页委外方排行榜", notes = "管理员首页委外方排行榜")
    public ResponseEntity<Page<OutsourceRankingModel>> outsourceRanking(CollectorRankingParams params,
                                                                        @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            if (Objects.nonNull(user.getCompanyCode())) {
                params.setCompanyCode(user.getCompanyCode());
            }
            List<OutsourceRankingModel> outsourceRankingModels = adminPageMapper.OutsourceRanking(params);
            Page<OutsourceRankingModel> page = new PageImpl(outsourceRankingModels);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "委外方排行榜统计错误!")).body(null);
        }
    }

    @GetMapping("/getCaseDate")
    @ApiOperation(value = "获取案件池中所有的日期", notes = "获取案件池中所有的日期")
    public ResponseEntity<CaseDateModel> getCaseDate(CollectorRankingParams collectorRankingParams) {
        try {
            CaseDateModel caseDateModel = adminPageMapper.getCaseDate(collectorRankingParams);
            return ResponseEntity.ok().body(caseDateModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "获取案件时间失败!")).body(null);
        }
    }

    @GetMapping("/getCaseAmtAndCount")
    @ApiOperation(value = "管理员首页 获取已还款案件金额/获取还款审核中案件金额/", notes = " 管理员首页 获取已还款案件数量/获取还款审核中案件数量/")
    public ResponseEntity<ReturnDataModel> getCaseAmtAndCount(CollectorRankingParams collectorRankingParams,
                                                              @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            String code = user.getDepartment().getCode();
            collectorRankingParams.setDeptCode(code);
            ReturnDataModel collectorRankingModels = homePageService.getCaseAmtAndCount(collectorRankingParams);
            return ResponseEntity.ok().body(collectorRankingModels);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "获取案件时间失败!")).body(null);
        }
    }

    @GetMapping("/getRecordReport")
    @ApiOperation(value = "根据年份查询该年度各月的催记，外呼数据量", notes = "根据年份查询该年度各月的催记，外呼数据量")
    public ResponseEntity<List<GroupMonthFollowRecord>> getRecordReport(CollectorRankingParams collectorRankingParams,
                                                                        @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            String code = user.getDepartment().getCode();
            collectorRankingParams.setDeptCode(code);
            List<GroupMonthFollowRecord> result = adminPageMapper.getRecordReport(collectorRankingParams);
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "催收数据查询失败!")).body(null);
        }
    }

    @GetMapping("/getCollectionedDate")
    @ApiOperation(value = "管理员首页获取催收中数据", notes = "管理员首页获取催收中数据")
    public ResponseEntity<CollectionDateModel> getCollectionedDate(CollectorRankingParams params,
                                                                   @RequestHeader(value = "X-UserToken") String token) {
        try {
            User user = getUserByToken(token);
            String code = user.getDepartment().getCode();
            params.setDeptCode(code);
            if (Objects.nonNull(user.getCompanyCode())) {
                params.setCompanyCode(user.getCompanyCode());
            }
            CollectionDateModel collectionDateModel = homePageService.getCollectionedDate(params);
            return ResponseEntity.ok().body(collectionDateModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "委外方排行榜统计错误!")).body(null);
        }
    }

    @GetMapping("/getCaseGroupInfo")
    @ApiOperation(value = "管理员首页获获取某时间段根据回款类型分组得到案件金额和数量", notes = "管理员首页获获取某时间段根据回款类型分组得到案件金额和数量")
    public ResponseEntity<List<CaseRepaymentTypeGroupInfo>> getCaseGroupInfo(CollectorRankingParams params,
                                                                             @RequestHeader(value = "X-UserToKen") String token) {
        try {
            User user = getUserByToken(token);
            String code = user.getDepartment().getCode();
            params.setDeptCode(code);
            List<CaseRepaymentTypeGroupInfo> result = adminPageMapper.getCaseGroupInfo(params);
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "", "获取催收回款类型出错!")).body(null);
        }
    }


}
