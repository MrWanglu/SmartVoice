package cn.fintecher.pangolin.report.service;

import cn.fintecher.pangolin.entity.SysParam;
import cn.fintecher.pangolin.entity.User;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.entity.util.ExcelUtil;
import cn.fintecher.pangolin.report.entity.BackMoneyReport;
import cn.fintecher.pangolin.report.entity.DailyProcessReport;
import cn.fintecher.pangolin.report.entity.DailyResultReport;
import cn.fintecher.pangolin.report.mapper.BackMoneyReportMapper;
import cn.fintecher.pangolin.report.mapper.DailyProcessReportMapper;
import cn.fintecher.pangolin.report.mapper.DailyResultReportMapper;
import cn.fintecher.pangolin.report.mapper.PerformanceReportMapper;
import cn.fintecher.pangolin.report.model.*;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author : xiaqun
 * @Description : 报表服务业务
 * @Date : 13:54 2017/8/2
 */

@Service("reportService")
public class ReportService {
    private final Logger log = LoggerFactory.getLogger(ReportService.class);

    @Inject
    BackMoneyReportMapper backMoneyReportMapper;

    @Inject
    PerformanceReportMapper performanceReportMapper;

    @Inject
    DailyProcessReportMapper dailyProcessReportMapper;

    @Inject
    DailyResultReportMapper dailyResultReportMapper;

    @Inject
    RestTemplate restTemplate;

    /**
     * @Description 查询催收员回款报表
     */
    public List<BackMoneyModel> getBackMoneyReport(GeneralParams generalParams, User tokenUser) {
        List<BackMoneyModel> backMoneyModels = new ArrayList<>();
        List<BackMoneyReport> backMoneyReports = makeBackMoneyReport(generalParams, tokenUser);
        DeptModel deptModel = backMoneyReportMapper.getDept(tokenUser.getUserName()); //获取登录人的部门信息
        //构建报表展示模型
        if (backMoneyReports.isEmpty()) {
            return null;
        }
        BackMoneyModel backMoneyModel = new BackMoneyModel();
        BackMoneySecModel backMoneySecModel = new BackMoneySecModel();
        BackMoneyThiModel backMoneyThiModel = new BackMoneyThiModel();
        for (BackMoneyReport backMoneyReport : backMoneyReports) {
            if (Objects.equals(backMoneyReport.getDeptCode(), deptModel.getCode())
                    && !Objects.equals(backMoneyReport.getUserName(), tokenUser.getUserName())) { //过滤同部门
                continue;
            }
            if (Objects.equals(backMoneyModel.getDeptCode(), backMoneyReport.getParentDeptCode())) { //一级模型中有该部门code码
                List<BackMoneySecModel> backMoneySecModelList = backMoneyModel.getBackMoneySecModels(); //获得二级模型集合
                if (Objects.isNull(backMoneySecModelList)) { //如果二级模型集合为空则new
                    backMoneySecModelList = new ArrayList<>();
                    backMoneySecModel = new BackMoneySecModel();
                    backMoneySecModel.setGroupCode(backMoneyReport.getDeptCode());
                    backMoneySecModel.setGroupName(backMoneyReport.getDeptName());

                    //回款报表三级模型
                    List<BackMoneyThiModel> backMoneyThiModelList = new ArrayList<>();
                    backMoneyThiModel = new BackMoneyThiModel();
                    backMoneyThiModel.setUserName(backMoneyReport.getUserName());
                    backMoneyThiModel.setCode(backMoneyReport.getDeptCode());

                    //回款报表集合
                    List<BackMoneyReport> backMoneyReportList = new ArrayList<>();
                    backMoneyReportList.add(backMoneyReport);
                    backMoneyThiModelList.add(backMoneyThiModel);

                    backMoneyThiModel.setBackMoneyReports(backMoneyReportList); //三级模型中加入报表集合
                    backMoneyThiModelList.add(backMoneyThiModel);
                    backMoneySecModel.setBackMoneyThiModels(backMoneyThiModelList); //二级模型中加入三级模型
                    backMoneySecModelList.add(backMoneySecModel);
                    backMoneyModel.setBackMoneySecModels(backMoneySecModelList); //一级模型中加入二级模型
                } else { //二级模型不为空
                    int flag1 = 0; //判断二级模型中是否包含该对象组别code
                    for (BackMoneySecModel backMoneySecModel1 : backMoneySecModelList) {
                        if (Objects.equals(backMoneySecModel1.getGroupCode(), backMoneyReport.getDeptCode())) {
                            flag1 = 1;
                            backMoneySecModel = backMoneySecModel1;
                            break;
                        }
                    }
                    if (0 == flag1) { //不包含
                        backMoneySecModel = new BackMoneySecModel();
                        backMoneySecModel.setGroupCode(backMoneyReport.getDeptCode());
                        backMoneySecModel.setGroupName(backMoneyReport.getDeptName());

                        //回款报表三级模型
                        List<BackMoneyThiModel> backMoneyThiModelList = new ArrayList<>();
                        backMoneyThiModel = new BackMoneyThiModel();
                        backMoneyThiModel.setUserName(backMoneyReport.getUserName());
                        backMoneyThiModel.setCode(backMoneyReport.getDeptCode());

                        //回款报表集合
                        List<BackMoneyReport> backMoneyReportList = new ArrayList<>();
                        backMoneyReportList.add(backMoneyReport);
                        backMoneyThiModelList.add(backMoneyThiModel);

                        backMoneyThiModel.setBackMoneyReports(backMoneyReportList); //三级模型中加入报表集合
                        backMoneyThiModelList.add(backMoneyThiModel);
                        backMoneySecModel.setBackMoneyThiModels(backMoneyThiModelList); //二级模型中加入三级模型
                        backMoneySecModelList.add(backMoneySecModel);
                        backMoneyModel.setBackMoneySecModels(backMoneySecModelList); //一级模型中加入二级模型
                    } else { //包含
                        List<BackMoneyThiModel> backMoneyThiModelList = backMoneySecModel.getBackMoneyThiModels(); //获取三级模型
                        int flag2 = 0; //判断三级模型中是否包含该对象的部门code
                        for (BackMoneyThiModel backMoneyThiModel1 : backMoneyThiModelList) {
                            if (Objects.equals(backMoneyThiModel1.getCode(), backMoneyReport.getDeptCode())) {
                                flag2 = 1;
                                backMoneyThiModel = backMoneyThiModel1;
                                break;
                            }
                        }
                        if (0 == flag2) { //不包含
                            backMoneyThiModel = new BackMoneyThiModel();
                            backMoneyThiModel.setUserName(backMoneyReport.getUserName());
                            backMoneyThiModel.setCode(backMoneyReport.getDeptCode());

                            //回款报表集合
                            List<BackMoneyReport> backMoneyReportList = new ArrayList<>();
                            backMoneyReportList.add(backMoneyReport);
                            backMoneyThiModel.setBackMoneyReports(backMoneyReportList); //三级模型中加入报表集合
                        } else { //包含
                            List<BackMoneyReport> backMoneyReportList = backMoneyThiModel.getBackMoneyReports();
                            backMoneyReportList.add(backMoneyReport);
                            backMoneyThiModel.setBackMoneyReports(backMoneyReportList); //三级模型中加入报表集合
                        }
                    }
                }
            } else { //一级模型中没有该部门code码
                //回款报表一级模型
                backMoneyModel = new BackMoneyModel();
                backMoneyModel.setDeptCode(backMoneyReport.getParentDeptCode());
                backMoneyModel.setDeptName(backMoneyReport.getParentDeptName());

                //回款报表二级模型
                List<BackMoneySecModel> backMoneySecModelList = new ArrayList<>();
                backMoneySecModel = new BackMoneySecModel();
                backMoneySecModel.setGroupCode(backMoneyReport.getDeptCode());
                backMoneySecModel.setGroupName(backMoneyReport.getDeptName());

                //回款报表三级模型
                List<BackMoneyThiModel> backMoneyThiModelList = new ArrayList<>();
                backMoneyThiModel = new BackMoneyThiModel();
                backMoneyThiModel.setUserName(backMoneyReport.getUserName());
                backMoneyThiModel.setCode(backMoneyReport.getDeptCode());

                //回款报表集合
                List<BackMoneyReport> backMoneyReportList = new ArrayList<>();
                backMoneyReportList.add(backMoneyReport);
                backMoneyThiModelList.add(backMoneyThiModel);

                backMoneyThiModel.setBackMoneyReports(backMoneyReportList); //三级模型中加入报表集合
                backMoneyThiModelList.add(backMoneyThiModel);
                backMoneySecModel.setBackMoneyThiModels(backMoneyThiModelList); //二级模型中加入三级模型
                backMoneySecModelList.add(backMoneySecModel);
                backMoneyModel.setBackMoneySecModels(backMoneySecModelList); //一级模型中加入二级模型
            }
            backMoneyModels.add(backMoneyModel);
        }
        return backMoneyModels;
    }

