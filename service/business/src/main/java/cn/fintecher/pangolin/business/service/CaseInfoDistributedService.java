package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.model.AccCaseInfoDisModel;
import cn.fintecher.pangolin.business.repository.*;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.util.ZWDateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author: PeiShouWen
 * @Description: 案件分配服务类
 * @Date 16:38 2017/8/7
 */
@Service("caseInfoDistributedService")
public class CaseInfoDistributedService {

    Logger logger = LoggerFactory.getLogger(CaseInfoDistributedService.class);

    @Inject
    CaseInfoRepository caseInfoRepository;

    @Inject
    CaseAssistRepository caseAssistRepository;

    @Inject
    CaseAssistApplyRepository caseAssistApplyRepository;

    @Inject
    CaseTurnRecordRepository caseTurnRecordRepository;

    @Inject
    CasePayApplyRepository casePayApplyRepository;

    @Inject
    CaseFollowupRecordRepository caseFollowupRecordRepository;

    @Inject
    SysParamRepository sysParamRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    UserService userService;

    @Inject
    CasePayFileRepository casePayFileRepository;

    @Inject
    PersonalContactRepository personalContactRepository;

    @Inject
    RestTemplate restTemplate;

    @Autowired
    CaseInfoExceptionService caseInfoExceptionService;

    @Autowired
    CaseInfoDistributedRepository caseInfoDistributedRepository;

    @Autowired
    CaseRepairRepository caseRepairRepository;

    @Inject
    DepartmentRepository departmentRepository;

    @Inject
    CaseInfoService caseInfoService;

