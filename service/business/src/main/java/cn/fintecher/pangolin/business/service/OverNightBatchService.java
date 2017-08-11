package cn.fintecher.pangolin.business.service;

import cn.fintecher.pangolin.business.repository.CaseInfoRepository;
import cn.fintecher.pangolin.entity.*;
import cn.fintecher.pangolin.entity.util.Constants;
import com.querydsl.jpa.impl.JPAQuery;
import org.quartz.JobDataMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
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
            jobTaskService.updateSysparam(jobDataMap.getString("sysParamCode"),Constants.SYSPARAM_OVERNIGHT_STEP,step);
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw  new Exception(jobDataMap.getString("sysParamCode").concat("重置序列失败"));
        }
    }

    /**
     * 案件小流转（电催和外访及案件的剩余天数）
     * @param jobDataMap
     * @param step
     */
    @Transactional
    public void doOverNightTwo(JobDataMap jobDataMap,String step) throws Exception{
        //应该先批处理持案天数
        SysParam sysParam = jobTaskService.getSysparam(jobDataMap.getString("companyCode"), Constants.SYS_PHNOEFLOW_SMALLDAYS);
        QCaseInfo qCaseInfo=QCaseInfo.caseInfo;
        QCaseFollowupRecord qCaseFollowupRecord=QCaseFollowupRecord.caseFollowupRecord;
        JPAQuery<CaseInfo> jpaQuery=new JPAQuery<>(entityManager);
        //查找非留案、非结案、配置天数类无任何催收反馈的电催案件
        try {
            jpaQuery.select(qCaseInfo)
                    .from(QCaseInfo.caseInfo)
                    .leftJoin(QCaseFollowupRecord.caseFollowupRecord)
                    .on(QCaseInfo.caseInfo.id.eq(QCaseFollowupRecord.caseFollowupRecord.caseId));
            jpaQuery.where(qCaseInfo.leaveCaseFlag.ne(CaseInfo.leaveCaseFlagEnum.NO_LEAVE.getValue())//未留案
                            .and(qCaseInfo.collectionStatus.ne(CaseInfo.CollectionStatus.CASE_OVER.getValue()))//未结案
                            .and(qCaseInfo.currentCollector.isNotNull())//催收员不为空
                            .and(qCaseInfo.collectionType.eq(CaseInfo.CollectionType.TEL.getValue()))//电催
                            .and(qCaseInfo.holdDays.goe(Integer.parseInt(sysParam.getValue())))//持有天数大于等于配置参数
                            .and(qCaseFollowupRecord.collectionWay.eq(CaseFollowupRecord.CollectionWayEnum.MANUAL.getValue()))//跟进记录为手动
                            .and(qCaseFollowupRecord.operatorTime.goe(qCaseInfo.caseFollowInTime))//跟进日期大于等于流入日期
                            .and(qCaseFollowupRecord.id.isNull()));//无跟进记录的案件
            List<CaseInfo> caseInfoList=jpaQuery.fetch();
            //TODO
        }catch (Exception e){

        }
    }

}
