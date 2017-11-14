package cn.fintecher.pangolin.report.service;

import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.report.mapper.AdminPageMapper;
import cn.fintecher.pangolin.report.mapper.CaseInfoMapper;
import cn.fintecher.pangolin.report.mapper.CollectPageMapper;
import cn.fintecher.pangolin.report.mapper.CupoPageMapper;
import cn.fintecher.pangolin.report.model.*;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
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

    //催收员首页 - 第一，二部分 本周和本月完成进度
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
        Integer caseMonthTotalCount = collectPageMapper.getCaseInfoMonthAllCount(user.getId());
        //本月已结案案件总数
        Integer caseMonthFinishedCount = collectPageMapper.getCaseInfoMonthClosedCount(user.getUserName());
        //本月需回款总案件个数
        Integer caseMonthBackTotalCount = collectPageMapper.getMonthTotalBackCash(user.getId());
        //本月已回款案件个数
        Integer caseMonthBackFinishedCount = collectPageMapper.getMonthHadBackCash(user.getUserName());

        collectPage.setCaseWeekTotalCount(Objects.isNull(caseWeekTotalCount) ? 0 : caseWeekTotalCount);
        collectPage.setCaseWeekFinishedCount(Objects.isNull(caseWeekFinishedCount) ? 0 : caseWeekFinishedCount);
        collectPage.setCaseWeekBackTotalCount(Objects.isNull(caseWeekBackTotalCount) ? 0 : caseWeekBackTotalCount);
        collectPage.setCaseWeekBackFinishedCount(Objects.isNull(caseWeekBackFinishedCount) ? 0 : caseWeekBackFinishedCount);

        collectPage.setCaseMonthTotalCount(Objects.isNull(caseMonthTotalCount) ? 0 : caseMonthTotalCount);
        collectPage.setCaseMonthFinishedCount(Objects.isNull(caseMonthFinishedCount) ? 0 : caseMonthFinishedCount);
        collectPage.setCaseMonthBackTotalCount(Objects.isNull(caseMonthBackTotalCount) ? 0 : caseMonthBackTotalCount);
        collectPage.setCaseMonthBackFinishedCount(Objects.isNull(caseMonthBackFinishedCount) ? 0 : caseMonthBackFinishedCount);
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
        caseStatusTotalPreview.setToFollowCaseCount(Objects.isNull(toFollowCaseCount) ? 0 : toFollowCaseCount);
        caseStatusTotalPreview.setFollowingCaseCount(Objects.isNull(followingCaseCount) ? 0 : followingCaseCount);
        caseStatusTotalPreview.setCommitmentBackCaseCount(Objects.isNull(commitmentBackCaseCount) ? 0 : commitmentBackCaseCount);
        caseStatusTotalPreview.setFlowInCaseToday(Objects.isNull(flowInCaseToday) ? 0 : flowInCaseToday);
        caseStatusTotalPreview.setFinishCaseToday(Objects.isNull(finishCaseToday) ? 0 : finishCaseToday);
        caseStatusTotalPreview.setFlowOutCaseToday(Objects.isNull(flowOutCaseToday) ? 0 : flowOutCaseToday);
        return caseStatusTotalPreview;
    }

    //催收员首页 - 第五部分 催收员回款排名
    public CaseInfoRank getCollectedCaseBackRank(User user, String depCode) {

        CaseInfoRank caseInfoRank = new CaseInfoRank();
        List<BackAmtModel> backAmtModels = collectPageMapper.getCaseInfoBackRank(depCode);
        for (int i = 0; i < backAmtModels.size(); i++) {
            if (Objects.isNull(backAmtModels.get(i).getCollectionName())) {
                backAmtModels.remove(backAmtModels.get(i));
            } else {
                if (user.getRealName().equals(backAmtModels.get(i).getCollectionName())) {
                    if (i == 0) {
                        i = +1;
                    }
                    //添加该催收员的排名
                    caseInfoRank.setCollectRank(i);
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
    public CaseInfoRank getCollectedFollowedRank(User user, String depName) {

        CaseInfoRank caseInfoRank = new CaseInfoRank();
        List<FollowCountModel> followCountModels = collectPageMapper.getCaseInfoFollowRank(depName);
        for (int i = 0; i < followCountModels.size(); i++) {
            if (Objects.isNull(followCountModels.get(i).getCollectionFollowName())) {
                followCountModels.remove(followCountModels.get(i));
            } else {
                if (user.getRealName().equals(followCountModels.get(i).getCollectionFollowName())) {
                    if (i == 0) {
                        i = +1;
                    }
                    //添加该催收员的排名
                    caseInfoRank.setCollectRank(i);
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
                caseInfoConditionParams.getAreaId(),
                user.getType(),
                user.getManager(),
                user.getId(),
                caseInfoConditionParams.getRealPayMaxAmt(),
                caseInfoConditionParams.getRealPayMinAmt());
        //获取待催收安建宁的第一条案件进行快速催收
        CaseInfoModel caseInfoModel = caseInfoModels.get(0);
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

        ReturnDataModel returnDataModel = new ReturnDataModel();
        List<BigDecimal> hadAmountList = new ArrayList<>();
        List<Integer> hadCountList = new ArrayList<>();
        List<BigDecimal> applyAmountList = new ArrayList<>();
        List<Integer> applyCountList = new ArrayList<>();
        BigDecimal hadTotalAmount = new BigDecimal(0);
        Integer hadTotalCount = 0;
        BigDecimal applyTotalAmount = new BigDecimal(0);
        Integer applyTotalCount = 0;

        List<AdminCasePaymentModel> paymentModels = adminPageMapper.getCaseAmtAndCount(collectorRankingParams);
        List<AdminCasePaymentModel> paymentApplyModels = adminPageMapper.getCaseApplyAmtAndCount(collectorRankingParams);
        if (Objects.equals(collectorRankingParams.getTimeType(), CollectorRankingParams.TimeType.YEAR.getValue())) {
            //年
            for (String tempMonth : Constants.monthList) {
                SetReturnData(paymentModels, tempMonth, hadAmountList, hadCountList, hadTotalAmount, hadTotalCount, collectorRankingParams.getTimeType());
                SetReturnData(paymentApplyModels, tempMonth, applyAmountList, applyCountList, applyTotalAmount, applyTotalCount, collectorRankingParams.getTimeType());
            }
        } else {
            for (String tempDay : Constants.dayList) {
                SetReturnData(paymentModels, tempDay, hadAmountList, hadCountList, hadTotalAmount, hadTotalCount, collectorRankingParams.getTimeType());
                SetReturnData(paymentApplyModels, tempDay, applyAmountList, applyCountList, applyTotalAmount, applyTotalCount, collectorRankingParams.getTimeType());
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
        return returnDataModel;
    }

    /*
    将数据库查询出来的数据组装成前端需要的数据
     */
    public void SetReturnData(List<AdminCasePaymentModel> paymentApplyModels, String temp, List<BigDecimal> applyAmountList, List<Integer> applyCountList, BigDecimal applyTotalAmount, Integer applyTotalCount, Integer timeType) {
        boolean isExist = false;
        for (AdminCasePaymentModel adminCasePaymentModel : paymentApplyModels) {
            if (adminCasePaymentModel.getQueryMonth().endsWith(temp)) {
                applyAmountList.add(adminCasePaymentModel.getCaseAmount());
                applyCountList.add(adminCasePaymentModel.getCaseCount());
                applyTotalAmount = applyTotalAmount.add(adminCasePaymentModel.getCaseAmount());
                applyTotalCount += adminCasePaymentModel.getCaseCount();
                isExist = true;
            }
        }
        if (!isExist) {
            applyAmountList.add(new BigDecimal(0));
            applyCountList.add(0);
        }
    }

}
