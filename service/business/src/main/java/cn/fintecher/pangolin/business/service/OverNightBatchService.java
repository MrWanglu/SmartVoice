package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.repository.CaseAssistRepository;
import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.business.repository.CaseTurnRecordRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.Constants;
import cn.fintecher.pangolin.util.ZWDateUtil;
import com.querydsl.jpa.impl.JPAQuery;
import org.quartz.JobDataMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @Author: PeiShouWen
 * @Description:  晚间批量
 * @Date 15:46 2017/8/11
 */
@Service("overNightBatchService")
public class OverNightBatchService {
    Logger logger= LoggerFactory.getLogger(OverNightBatchService.class);

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    JobTaskService jobTaskService;

    @Autowired
    CaseInfoRepository caseInfoRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    CaseAssistRepository caseAssistRepository;

    @Autowired
    CaseTurnRecordRepository caseTurnRecordRepository;

    /**
     * 重置批次号和案件编号
     * @param jobDataMap
     * @param step
     */
    @Transactional
    public void doOverNightOne(JobDataMap jobDataMap,String step)throws Exception{
        ResponseEntity responseEntity=null;
        try {
            responseEntity=restTemplate.getForEntity("http://dataimp-service/api/sequenceController/restSequence?id1=".concat(Constants.ORDER_SEQ)
                    .concat("&id2=").concat(Constants.CASE_SEQ).
                            concat("&companyCode=").concat(jobDataMap.getString("sysParamCode")), ResponseEntity.class);
            if(Objects.isNull(responseEntity) || Objects.isNull(responseEntity.getBody())){
                throw  new Exception(jobDataMap.getString("sysParamCode").concat("重置序列失败"));
            }
            //更新批量步骤
            jobTaskService.updateSysparam(jobDataMap.getString("sysParamCode"),Constants.SYSPARAM_OVERNIGHT_STEP,step);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw  new Exception(jobDataMap.getString("sysParamCode").concat("重置序列失败"));
        }
    }

    /**
     * 案件强制流转
     * @param jobDataMap
     * @param step
     * @throws Exception
     */
    @Transactional
    public void doOverNightTwo(JobDataMap jobDataMap,String step,User user)throws Exception{
        QCaseInfo qCaseInfo=QCaseInfo.caseInfo;
        JPAQuery<CaseInfo> jpaQuery=new JPAQuery<>(entityManager);
        try{
            //获取电催留案天数
            SysParam sysParam = jobTaskService.getSysparam(jobDataMap.getString("companyCode"), Constants.SYS_PHNOEFLOW_BIGDAYS);
            List<CaseInfo> caseInfoList = forceTurnCase(sysParam, qCaseInfo, jpaQuery,CaseInfo.CollectionType.TEL.getValue());
            //电催更新
            updateCaseInfo(user, caseInfoList,CaseInfo.CaseType.PHNONEFORCETURN.getValue(),Constants.SYS_PHNOETURN_BIGDEPTNAME);
            //获取外访留案天数
            SysParam sysParam2 = jobTaskService.getSysparam(jobDataMap.getString("companyCode"), Constants.SYS_OUTBOUNDFLOW_BIGDAYS);
            List<CaseInfo> caseInfoList2 = forceTurnCase(sysParam2, qCaseInfo, jpaQuery,CaseInfo.CollectionType.VISIT.getValue());
            //外访更新
            updateCaseInfo(user, caseInfoList2,CaseInfo.CaseType.OUTFORCETURN.getValue(),Constants.SYS_OUTTURN_BIGDEPTNAME);
            //更新批量步骤
            jobTaskService.updateSysparam(jobDataMap.getString("sysParamCode"),Constants.SYSPARAM_OVERNIGHT_STEP,step);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new Exception(jobDataMap.getString("sysParamCode").concat("案件强制流转批量失败"));
        }
    }


