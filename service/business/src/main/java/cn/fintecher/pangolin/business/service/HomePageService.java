package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.*;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.entity.User;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
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

    @Inject
    private AdminPageRepository adminPageRepository;
    @Inject
    private CupoPageRepository cupoPageRepository;
    @Inject
    private UserService userService;
    @Inject
    private UserRepository userRepository;
    @Inject
    private CaseInfoRepository caseInfoRepository;
    @Inject
    private CaseAssistRepository caseAssistRepository;

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
        //  管理者部门
        String code = user.getDepartment().getCode();

        List<User> allUser = userService.getAllUser(user.getDepartment().getId(), 0);//获取部门下所有的用户
        List<User> users = userRepository.findAll(); //该系统中所有的用户数
        //第一部分 案件总金额 人均金额
        BigDecimal caseSumAmt = adminPageRepository.getCaseSumAmt(code); //案件总金额
        Integer deptUserSum = allUser.size();
        deptUserSum = deptUserSum == 0 ? 1 : deptUserSum;
        BigDecimal personCount = new BigDecimal(deptUserSum);
        adminPage.setCaseSumAmt(Objects.isNull(caseSumAmt) ? new BigDecimal(0.00) : caseSumAmt.setScale(2, BigDecimal.ROUND_HALF_UP));
        adminPage.setCaseSumAmtPerson(adminPage.getCaseSumAmt().divide(personCount, 2, BigDecimal.ROUND_HALF_UP));

        // 第二部分 案件已还款总金额 人均金额
        BigDecimal repaySumAmt = adminPageRepository.getRepaySumAmt(code);
        adminPage.setRepaySumAmt(Objects.isNull(repaySumAmt) ? new BigDecimal(0.00) : repaySumAmt.setScale(2, BigDecimal.ROUND_HALF_UP));
        adminPage.setRepaySumAmtPerson(adminPage.getRepaySumAmt().divide(personCount, 2, BigDecimal.ROUND_HALF_UP));

        // 第三部分 催收员总数 在线人数 离线人数
        adminPage.setCupoSum(users.size());
        adminPage.setCupoOnlineSum(0);
        adminPage.setCupoOfflineSum(0);


        //第四部分 客户总数 在案客户总数
        Integer custSum = adminPageRepository.getCustNum(code);
        Integer custSumIn = adminPageRepository.getCustNumIN(code);
        adminPage.setCustSum(Objects.isNull(custSum) ? 0 : custSum);
        adminPage.setCustSumIn(Objects.isNull(custSumIn) ? 0 : custSumIn);


        //第五部分 周回款金额
        List<Object[]> weekRepaySumAmt = adminPageRepository.getWeekRepaySumAmt(code);
        List<WeekCountResult> weekRepayList = new ArrayList<>();
        for (Object[] obj : weekRepaySumAmt) {
            WeekCountResult weekCountResult = new WeekCountResult();
            weekCountResult.setAmount((BigDecimal) obj[0]);
            weekCountResult.setDayOfWeek((Integer) obj[1]);
            weekRepayList.add(weekCountResult);
        }
        adminPage.setWeekRepayList(addWeekListZero(weekRepayList));

        // 第六部分 周催计数
        List<Object[]> weekFollCount = adminPageRepository.getWeekFollCount(code);
        List<WeekCountResult> weekFollList = new ArrayList<>();
        for (Object[] obj : weekFollCount) {
            WeekCountResult weekCountResult = new WeekCountResult();
            weekCountResult.setNum( Integer.valueOf(obj[0].toString()));
            weekCountResult.setDayOfWeek((Integer) obj[1]);
            weekFollList.add(weekCountResult);
        }
        adminPage.setWeekFollList(addWeekListZero(weekFollList));

        // 第七部分 周结案数
        List<Object[]> weekCaseEndCount = adminPageRepository.getWeekCaseEndCount(code);
        List<WeekCountResult> weekCaseEndList = new ArrayList<>();
        for (Object[] obj : weekCaseEndCount) {
            WeekCountResult weekCountResult = new WeekCountResult();
            weekCountResult.setNum( Integer.valueOf(obj[0].toString()));
            weekCountResult.setDayOfWeek(Integer.valueOf(obj[1].toString()));
            weekCaseEndList.add(weekCountResult);
        }
        adminPage.setWeekCaseEndList(addWeekListZero(weekCaseEndList));

        // 第八部分 催收员排行榜
        List<Object[]> cupoSort = adminPageRepository.getCupoSort(code);
        List<PageSortResult> cupoSortList = new ArrayList<>();
        for (Object[] obj : cupoSort) {
            PageSortResult pageSortResult = new PageSortResult();
            pageSortResult.setName((String) obj[1]);
            pageSortResult.setAmount((BigDecimal) obj[2]);
            pageSortResult.setPayed((BigDecimal) obj[3]);
            pageSortResult.setRate( Objects.isNull(obj[4]) ? 0.00 : Double.valueOf(obj[4].toString()));
            cupoSortList.add(pageSortResult);
        }
        adminPage.setCupoSortList(cupoSortList);

        // 第九部分 客户排行榜
        List<Object[]> custSort = adminPageRepository.getCustSort(code);
        List<PageSortResult> custSortList = new ArrayList<>();
        for (Object[] obj : custSort) {
            PageSortResult pageSortResult = new PageSortResult();
            pageSortResult.setName((String) obj[0]);
            pageSortResult.setAmount((BigDecimal) obj[1]);
            pageSortResult.setPayed((BigDecimal) obj[2]);
            pageSortResult.setRate(Objects.isNull(obj[3])? 0.00 : Double.valueOf(obj[3].toString()));
            custSortList.add(pageSortResult);
        }
        adminPage.setCustSortList(custSortList);

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
        Double taskFinished = cupoPageRepository.getTodyTashFinished(user.getUserName());
        taskFinished = Objects.isNull(taskFinished) ? 0D : taskFinished >1D ? 1D : taskFinished;
        cupoPage.setTaskFinished(taskFinished);
        // 第二部分 案件情况总计  第三部分 案件金额总计 (饼图部分)
        List<Object[]> caseCountResult = cupoPageRepository.getCaseCountResult(user.getId());
        List<CaseCountResult> caseCountResultList = new ArrayList<>();
        for (int i= 0; i<caseCountResult.size();i++) {
            CaseCountResult c = new CaseCountResult();
            c.setStatus((Integer) caseCountResult.get(i)[0]);
            c.setNum(Integer.valueOf(caseCountResult.get(i)[1].toString()));
            c.setAmt((BigDecimal) caseCountResult.get(i)[2]);
            caseCountResultList.add(c);
        }
        cupoPage.setCaseCountResultList(addCaseCountZero(caseCountResultList));
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
        List<Object[]> repayWeek = cupoPageRepository.getRepayWeek(user.getUserName());
        List<WeekCountResult> weekCountResults = new ArrayList<>();
        for (Object[] obj : repayWeek) {
            WeekCountResult weekCountResult = new WeekCountResult();
            weekCountResult.setAmount((BigDecimal) obj[0]);
            weekCountResult.setDayOfWeek((Integer) obj[1]);
            weekCountResults.add(weekCountResult);
        }
        cupoPage.setWeekRepaySum(addWeekListZero(weekCountResults));

        // 第六部分 周催计数统计
        List<Object[]> folWeek = cupoPageRepository.getFolWeek(user.getId());
        List<WeekCountResult> weekCountResultList = new ArrayList<>();
        for (Object[] obj : folWeek) {
            WeekCountResult weekCountResult = new WeekCountResult();
            weekCountResult.setNum(Integer.valueOf(obj[0].toString()));
            weekCountResult.setDayOfWeek((Integer) obj[1]);
            weekCountResultList.add(weekCountResult);
        }
        cupoPage.setWeekFollCount(addWeekListZero(weekCountResultList));

        // 第七部分 本周结案数统计
        List<Object[]> caseEndWeek = cupoPageRepository.getCaseEndWeek(user.getId());
        List<WeekCountResult> weekCountResultLists = new ArrayList<>();
        for (Object[] obj : caseEndWeek) {
            WeekCountResult weekCountResult = new WeekCountResult();
            weekCountResult.setNum(Integer.valueOf(obj[0].toString()));
            weekCountResult.setDayOfWeek((Integer) obj[1]);
            weekCountResultLists.add(weekCountResult);
        }
        cupoPage.setWeekCaseEndCount(addWeekListZero(weekCountResultLists));
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
        Integer flowInCaseToday = cupoPageRepository.getFlowInCaseToday(user.getId()); //今日流入案件数
        Integer finishCaseToday = cupoPageRepository.getFinishCaseToday(user.getId()); //今日结案数
        Integer flowOutCaseToday = cupoPageRepository.getFlowOutCaseToday(user.getId()); //今日流出案件数
        BigDecimal moneySumResult = cupoPageRepository.getMoneySumResult(user.getUserName()); //回款总金额
        BigDecimal monthMoneyResult = cupoPageRepository.getMonthMoneyResult(user.getUserName()); // 本月回款金额
        BigDecimal dayMoneyResult = cupoPageRepository.getDayMoneyResult(user.getUserName()); // 本天回款金额
        Integer dayFollowCount = cupoPageRepository.getDayFollowCount(user.getId());//今日累计催收次数
        Integer monthFollowCount = cupoPageRepository.getMonthFollowCount(user.getId());// 本月累计催收次数
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