    /**
     * @Description 催收员业绩进展报表
     */
    public List<PerformanceModel> getPerformanceReport(PerformanceParams performanceParams, User tokenUser) {
        List<PerformanceModel> performanceModels = new ArrayList<>();
        DeptModel deptModel = backMoneyReportMapper.getDept(tokenUser.getUserName()); //获取登录人的部门信息
        List<PerformanceBasisModel> performanceBasisModels = performanceReportMapper.getPerformanceReport(deptModel.getCode(), performanceParams.getCode(), performanceParams.getUserName(), tokenUser.getCompanyCode());
        if (performanceBasisModels.isEmpty()) {
            return null;
        }
        //构建报表展示模型
        PerformanceModel performanceModel = new PerformanceModel();
        PerformanceSecModel performanceSecModel = new PerformanceSecModel();
        for (PerformanceBasisModel performanceBasisModel : performanceBasisModels) {
            if (Objects.equals(deptModel.getCode(), performanceBasisModel.getDeptCode())
                    && !Objects.equals(deptModel.getName(), performanceBasisModel.getDeptName())) { //过滤同部门
                continue;
            }
            if (Objects.equals(performanceModel.getDeptCode(), performanceBasisModel.getParentDeptCode())) { //一级模型中有该部门code码
                List<PerformanceSecModel> performanceSecModels = performanceModel.getPerformanceSecModels(); //获得二级模型集合
                if (Objects.isNull(performanceSecModels)) { //如果二级模型集合为空则new
                    performanceSecModels = new ArrayList<>();

                    //二级模型
                    performanceSecModel = new PerformanceSecModel();
                    performanceSecModel.setGroupName(performanceBasisModel.getDeptCode());
                    performanceSecModel.setGroupName(performanceBasisModel.getDeptName());

                    //基础模型
                    List<PerformanceBasisModel> performanceBasisModelList = new ArrayList<>();
                    performanceBasisModelList.add(performanceBasisModel);

                    performanceSecModel.setPerformanceBasisModels(performanceBasisModelList); //二级模型中加入基础模型
                    performanceSecModels.add(performanceSecModel);
                    performanceModel.setPerformanceSecModels(performanceSecModels); //一级模型中加入二级模型
                } else { //二级模型不为空
                    int flag = 0; //判断二级模型中是否包含该code码
                    for (PerformanceSecModel performanceSecModel1 : performanceSecModels) {
                        if (Objects.equals(performanceSecModel1.getGroupCode(), performanceBasisModel.getDeptCode())) {
                            flag = 1;
                            break;
                        }
                    }
                    if (0 == flag) { //不包含
                        performanceSecModel = new PerformanceSecModel();
                        performanceSecModel.setGroupCode(performanceBasisModel.getDeptCode());
                        performanceSecModel.setGroupName(performanceBasisModel.getDeptName());

                        //基础模型
                        List<PerformanceBasisModel> performanceBasisModelList = new ArrayList<>();
                        performanceBasisModelList.add(performanceBasisModel);
                        performanceSecModel.setPerformanceBasisModels(performanceBasisModelList); //二级模型中加入基础模型
                    } else { //包含
                        List<PerformanceBasisModel> performanceBasisModelList = performanceSecModel.getPerformanceBasisModels();
                        performanceBasisModelList.add(performanceBasisModel);
                        performanceSecModel.setPerformanceBasisModels(performanceBasisModelList); //二级模型中加入基础模型
                    }
                }
            } else { //一级模型中没有该部门code码
                //一级模型
                performanceModel = new PerformanceModel();
                performanceModel.setDeptCode(performanceBasisModel.getParentDeptCode());
                performanceModel.setDeptName(performanceBasisModel.getParentDeptName());

                //二级模型
                List<PerformanceSecModel> performanceSecModels = new ArrayList<>();
                performanceSecModel = new PerformanceSecModel();
                performanceSecModel.setGroupCode(performanceBasisModel.getDeptCode());
                performanceSecModel.setGroupName(performanceBasisModel.getDeptName());

                //基础模型
                List<PerformanceBasisModel> performanceBasisModelList = new ArrayList<>();
                performanceBasisModelList.add(performanceBasisModel);

                performanceSecModel.setPerformanceBasisModels(performanceBasisModelList); //二级模型中加入基础模型
                performanceSecModels.add(performanceSecModel);
                performanceModel.setPerformanceSecModels(performanceSecModels); //一级模型中加入二级模型
                performanceModels.add(performanceModel);
            }
        }
        return performanceModels;
    }

    /**
     * @Description 催收员每日催收过程报表
     */
    public List<DailyProcessModel> getDailyProcessReport(GeneralParams generalParams, User tokenUser) {
        List<DailyProcessModel> dailyProcessModels = new ArrayList<>();
        List<DailyProcessReport> dailyProcessReports;
        DeptModel deptModel = backMoneyReportMapper.getDept(tokenUser.getUserName()); //获取登录人的部门信息
        if (Objects.equals(generalParams.getType(), 0)) { //实时报表
            dailyProcessReports = dailyProcessReportMapper.getRealTimeReport(deptModel.getCode(), generalParams.getCode(), generalParams.getRealName(), tokenUser.getCompanyCode()); //获得实时报表
        } else { //历史报表
            dailyProcessReports = dailyProcessReportMapper.getHistoryReport(generalParams.getStartDate(), generalParams.getEndDate(),
                    deptModel.getCode(), generalParams.getCode(), generalParams.getRealName(), tokenUser.getCompanyCode());
        }
        //构建报表展示模型
        if (dailyProcessReports.isEmpty()) { //如果报表为空则返回null
            return null;
        }
        DailyProcessModel dailyProcessModel = new DailyProcessModel();
        DailyProcessSecModel dailyProcessSecModel = new DailyProcessSecModel();
        DailyProcessThiModel dailyProcessThiModel = new DailyProcessThiModel();
        for (DailyProcessReport dailyProcessReport : dailyProcessReports) {
            if (Objects.equals(deptModel.getCode(), dailyProcessReport.getDeptCode())
                    && !Objects.equals(deptModel.getName(), dailyProcessReport.getDeptName())) { //过滤同部门
                continue;
            }
            if (Objects.equals(dailyProcessModel.getDeptCode(), dailyProcessReport.getParentDeptCode())) { //一级模型中有该部门code码
                List<DailyProcessSecModel> dailyProcessSecModels = dailyProcessModel.getDailyProcessSecModels();
                if (Objects.isNull(dailyProcessSecModels)) { //如果二级模型为空则new
                    dailyProcessSecModels = new ArrayList<>();
                    //二级模型
                    dailyProcessSecModel = new DailyProcessSecModel();
                    dailyProcessSecModel.setGroupCode(dailyProcessReport.getDeptCode());
                    dailyProcessSecModel.setGroupName(dailyProcessReport.getDeptName());

                    //三级模型
                    List<DailyProcessThiModel> dailyProcessThiModels = new ArrayList<>();
                    dailyProcessThiModel = new DailyProcessThiModel();
                    dailyProcessThiModel.setDeptCode(dailyProcessReport.getDeptCode());
                    dailyProcessThiModel.setUserName(dailyProcessReport.getUserName());

                    //基础报表模型
                    List<DailyProcessReport> dailyProcessReportList = new ArrayList<>();
                    dailyProcessReportList.add(dailyProcessReport);

                    dailyProcessThiModel.setDailyProcessReports(dailyProcessReportList); //三级模型中加入基础模型
                    dailyProcessThiModels.add(dailyProcessThiModel);
                    dailyProcessSecModel.setDailyProcessThiModels(dailyProcessThiModels); //二级模型中加入三级模型
                    dailyProcessSecModels.add(dailyProcessSecModel);
                    dailyProcessModel.setDailyProcessSecModels(dailyProcessSecModels); //一级模型中加入二级模型
                } else { //不为空
                    int flag1 = 0; //判断二级模型中是否有该code码
                    for (DailyProcessSecModel dailyProcessSecModel1 : dailyProcessSecModels) {
                        if (Objects.equals(dailyProcessSecModel1.getGroupCode(), dailyProcessReport.getDeptCode())) {
                            flag1 = 1;
                            break;
                        }
                    }
                    if (0 == flag1) { //如果不包含
                        //二级模型
                        dailyProcessSecModel = new DailyProcessSecModel();
                        dailyProcessSecModel.setGroupCode(dailyProcessReport.getDeptCode());
                        dailyProcessSecModel.setGroupName(dailyProcessReport.getDeptName());

                        //三级模型
                        List<DailyProcessThiModel> dailyProcessThiModels = new ArrayList<>();
                        dailyProcessThiModel = new DailyProcessThiModel();
                        dailyProcessThiModel.setDeptCode(dailyProcessReport.getDeptCode());
                        dailyProcessThiModel.setUserName(dailyProcessReport.getUserName());

                        //基础模型
                        List<DailyProcessReport> dailyProcessReportList = new ArrayList<>();
                        dailyProcessReportList.add(dailyProcessReport);

                        dailyProcessThiModel.setDailyProcessReports(dailyProcessReportList); //三级模型中加入基础模型
                        dailyProcessThiModels.add(dailyProcessThiModel);
                        dailyProcessSecModel.setDailyProcessThiModels(dailyProcessThiModels); //二级模型中加入三级模型
                        dailyProcessSecModels.add(dailyProcessSecModel);
                        dailyProcessModel.setDailyProcessSecModels(dailyProcessSecModels); //一级模型中加入二级模型
                    } else { //如果包含
                        List<DailyProcessThiModel> dailyProcessThiModels = dailyProcessSecModel.getDailyProcessThiModels(); //获得三级模型集合
                        int flag2 = 0; //判断三级模型中是否包含该code码
                        for (DailyProcessThiModel dailyProcessThiModel1 : dailyProcessThiModels) {
                            if (Objects.equals(dailyProcessThiModel1.getDeptCode(), dailyProcessReport.getDeptCode())) {
                                flag2 = 1;
                                break;
                            }
                        }
                        if (0 == flag2) { //不包含
                            //三级模型
                            dailyProcessThiModel = new DailyProcessThiModel();
                            dailyProcessThiModel.setDeptCode(dailyProcessReport.getDeptCode());
                            dailyProcessThiModel.setUserName(dailyProcessReport.getUserName());

                            //基础模型
                            List<DailyProcessReport> dailyProcessReportList = new ArrayList<>();
                            dailyProcessReportList.add(dailyProcessReport);
                            dailyProcessThiModel.setDailyProcessReports(dailyProcessReportList); //三级模型中加入基础模型
                        } else { //包含
                            List<DailyProcessReport> dailyProcessReportList = dailyProcessThiModel.getDailyProcessReports();
                            dailyProcessReportList.add(dailyProcessReport);
                            dailyProcessThiModel.setDailyProcessReports(dailyProcessReportList);
                        }
                    }
                }
            } else { //一级模型中没有该部门code码
                //一级模型
                dailyProcessModel = new DailyProcessModel();
                dailyProcessModel.setDeptCode(dailyProcessReport.getParentDeptCode());
                dailyProcessModel.setDeptName(dailyProcessReport.getParentDeptName());

                //二级模型
                List<DailyProcessSecModel> dailyProcessSecModels = new ArrayList<>();
                dailyProcessSecModel = new DailyProcessSecModel();
                dailyProcessSecModel.setGroupCode(dailyProcessReport.getDeptCode());
                dailyProcessSecModel.setGroupName(dailyProcessReport.getDeptName());

                //三级模型
                List<DailyProcessThiModel> dailyProcessThiModels = new ArrayList<>();
                dailyProcessThiModel = new DailyProcessThiModel();
                dailyProcessThiModel.setDeptCode(dailyProcessReport.getDeptCode());
                dailyProcessThiModel.setUserName(dailyProcessReport.getUserName());

                //基础报表模型
                List<DailyProcessReport> dailyProcessReportList = new ArrayList<>();
                dailyProcessReportList.add(dailyProcessReport);

                dailyProcessThiModel.setDailyProcessReports(dailyProcessReportList); //三级模型中加入基础模型
                dailyProcessThiModels.add(dailyProcessThiModel);
                dailyProcessSecModel.setDailyProcessThiModels(dailyProcessThiModels); //二级模型中就加入三级模型
                dailyProcessSecModels.add(dailyProcessSecModel);
                dailyProcessModel.setDailyProcessSecModels(dailyProcessSecModels); //一级模型中加入二级模型
            }
            dailyProcessModels.add(dailyProcessModel);
        }
        return dailyProcessModels;
    }