    /**
     * 案件分配
     *
     * @param accCaseInfoDisModel
     * @param user
     * @throws Exception
     */
    @Transactional
    public void distributeCeaseInfo(AccCaseInfoDisModel accCaseInfoDisModel, User user) throws Exception {
        //检查案件异常池是否有未处理的数据
        if (caseInfoExceptionService.checkCaseExceptionExist(user)) {
            throw new Exception("有未处理的异常案件，请处理");
        } else {
            //案件列表
            List<CaseInfo> caseInfoObjList = new ArrayList<>();
            //流转记录列表
            List<CaseTurnRecord> caseTurnRecordList = new ArrayList<>();
            List<CaseRepair> caseRepairList = new ArrayList<>();
            //选择的案件ID列表
            List<String> caseInfoList = accCaseInfoDisModel.getCaseIdList();
            //每个机构或人分配的数量
            List<Integer> disNumList = accCaseInfoDisModel.getCaseNumList();
            //已经分配的案件数量
            int alreadyCaseNum = 0;
            //接收案件列表信息
            List<String> deptOrUserList = null;
            //机构分配
            if (accCaseInfoDisModel.getDisType().equals(AccCaseInfoDisModel.DisType.DEPART_WAY.getValue())) {
                //所要分配 机构id
                deptOrUserList = accCaseInfoDisModel.getDepIdList();
            } else if (accCaseInfoDisModel.getDisType().equals(AccCaseInfoDisModel.DisType.USER_WAY.getValue())) {
                //得到所有用户ID
                deptOrUserList = accCaseInfoDisModel.getUserIdList();
            }
            for (int i = 0; i < (deptOrUserList != null ? deptOrUserList.size() : 0); i++) {
                //如果按机构分配则是机构的ID，如果是按用户分配则是用户ID
                String deptOrUserid = deptOrUserList.get(i);
                Department department = null;
                User targetUser = null;
                if (accCaseInfoDisModel.getDisType().equals(AccCaseInfoDisModel.DisType.DEPART_WAY.getValue())) {
                    department = departmentRepository.findOne(deptOrUserid);
                } else if (accCaseInfoDisModel.getDisType().equals(AccCaseInfoDisModel.DisType.USER_WAY.getValue())) {
                    targetUser = userRepository.findOne(deptOrUserid);
                }
                //需要分配的案件数据
                Integer disNum = disNumList.get(i);
                for (int j = 0; j < disNum; j++) {
                    //检查输入的案件数量是否和选择的案件数量一致
                    if (alreadyCaseNum > caseInfoList.size()) {
                        throw new Exception("选择的案件总量与实际输入的案件数量不匹配");
                    }
                    String caseId = caseInfoList.get(alreadyCaseNum);
                    CaseInfoDistributed caseInfoDistributed = caseInfoDistributedRepository.findOne(caseId);
                    if (Objects.nonNull(caseInfoDistributed)) {
                        CaseInfo caseInfo = new CaseInfo();
                        BeanUtils.copyProperties(caseInfoDistributed, caseInfo);
//                        caseInfo.setHasPayAmount(new BigDecimal(0)); //已还款金额
//                        caseInfo.setImpHasPayAmount(caseInfoDistributed.getHasPayAmount()); //导入已还款款金额
                        caseInfo.setCaseType(CaseInfo.CaseType.DISTRIBUTE.getValue()); //案件类型-案件分配
                        if (Objects.nonNull(department)) {
                            caseInfo.setDepartment(department); //部门
                            caseInfoService.setCollectionType(caseInfo, department, null);
                            caseInfo.setCaseFollowInTime(null); //案件流入时间
                            caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.WAIT_FOR_DIS.getValue()); //催收状态-待分配
                        }
                        if (Objects.nonNull(targetUser)) {
                            caseInfo.setDepartment(targetUser.getDepartment());
                            caseInfo.setCurrentCollector(targetUser);
                            caseInfoService.setCollectionType(caseInfo, null, targetUser);
                            caseInfo.setCaseFollowInTime(ZWDateUtil.getNowDateTime());
                            caseInfo.setCollectionStatus(CaseInfo.CollectionStatus.WAITCOLLECTION.getValue()); //催收状态-待催收
                        }
                        caseInfo.setLeaveCaseFlag(CaseInfo.leaveCaseFlagEnum.NO_LEAVE.getValue()); //留案标识默认-非留案
                        caseInfo.setAssistFlag(CaseInfo.AssistFlag.NO_ASSIST.getValue());
                        //案件剩余天数(结案日期-当前日期)
                        caseInfo.setLeftDays(ZWDateUtil.getBetween(ZWDateUtil.getNowDate(), caseInfo.getCloseDate(), ChronoUnit.DAYS));
                        //案件类型
                        caseInfo.setCaseType(CaseInfo.CaseType.DISTRIBUTE.getValue());
                        caseInfo.setOperator(user);
                        caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime());
                        //案件流转记录
                        CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
                        BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
                        caseTurnRecord.setId(null); //主键置空
                        caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
                        caseTurnRecord.setDepartId(caseInfo.getDepartment().getId()); //部门ID
                        if (Objects.nonNull(caseInfo.getCurrentCollector())) { //催收员不为空则是分给催收员
                            caseTurnRecord.setReceiveDeptName(caseInfo.getCurrentCollector().getDepartment().getName()); //接收部门名称
                            caseTurnRecord.setReceiveUserId(caseInfo.getCurrentCollector().getId()); //接收人ID
                            caseTurnRecord.setReceiveUserRealName(caseInfo.getCurrentCollector().getRealName()); //接受人名称
                        } else {
                            caseTurnRecord.setReceiveDeptName(caseInfo.getDepartment().getName());
                        }
                        caseTurnRecord.setCollectionType(3); //流转类型 3-正常流转
                        caseTurnRecord.setOperatorUserName(user.getUserName()); //操作员用户名
                        caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
                        caseTurnRecordList.add(caseTurnRecord);

                        //进入案件修复池
                        CaseRepair caseRepair = new CaseRepair();
                        caseRepair.setCaseId(caseInfo);
                        caseRepair.setRepairStatus(CaseRepair.CaseRepairStatus.REPAIRING.getValue());
                        caseRepair.setOperatorTime(ZWDateUtil.getNowDateTime());
                        caseRepair.setCompanyCode(user.getCompanyCode());
                        caseRepairList.add(caseRepair);
                        caseInfo.setId(null);
                        //案件列表
                        caseInfoObjList.add(caseInfo);
                    }
                    alreadyCaseNum = alreadyCaseNum + 1;
                }
            }
            //保存案件信息
            caseInfoRepository.save(caseInfoObjList);
            //保存流转记录
            caseTurnRecordRepository.save(caseTurnRecordList);
            //保存修复信息
            caseRepairRepository.save(caseRepairList);
            //删除待分配案件
            for (String id : caseInfoList) {
                caseInfoDistributedRepository.delete(id);
            }
        }
    }


}
