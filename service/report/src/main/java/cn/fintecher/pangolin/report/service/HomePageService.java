package cn.fintecher.pangolin.report.service;

import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.report.mapper.AdminPageMapper;
import cn.fintecher.pangolin.report.mapper.CupoPageMapper;
import cn.fintecher.pangolin.report.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    @Autowired
    private AdminPageMapper adminPageMapper;
    @Autowired
    private CupoPageMapper cupoPageMapper;

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
        List<User> allUser = adminPageMapper.getAllUserOnDepartment(code);
        List<User> users = adminPageMapper.getAllUserOnCompany(user.getCompanyCode()); //该系统中所有的用户数
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
        adminPage.setCupoSum(users.size());
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

    private HomePageResult getCollectPage(User user) {
        HomePageResult<CupoPage> homePageResult = new HomePageResult<>();
        homePageResult.setType(user.getManager());
        CupoPage cupoPage = new CupoPage();

        CollectorCaseResult collectorCaseResult = getCollectorCaseResult(user);

        // 第一部分 本月完成度
        Double taskFinished = cupoPageMapper.getTodyTashFinished(user.getUserName());
        taskFinished = Objects.isNull(taskFinished) ? 0D : taskFinished >1D ? 1D : taskFinished;
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
        homePageResult.setData(cupoPage);
        return homePageResult;
    }

    private List<WeekCountResult> addWeekListZero (List<WeekCountResult> weekCountResults){
        if(weekCountResults.size() == 7){
            return weekCountResults;
        }
        Integer[] items = {0,1,2,3,4,5,6};
        List<Integer> addList = new ArrayList<>();
        for(Integer item : items){
            addList.add(item);
        }
        for(WeekCountResult weekCountResult : weekCountResults){
            if(addList.contains(weekCountResult.getDayOfWeek())){
                addList.remove(weekCountResult.getDayOfWeek());
            }
        }
        for(Integer value : addList){
            WeekCountResult addResult = new WeekCountResult();
            addResult.setDayOfWeek(value);
            addResult.setAmount(new BigDecimal(0));
            addResult.setNum(0);
            weekCountResults.add(addResult);
        }
        weekCountResults.sort(Comparator.comparingInt(WeekCountResult::getDayOfWeek));
        return weekCountResults;
    }

    private List<CaseCountResult> addCaseCountZero(List<CaseCountResult> caseCountResultList){
        // 案件状态
        Integer[] items = {20,21,22,23,14};
        List<Integer> addList = new ArrayList<>();
        for(Integer item : items){
            addList.add(item);
        }

        for(CaseCountResult caseCountResult : caseCountResultList){
            caseCountResult.initObject();
            if(addList.contains(caseCountResult.getStatus())){
                addList.remove(caseCountResult.getStatus());
            }
        }
        for(Integer value : addList){
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
