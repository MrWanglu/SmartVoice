package cn.fintecher.pangolin.report.service;

import cn.fintecher.pangolin.entity.CaseFollowupRecord;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.report.mapper.AdminPageMapper;
import cn.fintecher.pangolin.report.mapper.CaseInfoMapper;
import cn.fintecher.pangolin.report.mapper.CollectPageMapper;
import cn.fintecher.pangolin.report.mapper.CupoPageMapper;
import cn.fintecher.pangolin.report.model.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author : sunyanping
 * @Description : 主页
 * @Date : 2017/7/31.
 */
@Service("homePageService")
public class HomePageService {

    private final Logger log = LoggerFactory.getLogger(HomePageService.class);

    @Autowired
    private AdminPageMapper adminPageMapper;
    @Autowired
    private CupoPageMapper cupoPageMapper;
    @Autowired
    private CollectPageMapper collectPageMapper;
    @Inject
    CaseInfoMapper caseInfoMapper;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public HomePageResult getHomePageInformation(User user) {
        // 0-没有(不是管理员)，1-有（是管理员）
        if (Objects.equals(user.getManager(), 0)) {
            return getCollectPage(user);
        } else {
            return getAdminPage(user);
        }
    }


    private HomePageResult getAdminPage(User user) {
        HomePageResult<AdminPage> homePageResult = new HomePageResult<>();
        homePageResult.setType(user.getManager());
        //管理员首页数据
        AdminPage adminPage = new AdminPage();
        //管理者部门
        String code = user.getDepartment().getCode();
        List<User> allUser = adminPageMapper.getAllUserOnDepartment(code); //部门下用户数

        //第一部分 案件总金额 人均金额
        BigDecimal caseSumAmt = adminPageMapper.getCaseSumAmt(code); //案件总金额
        Integer deptUserSum = allUser.size();
        deptUserSum = deptUserSum == 0 ? 1 : deptUserSum;
        BigDecimal personCount = new BigDecimal(deptUserSum);
        adminPage.setCaseSumAmt(Objects.isNull(caseSumAmt) ? new BigDecimal(0.00) : caseSumAmt.setScale(2, BigDecimal.ROUND_HALF_UP));
        adminPage.setCaseSumAmtPerson(adminPage.getCaseSumAmt().divide(personCount, 2, BigDecimal.ROUND_HALF_UP));

        // 第二部分 案件已还款总金额 人均金额
        BigDecimal repaySumAmt = adminPageMapper.getRepaySumAmt(code);
        adminPage.setRepaySumAmt(Objects.isNull(repaySumAmt) ? new BigDecimal(0.00) : repaySumAmt.setScale(2, BigDecimal.ROUND_HALF_UP));
        adminPage.setRepaySumAmtPerson(adminPage.getRepaySumAmt().divide(personCount, 2, BigDecimal.ROUND_HALF_UP));

        // 第三部分 催收员总数 在线人数 离线人数
        if (Objects.isNull(user.getCompanyCode())) {
            adminPage.setCupoSum(allUser.size());
        } else {
            List<User> users = adminPageMapper.getAllUserOnCompany(user.getCompanyCode()); //公司下所有的用户数
            adminPage.setCupoSum(users.size());
        }
        adminPage.setCupoOnlineSum(0);
        adminPage.setCupoOfflineSum(0);


        //第四部分 客户总数 在案客户总数
        Integer custSum = adminPageMapper.getCustNum(code);
        Integer custSumIn = adminPageMapper.getCustNumIN(code);
        adminPage.setCustSum(Objects.isNull(custSum) ? 0 : custSum);
        adminPage.setCustSumIn(Objects.isNull(custSumIn) ? 0 : custSumIn);


        //第五部分 周回款金额
        List<WeekCountResult> weekRepaySumAmt = adminPageMapper.getWeekRepaySumAmt(code);
        adminPage.setWeekRepayList(addWeekListZero(weekRepaySumAmt));

        // 第六部分 周催计数
        List<WeekCountResult> weekFollCount = adminPageMapper.getWeekFollCount(code);
        adminPage.setWeekFollList(addWeekListZero(weekFollCount));

        // 第七部分 周结案数
        List<WeekCountResult> weekCaseEndCount = adminPageMapper.getWeekCaseEndCount(code);
        adminPage.setWeekCaseEndList(addWeekListZero(weekCaseEndCount));

        // 第八部分 催收员排行榜
        List<PageSortResult> cupoSort = adminPageMapper.getCupoSort(code);
        adminPage.setCupoSortList(cupoSort);

        // 第九部分 客户排行榜
        List<PageSortResult> custSort = adminPageMapper.getCustSort(code);
        adminPage.setCustSortList(custSort);

        // 第十部分 系统公告
        List<SysNotice> sysNoticeList = new ArrayList<>();
        SysNotice sysNotice = new SysNotice();
        sysNotice.setTitle("批量成功");
        sysNotice.setContent("您于2017-08-02 12:21:35完成批量");
        sysNoticeList.add(sysNotice);
        adminPage.setSysNotice(sysNoticeList);

        adminPage.initRate();
        homePageResult.setData(adminPage);
        return homePageResult;
    }