    /**
     * @Description 催收员每日催收结果报表
     */
    public List<DailyResultModel> getDailyResultReport(GeneralParams generalParams, User tokenUser) {
        List<DailyResultModel> dailyResultModels = new ArrayList<>();
        List<DailyResultReport> dailyResultReports;
        DeptModel deptModel = backMoneyReportMapper.getDept(tokenUser.getUserName()); //获取登录人的部门信息
        if (Objects.equals(generalParams.getType(), 0)) { //实时报表
            dailyResultReports = dailyResultReportMapper.getRealTimeReport(deptModel.getCode(), generalParams.getCode(), generalParams.getRealName(), tokenUser.getCompanyCode());
        } else { //历史报表
            dailyResultReports = dailyResultReportMapper.getHistoryReport(generalParams.getStartDate(), generalParams.getEndDate(),
                    deptModel.getCode(), generalParams.getCode(), generalParams.getRealName(), tokenUser.getCompanyCode());
        }
        //构建报表展示模型
        if (Objects.isNull(dailyResultReports)) {
            return null;
        }
        DailyResultModel dailyResultModel = new DailyResultModel();
        DailyResultSecModel dailyResultSecModel = new DailyResultSecModel();
        DailyResultThiModel dailyResultThiModel = new DailyResultThiModel();
        for (DailyResultReport dailyResultReport : dailyResultReports) {
            if (Objects.equals(deptModel.getCode(), dailyResultReport.getDeptCode())
                    && !Objects.equals(deptModel.getName(), dailyResultReport.getDeptName())) { //过滤同部门
                continue;
            }
            if (Objects.equals(dailyResultModel.getDeptCode(), dailyResultReport.getParentDeptCode())) { //一级模型中有该部门code码
                List<DailyResultSecModel> dailyResultSecModels = dailyResultModel.getDailyResultSecModels();
                if (Objects.isNull(dailyResultSecModels)) { //如果二级模型集合为空则new
                    dailyResultSecModels = new ArrayList<>();
                    //二级模型
                    dailyResultSecModel = new DailyResultSecModel();
                    dailyResultSecModel.setGroupCode(dailyResultReport.getDeptCode());
                    dailyResultSecModel.setGroupName(dailyResultReport.getDeptName());

                    //三级模型
                    List<DailyResultThiModel> dailyResultThiModels = new ArrayList<>();
                    dailyResultThiModel = new DailyResultThiModel();
                    dailyResultThiModel.setDeptCode(dailyResultReport.getDeptCode());
                    dailyResultThiModel.setUserName(dailyResultReport.getUserName());

                    //基础报表模型
                    List<DailyResultReport> dailyResultReportList = new ArrayList<>();
                    dailyResultReportList.add(dailyResultReport);

                    dailyResultThiModel.setDailyResultReports(dailyResultReportList); //三级模型中加入基础模型
                    dailyResultThiModels.add(dailyResultThiModel);
                    dailyResultSecModel.setDailyResultThiModels(dailyResultThiModels); //二级模型中加入三级模型
                    dailyResultSecModels.add(dailyResultSecModel);
                    dailyResultModel.setDailyResultSecModels(dailyResultSecModels); //一级模型中加入二级模型
                } else { //不为空
                    int flag1 = 0; //判断二级模型中是否有该code码
                    for (DailyResultSecModel dailyResultSecModel1 : dailyResultSecModels) {
                        if (Objects.equals(dailyResultSecModel1.getGroupCode(), dailyResultReport.getDeptCode())) {
                            flag1 = 1;
                            break;
                        }
                    }
                    if (0 == flag1) { //如果不包含
                        //二级模型
                        dailyResultSecModel = new DailyResultSecModel();
                        dailyResultSecModel.setGroupCode(dailyResultReport.getDeptCode());
                        dailyResultSecModel.setGroupName(dailyResultReport.getDeptName());

                        //三级模型
                        List<DailyResultThiModel> dailyResultThiModels = new ArrayList<>();
                        dailyResultThiModel = new DailyResultThiModel();
                        dailyResultThiModel.setDeptCode(dailyResultReport.getDeptCode());
                        dailyResultThiModel.setUserName(dailyResultReport.getUserName());

                        //基础模型
                        List<DailyResultReport> dailyResultReportList = new ArrayList<>();
                        dailyResultReportList.add(dailyResultReport);

                        dailyResultThiModel.setDailyResultReports(dailyResultReportList); //三级模型中加入基础模型
                        dailyResultThiModels.add(dailyResultThiModel);
                        dailyResultSecModel.setDailyResultThiModels(dailyResultThiModels); //二级模型中加入三级模型
                        dailyResultSecModels.add(dailyResultSecModel);
                        dailyResultModel.setDailyResultSecModels(dailyResultSecModels); //一级模型中加入二级模型
                    } else { //如果包含
                        List<DailyResultThiModel> dailyResultThiModels = dailyResultSecModel.getDailyResultThiModels(); //获得三级模型集合
                        int flag2 = 0; //判断三级模型中是否包含该code码
                        for (DailyResultThiModel dailyResultThiModel1 : dailyResultThiModels) {
                            if (Objects.equals(dailyResultThiModel1.getDeptCode(), dailyResultReport.getDeptCode())) {
                                flag2 = 1;
                                break;
                            }
                        }
                        if (0 == flag2) { //不包含
                            //三级模型
                            dailyResultThiModel = new DailyResultThiModel();
                            dailyResultThiModel.setDeptCode(dailyResultReport.getDeptCode());
                            dailyResultThiModel.setUserName(dailyResultReport.getUserName());

                            //基础模型
                            List<DailyResultReport> dailyResultReportList = new ArrayList<>();
                            dailyResultReportList.add(dailyResultReport);
                            dailyResultThiModel.setDailyResultReports(dailyResultReportList); //三级模型中加入基础模型
                        } else { //包含
                            List<DailyResultReport> dailyResultReportList = dailyResultThiModel.getDailyResultReports();
                            dailyResultReportList.add(dailyResultReport);
                            dailyResultThiModel.setDailyResultReports(dailyResultReportList);
                        }
                    }
                }
            } else { //一级模型中没有该部门code码
                //一级模型
                dailyResultModel = new DailyResultModel();
                dailyResultModel.setDeptCode(dailyResultReport.getParentDeptCode());
                dailyResultModel.setDeptName(dailyResultReport.getParentDeptName());

                //二级模型
                List<DailyResultSecModel> dailyResultSecModels = new ArrayList<>();
                dailyResultSecModel = new DailyResultSecModel();
                dailyResultSecModel.setGroupCode(dailyResultReport.getDeptCode());
                dailyResultSecModel.setGroupName(dailyResultReport.getDeptName());

                //三级模型
                List<DailyResultThiModel> dailyResultThiModels = new ArrayList<>();
                dailyResultThiModel = new DailyResultThiModel();
                dailyResultThiModel.setDeptCode(dailyResultReport.getDeptCode());
                dailyResultThiModel.setUserName(dailyResultReport.getUserName());

                //基础报表模型
                List<DailyResultReport> dailyResultReportList = new ArrayList<>();
                dailyResultReportList.add(dailyResultReport);

                dailyResultThiModel.setDailyResultReports(dailyResultReportList); //三级模型中加入基础模型
                dailyResultThiModels.add(dailyResultThiModel);
                dailyResultSecModel.setDailyResultThiModels(dailyResultThiModels); //二级模型中就加入三级模型
                dailyResultSecModels.add(dailyResultSecModel);
                dailyResultModel.setDailyResultSecModels(dailyResultSecModels); //一级模型中加入二级模型
            }
            dailyResultModels.add(dailyResultModel);
        }
        return dailyResultModels;
    }

