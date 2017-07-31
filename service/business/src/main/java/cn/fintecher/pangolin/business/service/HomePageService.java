package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.AdminPage;
import cn.fintecher.pangolin.business.model.HomePageResult;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.UserRepository;
import cn.fintecher.pangolin.entity.QCaseInfo;
import cn.fintecher.pangolin.entity.User;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @Author : sunyanping
 * @Description :
 * @Date : 2017/7/31.
 */
@Service("homePageService")
public class HomePageService {

    @Inject
    private UserRepository userRepository;
    @Inject
    private CaseInfoRepository caseInfoRepository;
    @Inject
    private UserService userService;

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
        QCaseInfo qCaseInfo = QCaseInfo.caseInfo;
        String code = user.getDepartment().getCode();

        //第一部分
        BigDecimal caseSumAmt = caseInfoRepository.getCaseSumAmt(code); //案件总金额
        Integer deptUserSum = userService.getAllUser(user.getDepartment().getId(), 0).size();//获取部门下所有的用户
        deptUserSum = deptUserSum == 0 ? 1 : deptUserSum;
        BigDecimal personCount = new BigDecimal(deptUserSum);
        adminPage.setCaseSumAmt(Objects.isNull(caseSumAmt) ? new BigDecimal(0.00) : caseSumAmt.setScale(2, BigDecimal.ROUND_HALF_UP));
        adminPage.setCaseSumAmtPerson(adminPage.getCaseSumAmt().divide(personCount, 2, BigDecimal.ROUND_HALF_UP));
//
//        // 第二部分
//
//        BigDecimal repaySumAmt = adminPageMapper.getRepaySumAmt(degrId);
//        adminPage.setRepaySumAmt(Objects.isNull(repaySumAmt) ? new BigDecimal(0.00) : repaySumAmt.setScale(2,BigDecimal.ROUND_HALF_UP));
//        adminPage.setRepaySumAmtPerson(adminPage.getRepaySumAmt().divide(personCount,2,BigDecimal.ROUND_HALF_UP));
//
//        // 第三部分
//
//        adminPage.setCupoSum(userSum);
//        adminPage.setCupoOnlineSum(0);
//        adminPage.setCupoOfflineSum(0);
//
//
//        //第四部分
//        Integer custSum = adminPageMapper.getCustNum(degrId);
//        Integer custSumIn = adminPageMapper.getCustNumIN(degrId);
//        adminPage.setCustSum(Objects.isNull(custSum) ? 0 : custSum);
//        adminPage.setCustSumIn(Objects.isNull(custSumIn) ? 0 : custSumIn);
//
//
//        //第五部分
//        List<WeekCountResult> weekRepayList = adminPageMapper.getWeekRepaySumAmt(degrId);
//        adminPage.setWeekRepayList(addWeekListZero(weekRepayList));
//
//        // 第六部分
//        List<WeekCountResult> weekFollList = adminPageMapper.getWeekFollCount(degrId);
//        adminPage.setWeekFollList(addWeekListZero(weekFollList));
//
//        // 第七部分
//        List<WeekCountResult> weekCaseEndList = adminPageMapper.getWeekCaseEndCount(degrId);
//        adminPage.setWeekCaseEndList(addWeekListZero(weekCaseEndList));
//
//        // 第八部分
//        adminPage.setCupoSortList(adminPageMapper.getCupoSort(degrId));
//
//        // 第九部分
//        adminPage.setCustSortList(adminPageMapper.getCustSort(degrId));
//
//        // 第十部分
///*        List<SysNotice> sysNoticeList = new ArrayList<>();
//        SysNotice sysNotice = new SysNotice();
//        sysNotice.setTitle("批量成功");
//        sysNotice.setContent("您于2017-05-25 12:21:35完成批量");
//        sysNoticeList.add(sysNotice);
//        adminPage.setSysNotice(sysNoticeList);*/
//
//        adminPage.initRate();
//        homePageResult.setData(adminPage);
        return homePageResult;
    }

    private HomePageResult getCollectPage(User user) {
//        HomePageResult<CupoPage> homePageResult = new HomePageResult<>();
//        homePageResult.setPageType(user.getUserManager());
//        CupoPage cupoPage = new CupoPage();
//        OperatorPlatformResult operatorPlatformResult = operatorPlatformService.countResult(user.getUserName());
//
//        // 第一部分
//        Double taskFinished = cupoPageMapper.getTodyTashFinished(user.getUserName());
//        taskFinished = Objects.isNull(taskFinished) ? 0D : taskFinished >1D ? 1D : taskFinished;
//        cupoPage.setTaskFinished(taskFinished);
//        // 第二部分(第三部分饼图)
//        cupoPage.setCaseCountResultList(addCaseCountZero(cupoPageMapper.getCaseCountResult(user.getUserName())));
//        cupoPage.setFlowInCaseToday(operatorPlatformResult.getFlowInCaseToday());
//        cupoPage.setFinishCaseToday(operatorPlatformResult.getFinishCaseToday());
//        cupoPage.setFlowOutCaseToday(operatorPlatformResult.getFlowOutCaseToday());
//
//        // 第三部分
////        CaseAmtResult caseAmtResult = cupoPageMapper.getCaseSumAmount(user.getUserName());
////        if(Objects.isNull(caseAmtResult)){
////            caseAmtResult = new CaseAmtResult();
////            caseAmtResult.setInAmount(new BigDecimal(0.00));
////            caseAmtResult.setWaitCupo(new BigDecimal(0.00));
////        }
////        cupoPage.setCaseAmtResult(caseAmtResult);
//        cupoPage.setMoneySumResult(operatorPlatformResult.getMoneySumResult());
//        cupoPage.setMonthMoneyResult(operatorPlatformResult.getMonthMoneyResult());
//        cupoPage.setDayMoneyResult(operatorPlatformResult.getDayMoneyResult());
//        // 第四部分
//        cupoPage.setDayFollowCount(operatorPlatformResult.getDayFollowCount());
//        cupoPage.setMonthFollowCount(operatorPlatformResult.getMonthFollowCount());
//        // 第五部分
//        cupoPage.setWeekRepaySum(addWeekListZero(cupoPageMapper.getRepayWeek(user.getUserName())));
//        // 第六部分
//        cupoPage.setWeekFollCount(addWeekListZero(cupoPageMapper.getFolWeek(user.getUserName())));
//        // 第七部分
//        cupoPage.setWeekCaseEndCount(addWeekListZero(cupoPageMapper.getCaseEndWeek(user.getUserName())));
//        homePageResult.setData(cupoPage);
        return null;
    }

}