    //催收员首页 - 第一，二部分 本周和本月完成进度
    public CollectPage getCollectedWeekOrMonthPage(User user) {

        CollectPage collectPage = new CollectPage();
        //催收员案件总数
        Integer caseTotalCount = collectPageMapper.getCaseInfoWeekAllCount(user.getId());
        //本周已结案案件总数
        Integer caseWeekFinishedCount = collectPageMapper.getCaseInfoWeekClosedCount(user.getId());
        //催收员回款总金额
        BigDecimal caseBackTotalAmt = collectPageMapper.getWeekTotalBackCash(user.getId());
        //本周已回款总金额
        BigDecimal caseWeekBackFinishedAmt = collectPageMapper.getWeekHadBackCash(user.getUserName());
        //本月已结案案件总数
        Integer caseMonthFinishedCount = collectPageMapper.getCaseInfoMonthClosedCount(user.getId());
        //本月已回款总金额
        BigDecimal caseMonthBackFinishedAmt = collectPageMapper.getMonthHadBackCash(user.getUserName());

        collectPage.setCaseWeekTotalCount(Objects.isNull(caseTotalCount) ? 0 : caseTotalCount);
        collectPage.setCaseWeekFinishedCount(Objects.isNull(caseWeekFinishedCount) ? 0 : caseWeekFinishedCount);
        collectPage.setCaseWeekBackTotalCount(Objects.isNull(caseBackTotalAmt) ? BigDecimal.ZERO : caseBackTotalAmt);
        collectPage.setCaseWeekBackFinishedCount(Objects.isNull(caseWeekBackFinishedAmt) ? BigDecimal.ZERO : caseWeekBackFinishedAmt);

        collectPage.setCaseMonthTotalCount(Objects.isNull(caseTotalCount) ? 0 : caseTotalCount);
        collectPage.setCaseMonthFinishedCount(Objects.isNull(caseMonthFinishedCount) ? 0 : caseMonthFinishedCount);
        collectPage.setCaseMonthBackTotalCount(Objects.isNull(caseBackTotalAmt) ? BigDecimal.ZERO : caseBackTotalAmt);
        collectPage.setCaseMonthBackFinishedCount(Objects.isNull(caseMonthBackFinishedAmt) ? BigDecimal.ZERO : caseMonthBackFinishedAmt);
        return collectPage;
    }

    //催收员首页 - 第三部分 跟催量总览
    public PreviewTotalFollowModel getPreviewTotal(User user) {
        PreviewTotalFollowModel previewTotalFollowModel = new PreviewTotalFollowModel();
        // 第三部分 跟催量总览
        //今日外呼
        Integer currentDayCalled = collectPageMapper.getCalledDay(user.getUserName());
        //本周外呼
        Integer currentWeekCalled = collectPageMapper.getCalledWeek(user.getUserName());
        //本月外呼
        Integer currentMonthCalled = collectPageMapper.getCalledMonth(user.getUserName());
        //今日催记数
        Integer currentDayCount = collectPageMapper.getFollowDay(user.getUserName());
        //本周催记数
        Integer currentWeekCount = collectPageMapper.getFollowWeek(user.getUserName());
        //本月催记数
        Integer currentMonthCount = collectPageMapper.getFollowMonth(user.getUserName());
        //在线时长
        Double onlineTime = collectPageMapper.getUserOnlineTime(user.getId());
        if (Objects.isNull(onlineTime)) {
            onlineTime = 0.00;
        }
        onlineTime = onlineTime / 3600;
        DecimalFormat df = new DecimalFormat("######0");
        String onLine = df.format(onlineTime);
        //离线时长
        Integer offlineTime = 24 - Integer.parseInt(onLine);
        previewTotalFollowModel.setCurrentDayCalled(Objects.isNull(currentDayCalled) ? 0 : currentDayCalled);
        previewTotalFollowModel.setCurrentWeekCalled(Objects.isNull(currentWeekCalled) ? 0 : currentWeekCalled);
        previewTotalFollowModel.setCurrentMonthCalled(Objects.isNull(currentMonthCalled) ? 0 : currentMonthCalled);
        previewTotalFollowModel.setCurrentDayCount(Objects.isNull(currentDayCount) ? 0 : currentDayCount);
        previewTotalFollowModel.setCurrentWeekCount(Objects.isNull(currentWeekCount) ? 0 : currentWeekCount);
        previewTotalFollowModel.setCurrentMonthCount(Objects.isNull(currentMonthCount) ? 0 : currentMonthCount);
        previewTotalFollowModel.setOnlineTime(Integer.parseInt(onLine));
        previewTotalFollowModel.setOfflineTime(offlineTime);
        return previewTotalFollowModel;
    }

    // 催收员首页 - 第四部分 案件状况总览
    public CaseStatusTotalPreview getPreviewCaseStatus(User user) {

        CaseStatusTotalPreview caseStatusTotalPreview = new CaseStatusTotalPreview();
        //未催收案件数
        Integer toFollowCaseCount = collectPageMapper.getCaseInfoToFollowCount(user.getId());
        //催收中案件数
        Integer followingCaseCount = collectPageMapper.getCaseInfoFollowingCount(user.getId());
        //承诺还款案件数
        Integer commitmentBackCaseCount = collectPageMapper.getCaseInfoPromisedCount(user.getUserName());
        //今日流入案件
        Integer flowInCaseToday = collectPageMapper.getFlowInCaseToday(user.getId());
        //今日结清案件
        Integer finishCaseToday = collectPageMapper.getFinishCaseToday(user.getId());
        //今日流出案件
        Integer flowOutCaseToday = collectPageMapper.getFlowOutCaseToday(user.getId());
        caseStatusTotalPreview.setToFollowCaseCount(Objects.isNull(toFollowCaseCount) ? 0 : toFollowCaseCount);
        caseStatusTotalPreview.setFollowingCaseCount(Objects.isNull(followingCaseCount) ? 0 : followingCaseCount);
        caseStatusTotalPreview.setCommitmentBackCaseCount(Objects.isNull(commitmentBackCaseCount) ? 0 : commitmentBackCaseCount);
        caseStatusTotalPreview.setFlowInCaseToday(Objects.isNull(flowInCaseToday) ? 0 : flowInCaseToday);
        caseStatusTotalPreview.setFinishCaseToday(Objects.isNull(finishCaseToday) ? 0 : finishCaseToday);
        caseStatusTotalPreview.setFlowOutCaseToday(Objects.isNull(flowOutCaseToday) ? 0 : flowOutCaseToday);
        return caseStatusTotalPreview;
    }