    /**
     * @Description 获取催收员回款报表
     */
    private List<BackMoneyReport> makeBackMoneyReport(GeneralParams generalParams, User tokenUser) {
        List<BackMoneyReport> backMoneyReports;
        DeptModel deptModel = backMoneyReportMapper.getDept(tokenUser.getUserName()); //获取登录人的部门信息
        if (Objects.equals(generalParams.getType(), 0)) { //实时报表
            //获取当日有回款的记录
            backMoneyReports = backMoneyReportMapper.getRealTimeReport(deptModel.getCode(), generalParams.getCode(), generalParams.getRealName(), tokenUser.getCompanyCode());

            //获取没有案件中有案件但没有回款的记录
            List<NoBackMoneyModel> noBackMoneyModels = backMoneyReportMapper.getUserNames(deptModel.getCode(), generalParams.getCode(), generalParams.getRealName(), tokenUser.getCompanyCode());
            DeptModel dept;
            List<BackMoneyReport> backMoneyReportList = new ArrayList<>();
            if (!noBackMoneyModels.isEmpty()) { //存在有案件却没有回款的用户
                BackMoneyReport backMoneyReport;
                for (NoBackMoneyModel noBackMoneyModel : noBackMoneyModels) {
                    backMoneyReport = new BackMoneyReport();
                    backMoneyReport.setUserName(noBackMoneyModel.getUserName()); //用户名
                    backMoneyReport.setRealName(noBackMoneyModel.getRealName()); //用户姓名
                    backMoneyReport.setOperatorDate(ZWDateUtil.getNowDateTime()); //操作时间
                    dept = backMoneyReportMapper.getDept(noBackMoneyModel.getUserName()); //获取本部门信息
                    backMoneyReport.setDeptCode(dept.getCode()); //本部门code码
                    backMoneyReport.setDeptName(dept.getName()); //本部门名称
                    if (Objects.equals(dept.getFlag(), 1)) { //存在父部门
                        dept = backMoneyReportMapper.getParentDept(noBackMoneyModel.getUserName()); //获得父部门信息
                        backMoneyReport.setParentDeptCode(dept.getCode()); //父部门code码
                        backMoneyReport.setParentDeptName(dept.getName()); //父部门名称
                    }
                    backMoneyReportList.add(backMoneyReport);
                }
            }
            //合并实时回款记录和没有回款的记录
            backMoneyReports.addAll(backMoneyReportList);
        } else { //历史报表
            backMoneyReports = backMoneyReportMapper.getHistoryReport(deptModel.getCode(), generalParams.getStartDate(),
                    generalParams.getEndDate(), generalParams.getCode(), generalParams.getRealName(), tokenUser.getCompanyCode());
        }
        return backMoneyReports;
    }

    /**
     * @Description 导出催收员回款报表
     */
    public String exportBackMoneyReport(GeneralParams generalParams, User tokenUser) throws IOException {
        //获取导出数据模型
        List<BackMoneyModel> backMoneyModels = getBackMoneyReport(generalParams, tokenUser);
        if (Objects.isNull(backMoneyModels)) {
            backMoneyModels = new ArrayList<>();
        }

        //下载催收员回款报表模版
        //拼接请求地址
        String requestUrl = Constants.SYSPARAM_URL.concat("?").concat("userId").concat("=").concat(tokenUser.getId().
                concat("&").concat("companyCode").concat("=").concat(tokenUser.getCompanyCode()).concat("&").concat("code").concat("=").concat(Constants.BACK_MONEY_REPORT_EXCEL_URL_CODE).
                concat("&").concat("type").concat("=").concat(Constants.BACK_MONEY_REPORT_EXCEL_URL_TYPE));
        log.debug(requestUrl);
        //下载模版
        HSSFWorkbook hssfWorkbook = downloadTemplate(requestUrl);
        //设置excel表格样式
        HSSFCellStyle hssfCellStyle = setStyle(hssfWorkbook);
        //创建excel表格
        HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);

        //催收员开始行
        int userIndex = 2;

        //组合并开始行
        int groupIndex = 2;

        //部门合并开始行
        int deptIndex = 2;

        //回款累计，只在实时报表中使用
        BigDecimal total;

        //定义excel数据展示顺序
        String[] paramArray = {"realName", "backDate", "backMoney"};

        //给excel中填值
        for (BackMoneyModel backMoneyModel : backMoneyModels) { //循环一级模型集合，获取二级模型集合
            List<BackMoneySecModel> backMoneySecModels = backMoneyModel.getBackMoneySecModels();
            for (BackMoneySecModel backMoneySecModel : backMoneySecModels) { //循环二级模型集合，获取三级模型集合
                List<BackMoneyThiModel> backMoneyThiModels = backMoneySecModel.getBackMoneyThiModels();

                //对累计数据初始化
                total = new BigDecimal(0);

                for (BackMoneyThiModel backMoneyThiModel : backMoneyThiModels) { //循环三级模型集合，获取回款报表基础模型集合
                    List<BackMoneyReport> backMoneyReports = backMoneyThiModel.getBackMoneyReports();
                    for (BackMoneyReport backMoneyReport : backMoneyReports) { //遍历每一个回款报表模型
                        //在excel创建一行
                        HSSFRow row = hssfSheet.createRow((short) userIndex);

                        //创建单元格
                        HSSFCell cell;
                        int paramIndex = 0; //excel数据展示顺序数组角标

                        //给该行每一列设置数据
                        for (int i = 2; i < 5; i++) { //i为报表模版数据列数,从第[2]列姓名开始
                            Object obj = ExcelUtil.getProValue(paramArray[paramIndex], backMoneyReport); //通过字段映射获取相应的数据
                            cell = setCellValue(obj, row, i); //给单元格set值
                            cell.setCellStyle(hssfCellStyle); //给单元格设置格式

                            //如果为实时报表的话则累计还款金额
                            if (4 == i && Objects.equals(generalParams.getType(), 0)) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = new BigDecimal(0);
                                }
                                total = ((BigDecimal) obj).setScale(2, BigDecimal.ROUND_HALF_UP).add(total); //计算累计数据
                            }
                            paramIndex++;
                        }
                        //设置完毕后行数+1,进行下一行设置
                        userIndex++;
                    }
                }
                //合并组别
                CellRangeAddress cra = new CellRangeAddress(groupIndex, userIndex, 1, 1); // 四个参数分别是：起始行，结束行,起始列，结束列
                hssfSheet.addMergedRegion(cra); //在excel里增加合并单元格