    /**
     * 案件小流转
     * @param jobDataMap
     * @param step
     */
    @Transactional
    public void doOverNightThree(JobDataMap jobDataMap,String step,User user) throws Exception{
        QCaseInfo qCaseInfo=QCaseInfo.caseInfo;
        QCaseFollowupRecord qCaseFollowupRecord=QCaseFollowupRecord.caseFollowupRecord;
        JPAQuery<CaseInfo> jpaQuery=new JPAQuery<>(entityManager);
        //查找非留案、非结案、配置天数类无任何催收反馈的电催案件
        try {
            //电催小流转配置参数
            SysParam sysParam1 = jobTaskService.getSysparam(jobDataMap.getString("companyCode"), Constants.SYS_PHNOEFLOW_SMALLDAYS);
            List<CaseInfo> caseInfoList1 = smallTurnCase(sysParam1, qCaseInfo, qCaseFollowupRecord, jpaQuery, CaseInfo.CollectionType.TEL.getValue());
            //电催更新
            updateCaseInfo(user, caseInfoList1,CaseInfo.CaseType.PHNONESMALLTURN.getValue(),Constants.SYS_PHNOETURN_SMALLDEPTNAME);

            //外访小流转
            SysParam sysParam2 = jobTaskService.getSysparam(jobDataMap.getString("companyCode"), Constants.SYS_OUTBOUNDFLOW_SMALLDAYS);
            List<CaseInfo> caseInfoList2 = smallTurnCase(sysParam2, qCaseInfo, qCaseFollowupRecord, jpaQuery, CaseInfo.CollectionType.VISIT.getValue());
            //外访更新
            updateCaseInfo(user, caseInfoList2,CaseInfo.CaseType.OUTSMALLTURN.getValue(),Constants.SYS_OUTTURN_SMALLDEPTNAME);
            //更新批量步骤
            jobTaskService.updateSysparam(jobDataMap.getString("sysParamCode"),Constants.SYSPARAM_OVERNIGHT_STEP,step);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new Exception(jobDataMap.getString("sysParamCode").concat("案件小流转批量失败"));
        }
    }

    /**
     * 留案案件流转
     * @param jobDataMap
     * @param step
     */
    @Transactional
    public void doOverNightFour(JobDataMap jobDataMap,String step,User user) throws Exception{
        QCaseInfo qCaseInfo=QCaseInfo.caseInfo;
        JPAQuery<CaseInfo> jpaQuery=new JPAQuery<>(entityManager);
        try{
            //电催小流转配置参数
            SysParam sysParam1 = jobTaskService.getSysparam(jobDataMap.getString("companyCode"), Constants.SYS_PHNOEFLOW_LEAVEDAYS);
            List<CaseInfo> caseInfoList1 = leaveTurnCase(qCaseInfo, jpaQuery, sysParam1, CaseInfo.CollectionType.TEL.getValue());
            //电催更新
            updateCaseInfo(user, caseInfoList1,CaseInfo.CaseType.PHNONELEAVETURN.getValue(),Constants.SYS_PHNOETURN_LEAVEDEPTNAME);

            //外访小流转配置参数
            SysParam sysParam2 = jobTaskService.getSysparam(jobDataMap.getString("companyCode"), Constants.SYS_OUTBOUNDFLOW_LEAVEDAYS);
            List<CaseInfo> caseInfoList2 = leaveTurnCase(qCaseInfo, jpaQuery, sysParam2, CaseInfo.CollectionType.VISIT.getValue());
            //外访更新
            updateCaseInfo(user, caseInfoList2,CaseInfo.CaseType.OUTLEAVETURN.getValue(),Constants.SYS_OUTTURN_LEAVEDEPTNAME);

            //更新批量步骤
            jobTaskService.updateSysparam(jobDataMap.getString("sysParamCode"),Constants.SYSPARAM_OVERNIGHT_STEP,step);

        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new Exception(jobDataMap.getString("sysParamCode").concat("案件留案流转批量失败"));
        }
    }



