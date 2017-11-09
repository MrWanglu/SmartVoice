package cn.fintecher.pangolin.report.service;

import cn.fintecher.pangolin.entity.User;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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

    //第一部分 本周和本月完成进度
    public CollectPage getCollectedWeekOrMonthPage(User user) {

        CollectPage collectPage = new CollectPage();
        //本周流入案件总数
        Integer caseWeekTotalCount = collectPageMapper.getCaseInfoWeekAllCount(user.getId());
        //本周已结案案件总数
        Integer caseWeekFinishedCount = collectPageMapper.getCaseInfoWeekClosedCount(user.getId());
        //本周需回款总案件个数
        Integer caseWeekBackTotalCount = collectPageMapper.getWeekTotalBackCash(user.getId());
        //本周已回款案件个数
        Integer caseWeekBackFinishedCount = collectPageMapper.getWeekHadBackCash(user.getId());
        //本月流入案件总数
        Integer caseMonthTotalCount = collectPageMapper.getCaseInfoMonthAllCount(user.getUserName());
        //本月已结案案件总数
        Integer caseMonthFinishedCount = collectPageMapper.getCaseInfoMonthClosedCount(user.getUserName());
        //本月需回款总案件个数
        Integer caseMonthBackTotalCount = collectPageMapper.getMonthTotalBackCash(user.getUserName());
        //本月已回款案件个数
        Integer caseMonthBackFinishedCount = collectPageMapper.getMonthHadBackCash(user.getUserName());

        collectPage.setCaseWeekTotalCount(Objects.nonNull(caseWeekTotalCount) ? 0 : caseWeekTotalCount);
        collectPage.setCaseWeekFinishedCount(Objects.nonNull(caseWeekFinishedCount) ? 0 : caseWeekFinishedCount);
        collectPage.setCaseWeekBackTotalCount(Objects.nonNull(caseWeekBackTotalCount) ? 0 : caseWeekBackTotalCount);
        collectPage.setCaseWeekBackFinishedCount(Objects.nonNull(caseWeekBackFinishedCount) ? 0 : caseWeekBackFinishedCount);

        collectPage.setCaseMonthTotalCount(Objects.nonNull(caseMonthTotalCount) ? 0 : caseMonthTotalCount);
        collectPage.setCaseMonthFinishedCount(Objects.nonNull(caseMonthFinishedCount) ? 0 : caseMonthFinishedCount);
        collectPage.setCaseMonthBackTotalCount(Objects.nonNull(caseMonthBackTotalCount) ? 0 : caseMonthBackTotalCount);
        collectPage.setCaseMonthBackFinishedCount(Objects.nonNull(caseMonthBackFinishedCount) ? 0 : caseMonthBackFinishedCount);
        return collectPage;
    }

    // 第三部分 跟催量总览
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
        onlineTime = onlineTime / 3600;
        DecimalFormat df = new DecimalFormat("######0");
        String onLine = df.format(onlineTime);
        //离线时长
        Integer offlineTime = 24 - Integer.parseInt(onLine);
        previewTotalFollowModel.setCurrentDayCalled(Objects.nonNull(currentDayCalled) ? 0 : currentDayCalled);
        previewTotalFollowModel.setCurrentWeekCalled(Objects.nonNull(currentWeekCalled) ? 0 : currentWeekCalled);
        previewTotalFollowModel.setCurrentMonthCalled(Objects.nonNull(currentMonthCalled) ? 0 : currentMonthCalled);
        previewTotalFollowModel.setCurrentDayCount(Objects.nonNull(currentDayCount) ? 0 : currentDayCount);
        previewTotalFollowModel.setCurrentWeekCount(Objects.nonNull(currentWeekCount) ? 0 : currentWeekCount);
        previewTotalFollowModel.setCurrentMonthCount(Objects.nonNull(currentMonthCount) ? 0 : currentMonthCount);
        previewTotalFollowModel.setOnlineTime(Integer.parseInt(onLine));
        previewTotalFollowModel.setOfflineTime(offlineTime);
        return previewTotalFollowModel;
    }

    // 第四部分 案件状况总览
    public CaseStatusTotalPreview getPreviewCaseStatus(User user) {

        CaseStatusTotalPreview caseStatusTotalPreview = new CaseStatusTotalPreview();
        //未催收案件数
        Integer toFollowCaseCount = collectPageMapper.getCaseInfoToFollowCount(user.getUserName());
        //催收中案件数
        Integer followingCaseCount = collectPageMapper.getCaseInfoFollowingCount(user.getUserName());
        //承诺还款案件数
        Integer commitmentBackCaseCount = collectPageMapper.getCaseInfoPromisedCount(user.getUserName());
        //今日流入案件
        Integer flowInCaseToday = collectPageMapper.getFlowInCaseToday(user.getId());
        //今日结清案件
        Integer finishCaseToday = collectPageMapper.getFinishCaseToday(user.getId());
        //今日流出案件
        Integer flowOutCaseToday = collectPageMapper.getFlowOutCaseToday(user.getId());
        caseStatusTotalPreview.setToFollowCaseCount(Objects.nonNull(toFollowCaseCount) ? 0 : toFollowCaseCount);
        caseStatusTotalPreview.setFollowingCaseCount(Objects.nonNull(followingCaseCount) ? 0 : followingCaseCount);
        caseStatusTotalPreview.setCommitmentBackCaseCount(Objects.nonNull(commitmentBackCaseCount) ? 0 : commitmentBackCaseCount);
        caseStatusTotalPreview.setFlowInCaseToday(Objects.nonNull(flowInCaseToday) ? 0 : flowInCaseToday);
        caseStatusTotalPreview.setFinishCaseToday(Objects.nonNull(finishCaseToday) ? 0 : finishCaseToday);
        caseStatusTotalPreview.setFlowOutCaseToday(Objects.nonNull(flowOutCaseToday) ? 0 : flowOutCaseToday);
        return caseStatusTotalPreview;
    }

    // 第五部分 催收员回款排名
    public CaseInfoRank getCollectedCaseBackRank(User user) {

        CaseInfoRank caseInfoRank = new CaseInfoRank();
        List<BackAmtModel> backAmtModels = collectPageMapper.getCaseInfoBackRank();
        for (int i = 0; i < backAmtModels.size(); i++) {
            if (Objects.isNull(backAmtModels.get(i).getCollectionName())) {
                backAmtModels.remove(backAmtModels.get(i));
            } else {
                if (user.getRealName().equals(backAmtModels.get(i).getCollectionName())) {
                    caseInfoRank.setCollectRank(i);
                }
            }
            if (Objects.nonNull(backAmtModels.get(i).getBackRate())) {
                BigDecimal bigDecimal = new BigDecimal(backAmtModels.get(i).getBackRate());
                backAmtModels.get(i).setBackRate(bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
        }
        caseInfoRank.setBackAmtModels(backAmtModels);
        return caseInfoRank;
    }

    // 第六部分 催收计数排名
    public CaseInfoRank getCollectedFollowedRank(User user) {

        CaseInfoRank caseInfoRank = new CaseInfoRank();
        List<FollowCountModel> followCountModels = collectPageMapper.getCaseInfoFollowRank();
        for (int i = 0; i < followCountModels.size(); i++) {
            if (Objects.isNull(followCountModels.get(i).getCollectionFollowName())) {
                followCountModels.remove(followCountModels.get(i));
            } else {
                if (user.getRealName().equals(followCountModels.get(i).getCollectionFollowName())) {
                    caseInfoRank.setCollectRank(i);
                }
            }
        }
        caseInfoRank.setFollowCountModels(followCountModels);
        return caseInfoRank;
    }

    public CaseInfoModel quickAccessCaseInfo(User user, CaseInfoConditionParams caseInfoConditionParams) {

        String sort = "";
        String newSort = "";
        if (Objects.nonNull(caseInfoConditionParams.getSort())) {
            sort = caseInfoConditionParams.getSort();
            newSort = sort.replace(",", " ");
        }
        List<CaseInfoModel> caseInfoModels = caseInfoMapper.getCaseInfoByCondition(StringUtils.trim(caseInfoConditionParams.getPersonalName()),
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
                caseInfoConditionParams.getAreaId(), 0, 0, "");
        CaseInfoModel caseInfoModel = caseInfoModels.get(0);
        return caseInfoModel;
    }

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

}