                //如果导出实时报表
                if (Objects.equals(generalParams.getType(), 0)) { //如果导出实时报表，则多加一行累计
                    //在excel创建一行
                    HSSFRow row = hssfSheet.createRow((short) (userIndex));

                    //创建单元格
                    HSSFCell cell;

                    //给该行每一列设置数据
                    for (int i = 1; i < 5; i++) { //i为报表模版数据列数,从第[1]列组别开始
                        if (2 == i || 3 == i) { //如果为第[2]列姓名或者第[3]列日期,则设置为空
                            cell = row.createCell(i, CellType.STRING);
                            cell.setCellValue("");
                        } else if (1 == i) { //如果为第[1]列组别,则设置为累计
                            cell = row.createCell(i, CellType.STRING);
                            cell.setCellValue("累计");
                        } else { //如果为第[4]列还款金额,则设置为total
                            cell = row.createCell(i, CellType.NUMERIC);
                            cell.setCellValue(total.doubleValue());
                        }
                    }
                    //设置完毕后行数+1,进行下一行设置
                    userIndex++;
                }
            }
            //设置组合并起始行
            groupIndex = userIndex;

            //合并部门
            CellRangeAddress cra = new CellRangeAddress(deptIndex, userIndex, 0, 0); // 四个参数分别是：起始行，结束行,起始列，结束列
            hssfSheet.addMergedRegion(cra); //在excel里增加合并单元格
        }

        //上传填好数据的报表
        return uploadExcel(hssfWorkbook);
    }

    /**
     * @Description 导出催收员业绩进展报表
     */
    public String exportPerformanceReport(PerformanceParams performanceParams, User tokenUser) throws IOException {
        //获取导出数据模型
        List<PerformanceModel> performanceModels = getPerformanceReport(performanceParams, tokenUser);

        //下载催收员回款报表模版
        //拼接请求地址
        String requestUrl = Constants.SYSPARAM_URL.concat("?").concat("userId").concat("=").concat(tokenUser.getId().
                concat("&").concat("companyCode").concat("=").concat(tokenUser.getCompanyCode()).concat("&").concat("code").concat("=").concat(Constants.PERFORMANCE_REPORT_EXCEL_URL_CODE).
                concat("&").concat("type").concat("=").concat(Constants.PERFORMANCE_REPORT_EXCEL_URL_TYPE));
        log.debug(requestUrl);
        //下载模版
        HSSFWorkbook hssfWorkbook = downloadTemplate(requestUrl);
        //设置excel表格样式
        HSSFCellStyle hssfCellStyle = setStyle(hssfWorkbook);
        //创建excel表格
        HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);

        //催收员开始行
        int userIndex = 2;

        //组合并开始行
        int groupIndex = 2;

        //部门合并开始行
        int deptIndex = 2;

        //待催收案件总数
        Integer waitCollectNumTotal;
        //待催收案件总金额
        BigDecimal waitCollectAmtTotal;
        //催收中的案件总数
        Integer collectingNumTotal;
        //催收中的案件总金额
        BigDecimal collectingAmtTotal;
        //逾期还款中案件总数
        Integer overdueNumTotal;
        //逾期还款中案件总金额
        BigDecimal overdueAmtTotal;
        //提前结清中案件总数
        Integer advanceNumTotal;
        //提前结清中案件总金额
        BigDecimal advanceAmtTotal;
        //承诺还款案件总数
        Integer promiseNumTotal;
        //承诺还款案件总金额
        BigDecimal promiseAmtTotal;
        //结案案件总数
        Integer endNumTotal;
        //结案案件总金额
        BigDecimal endAmtTotal;

        //定义excel数据展示顺序
        String[] paramArray = {"realName", "waitCollectNum", "waitCollectAmt", "collectingNum", "collectingAmt", "overdueNum", "overdueAmt", "advanceNum", "advanceAmt", "promiseNum", "promiseAmt", "endNum", "endAmt"};

        //给excel中填值
        for (PerformanceModel performanceModel : performanceModels) { //循环一级模型集合，获取二级模型集合
            List<PerformanceSecModel> performanceSecModels = performanceModel.getPerformanceSecModels();
            for (PerformanceSecModel performanceSecModel : performanceSecModels) { //循环二级模型集合，获取报表基础模型集合
                List<PerformanceBasisModel> performanceBasisModels = performanceSecModel.getPerformanceBasisModels();

                //给累计的数据初始化
                waitCollectNumTotal = 0;
                waitCollectAmtTotal = new BigDecimal(0);
                collectingNumTotal = 0;
                collectingAmtTotal = new BigDecimal(0);
                overdueNumTotal = 0;
                overdueAmtTotal = new BigDecimal(0);
                advanceNumTotal = 0;
                advanceAmtTotal = new BigDecimal(0);
                promiseNumTotal = 0;
                promiseAmtTotal = new BigDecimal(0);
                endNumTotal = 0;
                endAmtTotal = new BigDecimal(0);

                for (PerformanceBasisModel performanceBasisModel : performanceBasisModels) { //遍历每一个业绩进展报表模型
                    //在excel创建一行
                    HSSFRow row = hssfSheet.createRow((short) userIndex);

                    //创建单元格
                    HSSFCell cell;
                    int paramIndex = 0; //excel数据展示顺序数组角标

                    //给该行每一列设置数据
                    for (int i = 2; i < 15; i++) { //i为报表模版数据列数,从第[2]列姓名开始
                        Object obj = ExcelUtil.getProValue(paramArray[paramIndex], performanceBasisModel); //通过字段映射获取相应的数据
                        cell = setCellValue(obj, row, i); //给单元格set值
                        cell.setCellStyle(hssfCellStyle); //给单元格设置格式

                        //计算累计数据
                        if (3 == i) {
                            if (Objects.isNull(obj)) { //如果为空的话则设为0
                                obj = 0;
                            }
                            waitCollectNumTotal = (Integer) obj + waitCollectNumTotal; //待催收案件总数
                        } else if (4 == i) {
                            if (Objects.isNull(obj)) { //如果为空的话则设为0
                                obj = new BigDecimal(0);
                            }
                            waitCollectAmtTotal = ((BigDecimal) obj).setScale(2, BigDecimal.ROUND_HALF_UP).add(waitCollectAmtTotal); //待催收案件总金额
                        } else if (5 == i) {
                            if (Objects.isNull(obj)) { //如果为空的话则设为0
                                obj = 0;
                            }
                            collectingNumTotal = (Integer) obj + collectingNumTotal; //催收中案件总数
                        } else if (6 == i) {
                            if (Objects.isNull(obj)) { //如果为空的话则设为0
                                obj = new BigDecimal(0);
                            }
                            collectingAmtTotal = ((BigDecimal) obj).setScale(2, BigDecimal.ROUND_HALF_UP).add(collectingAmtTotal); //催收中案件总金额
                        } else if (7 == i) {
                            if (Objects.isNull(obj)) { //如果为空的话则设为0
                                obj = 0;
                            }
                            overdueNumTotal = (Integer) obj + overdueNumTotal; //逾期还款中案件总数
                        } else if (8 == i) {
                            if (Objects.isNull(obj)) { //如果为空的话则设为0
                                obj = new BigDecimal(0);
                            }
                            overdueAmtTotal = ((BigDecimal) obj).setScale(2, BigDecimal.ROUND_HALF_UP).add(overdueAmtTotal); //逾期还款中案件总金额
                        } else if (9 == i) {
                            if (Objects.isNull(obj)) { //如果为空的话则设为0
                                obj = 0;
                            }
                            advanceNumTotal = (Integer) obj + advanceNumTotal; //提前结清中案件总数
                        } else if (10 == i) {
                            if (Objects.isNull(obj)) { //如果为空的话则设为0
                                obj = new BigDecimal(0);
                            }
                            advanceAmtTotal = ((BigDecimal) obj).setScale(2, BigDecimal.ROUND_HALF_UP).add(advanceAmtTotal); //提前结清中案件总金额
                        } else if (11 == i) {
                            if (Objects.isNull(obj)) { //如果为空的话则设为0
                                obj = 0;
                            }
                            promiseNumTotal = (Integer) obj + promiseNumTotal; //承诺还款案件总数
                        } else if (12 == i) {
                            if (Objects.isNull(obj)) { //如果为空的话则设为0
                                obj = new BigDecimal(0);
                            }
                            promiseAmtTotal = ((BigDecimal) obj).setScale(2, BigDecimal.ROUND_HALF_UP).add(promiseAmtTotal); //承诺还款案件总金额
                        } else if (13 == i) {
                            if (Objects.isNull(obj)) { //如果为空的话则设为0
                                obj = 0;
                            }
                            endNumTotal = (Integer) obj + endNumTotal; //结案案件总数
                        } else if (14 == i) {
                            if (Objects.isNull(obj)) { //如果为空的话则设为0
                                obj = new BigDecimal(0);
                            }
                            endAmtTotal = ((BigDecimal) obj).setScale(2, BigDecimal.ROUND_HALF_UP).add(endAmtTotal); //结案案件总金额
                        }

                        paramIndex++;
                    }
                    //设置完毕后行数+1,进行下一行设置
                    userIndex++;
                }
                //合并组别
                CellRangeAddress cra = new CellRangeAddress(groupIndex, userIndex, 1, 1); // 四个参数分别是：起始行，结束行,起始列，结束列
                hssfSheet.addMergedRegion(cra); //在excel里增加合并单元格

                //在excel创建一行
                HSSFRow row = hssfSheet.createRow((short) (userIndex));

                //创建单元格
                HSSFCell cell;

                //给该行每一列设置数据
                for (int i = 1; i < 15; i++) { //i为报表模版数据列数,从第[1]列组别开始
                    if (2 == i) { //如果为第[2]列姓名,则设置为空
                        cell = row.createCell(i, CellType.STRING);
                        cell.setCellValue("");
                    } else if (1 == i) { //如果为第[1]列组别,则设置为累计
                        cell = row.createCell(i, CellType.STRING);
                        cell.setCellValue("累计");
                    } else if (3 == i) { //如果为第[3]列待催收案件数,则设置为waitCollectNumTotal
                        cell = row.createCell(i, CellType.NUMERIC);
                        cell.setCellValue(waitCollectNumTotal);
                    } else if (4 == i) { //如果为第[4]列待催收案件金额,则设置为waitCollectAmtTotal
                        cell = row.createCell(i, CellType.NUMERIC);
                        cell.setCellValue(waitCollectAmtTotal.doubleValue());
                    } else if (5 == i) { //如果为第[5]列催收中案件数,则设置为collectingNumTotal
                        cell = row.createCell(i, CellType.NUMERIC);
                        cell.setCellValue(collectingNumTotal);
                    } else if (6 == i) { //如果为第[6]列催收中案件金额,则设置为collectingAmtTotal
                        cell = row.createCell(i, CellType.NUMERIC);
                        cell.setCellValue(collectingAmtTotal.doubleValue());
                    } else if (7 == i) { //如果为第[7]列逾期还款中案件数,则设置为overdueNumTotal
                        cell = row.createCell(i, CellType.NUMERIC);
                        cell.setCellValue(overdueNumTotal);
                    } else if (8 == i) { //如果为第[8]列逾期还款中案件金额,则设置为overdueAmtTotal
                        cell = row.createCell(i, CellType.NUMERIC);
                        cell.setCellValue(overdueAmtTotal.doubleValue());
                    } else if (9 == i) { //如果为第[9]列提前结清中案件数,则设置为advanceNumTotal
                        cell = row.createCell(i, CellType.NUMERIC);
                        cell.setCellValue(advanceNumTotal);
                    } else if (10 == i) { //如果为第[10]列提前结清中案件金额,则设置为advanceAmtTotal
                        cell = row.createCell(i, CellType.NUMERIC);
                        cell.setCellValue(advanceAmtTotal.doubleValue());
                    } else if (11 == i) { //如果为第[11]列承诺还款案件数,则设置为promiseNumTotal
                        cell = row.createCell(i, CellType.NUMERIC);
                        cell.setCellValue(promiseNumTotal);
                    } else if (12 == i) { //如果为第12]列承诺还款案件金额,则设置为promiseAmtTotal
                        cell = row.createCell(i, CellType.NUMERIC);
                        cell.setCellValue(promiseAmtTotal.doubleValue());
                    } else if (13 == i) { //如果为第[13]列结案案件数,则设置为endNumTotal
                        cell = row.createCell(i, CellType.NUMERIC);
                        cell.setCellValue(endNumTotal);
                    } else if (14 == i) { //如果为第[14]列结案案件金额,则设置为endAmtTotal
                        cell = row.createCell(i, CellType.NUMERIC);
                        cell.setCellValue(endAmtTotal.doubleValue());
                    }
                }
                //设置完毕后行数+1,进行下一行设置
                userIndex++;
            }
            //设置组合并起始行
            groupIndex = userIndex;

            //合并部门
            CellRangeAddress cra = new CellRangeAddress(deptIndex, userIndex, 0, 0); // 四个参数分别是：起始行，结束行,起始列，结束列
            hssfSheet.addMergedRegion(cra); //在excel里增加合并单元格
        }
        //上传填好数据的报表
        return uploadExcel(hssfWorkbook);
    }

    /**
     * @Description 导出催收员每日催收过程报表
     */
    public String exportDailyProcessReport(GeneralParams generalParams, User tokenUser) throws IOException {
        //获取导出数据模型
        List<DailyProcessModel> dailyProcessModels = getDailyProcessReport(generalParams, tokenUser);
        if (Objects.isNull(dailyProcessModels)) {
            dailyProcessModels = new ArrayList<>();
        }

        //下载催收员回款报表模版
        //拼接请求地址
        String requestUrl = Constants.SYSPARAM_URL.concat("?").concat("userId").concat("=").concat(tokenUser.getId().
                concat("&").concat("companyCode").concat("=").concat(tokenUser.getCompanyCode()).concat("&").concat("code").concat("=").concat(Constants.DAILY_PROCESS_REPORT_EXCEL_URL_CODE).
                concat("&").concat("type").concat("=").concat(Constants.DAILY_PROCESS_REPORT_EXCEL_URL_TYPE));
        log.debug(requestUrl);
        //下载模版
        HSSFWorkbook hssfWorkbook = downloadTemplate(requestUrl);
        //设置excel表格样式
        HSSFCellStyle hssfCellStyle = setStyle(hssfWorkbook);
        //创建excel表格
        HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);

        //催收员开始行
        int userIndex = 3;

        //组合并开始行
        int groupIndex = 3;

        //部门合并开始行
        int deptIndex = 3;

        //案件总数
        Integer caseNumTotal;
        //案件总金额
        BigDecimal caseAmtTotal;
        //当日处理案件总数
        Integer handleNumTotal;
        //承诺还款案件总数
        Integer promiseNumTotal;
        //协商跟进案件总数
        Integer consultNumTotal;
        //他人转告案件总数
        Integer otherTellNumTotal;
        //查找案件总数
        Integer findNumTotal;
        //无人应答案件总数
        Integer noAnswerNumTotal;
        //每日呼叫总数
        Integer callNumTotal;
        //每日有效呼叫总数
        Integer effectiveCallNumTotal;
        //每日通话总时长
        Integer callDurationTotal;
        //沟通记录总条数
        Integer communicateNumTotal;

        //定义excel数据展示顺序
        String[] paramArray = {"realName", "nowDate", "caseNum", "caseAmt", "handleNum", "promiseNum", "consultNum", "otherTellNum", "findNum", "noAnswerNum", "callNum", "effectiveCallNum", "callDuration", "communicateNum"};

        //给excel中填值
        for (DailyProcessModel dailyProcessModel : dailyProcessModels) { //循环一级模型集合，获取二级模型集合
            List<DailyProcessSecModel> dailyProcessSecModels = dailyProcessModel.getDailyProcessSecModels();
            for (DailyProcessSecModel dailyProcessSecModel : dailyProcessSecModels) { //循环二级模型集合，获取三级模型集合
                List<DailyProcessThiModel> dailyProcessThiModels = dailyProcessSecModel.getDailyProcessThiModels();

                //给累计的数据初始化
                caseNumTotal = 0;
                caseAmtTotal = new BigDecimal(0);
                handleNumTotal = 0;
                promiseNumTotal = 0;
                consultNumTotal = 0;
                otherTellNumTotal = 0;
                findNumTotal = 0;
                noAnswerNumTotal = 0;
                callNumTotal = 0;
                effectiveCallNumTotal = 0;
                callDurationTotal = 0;
                communicateNumTotal = 0;
                for (DailyProcessThiModel dailyProcessThiModel : dailyProcessThiModels) { //循环三级模型集合，获取每日催收过程报表集合
                    List<DailyProcessReport> dailyProcessReports = dailyProcessThiModel.getDailyProcessReports();
                    for (DailyProcessReport dailyProcessReport : dailyProcessReports) { //遍历每一个每日催收过程报表
                        //在excel创建一行
                        HSSFRow row = hssfSheet.createRow((short) userIndex);

                        //创建单元格
                        HSSFCell cell;
                        int paramIndex = 0; //excel数据展示顺序数组角标

                        //给该行每一列设置数据
                        for (int i = 2; i < 16; i++) { //i为报表模版数据列数,从第[2]列姓名开始
                            Object obj = ExcelUtil.getProValue(paramArray[paramIndex], dailyProcessReport); //通过字段映射获取相应的数据
                            cell = setCellValue(obj, row, i); //给单元格set值
                            cell.setCellStyle(hssfCellStyle); //给单元格设置格式

                            //计算累计数据
                            if (4 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                caseNumTotal = (Integer) obj + caseNumTotal; //案件总数
                            } else if (5 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                caseAmtTotal = ((BigDecimal) obj).setScale(2, BigDecimal.ROUND_HALF_UP).add(caseAmtTotal); //案件总金额
                            } else if (6 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                handleNumTotal = (Integer) obj + handleNumTotal; //当日处理案件总数
                            } else if (7 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                promiseNumTotal = (Integer) obj + promiseNumTotal; //承诺还款案件总数
                            } else if (8 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                consultNumTotal = (Integer) obj + consultNumTotal; //协商跟进收案件总数
                            } else if (9 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                otherTellNumTotal = (Integer) obj + otherTellNumTotal; //他人转告案件总数
                            } else if (10 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                findNumTotal = (Integer) obj + findNumTotal; //查找收案件总数
                            } else if (11 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                noAnswerNumTotal = (Integer) obj + noAnswerNumTotal; //无人应答案件总数
                            } else if (12 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                callNumTotal = (Integer) obj + callNumTotal; //每日呼叫总数
                            } else if (13 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                effectiveCallNumTotal = (Integer) obj + effectiveCallNumTotal; //每日有效呼叫总数
                            } else if (14 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                callDurationTotal = (Integer) obj + callDurationTotal; //每日通话总时长
                            } else if (15 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                communicateNumTotal = (Integer) obj + communicateNumTotal; //沟通记录总条数
                            }
                            paramIndex++;
                        }
                        //设置完毕后行数+1,进行下一行设置
                        userIndex++;
                    }
                }
                //合并组别
                CellRangeAddress cra = new CellRangeAddress(groupIndex, userIndex, 1, 1); // 四个参数分别是：起始行，结束行,起始列，结束列
                hssfSheet.addMergedRegion(cra); //在excel里增加合并单元格

                //如果导出实时报表
                if (Objects.equals(generalParams.getType(), 0)) { //如果导出实时报表，则多加一行累计
                    //在excel创建一行
                    HSSFRow row = hssfSheet.createRow((short) (userIndex));

                    //创建单元格
                    HSSFCell cell;

                    //给该行每一列设置数据
                    for (int i = 1; i < 16; i++) { //i为报表模版数据列数,从第[1]列组别开始
                        if (2 == i || 3 == i) { //如果为第[2]列姓名或者第[3]列日期,则设置为空
                            cell = row.createCell(i, CellType.STRING);
                            cell.setCellValue("");
                        } else if (1 == i) { //如果为第[1]列组别,则设置为累计
                            cell = row.createCell(i, CellType.STRING);
                            cell.setCellValue("累计");
                        } else if (i == 4) { //如果为第[4]列案件数量,则设置为caseNumTotal
                            cell = row.createCell(i, CellType.NUMERIC);
                            cell.setCellValue(caseNumTotal);
                        } else if (i == 5) { //如果为第[5]列案件金额,则设置为caseAmtTotal
                            cell = row.createCell(i, CellType.NUMERIC);
                            cell.setCellValue(caseAmtTotal.doubleValue());
                        } else if (i == 6) { //如果为第[6]列当日处理案件数量,则设置为handleNumTotal
                            cell = row.createCell(i, CellType.NUMERIC);
                            cell.setCellValue(handleNumTotal);
                        } else if (i == 7) { //如果为第[7]列承诺还款案件数,则设置为promiseNumTotal
                            cell = row.createCell(i, CellType.NUMERIC);
                            cell.setCellValue(promiseNumTotal);
                        } else if (i == 8) { //如果为第[8]列协商跟进案件数,则设置为consultNumTotal
                            cell = row.createCell(i, CellType.NUMERIC);
                            cell.setCellValue(consultNumTotal);
                        } else if (i == 9) { //如果为第[9]列他人转告案件数,则设置为otherTellNumTotal
                            cell = row.createCell(i, CellType.NUMERIC);
                            cell.setCellValue(otherTellNumTotal);
                        } else if (i == 10) { //如果为第[10]列查找案件数,则设置为findNumTotal
                            cell = row.createCell(i, CellType.NUMERIC);
                            cell.setCellValue(findNumTotal);
                        } else if (i == 11) { //如果为第[11]列无人应答案件数,则设置为noAnswerNumTotal
                            cell = row.createCell(i, CellType.NUMERIC);
                            cell.setCellValue(noAnswerNumTotal);
                        } else if (i == 12) { //如果为第[12]列每日呼叫数,则设置为callNumTotal
                            cell = row.createCell(i, CellType.NUMERIC);
                            cell.setCellValue(callNumTotal);
                        } else if (i == 13) { //如果为第[13]列每日有效呼叫数,则设置为effectiveCallNumTotal
                            cell = row.createCell(i, CellType.NUMERIC);
                            cell.setCellValue(effectiveCallNumTotal);
                        } else if (i == 14) { //如果为第[14]列每日通话时长,则设置为callDurationTotal
                            cell = row.createCell(i, CellType.NUMERIC);
                            cell.setCellValue(callDurationTotal);
                        } else if (i == 15) { //如果为第[15]列沟通记录条数,则设置为communicateNumTotal
                            cell = row.createCell(i, CellType.NUMERIC);
                            cell.setCellValue(communicateNumTotal);
                        }
                    }
                    //设置完毕后行数+1,进行下一行设置
                    userIndex++;
                }
            }
            //设置组合并起始行
            groupIndex = userIndex;

            //合并部门
            CellRangeAddress cra = new CellRangeAddress(deptIndex, userIndex, 0, 0); // 四个参数分别是：起始行，结束行,起始列，结束列
            hssfSheet.addMergedRegion(cra); //在excel里增加合并单元格
        }
        //上传填好数据的报表
        return uploadExcel(hssfWorkbook);
    }

    /**
     * @Description 导出催收员每日催收结果报表
     */
    public String exportDailyResultReport(GeneralParams generalParams, User tokenUser) throws IOException {
        //获取导出数据模型
        List<DailyResultModel> dailyResultModels = getDailyResultReport(generalParams, tokenUser);
        if (Objects.isNull(dailyResultModels)) {
            dailyResultModels = new ArrayList<>();
        }

        //下载催收员回款报表模版
        //拼接请求地址
        String requestUrl = Constants.SYSPARAM_URL.concat("?").concat("userId").concat("=").concat(tokenUser.getId().
                concat("&").concat("companyCode").concat("=").concat(tokenUser.getCompanyCode()).concat("&").concat("code").concat("=").concat(Constants.DAILY_RESULT_REPORT_EXCEL_URL_CODE).
                concat("&").concat("type").concat("=").concat(Constants.DAILY_RESULT_REPORT_EXCEL_URL_TYPE));
        log.debug(requestUrl);
        //下载模版
        HSSFWorkbook hssfWorkbook = downloadTemplate(requestUrl);
        //设置excel表格样式
        HSSFCellStyle hssfCellStyle = setStyle(hssfWorkbook);
        //创建excel表格
        HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);

        //催收员开始行
        int userIndex = 2;

        //组合并开始行
        int groupIndex = 2;

        //部门合并开始行
        int deptIndex = 2;

        //案件总数
        Integer caseNumTotal;
        //案件总金额
        BigDecimal caseAmtTotal;
        //当日回款总户数
        Integer dayNumTotal;
        //当日回款总金额
        BigDecimal dayAmtTotal;
        //月度回款总户数
        Integer monthNumTotal;
        //月度回款总金额
        BigDecimal monthAmtTotal;
        //当日回款总户数比
        BigDecimal dayNumRateTotal;
        //当日回款总金额比
        BigDecimal dayAmtRateTotal;
        //总回款比
        BigDecimal backMoneyRateTotal;
        //月度回款金额总目标
        BigDecimal targetTotal;

        //定义excel数据展示顺序
        String[] paramArray = {"realName", "nowDate", "caseNum", "caseAmt", "dayNum", "dayAmt", "monthNum", "monthAmt", "dayNumRate", "dayAmtRate", "backMoneyRate", "target"};

        //给excel中填值
        for (DailyResultModel dailyResultModel : dailyResultModels) { //循环一级模型集合，获取二级模型集合
            List<DailyResultSecModel> dailyResultSecModels = dailyResultModel.getDailyResultSecModels();
            for (DailyResultSecModel dailyResultSecModel : dailyResultSecModels) { //循环二级模型集合，获取三级模型集合
                List<DailyResultThiModel> dailyResultThiModels = dailyResultSecModel.getDailyResultThiModels();

                //给累计的数据初始化
                caseNumTotal = 0;
                caseAmtTotal = new BigDecimal(0);
                dayNumTotal = 0;
                dayAmtTotal = new BigDecimal(0);
                monthNumTotal = 0;
                monthAmtTotal = new BigDecimal(0);
                dayNumRateTotal = new BigDecimal(0);
                dayAmtRateTotal = new BigDecimal(0);
                backMoneyRateTotal = new BigDecimal(0);
                targetTotal = new BigDecimal(0);
                for (DailyResultThiModel dailyResultThiModel : dailyResultThiModels) { //循环三级模型集合，获取每日催收过程报表集合
                    List<DailyResultReport> dailyResultReports = dailyResultThiModel.getDailyResultReports();
                    for (DailyResultReport dailyResultReport : dailyResultReports) { //遍历每一个每日催收过程报表
                        //在excel创建一行
                        HSSFRow row = hssfSheet.createRow((short) userIndex);

                        //创建单元格
                        HSSFCell cell;
                        int paramIndex = 0; //excel数据展示顺序数组角标

                        //给该行每一列设置数据
                        for (int i = 2; i < 14; i++) { //i为报表模版数据列数,从第[2]列姓名开始
                            Object obj = ExcelUtil.getProValue(paramArray[paramIndex], dailyResultReport); //通过字段映射获取相应的数据
                            if (10 == i || 11 == i || 12 == i) {
                                cell = row.createCell(i, CellType.STRING);
                                BigDecimal temp = ((BigDecimal) obj).setScale(2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                                cell.setCellValue(temp.toString() + "%");
                            } else {
                                cell = setCellValue(obj, row, i); //给单元格set值
                            }
                            cell.setCellStyle(hssfCellStyle); //给单元格设置格式

                            //计算累计数据
                            if (4 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                caseNumTotal = (Integer) obj + caseNumTotal; //案件总数
                            } else if (5 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                caseAmtTotal = ((BigDecimal) obj).setScale(2, BigDecimal.ROUND_HALF_UP).add(caseAmtTotal); //案件总金额
                            } else if (6 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                dayNumTotal = (Integer) obj + dayNumTotal; //当日回款总户数
                            } else if (7 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                dayAmtTotal = ((BigDecimal) obj).setScale(2, BigDecimal.ROUND_HALF_UP).add(dayAmtTotal); //当日回款总金额
                            } else if (8 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                monthNumTotal = (Integer) obj + monthNumTotal; //月度回款总户数
                            } else if (9 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                monthAmtTotal = ((BigDecimal) obj).setScale(2, BigDecimal.ROUND_HALF_UP).add(monthAmtTotal); //月度回款总金额
                            } else if (10 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                dayNumRateTotal = ((BigDecimal) obj).setScale(2, BigDecimal.ROUND_HALF_UP).add(dayNumRateTotal); //当日回款总户数比
                            } else if (11 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                dayAmtRateTotal = ((BigDecimal) obj).setScale(2, BigDecimal.ROUND_HALF_UP).add(dayAmtRateTotal); //当日回款总金额比
                            } else if (12 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                backMoneyRateTotal = ((BigDecimal) obj).setScale(2, BigDecimal.ROUND_HALF_UP).add(backMoneyRateTotal); //总回款比
                            } else if (13 == i) {
                                if (Objects.isNull(obj)) { //如果为空的话则设为0
                                    obj = 0;
                                }
                                targetTotal = ((BigDecimal) obj).setScale(2, BigDecimal.ROUND_HALF_UP).add(targetTotal); //月度回款金额总目标
                            }
                            paramIndex++;
                        }
                        //设置完毕后行数+1,进行下一行设置
                        userIndex++;
                    }
                }
                //合并组别
                CellRangeAddress cra = new CellRangeAddress(groupIndex, userIndex, 1, 1); // 四个参数分别是：起始行，结束行,起始列，结束列
                hssfSheet.addMergedRegion(cra); //在excel里增加合并单元格

                //如果导出实时报表
                if (Objects.equals(generalParams.getType(), 0)) { //如果导出实时报表，则多加一行累计
                    //在excel创建一行
                    HSSFRow row = hssfSheet.createRow((short) (userIndex));

                    //创建单元格
                    HSSFCell cell;

                    //给该行每一列设置数据
                    for (int i = 1; i < 14; i++) { //i为报表模版数据列数,从第[1]列组别开始
                        if (2 == i || 3 == i) { //如果为第[2]列姓名或者第[3]列日期,则设置为空
                            cell = row.createCell(i, CellType.STRING);
                            cell.setCellValue("");
                        } else if (1 == i) { //如果为第[1]列组别,则设置为累计
                            cell = row.createCell(i, CellType.STRING);
                            cell.setCellValue("累计");
                        } else if (i == 4) { //如果为第[4]列案件数量,则设置为caseNumTotal
                            cell = row.createCell(i, CellType.NUMERIC);
                            cell.setCellValue(caseNumTotal);
                        } else if (i == 5) { //如果为第[5]列案件金额,则设置为caseAmtTotal
                            cell = row.createCell(i, CellType.NUMERIC);
                            cell.setCellValue(caseAmtTotal.doubleValue());
                        } else if (i == 6) { //如果为第[6]列当日回款户数,则设置为dayNumTotal
                            cell = row.createCell(i, CellType.NUMERIC);
                            cell.setCellValue(dayNumTotal);
                        } else if (i == 7) { //如果为第[7]列单日回款金额,则设置为dayAmtTotal
                            cell = row.createCell(i, CellType.NUMERIC);
                            cell.setCellValue(dayAmtTotal.doubleValue());
                        } else if (i == 8) { //如果为第[8]列月度累计回款户数,则设置为monthNumTotal
                            cell = row.createCell(i, CellType.NUMERIC);
                            cell.setCellValue(monthNumTotal);
                        } else if (i == 9) { //如果为第[9]列月度累计回款金额,则设置为monthAmtTotal
                            cell = row.createCell(i, CellType.NUMERIC);
                            cell.setCellValue(monthAmtTotal.doubleValue());
                        } else if (i == 10) { //如果为第[10]列当日回款户数比,则设置为dayNumRateTotal
                            cell = row.createCell(i, CellType.STRING);
                            cell.setCellValue(dayNumRateTotal.multiply(new BigDecimal(100)).toString() + "%");
                        } else if (i == 11) { //如果为第[11]列当日回款金额比,则设置为dayAmtRateTotal
                            cell = row.createCell(i, CellType.STRING);
                            cell.setCellValue(dayNumRateTotal.multiply(new BigDecimal(100)).toString() + "%");
                        } else if (i == 12) { //如果为第[12]列回款比,则设置为backMoneyRateTotal
                            cell = row.createCell(i, CellType.STRING);
                            cell.setCellValue(dayNumRateTotal.multiply(new BigDecimal(100)).toString() + "%");
                        } else if (i == 13) { //如果为第[13]列月度回款金额目标,则设置为targetTotal
                            cell = row.createCell(i, CellType.NUMERIC);
                            cell.setCellValue(targetTotal.doubleValue());
                        }
                    }
                    //设置完毕后行数+1,进行下一行设置
                    userIndex++;
                }
            }
            //设置组合并起始行
            groupIndex = userIndex;

            //合并部门
            CellRangeAddress cra = new CellRangeAddress(deptIndex, userIndex, 0, 0); // 四个参数分别是：起始行，结束行,起始列，结束列
            hssfSheet.addMergedRegion(cra); //在excel里增加合并单元格
        }
        //上传填好数据的报表
        return uploadExcel(hssfWorkbook);
    }

    /**
     * @Description 给报表单元格set值
     */
    private HSSFCell setCellValue(Object obj, HSSFRow row, Integer j) {
        HSSFCell cell;
        if (obj instanceof String) {
            cell = row.createCell(j, CellType.STRING);
            cell.setCellValue((String) obj);
        } else if (obj instanceof BigDecimal) {
            cell = row.createCell(j, CellType.NUMERIC);
            BigDecimal amt = ((BigDecimal) obj).setScale(2, BigDecimal.ROUND_HALF_UP);
            cell.setCellValue(amt.doubleValue());
        } else if (obj instanceof Integer) {
            cell = row.createCell(j, CellType.NUMERIC);
            cell.setCellValue((Integer) obj);
        } else if (obj instanceof Double) {
            cell = row.createCell(j, CellType.STRING);
            cell.setCellValue(new BigDecimal((Double) obj * 100).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%");
        } else if (obj instanceof Date) {
            cell = row.createCell(j, CellType.STRING);
            cell.setCellValue(ZWDateUtil.fomratterDate((Date) obj, "yyyy-MM-dd"));
        } else {
            cell = row.createCell(j, CellType.STRING);
            cell.setCellValue(obj.toString());
        }
        return cell;
    }

    /**
     * @Description excel样式
     */
    private HSSFCellStyle setStyle(HSSFWorkbook hssfWorkbook) throws IOException {
        //设置报表excel格式
        HSSFCellStyle hssfCellStyle = hssfWorkbook.createCellStyle();
        Font fontBody = hssfWorkbook.createFont();
        fontBody.setFontName("宋体");
        fontBody.setFontHeightInPoints((short) 11);
        hssfCellStyle.setFont(fontBody);
        hssfCellStyle.setBorderLeft(BorderStyle.THIN);
        hssfCellStyle.setBorderRight(BorderStyle.THIN);
        hssfCellStyle.setBorderBottom(BorderStyle.THIN);
        hssfCellStyle.setBorderTop(BorderStyle.THIN);
        hssfCellStyle.setAlignment(HorizontalAlignment.CENTER);
        hssfCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return hssfCellStyle;
    }

    /**
     * @Description 下载报表模版
     */
    private HSSFWorkbook downloadTemplate(String requestUrl) throws IOException {
        ResponseEntity<SysParam> responseEntity = restTemplate.getForEntity(requestUrl, SysParam.class);
        if (!responseEntity.hasBody()) {
            throw new RuntimeException("报表模版系统参数未找到");
        }
        //获得催收员回款报表模型地址
        String url = responseEntity.getBody().getValue();

        //获取报表模型
        HttpHeaders headers = new HttpHeaders();
        RestTemplate restOutTemplate = new RestTemplate();
        ResponseEntity<byte[]> response = restOutTemplate.exchange(url,
                HttpMethod.GET, new HttpEntity<byte[]>(headers),
                byte[].class);
        InputStream inputStream = new ByteArrayInputStream(response.getBody());
        return new HSSFWorkbook(inputStream);
    }

    /**
     * @Description 把生成的报表上传到文件服务器
     */
    private String uploadExcel(HSSFWorkbook hssfWorkbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        hssfWorkbook.write(out);
        String resultFilePath = FileUtils.getTempDirectoryPath().concat(File.separator).concat(DateTime.now().toString("yyyyMMddhhmmss") + "催收员回款报表.xls");
        File file = new File(resultFilePath);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(out.toByteArray());
        FileSystemResource resource = new FileSystemResource(file);
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("file", resource);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity("http://file-service/api/uploadFile/addUploadFileUrl", param, String.class);
        return responseEntity.getBody();
    }
}