    /**
     * 持案天数/剩余天数/留案天数更新
     * @param jobDataMap
     * @param step
     */
    @Transactional
    public void doOverNightFive(JobDataMap jobDataMap,String step) throws Exception{
        try{
            QCaseInfo qCaseInfo=QCaseInfo.caseInfo;
            JPAQuery<CaseInfo> jpaQuery=new JPAQuery<>(entityManager);
            jpaQuery.select(qCaseInfo)
                    .from(QCaseInfo.caseInfo);
            jpaQuery.where(qCaseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue())//未结案
                    .and(qCaseInfo.currentCollector.isNotNull())
                    .and(qCaseInfo.companyCode.eq(jobDataMap.getString("sysParamCode"))));//催收员不为空
            List<CaseInfo> caseInfoList = jpaQuery.fetch();
            //更新相关天数
            for(CaseInfo caseInfo :caseInfoList){
                caseInfo.setHoldDays(caseInfo.getHoldDays()+1);//持案天数
                if(Objects.nonNull(caseInfo.getLeaveCaseFlag()) && caseInfo.getLeaveCaseFlag().equals(CaseInfo.leaveCaseFlagEnum.YES_LEAVE.getValue())){
                //留案天数
                 caseInfo.setHasLeaveDays(caseInfo.getHasLeaveDays()+1);
                }
                //案件剩余天数
               Integer leftDays= ZWDateUtil.getBetween(ZWDateUtil.getNowDate(),caseInfo.getCloseDate(), ChronoUnit.DAYS);
                if(leftDays.intValue()<0){
                    leftDays=0;
                }
                caseInfo.setLeftDays(leftDays);
            }
            caseInfoRepository.save(caseInfoList);
            //协催的相关天数
            JPAQuery<CaseAssist> caseAssistJPAQuery=new JPAQuery<>(entityManager);
            QCaseAssist qCaseAssist=QCaseAssist.caseAssist;
            caseAssistJPAQuery.select(qCaseAssist)
                                .from(qCaseAssist);
            caseAssistJPAQuery.where(qCaseAssist.assistStatus.ne(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())//协催未结束
                                        .and(qCaseAssist.currentCollector.isNotNull())//协催已分配
                                        .and(qCaseAssist.companyCode.eq(jobDataMap.getString("sysParamCode"))));//公司CODE
            List<CaseAssist> caseAssistList = caseAssistJPAQuery.fetch();
            for(CaseAssist caseAssist:caseAssistList){
                caseAssist.setHoldDays(caseAssist.getHoldDays()+1);
            }
            caseAssistRepository.save(caseAssistList);
            //更新批量步骤
            jobTaskService.updateSysparam(jobDataMap.getString("sysParamCode"),Constants.SYSPARAM_OVERNIGHT_STEP,step);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new Exception(jobDataMap.getString("sysParamCode").concat("案件相关天数更新批量失败"));
        }
    }

    /**
     * 留案案件查询
     * @param qCaseInfo
     * @param jpaQuery
     * @param sysParam
     * @param collectionType
     * @return
     */
    private List<CaseInfo> leaveTurnCase(QCaseInfo qCaseInfo, JPAQuery<CaseInfo> jpaQuery, SysParam sysParam,Integer collectionType) {
        jpaQuery.select(qCaseInfo)
                .from(QCaseInfo.caseInfo);
        jpaQuery.where(qCaseInfo.leaveCaseFlag.eq(CaseInfo.leaveCaseFlagEnum.YES_LEAVE.getValue())//留案
                .and(qCaseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue()))//未结案
                .and(qCaseInfo.currentCollector.isNotNull())//催收员不为空
                .and(qCaseInfo.collectionType.eq(collectionType))//催收类型
                .and(qCaseInfo.hasLeaveDays.goe(Integer.parseInt(sysParam.getValue())))//留案天数大于等于配置参数
                .and(qCaseInfo.companyCode.eq(sysParam.getCompanyCode()))); //公司码
        return jpaQuery.fetch();
    }

    /**
     * 电催小流转案件查询
     * @param sysParam
     * @param qCaseInfo
     * @param qCaseFollowupRecord
     * @param jpaQuery
     * @param collectionType
     */
    private  List<CaseInfo> smallTurnCase(SysParam sysParam, QCaseInfo qCaseInfo, QCaseFollowupRecord qCaseFollowupRecord, JPAQuery<CaseInfo> jpaQuery,Integer collectionType) {
        jpaQuery.select(qCaseInfo)
                .from(QCaseInfo.caseInfo)
                .leftJoin(QCaseFollowupRecord.caseFollowupRecord)
                .on(QCaseInfo.caseInfo.id.eq(QCaseFollowupRecord.caseFollowupRecord.caseId));
        jpaQuery.where(qCaseInfo.leaveCaseFlag.eq(CaseInfo.leaveCaseFlagEnum.NO_LEAVE.getValue())//未留案
                .and(qCaseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue()))//未结案
                .and(qCaseInfo.currentCollector.isNotNull())//催收员不为空
                .and(qCaseInfo.collectionType.eq(collectionType))//催收类型
                .and(qCaseInfo.holdDays.goe(Integer.parseInt(sysParam.getValue())))//持有天数大于等于配置参数
                .and(qCaseFollowupRecord.collectionWay.eq(CaseFollowupRecord.CollectionWayEnum.MANUAL.getValue()))//跟进记录为手动
                .and(qCaseFollowupRecord.operatorTime.goe(qCaseInfo.caseFollowInTime))//跟进日期大于等于流入日期
                .and(qCaseFollowupRecord.id.isNull())//无跟进记录的案件
                .and(qCaseInfo.companyCode.eq(sysParam.getCompanyCode())));//公司码
        return jpaQuery.fetch();
    }

    /**
     * 强制流转案件查询
     * @param sysParam
     * @param qCaseInfo
     * @param jpaQuery
     * @param collectionType
     * @return
     */
    private List<CaseInfo> forceTurnCase(SysParam sysParam, QCaseInfo qCaseInfo, JPAQuery<CaseInfo> jpaQuery,Integer collectionType) {
        jpaQuery.select(qCaseInfo)
                .from(QCaseInfo.caseInfo);
        jpaQuery.where(qCaseInfo.leaveCaseFlag.eq(CaseInfo.leaveCaseFlagEnum.NO_LEAVE.getValue())//未留案
                .and(qCaseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue()))//未结案
                .and(qCaseInfo.currentCollector.isNotNull())//催收员不为空
                .and(qCaseInfo.collectionType.eq(collectionType))//催收类型
                .and(qCaseInfo.holdDays.goe(Integer.parseInt(sysParam.getValue())))
                .and(qCaseInfo.companyCode.eq(sysParam.getCompanyCode()))); //持有天数大于等于配置参数
        return jpaQuery.fetch();
    }

    /**
     * 更新跟案件流转相关的信息
     * @param user
     * @param caseInfoList
     * @param caseType 案件类型
     * @param trunDeptName 流转部门
     */
    private void updateCaseInfo(User user, List<CaseInfo> caseInfoList,Integer caseType,String trunDeptName ) {
        List<CaseTurnRecord> caseTurnRecordList=new ArrayList<>();
        //更新案件属性
        for(CaseInfo caseInfo :caseInfoList){
            //部门ID置空
            caseInfo.setDepartment(null);
            //有协催的话需要结束协催
            Integer assistFlag = caseInfo.getAssistFlag();//协催标志
            if(CaseInfo.AssistFlag.YES_ASSIST.getValue().equals(assistFlag)){
                //原案件结束
                caseInfo.setAssistFlag(CaseInfo.AssistFlag.NO_ASSIST.getValue());//协催标志
                caseInfo.setAssistCollector(null);//协催员
                caseInfo.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue());//协催状态
                caseInfo.setAssistWay(null);//协催方式
                //查询协催案件
                QCaseAssist qCaseAssist=QCaseAssist.caseAssist;
                Iterable<CaseAssist> caseAssistIterable=caseAssistRepository.findAll(qCaseAssist.caseId.id.eq(caseInfo.getId())
                        .and(qCaseAssist.assistStatus.ne(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue())));
                for(Iterator<CaseAssist> it = caseAssistIterable.iterator(); it.hasNext();){
                    CaseAssist caseAssist=it.next();
                    caseAssist.setAssistStatus(CaseInfo.AssistStatus.ASSIST_COMPLATED.getValue());
                    caseAssist.setAssistCloseFlag(CaseAssist.AssistCloseFlagEnum.AUTO.getValue());
                    caseAssist.setOperatorTime(ZWDateUtil.getNowDateTime());
                    caseAssist.setOperator(user);
                    caseAssist.setMarkId(CaseInfo.Color.NO_COLOR.getValue());
                }
                caseAssistRepository.save(caseAssistIterable);
            }
            caseInfo.setHoldDays(0);//持案天数
            caseInfo.setCaseType(caseType);//案件类型
            caseInfo.setCaseMark(CaseInfo.Color.NO_COLOR.getValue());//打标标记
            caseInfo.setCaseFollowInTime(ZWDateUtil.getNowDateTime());//流入时间
            caseInfo.setFollowUpNum(caseInfo.getFollowUpNum()+1);//流转次数
            caseInfo.setLatelyCollector(caseInfo.getCurrentCollector());//上一个催员
            caseInfo.setCurrentCollector(null);//当前催员
            caseInfo.setOperator(user);
            caseInfo.setOperatorTime(ZWDateUtil.getNowDateTime());
            //案件流转记录
            CaseTurnRecord caseTurnRecord = new CaseTurnRecord();
            BeanUtils.copyProperties(caseInfo, caseTurnRecord); //将案件信息复制到流转记录
            caseTurnRecord.setId(null); //主键置空
            caseTurnRecord.setCaseId(caseInfo.getId()); //案件ID
            caseTurnRecord.setDepartId(caseInfo.getDepartment().getId()); //部门ID
            caseTurnRecord.setReceiveUserRealName(user.getRealName()); //接受人名称
            caseTurnRecord.setReceiveDeptName(trunDeptName);
            caseTurnRecord.setOperatorUserName(user.getUserName()); //操作员用户名
            caseTurnRecord.setOperatorTime(ZWDateUtil.getNowDateTime()); //操作时间
            caseTurnRecordList.add(caseTurnRecord);
        }
        caseTurnRecordRepository.save(caseTurnRecordList);
        caseInfoRepository.save(caseInfoList);
    }

}