    //催收员首页 - 第五部分 催收员回款排名
    public CaseInfoRank getCollectedCaseBackRank(User user, CollectorRankingParams params) {

        CaseInfoRank caseInfoRank = new CaseInfoRank();
        List<BackAmtModel> backAmtModels = collectPageMapper.getCaseInfoBackRank(params);
        for (int i = 0; i < backAmtModels.size(); i++) {
            if (Objects.isNull(backAmtModels.get(i).getCollectionName())) {
                backAmtModels.remove(backAmtModels.get(i));
            } else {
                if (user.getRealName().equals(backAmtModels.get(i).getCollectionName())) {
                    //添加该催收员的排名,加1是因为list是从0开始的
                    caseInfoRank.setCollectRank(i + 1);
                }
            }
            if (Objects.nonNull(backAmtModels.get(i).getBackRate())) {
                BigDecimal bigDecimal = new BigDecimal(backAmtModels.get(i).getBackRate());
                backAmtModels.get(i).setBackRate(bigDecimal.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
        }
        caseInfoRank.setBackAmtModels(backAmtModels);
        return caseInfoRank;
    }

    //催收员首页 - 第六部分 催收计数排名
    public CaseInfoRank getCollectedFollowedRank(User user, CollectorRankingParams params) {

        CaseInfoRank caseInfoRank = new CaseInfoRank();
        List<FollowCountModel> followCountModels = collectPageMapper.getCaseInfoFollowRank(params);
        for (int i = 0; i < followCountModels.size(); i++) {
            if (Objects.isNull(followCountModels.get(i).getCollectionFollowName())) {
                followCountModels.remove(followCountModels.get(i));
            } else {
                if (user.getRealName().equals(followCountModels.get(i).getCollectionFollowName())) {
                    //添加该催收员的排名
                    caseInfoRank.setCollectRank(i + 1);
                }
            }
        }
        caseInfoRank.setFollowCountModels(followCountModels);
        return caseInfoRank;
    }

    //催收员首页 - 快速催收
    public CaseInfoModel quickAccessCaseInfo(User user, CaseInfoConditionParams caseInfoConditionParams) {

        String sort = "";
        String newSort = "";
        if (Objects.nonNull(caseInfoConditionParams.getSort())) {
            sort = caseInfoConditionParams.getSort();
            newSort = sort.replace(",", " ");
        }
        //查询待催收页面的所有待催收案件
        List<CaseInfoModel> caseInfoModels = caseInfoMapper.getCaseInfoByCondition(
                StringUtils.trim(caseInfoConditionParams.getPersonalName()),
                StringUtils.trim(caseInfoConditionParams.getMobileNo()),
                caseInfoConditionParams.getDeptCode(),
                StringUtils.trim(caseInfoConditionParams.getCollectorName()),
                caseInfoConditionParams.getOverdueMaxAmt(),
                caseInfoConditionParams.getOverdueMinAmt(),
                caseInfoConditionParams.getPayStatus(),
                caseInfoConditionParams.getOverMaxDay(),
                caseInfoConditionParams.getOverMinDay(),
                StringUtils.trim(caseInfoConditionParams.getBatchNumber()),
                caseInfoConditionParams.getPrincipalId(),
                StringUtils.trim(caseInfoConditionParams.getIdCard()),
                caseInfoConditionParams.getFollowupBack(),
                caseInfoConditionParams.getAssistWay(),
                caseInfoConditionParams.getCaseMark(),
                caseInfoConditionParams.getCollectionType(),
                caseInfoConditionParams.getSort() == null ? null : newSort,
                user.getDepartment().getCode(),
                caseInfoConditionParams.getCollectionStatusList(),
                caseInfoConditionParams.getCollectionStatus(),
                caseInfoConditionParams.getParentAreaId(),
                caseInfoConditionParams.getAreaId(),
                user.getType(),
                user.getManager(),
                user.getId(),
                user.getCompanyCode(),
                caseInfoConditionParams.getRealPayMaxAmt(),
                caseInfoConditionParams.getRealPayMinAmt());
        CaseInfoModel caseInfoModel = null;
        //获取待催收安建宁的第一条案件进行快速催收
        if (caseInfoModels.size() != 0) {
            caseInfoModel = caseInfoModels.get(0);
        }
        return caseInfoModel;
    }

    //催收员首页 - 快速催收
    private HomePageResult getCollectPage(User user) {
        HomePageResult<CupoPage> homePageResult = new HomePageResult<>();
        homePageResult.setType(user.getManager());
        CupoPage cupoPage = new CupoPage();

        CollectorCaseResult collectorCaseResult = getCollectorCaseResult(user);

        // 第一部分 本月完成度
        Double taskFinished = cupoPageMapper.getTodyTashFinished(user.getUserName());
        taskFinished = Objects.isNull(taskFinished) ? 0D : taskFinished > 1D ? 1D : taskFinished;
        cupoPage.setTaskFinished(taskFinished);
        // 第二部分 案件情况总计  第三部分 案件金额总计 (饼图部分)
        List<CaseCountResult> caseCountResult = cupoPageMapper.getCaseCountResult(user.getId());
        cupoPage.setCaseCountResultList(addCaseCountZero(caseCountResult));
        //第二部分 每日案件情况（流入、结清、流出）
        cupoPage.setFlowInCaseToday(collectorCaseResult.getFlowInCaseToday());
        cupoPage.setFinishCaseToday(collectorCaseResult.getFinishCaseToday());
        cupoPage.setFlowOutCaseToday(collectorCaseResult.getFlowOutCaseToday());
//
        // 第三部分
////        CaseAmtResult caseAmtResult = cupoPageMapper.getCaseSumAmount(user.getUserName());
////        if(Objects.isNull(caseAmtResult)){
////            caseAmtResult = new CaseAmtResult();
////            caseAmtResult.setInAmount(new BigDecimal(0.00));
////            caseAmtResult.setWaitCupo(new BigDecimal(0.00));
////        }
////        cupoPage.setCaseAmtResult(caseAmtResult);
        // 第三部分 案件金额（回款总额、本月回款、今日回款）
        cupoPage.setMoneySumResult(collectorCaseResult.getMoneySumResult());
        cupoPage.setMonthMoneyResult(collectorCaseResult.getMonthMoneyResult());
        cupoPage.setDayMoneyResult(collectorCaseResult.getDayMoneyResult());
        // 第四部分 今日在线（今日累计催收、本月累计催收）
        cupoPage.setDayFollowCount(collectorCaseResult.getDayFollowCount());
        cupoPage.setMonthFollowCount(collectorCaseResult.getMonthFollowCount());
        // 第五部分 周回款统计
        List<WeekCountResult> repayWeek = cupoPageMapper.getRepayWeek(user.getUserName());
        cupoPage.setWeekRepaySum(addWeekListZero(repayWeek));

        // 第六部分 周催计数统计
        List<WeekCountResult> folWeek = cupoPageMapper.getFolWeek(user.getUserName());
        cupoPage.setWeekFollCount(addWeekListZero(folWeek));

        // 第七部分 本周结案数统计
        List<WeekCountResult> caseEndWeek = cupoPageMapper.getCaseEndWeek(user.getId());
        cupoPage.setWeekCaseEndCount(addWeekListZero(caseEndWeek));

        // 任务回款金额
        BigDecimal backCash = cupoPageMapper.getBackCash(user.getUserName());
        cupoPage.setBackCash(backCash);
        homePageResult.setData(cupoPage);
        return homePageResult;
    }


    //管理员首页第四部分 催收中催收数据
    public CollectionDateModel getCollectionedDate(CollectorRankingParams caseInfoConditionParams) {
        CollectionDateModel collectionDateModel = new CollectionDateModel();
        ProvinceDateModel provinceModel;
        List<ProvinceCollectionDateModel> list;
        ProvinceDateModel provinceModelOutsource;
        List<ProvinceCollectionDateModel> listOutsource;
        List<ProvinceCollectionDateModel> totalProvinceCollectionCount;
        //查询内催
        if (caseInfoConditionParams.getQueryType() == 1) {
            provinceModel = adminPageMapper.getInnerCollectionDate(caseInfoConditionParams);
            //获取内催案件催收中总金额
            collectionDateModel.setInnerCollectingAmt(Objects.isNull(provinceModel.getCollectingAmt()) ? BigDecimal.ZERO : provinceModel.getCollectingAmt());
            //获取内催案件催收中总数量
            collectionDateModel.setInnerCollectingCount(Objects.isNull(provinceModel.getCollectingCount()) ? 0 : provinceModel.getCollectingCount());
            //获取内催各省份的案件金额和案件总数
            list = adminPageMapper.getProvinceInnerCollectionDate(caseInfoConditionParams);
            collectionDateModel.setInnerProvinceCollectionCount(Objects.isNull(list) || list.size() == 0 ? null : list);
        }
        //查询委外
        if (caseInfoConditionParams.getQueryType() == 2) {
            provinceModel = adminPageMapper.getOutsourceCollectionDate(caseInfoConditionParams);
            //获取内催案件催收中总金额
            collectionDateModel.setOutsourceCollectingAmt(Objects.isNull(provinceModel.getCollectingAmt()) ? BigDecimal.ZERO : provinceModel.getCollectingAmt());
            //获取内催案件催收中总数量
            collectionDateModel.setOutsourceCollectingCount(Objects.isNull(provinceModel.getCollectingCount()) ? 0 : provinceModel.getCollectingCount());
            //获取内催各省份的案件金额和案件总数
            list = adminPageMapper.getProvinceOutsourceCollectionDate(caseInfoConditionParams);
            collectionDateModel.setOutsourceProvinceCollectionCount(Objects.isNull(list) || list.size() == 0 ? null : list);
        }
        //查询委外+内催
        if (caseInfoConditionParams.getQueryType() == 0) {
            provinceModel = adminPageMapper.getInnerCollectionDate(caseInfoConditionParams);
            //获取内催案件催收中总金额
            collectionDateModel.setInnerCollectingAmt(Objects.isNull(provinceModel.getCollectingAmt()) ? BigDecimal.ZERO : provinceModel.getCollectingAmt());
            //获取内催案件催收中总数量
            collectionDateModel.setInnerCollectingCount(Objects.isNull(provinceModel.getCollectingCount()) ? 0 : provinceModel.getCollectingCount());
            //获取内催各省份的案件金额和案件总数
            list = adminPageMapper.getProvinceInnerCollectionDate(caseInfoConditionParams);
            collectionDateModel.setInnerProvinceCollectionCount(Objects.isNull(list) || list.size() == 0 ? null : list);

            provinceModelOutsource = adminPageMapper.getOutsourceCollectionDate(caseInfoConditionParams);
            //获取内催案件催收中总金额
            collectionDateModel.setOutsourceCollectingAmt(Objects.isNull(provinceModelOutsource.getCollectingAmt()) ? BigDecimal.ZERO : provinceModelOutsource.getCollectingAmt());
            //获取内催案件催收中总数量
            collectionDateModel.setOutsourceCollectingCount(Objects.isNull(provinceModelOutsource.getCollectingCount()) ? 0 : provinceModelOutsource.getCollectingCount());
            //获取内催各省份的案件金额和案件总数
            listOutsource = adminPageMapper.getProvinceOutsourceCollectionDate(caseInfoConditionParams);
            collectionDateModel.setOutsourceProvinceCollectionCount(Objects.isNull(listOutsource) || listOutsource.size() == 0 ? null : listOutsource);

            //内催和委外总金额
            BigDecimal totalAmt = collectionDateModel.getInnerCollectingAmt().add(collectionDateModel.getOutsourceCollectingAmt());
            //内催和委外总数量
            Integer totalCount = collectionDateModel.getInnerCollectingCount() + collectionDateModel.getOutsourceCollectingCount();

            collectionDateModel.setTotalCollectionAmt(Objects.isNull(totalAmt) ? BigDecimal.ZERO : totalAmt);
            collectionDateModel.setTotalCollectionCount(Objects.isNull(totalCount) ? 0 : totalCount);
            totalProvinceCollectionCount = adminPageMapper.getTotalProvinceCollectionDate(caseInfoConditionParams);
            //合并内崔与委外的各省份的催收数量
            collectionDateModel.setTotalProvinceCollectionCount(Objects.isNull(totalProvinceCollectionCount) || totalProvinceCollectionCount.size() == 0 ? null : totalProvinceCollectionCount);
        }
        return collectionDateModel;
    }

    private List<WeekCountResult> addWeekListZero(List<WeekCountResult> weekCountResults) {
        if (weekCountResults.size() == 7) {
            return weekCountResults;
        }
        Integer[] items = {0, 1, 2, 3, 4, 5, 6};
        List<Integer> addList = new ArrayList<>();
        for (Integer item : items) {
            addList.add(item);
        }
        for (WeekCountResult weekCountResult : weekCountResults) {
            if (addList.contains(weekCountResult.getDayOfWeek())) {
                addList.remove(weekCountResult.getDayOfWeek());
            }
        }
        for (Integer value : addList) {
            WeekCountResult addResult = new WeekCountResult();
            addResult.setDayOfWeek(value);
            addResult.setAmount(new BigDecimal(0));
            addResult.setNum(0);
            weekCountResults.add(addResult);
        }
        weekCountResults.sort(Comparator.comparingInt(WeekCountResult::getDayOfWeek));
        return weekCountResults;
    }

    private List<CaseCountResult> addCaseCountZero(List<CaseCountResult> caseCountResultList) {
        // 案件状态
        Integer[] items = {20, 21, 22, 23, 24};
        List<Integer> addList = new ArrayList<>();
        for (Integer item : items) {
            addList.add(item);
        }

        for (CaseCountResult caseCountResult : caseCountResultList) {
            caseCountResult.initObject();
            if (addList.contains(caseCountResult.getStatus())) {
                addList.remove(caseCountResult.getStatus());
            }
        }
        for (Integer value : addList) {
            CaseCountResult caseCountResult = new CaseCountResult();
            caseCountResult.setStatus(value);
            caseCountResult.initObject();
            caseCountResultList.add(caseCountResult);
        }
        caseCountResultList.sort(Comparator.comparingInt(CaseCountResult::getStatus));
        return caseCountResultList;
    }

    /**
     * 催收员案件情况
     *
     * @param user
     * @return
     */
    private CollectorCaseResult getCollectorCaseResult(User user) {
        CollectorCaseResult collectorCaseResult = new CollectorCaseResult();
        Integer flowInCaseToday = cupoPageMapper.getFlowInCaseToday(user.getId()); //今日流入案件数
        Integer finishCaseToday = cupoPageMapper.getFinishCaseToday(user.getId()); //今日结案数
        Integer flowOutCaseToday = cupoPageMapper.getFlowOutCaseToday(user.getId()); //今日流出案件数
        BigDecimal moneySumResult = cupoPageMapper.getMoneySumResult(user.getUserName()); //回款总金额
        BigDecimal monthMoneyResult = cupoPageMapper.getMonthMoneyResult(user.getUserName()); // 本月回款金额
        BigDecimal dayMoneyResult = cupoPageMapper.getDayMoneyResult(user.getUserName()); // 本天回款金额
        Integer dayFollowCount = cupoPageMapper.getDayFollowCount(user.getUserName());//今日累计催收次数
        Integer monthFollowCount = cupoPageMapper.getMonthFollowCount(user.getUserName());// 本月累计催收次数
        collectorCaseResult.setFlowInCaseToday(flowInCaseToday);
        collectorCaseResult.setFinishCaseToday(finishCaseToday);
        collectorCaseResult.setFlowOutCaseToday(flowOutCaseToday);
        collectorCaseResult.setMoneySumResult(moneySumResult);
        collectorCaseResult.setDayMoneyResult(dayMoneyResult);
        collectorCaseResult.setMonthMoneyResult(monthMoneyResult);
        collectorCaseResult.setDayFollowCount(dayFollowCount);
        collectorCaseResult.setMonthFollowCount(monthFollowCount);
        return collectorCaseResult;
    }

    /**
     * 管理员首页 查询 已还款案件金额 已还款案件数量 还款审核中案件金额 还款审核中案件数量
     * 胡艳敏
     *
     * @param collectorRankingParams
     * @return
     */
    public ReturnDataModel getCaseAmtAndCount(CollectorRankingParams collectorRankingParams) throws ParseException {

        ReturnDataModel returnOutDataModel = new ReturnDataModel();
        ReturnDataModel returnDataModel = new ReturnDataModel();
        List<BigDecimal> hadAmountList = new ArrayList<>();
        List<Integer> hadCountList = new ArrayList<>();
        BigDecimal hadTotalAmount = new BigDecimal(0);
        Integer hadTotalCount = 0;
        BigDecimal applyTotalAmount = new BigDecimal(0);
        Integer applyTotalCount = 0;
        List<String> daylist = new ArrayList<>();
        //获取内催已还款数据
        List<AdminCasePaymentModel> paymentInnerModels = adminPageMapper.getCaseAmtAndCount(collectorRankingParams);
        //获取委外已还款数据
        List<AdminCasePaymentModel> paymentOutModels = adminPageMapper.getOutCaseAmtAndCount(collectorRankingParams);
        List<AdminCasePaymentModel> paymentApplyModels = adminPageMapper.getCaseApplyAmtAndCount(collectorRankingParams);
        if (collectorRankingParams.getQueryType() == 1) {
            returnDataModel = getCaseAmt(paymentInnerModels, paymentApplyModels, collectorRankingParams, returnDataModel);
            return returnDataModel;
        }
        if (collectorRankingParams.getQueryType() == 2) {
            returnDataModel = getCaseAmt(paymentOutModels, paymentApplyModels, collectorRankingParams, returnDataModel);
            return returnDataModel;
        }
        //同时请求内催和委外
        if (collectorRankingParams.getQueryType() == 0) {
            returnDataModel = getCaseAmt(paymentInnerModels, paymentApplyModels, collectorRankingParams, returnDataModel);
            returnOutDataModel = getCaseAmt(paymentOutModels, paymentApplyModels, collectorRankingParams, returnOutDataModel);

            hadTotalAmount = returnDataModel.getHadTotalCaseAmount().add(returnOutDataModel.getHadTotalCaseAmount());
            hadTotalCount = returnDataModel.getHadTotalCaseCount() + returnOutDataModel.getHadTotalCaseCount();
            applyTotalAmount = returnDataModel.getApplyTotalCaseAmount();
            applyTotalCount = returnDataModel.getApplyTotalCaseCount();
            returnDataModel.setHadTotalCaseAmount(hadTotalAmount);
            returnDataModel.setHadTotalCaseCount(hadTotalCount);
            returnDataModel.setApplyTotalCaseAmount(applyTotalAmount);
            returnDataModel.setApplyTotalCaseCount(applyTotalCount);
            returnDataModel.setApplyAmountList(returnDataModel.getApplyAmountList());
            returnDataModel.setApplyCountList(returnDataModel.getApplyCountList());
            for (int i = 0; i < returnDataModel.getHadAmountList().size(); i++) {
                Integer hadCount;
                BigDecimal hadAmt = new BigDecimal(0.00);
                hadAmt = returnDataModel.getHadAmountList().get(i).add(returnOutDataModel.getHadAmountList().get(i));
                hadCount = returnDataModel.getHadCountList().get(i) + returnOutDataModel.getHadCountList().get(i);
                hadAmountList.add(hadAmt);
                hadCountList.add(hadCount);
            }
            returnDataModel.setHadAmountList(hadAmountList);
            returnDataModel.setHadCountList(hadCountList);
        }
        return returnDataModel;
    }

    /**
     *获取总的金额和数量
     *
     * */
    public ReturnDataModel getCaseAmt(List<AdminCasePaymentModel> paymentModels, List<AdminCasePaymentModel> paymentApplyModels, CollectorRankingParams collectorRankingParams, ReturnDataModel returnDataModel) throws ParseException {

        List<BigDecimal> hadAmountList = new ArrayList<>();
        List<Integer> hadCountList = new ArrayList<>();
        List<BigDecimal> applyAmountList = new ArrayList<>();
        List<Integer> applyCountList = new ArrayList<>();
        BigDecimal hadTotalAmount = new BigDecimal(0);
        Integer hadTotalCount = 0;
        BigDecimal applyTotalAmount = new BigDecimal(0);
        Integer applyTotalCount = 0;
        List<String> daylist = new ArrayList<>();
        List<String> dayListNew = new ArrayList<>();
        //获取内催已还款案件的金额和总数量
        if (paymentModels.size() != 0) {
            for (AdminCasePaymentModel ad : paymentModels) {
                if (Objects.nonNull(ad.getCaseAmount())) {
                    hadTotalAmount = hadTotalAmount.add(ad.getCaseAmount());
                    hadTotalCount += ad.getCaseCount();
                }
            }
        }
        //获取还款审核中案件的金额和总数量
        if (paymentApplyModels.size() != 0) {
            for (AdminCasePaymentModel ad : paymentApplyModels) {
                if (Objects.nonNull(ad.getCaseAmount())) {
                    applyTotalAmount = applyTotalAmount.add(ad.getCaseAmount());
                    applyTotalCount = +ad.getCaseCount();
                }
            }
        }
        if (Objects.equals(collectorRankingParams.getTimeType(), CollectorRankingParams.TimeType.YEAR.getValue())) {
            //年
            for (String tempMonth : Constants.monthList) {
                setReturnData(paymentModels, tempMonth, hadAmountList, hadCountList);
                setReturnData(paymentApplyModels, tempMonth, applyAmountList, applyCountList);
            }
            //月
        } else if (Objects.equals(collectorRankingParams.getTimeType(), CollectorRankingParams.TimeType.MONTH.getValue())) {
            daylist = getMonthDay(collectorRankingParams.getStartDate());
            for (String tempDay : daylist) {
                setReturnData(paymentModels, tempDay, hadAmountList, hadCountList);
                setReturnData(paymentApplyModels, tempDay, applyAmountList, applyCountList);
            }
            //周
        } else {
            for (String tempDay : Constants.weekList) {
                setReturnData(paymentModels, tempDay, hadAmountList, hadCountList);
                setReturnData(paymentApplyModels, tempDay, applyAmountList, applyCountList);
            }
        }
        returnDataModel.setHadAmountList(hadAmountList);
        returnDataModel.setHadCountList(hadCountList);
        returnDataModel.setHadTotalCaseAmount(hadTotalAmount);
        returnDataModel.setHadTotalCaseCount(hadTotalCount);
        returnDataModel.setApplyAmountList(applyAmountList);
        returnDataModel.setApplyCountList(applyCountList);
        returnDataModel.setApplyTotalCaseAmount(applyTotalAmount);
        returnDataModel.setApplyTotalCaseCount(applyTotalCount);
        //配合前段，前段需要添加"日"
        for (String day : daylist) {
            day = day + "日";
            dayListNew.add(day);
        }
        returnDataModel.setDayList(dayListNew);
        return returnDataModel;
    }

    /*
    将数据库查询出来的数据组装成前端需要的数据
     */
    public void setReturnData(List<AdminCasePaymentModel> paymentApplyModels, String temp, List<BigDecimal> applyAmountList, List<Integer> applyCountList) {
        boolean isExist = false;
        for (AdminCasePaymentModel adminCasePaymentModel : paymentApplyModels) {
            //年-获取每个月
            if (Objects.nonNull(adminCasePaymentModel.getQueryMonth()) && adminCasePaymentModel.getQueryMonth().equals(temp)) {
                applyAmountList.add(Objects.isNull(adminCasePaymentModel.getCaseAmount()) ? BigDecimal.ZERO : adminCasePaymentModel.getCaseAmount());
                applyCountList.add(Objects.isNull(adminCasePaymentModel.getCaseCount()) ? 0 : adminCasePaymentModel.getCaseCount());
                isExist = true;
            }
            //月- 获取当月的每一天
            if (Objects.nonNull(adminCasePaymentModel.getQueryDate()) && adminCasePaymentModel.getQueryDate().substring(8, 10).equals(temp)) {
                applyAmountList.add(Objects.isNull(adminCasePaymentModel.getCaseAmount()) ? BigDecimal.ZERO : adminCasePaymentModel.getCaseAmount());
                applyCountList.add(Objects.isNull(adminCasePaymentModel.getCaseCount()) ? 0 : adminCasePaymentModel.getCaseCount());
                isExist = true;
            }
            //周- 获取周的每一天
            if (Objects.nonNull(adminCasePaymentModel.getQueryWeek()) && adminCasePaymentModel.getQueryWeek().substring(8, 9).equals(temp)) {
                applyAmountList.add(Objects.isNull(adminCasePaymentModel.getCaseAmount()) ? BigDecimal.ZERO : adminCasePaymentModel.getCaseAmount());
                applyCountList.add(Objects.isNull(adminCasePaymentModel.getCaseCount()) ? 0 : adminCasePaymentModel.getCaseCount());
                isExist = true;
            }
        }
        if (!isExist) {
            applyAmountList.add(new BigDecimal(0));
            applyCountList.add(0);
        }
    }

    //管理员首页第二部分
    public PromisedDateModel getCaseGroupInfo(CollectorRankingParams params) {
        //案件还款意向数据案件金额集合
        PromisedDateModel promisedDateModel = new PromisedDateModel();
        List<PromisedModel> totalAmtList = new ArrayList<>();
        //案件还款意向数据案件数量集合
        List<PromisedModel> totalCountList = new ArrayList<>();
        List<CaseRepaymentTypeGroupInfo> modeList = adminPageMapper.getCaseGroupInfo(params);
        CaseFollowupRecord.EffectiveCollection[] feedBacks = CaseFollowupRecord.EffectiveCollection.values(); //有效催收反馈
        if (Objects.nonNull(modeList) && modeList.size() != 0) {

            //案件还款意向数据案件金额集合
            List<Integer> typeList = new ArrayList<>();
            for (CaseRepaymentTypeGroupInfo caseRepaymentTypeGroupInfo : modeList) {
                PromisedModel proAmount = new PromisedModel();
                PromisedModel proCount = new PromisedModel();

                for (int i = 0; i < feedBacks.length; i++) {
                    String name = "";
                    //获得有效联络的名称,如果存在有效联络，则把相应的有效联络加入相应的参数，如果无，则放入名称，其他值为0
                    if (feedBacks[i].getValue().equals(caseRepaymentTypeGroupInfo.getRePaymentType())) {
                        name = feedBacks[i].getRemark();
                        proAmount.setIndex(feedBacks[i].getValue());
                        proAmount.setName(name);
                        proAmount.setValue(caseRepaymentTypeGroupInfo.getTotalRePaymentMoney().toString());
                        totalAmtList.add(proAmount);

                        proCount.setIndex(feedBacks[i].getValue());
                        proCount.setName(name);
                        proCount.setValue(caseRepaymentTypeGroupInfo.getTotalCaseNumber().toString());
                        totalCountList.add(proCount);
                        typeList.add(feedBacks[i].getValue());
                        break;
                    }
                }
            }
            for (int i = 0; i < feedBacks.length; i++) {
                if (!typeList.contains(feedBacks[i].getValue())) {
                    PromisedModel proAmount = new PromisedModel();
                    proAmount.setIndex(feedBacks[i].getValue());
                    proAmount.setName(feedBacks[i].getRemark());
                    proAmount.setValue("0");
                    totalAmtList.add(proAmount);
                    totalCountList.add(proAmount);
                }
            }
        } else {
            //配合前端，如果查到空数据，需要对其值塞入0
            for (int i = 0; i < feedBacks.length; i++) {
                PromisedModel promisedModel = new PromisedModel();
                promisedModel.setIndex(feedBacks[i].getValue());
                promisedModel.setName(feedBacks[i].getRemark());
                promisedModel.setValue("0");
                totalAmtList.add(promisedModel);
                totalCountList.add(promisedModel);
            }
        }
        totalAmtList.sort(Comparator.comparing(PromisedModel::getIndex));
        totalCountList.sort(Comparator.comparing(PromisedModel::getIndex));
        promisedDateModel.setTotalAmtList(totalAmtList);
        promisedDateModel.setTotalCountList(totalCountList);
        return promisedDateModel;
    }

    //管理员首页第三部分
    public FollowCalledDateModel getRecordReport(CollectorRankingParams params) {

        FollowCalledDateModel followCalledDateModel = new FollowCalledDateModel();
        List<String> dayListNew = new ArrayList<>();
        //接收外呼数
        List<GroupMonthFollowRecord> callRecordList = new ArrayList<>();
        //接收催记数
        List<GroupMonthFollowRecord> followRecordList = new ArrayList<>();
        //接收外呼数
        List<Integer> callRecordListEmpty = new ArrayList<>();
        //接收催记数
        List<Integer> followRecordListEmpty = new ArrayList<>();
        //获取所选月份每一天的催记数和外呼数
        List<GroupMonthFollowRecord> result = adminPageMapper.getRecordReport(params);
        Integer followTotalCount = 0;
        Integer callTotalCount = 0;
        //催记平均数量
        Integer followAvgCount = 0;
        //呼叫平均数量
        Integer callAvgCount = 0;
        //分离外呼和催记数
        for (GroupMonthFollowRecord record : result) {
            if (Objects.nonNull(record.getWayType())) {
                //催记数
                if (record.getWayType() == 1) {
                    followRecordList.add(record);
                    followTotalCount += record.getTypeCount();
                } else {
                    callRecordList.add(record);
                    callTotalCount += record.getTypeCount();
                }
            }
        }
        if (Objects.nonNull(followRecordList.size()) && followRecordList.size() != 0) {
            followAvgCount = followTotalCount / followRecordList.size();
        }
        if (Objects.nonNull(callRecordList.size()) && callRecordList.size() != 0) {
            callAvgCount = callTotalCount / callRecordList.size();
        }

        followCalledDateModel.setFollowTotalCount(followTotalCount);
        followCalledDateModel.setFollowAvgCount(followAvgCount);
        followCalledDateModel.setCallTotalCount(callTotalCount);
        followCalledDateModel.setCallAvgCount(callAvgCount);
        List<String> daylist = getMonthDay(params.getStartDate());
        for (String tempDay : daylist) {
            followRecordListEmpty = setFollowCalledReturnData(followRecordList, tempDay, followRecordListEmpty);
            callRecordListEmpty = setFollowCalledReturnData(callRecordList, tempDay, callRecordListEmpty);
        }
        followCalledDateModel.setFollowCountList(followRecordListEmpty);
        followCalledDateModel.setCallCountList(callRecordListEmpty);
        //配合前段，前段需要添加"日"
        for (String day : daylist) {
            day = day + "日";
            dayListNew.add(day);
        }
        followCalledDateModel.setDayList(dayListNew);
        return followCalledDateModel;
    }

    //管理员催收员首页 - 第六部分 催收计数排名
    public CaseInfoRank getCaseInfoRecordFollowRank(CollectorRankingParams params) {

        CaseInfoRank caseInfoRank = new CaseInfoRank();
        List<FollowCountModel> followCountModels = adminPageMapper.getCaseInfoRecordFollowRank(params);
        caseInfoRank.setFollowCountModels(followCountModels);
        return caseInfoRank;
    }

    /**
     * 将所选月份的每一天的天数及count返回
     */
    public List<Integer> setFollowCalledReturnData(List<GroupMonthFollowRecord> recordList, String tempDay, List<Integer> recordListEmpty) {

        boolean isExist = false;
        if (Objects.nonNull(recordList) && recordList.size() != 0) {
            for (GroupMonthFollowRecord GroupMonthFollowRecord : recordList) {
                if (GroupMonthFollowRecord.getCurrentMonth().substring(8, 10).equals(tempDay)) {
                    recordListEmpty.add(GroupMonthFollowRecord.getTypeCount());
                    isExist = true;
                }
            }
        }
        if (!isExist) {
            recordListEmpty.add(0);
        }
        return recordListEmpty;
    }

    /**
     * 某一年某个月的每一天
     */
    public List<String> getMonthFullDay(String date) {
        List<String> fullDayList = new ArrayList();
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(5, 7));
        int day = 1;// 所有月份从1号开始
        Calendar cal = Calendar.getInstance();// 获得当前日期对象
        cal.clear();// 清除信息
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);// 1月从0开始
        cal.set(Calendar.DAY_OF_MONTH, day);
        int count = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int j = 0; j <= (count - 1); ) {
            if (sdf.format(cal.getTime()).equals(getLastDay(year, month)))
                break;
            cal.add(Calendar.DAY_OF_MONTH, j == 0 ? +0 : +1);
            j++;
            fullDayList.add(sdf.format(cal.getTime()));
        }
        return fullDayList;
    }

    public String getLastDay(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, 0);
        return sdf.format(cal.getTime());
    }

    /**
     * 获取每一天
     */
    public List<String> getMonthDay(String date) {
        List<String> list = getMonthFullDay(date);
        List<String> listDay = new ArrayList<>();
        for (String date1 : list) {
            listDay.add(date1.substring(8, 10));
        }
        return listDay;
    }
}